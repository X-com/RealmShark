package tomato.damagecalc;

import tomato.gui.TomatoGUI;
import tomato.save.PropertiesManager;
import packets.Packet;
import packets.data.StatData;
import packets.incoming.*;
import packets.outgoing.EnemyHitPacket;
import packets.outgoing.PlayerShootPacket;
import assets.AssetMissingException;
import assets.IdToName;
import util.Pair;
import util.Util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Map.Entry.comparingByValue;

/**
 * Damage calculator class made to compute dps of all players in
 * view distance around the player from incoming and outgoing packets.
 */
public class DpsLogger {

    private boolean saveToFile = false;
    private int displayIndex = 0;
    private Entity[] firstPage;
    private final ArrayList<Packet> logPackets = new ArrayList<>();
    private final ArrayList<Entity[]> entityLogs = new ArrayList<>();
    private final HashMap<Integer, Entity> entityList = new HashMap<>();
    private final HashMap<Integer, Entity> entityHitList = new HashMap<>();
    private MapInfoPacket mapInfo;
    private Entity player;
    private RNG rng;
    private static boolean dammahCountered = false;
    private static HashMap<Integer, Integer> crystalList = new HashMap<>();
    private Filter filter;

    // Load presets
    {
        setProfileFilter();
    }

    /**
     * Packet processing method used to unwrap packets and log any damage and effects.
     *
     * @param packet incoming packets to be processed.
     */
    public void packetCapture(Packet packet, boolean realTimeUpdate) {
        if (packet instanceof MapInfoPacket) {
            if (mapInfo != null) {
                addToLogs();
                if (saveToFile) saveDpsLogsToFile();
            }
            mapInfo = (MapInfoPacket) packet;
            entityList.clear();
            entityHitList.clear();
            rng = new RNG(mapInfo.seed);
            dammahCountered = false;
            if (filteredInstances(mapInfo.displayName)) {
                mapInfo = null;
            }
        } else if (mapInfo == null) {
            return;
        }

        if (saveToFile) logPackets.add(packet);
        if (packet instanceof CreateSuccessPacket) {
            CreateSuccessPacket p = (CreateSuccessPacket) packet;
            player = new Entity(p.objectId, "isMe");
            entityList.put(player.id, player);
        } else if (packet instanceof PlayerShootPacket) {
            PlayerShootPacket p = (PlayerShootPacket) packet;
            Bullet bullet = new Bullet(p);
            try {
                calculateBulletDamage(bullet, rng, player, p.weaponId, p.projectileId);
            } catch (AssetMissingException e) {
                e.printStackTrace();
            }
            player.setBullet(p.bulletId, bullet);
        } else if (packet instanceof ServerPlayerShootPacket) {
            ServerPlayerShootPacket p = (ServerPlayerShootPacket) packet;
            Bullet bullet = new Bullet(p);
            bullet.totalDmg = p.damage;
            if (p.spellBulletData) {
                for (int j = p.bulletId; j < p.bulletId + p.bulletCount; j++) {
                    player.setBullet(p.bulletId, bullet);
                }
            }
        } else if (packet instanceof EnemyHitPacket) {
            EnemyHitPacket p = (EnemyHitPacket) packet;
            Bullet bullet = player.getBullet(p.bulletId);
            Entity entity = getEntity(p.targetId);
            hit(entity, bullet, p);
            if (!entityHitList.containsKey(entity.id)) entityHitList.put(entity.id, entity);
        } else if (packet instanceof DamagePacket) {
            DamagePacket p = (DamagePacket) packet;
            if (p.damageAmount > 0) {
                Bullet bullet = new Bullet(packet);
                bullet.totalDmg = p.damageAmount;
                Entity entity = getEntity(p.targetId);
                hit(entity, bullet, p);
                if (!entityHitList.containsKey(entity.id)) entityHitList.put(entity.id, entity);
            }
        } else if (packet instanceof NewTickPacket) {
            NewTickPacket p = (NewTickPacket) packet;
            for (int j = 0; j < p.status.length; j++) {
                int id = p.status[j].objectId;
                StatData[] stats = p.status[j].stats;
                Entity entity = getEntity(id);
                entity.setTime(p.serverRealTimeMS);
                entity.setStats(stats);
            }
            if (realTimeUpdate) updateLogs();
        } else if (packet instanceof UpdatePacket) {
            UpdatePacket p = (UpdatePacket) packet;
            for (int j = 0; j < p.newObjects.length; j++) {
                int id = p.newObjects[j].status.objectId;
                StatData[] stats = p.newObjects[j].status.stats;
                if (id == player.id) {
                    player.setStats(stats);
                    continue;
                }
                int objectType = p.newObjects[j].objectType;
                Entity entity = getEntity(id);
                entity.setType(objectType);
                entity.setStats(stats);
                crystalTracker(id, objectType);
            }
            for (int j = 0; j < p.drops.length; j++) {
                crystalTracker(p.drops[j], 0);
            }
        } else if (packet instanceof TextPacket) {
            TextPacket p = (TextPacket) packet;
            if (p.text.equals("I SAID DO NOT INTERRUPT ME! For this I shall hasten your end!")) dammahCountered = true;
        }
    }

    /**
     * Adds shatters crystal entities to a short list to track floor pattern in shatters king fight.
     *
     * @param id   id of the entity.
     * @param type type of the entity.
     */
    private static void crystalTracker(int id, int type) { // blue, yellow, red, green crystal IDs in that order.
        if (type == 46721 || type == 46771 || type == 29501 || type == 33656) {
            crystalList.put(id, type);
        } else {
            crystalList.remove(id);
        }
    }

    /**
     * Determines the floor pattern in shatters king fight.
     *
     * @return Gives a mask id indicating the crystal colors in the king fight.
     */
    private static int floorPlanCrystals() {
        int mask = 0;
        for (Map.Entry<Integer, Integer> m : crystalList.entrySet()) {
            if (m.getValue() == 46721) {
                mask |= 1;
            } else if (m.getValue() == 46771) {
                mask |= 2;
            } else if (m.getValue() == 29501) {
                mask |= 4;
            } else if (m.getValue() == 33656) {
                mask |= 8;
            }
        }
        return mask;
    }

    /**
     * Toggle for saving dps logs to file.
     *
     * @param save Enable logging to file.
     */
    public void setSaveToFile(boolean save) {
        saveToFile = save;
        if (!save) logPackets.clear();
    }

    /**
     * Saves all dps packets to file for analysis.
     */
    private void saveDpsLogsToFile() {
        System.out.println("saving to file size: " + logPackets.size());
        for (Packet p : logPackets) {
            if (p == null || p.getPayload() == null) continue;
            StringBuilder sb = new StringBuilder();
            for (byte b : p.getPayload()) {
                sb.append(String.format("%02x", b));
            }
            Util.print("dpsLogs/" + mapInfo.displayName, sb.toString());
        }
        logPackets.clear();
        System.out.println("saved: " + mapInfo.displayName);
    }

    /**
     * Filtered dungeons that will never need logging such as Nexus dungeons.
     *
     * @param dungName Name of the dungeon.
     * @return true if dungeon should be logged.
     */
    private static boolean filteredInstances(String dungName) {
        switch (dungName) {
            case "{s.vault}":  // vault
            case "Daily Quest Room": // quest room
            case "Pet Yard": // pet yard
            case "{s.guildhall}": // guild hall
            case "{s.nexus}": // nexus
            case "Grand Bazaar": // bazaar
                return true;
            default:
                return false;
        }
    }

    /**
     * Full output string from damage logs of all hostile mobs.
     *
     * @return logged dps output as a string.
     */
    public static String stringDmg(Entity[] displayList, Filter filter) {
        if (displayList == null) return "";
        StringBuilder sb = new StringBuilder();
        int displaySize = 10;

        for (int i = 0; i < displayList.length && i < displaySize; i++) {
            Entity e = displayList[i];
            sb.append(e.display(filter)).append("\n");
        }

        return sb.toString();
    }

    /**
     * Generates display list of all entities that taken damage in the dungeon instance.
     *
     * @return Entities that taken damage sorted by max HP on death.
     */
    public Entity[] displayList() {
        ArrayList<Entity> list = new ArrayList<>();
        HashMap<Integer, Entity> damagerList = new HashMap<>();

        List<Entity> sortedList = Arrays.stream(entityHitList.values().toArray(new Entity[0])).sorted(Comparator.comparingInt(Entity::maxHp).reversed()).collect(Collectors.toList());

        for (Entity entity : sortedList) {
            if (entity.bulletDamageList.isEmpty()) continue;

            HashMap<Integer, Damage> dmgList = new HashMap<>();
            for (Bullet b : entity.bulletDamageList) {
                if (b.packet instanceof DamagePacket) {
                    DamagePacket p = (DamagePacket) b.packet;
                    int ownerId = p.objectId;
                    Entity owner = entityList.get(ownerId);
                    if (owner == null) continue;
                    if (owner.stats[31] == null) continue;
                    damagerList.put(ownerId, owner);
                    addDamage(dmgList, b, p.damageAmount, owner);
                } else if (b.packet instanceof EnemyHitPacket) {
                    addDamage(dmgList, b, b.totalDmg, player);
                }
            }

            Stream<Map.Entry<Integer, Damage>> sortedDmgList = dmgList.entrySet().stream().sorted(comparingByValue());
            int score = 0;
            ArrayList<Damage> dList = new ArrayList<>();
            for (Map.Entry<Integer, Damage> m : sortedDmgList.collect(Collectors.toList())) {
                score++;
                m.getValue().score = score;
                dList.add(m.getValue());
            }

            entity.setSortedDmgList(dList);
            entity.setPlayerList(damagerList);
            list.add(entity);
        }

        return list.toArray(new Entity[0]);
    }

    /**
     * Adds the amount of damage hit on the entity.
     *
     * @param dmgList      List of all players
     * @param bullet       Bullet that hit the entity.
     * @param damageAmount Amount of damage the bullet did.
     * @param owner        Owner of the bullet hitting the target.
     */
    private void addDamage(HashMap<Integer, Damage> dmgList, Bullet bullet, int damageAmount, Entity owner) {
        Damage dmg;
        if (!dmgList.containsKey(owner.id)) {
            dmg = new Damage(owner);
            dmgList.put(owner.id, dmg);
        } else {
            dmg = dmgList.get(owner.id);
        }

        if (bullet.oryx3GuardDmg || bullet.walledGardenReflectors || bullet.chancellorDammahDmg) {
            dmg.counterDmg += damageAmount;
            dmg.counterHits++;
            dmg.oryx3GuardDmg = bullet.oryx3GuardDmg;
            dmg.walledGardenReflectors = bullet.walledGardenReflectors;
            dmg.chancellorDammahDmg = bullet.chancellorDammahDmg;
        }
        dmg.dmg += damageAmount;
        dmg.hits++;
    }

    /**
     * Qualifies if the entity should be added to the total damage or not.
     *
     * @param entity The entity to be filtered into the list of dmg displayed.
     */
    private static boolean qualifiedEntityAddToTotalDmg(Entity entity) {
        return entity.getStat(31) == null; // TODO: Implement boss filter later.
    }

    /**
     * Gets the entity from id or creates a new Entity object, adds it to the list and returns it.
     *
     * @param id requested entity by id.
     * @return the entity to be requested by id.
     */
    private Entity getEntity(int id) {
        if (entityList.containsKey(id)) {
            return entityList.get(id);
        }
        Entity e = new Entity(id);
        entityList.put(id, e);
        return e;
    }

    /**
     * Calculates the damage to shot bullet from the player from main weapon.
     *
     * @param bullet       The bullet being shot.
     * @param rng          Seed used with randomizer to find the exact value of used weapon from min to max range of weapon dmg.
     * @param player       The player entity.
     * @param weaponId     Weapon ID used (retrieved from packet being sent).
     * @param projectileId Projectile ID used (retrieved from packet being sent).
     */
    private static void calculateBulletDamage(Bullet bullet, RNG rng, Entity player, int weaponId, int projectileId) throws AssetMissingException {
        if (player == null || rng == null) return;

        if (projectileId == -1) {
            projectileId = 0;
        }
        int min = IdToName.getIdProjectileMinDmg(weaponId, projectileId);
        int max = IdToName.getIdProjectileMaxDmg(weaponId, projectileId);
        boolean ap = IdToName.getIdProjectileArmorPierces(weaponId, projectileId);
        int dmg;
        if (min != max) {
            long r = rng.next();
            dmg = (int) (min + r % (max - min));
        } else {
            dmg = min;
        }
        float f = playerStatsMultiplier(player);
        bullet.totalDmg = (int) (dmg * f);
        bullet.armorPiercing = ap;
    }

    /**
     * Player entity stats multiplier such as attack, exalts and other buffs.
     *
     * @param player The player entity.
     * @return damage multiplier from player stats.
     */
    private static float playerStatsMultiplier(Entity player) {
        boolean weak = (player.getStat(29).statValue & 0x40) != 0;
        boolean damaging = (player.getStat(29).statValue & 0x40000) != 0;
        int attack = player.getStat(20).statValue;
        float exaltDmgBonus = (float) player.getStat(113).statValue / 1000;

        if (weak) {
            return 0.5f;
        }
        float number = (attack + 25) * 0.02f;
        if (damaging) {
            number *= 1.25;
        }
        return number * exaltDmgBonus;
    }

    /**
     * Used when an entity takes damage taking defence and other effects into account for final damage to entity.
     *
     * @param damage        the base damage the bullet can do.
     * @param armorPiercing if the bullet ignores defence.
     * @param defence       defence of the entity being hit.
     * @param conditions    condition effects the entity being shot can have.
     * @return final damage applied to the entity.
     */
    private static int damageWithDefense(int damage, boolean armorPiercing, int defence, int[] conditions) {
        if (damage == 0) return 0;

        if (armorPiercing || (conditions[0] & 0x4000000) != 0) {
            defence = 0;
        } else if ((conditions[0] & 0x2000000) != 0) {
            defence = (int) (defence * 1.5);
        }
        if ((conditions[1] & 0x20000) != 0) {
            defence = defence - 20;
        }
        int minDmg = damage * 2 / 20;
        int dmg = Math.max(minDmg, damage - defence);

        if ((conditions[0] & 0x1000000) != 0) {
            dmg = 0;
        }
        if ((conditions[1] & 0x8) != 0) {
            dmg = (int) (dmg * 0.9);
        }
        if ((conditions[1] & 0x40) != 0) {
            dmg = (int) (dmg * 1.25);
        }
        return dmg;
    }

    /**
     * Computes the effect of a player bullet hitting an entity.
     *
     * @param entity Entity being hit by a bullet.
     * @param bullet The bullet hitting the entity.
     * @param packet The packet sent when an entity is hit.
     */
    private static void hit(Entity entity, Bullet bullet, Packet packet) {
        if (bullet == null || bullet.totalDmg == 0) return;

        bullet.oryx3GuardDmg = entity.objectType == 45363 && entity.getStat(125) != null && (entity.getStat(125).statValue == -935464302 || entity.getStat(125).statValue == -918686683);
        bullet.walledGardenReflectors = entity.objectType == 29039 && entity.getStat(125) != null && (entity.getStat(125).statValue == -123818367 && floorPlanCrystals() == 12);
//        bullet.chancellorDammahDmg = entity.objectType == 9635 && entity.getStat(125) != null && (entity.getStat(125).statValue == -851576207 || entity.getStat(125).statValue == -901909064 || entity.getStat(125).statValue == -834798588 || entity.getStat(125).statValue == -818020969);

        if (packet instanceof DamagePacket) {
            bullet.chancellorDammahDmg = entity.objectType == 9635 && !dammahCountered;
            entity.bulletDamageList.add(bullet);
            return;
        }

        int[] conditions = new int[2];

        conditions[0] = entity.getStat(29) == null ? 0 : entity.getStat(29).statValue;
        conditions[1] = entity.getStat(96) == null ? 0 : entity.getStat(96).statValue;
        int defence = entity.getStat(21) == null ? 0 : entity.getStat(21).statValue;

        Bullet b = new Bullet(packet);
        b.totalDmg = damageWithDefense(bullet.totalDmg, bullet.armorPiercing, defence, conditions);

        if (b.totalDmg > 0) {
            bullet.chancellorDammahDmg = entity.objectType == 9635 && !dammahCountered;
            b.oryx3GuardDmg = bullet.oryx3GuardDmg;
            b.walledGardenReflectors = bullet.walledGardenReflectors;
            entity.bulletDamageList.add(b);
        }
    }

    /**
     * Clears all logged data.
     */
    public void clear() {
        entityList.clear();
        mapInfo = null;
        player = null;
        rng = null;
    }

    /**
     * Clears all dps logs
     */
    public void clearTextLogs() {
        entityLogs.clear();
        displayIndex = 1;
        TomatoGUI.setTextAreaAndLabelDPS("", "1/1", false);
    }

    /**
     * Find the next dps log to display in the dps calculator.
     */
    public void nextDisplay() {
        if (displayIndex < (entityLogs.size() - 1)) {
            displayIndex++;
            Entity[] e = entityLogs.get(displayIndex);
            String s = stringDmg(e, filter);
            String l = (displayIndex + 1) + "/" + (entityLogs.size() + 1);
            TomatoGUI.setTextAreaAndLabelDPS(s, l, true);
        } else if (displayIndex < entityLogs.size()) {
            displayIndex++;
            String l = (displayIndex + 1) + "/" + (entityLogs.size() + 1);
            TomatoGUI.setTextAreaAndLabelDPS(stringDmg(firstPage, filter), l, false);
        }
    }

    /**
     * Find the previous dps log to display in the dps calculator.
     */
    public void previousDisplay() {
        if (displayIndex > 0) {
            displayIndex--;
            Entity[] e = entityLogs.get(displayIndex);
            String s = stringDmg(e, filter);
            String l = (displayIndex + 1) + "/" + (entityLogs.size() + 1);
            TomatoGUI.setTextAreaAndLabelDPS(s, l, true);
        }
    }

    private void addToLogs() {
        boolean selectable = true;
        String text = null;
        if (displayIndex == entityLogs.size()) {
            displayIndex++;
            selectable = false;
            text = "";
        }
        firstPage = new Entity[0];
        entityLogs.add(displayList());
        String labelText = (displayIndex + 1) + "/" + (entityLogs.size() + 1);
        TomatoGUI.setTextAreaAndLabelDPS(text, labelText, selectable);
    }

    /**
     * Update the text area of the dps calculator.
     */
    private void updateLogs() {
        if (displayIndex != entityLogs.size()) return;
        String firstPage = stringDmg(displayList(), filter);
        TomatoGUI.setTextAreaAndLabelDPS(firstPage, null, displayIndex != entityLogs.size());
    }

    public void updateFilter() {
        setProfileFilter();
        Entity[] list;
        if (entityLogs.size() > 0 && displayIndex < entityLogs.size()) list = entityLogs.get(displayIndex);
        else list = firstPage;
        TomatoGUI.setTextAreaAndLabelDPS(stringDmg(list, filter), null, displayIndex != entityLogs.size());
    }

    private void setProfileFilter() {
        if (filter == null) filter = new Filter();
        String equipment = PropertiesManager.getProperty("equipment");
        String names = PropertiesManager.getProperty("nameFilter");
        String toggleFilter = PropertiesManager.getProperty("toggleFilter");

        if (equipment == null) {
            filter.equipmentFilter = 1;
        } else {
            filter.equipmentFilter = Integer.parseInt(equipment);
        }

        if (toggleFilter != null) {
            filter.nameFilter = toggleFilter.equals("T");
        } else {
            filter.nameFilter = false;
        }

        if (names != null) {
            filter.filteredStrings = names.split(" ");
        } else {
            filter.filteredStrings = new String[0];
        }
    }

    private static String getName(int id) {
        try {
            IdToName.objectName(id);
        } catch (AssetMissingException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Class used to store damage and counter info.
     */
    private static class Damage implements Comparable {
        public Entity owner;
        public int dmg;
        public int score;
        public int hits;
        public int counterDmg;
        public int counterHits;
        public boolean oryx3GuardDmg = false;
        public boolean walledGardenReflectors = false;
        public boolean chancellorDammahDmg = false;

        public Damage(Entity o) {
            owner = o;
        }

        @Override
        public int compareTo(Object o) {
            return ((Damage) o).dmg - dmg;
        }
    }

    /**
     * Class used for storing bullet data.
     */
    private static class Bullet {
        public boolean oryx3GuardDmg = false;
        public boolean walledGardenReflectors = false;
        public boolean chancellorDammahDmg = false;
        Packet packet;
        int totalDmg;
        boolean armorPiercing;

        public Bullet(Packet p) {
            packet = p;
        }
    }

    /**
     * Class used to display player Equipment
     */
    private static class Equipment implements Comparable {
        int id;
        long time;
        int swaps;

        public Equipment(int id, long time, int swaps) {
            this.id = id;
            this.time = time;
            this.swaps = swaps;
        }

        @Override
        public int compareTo(Object o) {
            return (int) (time - ((Equipment) o).time);
        }
    }

    /**
     * Class used for entity info.
     */
    private static class Entity {

        private boolean isMe = false;
        private final int id;
        private int objectType = -1;
        private final StatData[] stats = new StatData[256];
        private final Bullet[] bulletDmg = new Bullet[512];
        private final ArrayList<Bullet> bulletDamageList = new ArrayList<>();
        private final ArrayList<Pair<StatData, Long>>[] inv = new ArrayList[4];
        private HashMap<Integer, Entity> playerList;
        private List<Damage> damageList;
        private long entityStartTime;
        private long entityTime;

        public Entity(int id) {
            this.id = id;
            for (int i = 0; i < inv.length; i++) {
                inv[i] = new ArrayList<>();
            }
        }

        public Entity(int objectId, String me) {
            this(objectId);
            isMe = true;
        }

        public void setBullet(short bulletId, Bullet bullet) {
            bulletDmg[bulletId] = bullet;
        }

        public Bullet getBullet(short p) {
            return bulletDmg[p];
        }

        public void setStats(StatData[] stats) {
            for (StatData sd : stats) {
                if (sd.statTypeNum >= 8 && sd.statTypeNum <= 11) {
                    inv[sd.statTypeNum - 8].add(new Pair(sd, entityTime));
                }
                this.stats[sd.statTypeNum] = sd;
            }
        }

        public StatData getStat(int id) {
            return stats[id];
        }

        public void setType(int objectType) {
            this.objectType = objectType;
        }

        public int maxHp() {
            if (stats[0] == null) return 0;
            return stats[0].statValue;
        }

        public String showInv(int equipmentFilter, Entity owner) {
            if (stats[31] == null || equipmentFilter == 0) {
                return "";
            } else if (equipmentFilter == 1) {
                return itemToString(owner);
            }
            StringBuilder s = new StringBuilder();
            if (equipmentFilter == 3) s.append("\n");
            for (int inventory = 0; inventory < 4; inventory++) {
                s.append("<");

                if (inv[inventory].size() == 0) {
                    s.append("  ");
                } else if (inv[inventory].size() == 1) {
                    s.append(String.format("%s %.1fsec %s\n", getName(inv[inventory].get(0).left().statValue), (float) (entityTime - entityStartTime) / 1000, "100% Equipped:1 "));
                } else {
                    HashMap<Integer, Equipment> gear = new HashMap<>();
                    Pair<StatData, Long> pair2 = null;
                    long firstTime = 0;
                    for (int i = 1; i < inv[inventory].size(); i++) {
                        Pair<StatData, Long> pair1 = inv[inventory].get(i - 1);
                        pair2 = inv[inventory].get(i);
                        long time1 = pair1.right();
                        if (time1 == 0) time1 = entityStartTime;
                        if (firstTime == 0) firstTime = time1;
                        addGear(gear, time1, pair2.right(), pair1.left().statValue, pair1.left().statValue == pair2.left().statValue);
                    }
                    long totalTime = entityTime - firstTime;
                    addGear(gear, pair2.right(), entityTime, pair2.left().statValue, false);

                    Stream<Map.Entry<Integer, Equipment>> sorted2 = gear.entrySet().stream().sorted(comparingByValue());
                    for (Map.Entry<Integer, Equipment> m : sorted2.collect(Collectors.toList())) {
                        s.append(String.format("%s %.1fsec %.2f%% Equipped:%d / ", getName(m.getKey()), ((float) m.getValue().time / 1000), ((float) m.getValue().time * 100 / totalTime), m.getValue().swaps));
                    }
                }
                s = new StringBuilder(s.substring(0, s.length() - 3));
                s.append("> ");
                if (equipmentFilter == 3) s.append("\n");
            }
            return s.substring(0, s.length() - 1);
        }

        private void addGear(HashMap<Integer, Equipment> gear, long time1, long time2, int itemID, boolean sameItem) {
            if (!gear.containsKey(itemID)) {
                Equipment e = new Equipment(itemID, time2 - time1, sameItem ? 0 : 1);
                gear.put(itemID, e);
            } else {
                Equipment e = gear.get(itemID);
                e.time += time2 - time1;
                e.swaps++;
            }
        }

        /**
         * Item list to string parser.
         *
         * @param entity A player entity with items equipped needing to be parsed.
         * @return String of player equipped items.
         */
        private String itemToString(Entity entity) {
            StringBuilder s = new StringBuilder();
            s.append("[");
            for (int k = 0; k < 256; k++) {
                if (k >= 8 && k <= 11 && entity.getStat(k) != null) {
                    int itemID = entity.getStat(k).statValue;
                    s.append(getName(itemID));
                    if (k < 11) s.append(" / ");
                }
            }
            s.append("]");
            return s.toString();
        }

        public void setTime(long time) {
            entityTime = time;
            if (entityStartTime == 0) entityStartTime = time;
        }

        public void setPlayerList(HashMap<Integer, Entity> playerList) {
            this.playerList = playerList;
        }

        public HashMap<Integer, Entity> getPlayerList() {
            return playerList;
        }

        public void setSortedDmgList(List<Damage> d) {
            damageList = d;
        }

        public List<Damage> getDamageList() {
            return damageList;
        }

        public String display(Filter filter) {
            StringBuilder sb = new StringBuilder();
            sb.append(getName(objectType)).append(" HP: ").append(maxHp()).append("\n");
            for (Damage dmg : damageList) {
                String name = dmg.owner.getStat(31).stringStatValue;
                if (filter.nameFilter && filter.filteredStrings.length > 0) {
                    boolean found = false;
                    for (String n : filter.filteredStrings) {
                        if (name.toLowerCase().startsWith(n.toLowerCase())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) continue;
                }
                String extra = "    ";
                String isMe = dmg.owner.isMe ? "->" : "  ";
                int index = name.indexOf(',');
                if (index != -1) name = name.substring(0, index);
                float pers = ((float) dmg.dmg * 100 / (float) maxHp());
                if (dmg.oryx3GuardDmg) {
                    extra = String.format("[Guarded Hits:%d Dmg:%d]", dmg.counterHits, dmg.counterDmg);
                } else if (dammahCountered && dmg.chancellorDammahDmg) {
                    extra = String.format("[Dammah Counter Hits:%d Dmg:%d]", dmg.counterHits, dmg.counterDmg);
                } else if (dmg.walledGardenReflectors) {
                    extra = String.format("[Garden Counter Hits:%d Dmg:%d]", dmg.counterHits, dmg.counterDmg);
                }
                String inv = dmg.owner.showInv(filter.equipmentFilter, dmg.owner);
                sb.append(String.format("%s %3d %10s DMG: %7d %6.3f%% %s %s\n", isMe, dmg.score, name, dmg.dmg, pers, extra, inv));
            }
            sb.append("\n");
            return sb.toString();
        }

        @Override
        public String toString() {
            return getName(objectType);
        }
    }

    /**
     * Filter options class
     */
    private class Filter {
        int equipmentFilter;
        String[] filteredStrings;
        boolean nameFilter;
    }
}
