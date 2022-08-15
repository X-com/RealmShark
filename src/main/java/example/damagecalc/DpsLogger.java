package example.damagecalc;

import example.gui.TomatoGUI;
import packets.Packet;
import packets.data.StatData;
import packets.incoming.*;
import packets.outgoing.EnemyHitPacket;
import packets.outgoing.PlayerShootPacket;
import util.IdToName;
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
    private int stringIndex = 0;
    private final ArrayList<Packet> logPackets = new ArrayList<>();
    private final ArrayList<String> stringLogs = new ArrayList<>();
    private final HashMap<Integer, Entity> entityList = new HashMap<>();
    private final HashMap<Integer, Entity> entityHitList = new HashMap<>();
    private MapInfoPacket mapInfo;
    private Entity player;
    private RNG rng;
    private String firstPage;
    private static boolean dammahCountered = false;
    private long serverTime = 0;
    private long serverFirstTime = 0;

    /**
     * Packet processing method used to unwrap packets and log any damage and effects.
     *
     * @param packet incoming packets to be processed.
     */
    public void packetCapture(Packet packet, boolean realTimeUpdate) {
        if (packet instanceof MapInfoPacket) {
            if (mapInfo != null) {
                addToStringLogs();
                if (saveToFile) saveDpsLogsToFile();
            }
            mapInfo = (MapInfoPacket) packet;
            entityList.clear();
            rng = new RNG(mapInfo.seed);
            dammahCountered = false;
            serverFirstTime = serverTime = 0;
            if (filteredInstances(mapInfo.displayName)) {
                mapInfo = null;
            }
        } else if (mapInfo == null) {
            return;
        }

        if (saveToFile) logPackets.add(packet);
        if (packet instanceof CreateSuccessPacket) {
            CreateSuccessPacket p = (CreateSuccessPacket) packet;
            player = new Entity(p.objectId);
        } else if (packet instanceof PlayerShootPacket) {
            PlayerShootPacket p = (PlayerShootPacket) packet;
            Bullet bullet = new Bullet(p);
            calculateBulletDamage(bullet, rng, player, p.weaponId, p.projectileId);
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
            serverTime = p.serverRealTimeMS;
            if (serverFirstTime == 0) serverFirstTime = serverTime;
            for (int j = 0; j < p.status.length; j++) {
                int id = p.status[j].objectId;
                StatData[] stats = p.status[j].stats;
                if (id == player.id) {
                    player.setStats(stats, serverTime);
                    continue;
                }
                Entity entity = getEntity(id);
                entity.setStats(stats, serverTime);
            }
            if (realTimeUpdate) updateStringLogs();
        } else if (packet instanceof UpdatePacket) {
            UpdatePacket p = (UpdatePacket) packet;
            for (int j = 0; j < p.newObjects.length; j++) {
                int id = p.newObjects[j].status.objectId;
                StatData[] stats = p.newObjects[j].status.stats;
                if (id == player.id) {
                    player.setStats(stats, serverTime);
                    continue;
                }
                int objectType = p.newObjects[j].objectType;
                Entity entity = getEntity(id);
                entity.setType(objectType);
                entity.setStats(stats, serverTime);
            }
        } else if (packet instanceof TextPacket) {
            TextPacket p = (TextPacket) packet;
            if (p.text.equals("I SAID DO NOT INTERRUPT ME! For this I shall hasten your end!")) dammahCountered = true;
        }
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
    public String stringDmg() {
        StringBuilder sb = new StringBuilder();

        List<Entity> sortedList = Arrays.stream(entityList.values().toArray(new Entity[0])).sorted(Comparator.comparingInt(Entity::maxHp).reversed()).collect(Collectors.toList());
        int count = 0;
        for (Entity entity : sortedList) {
            if (entity.bulletDamageList.isEmpty()) continue;

            HashMap<Integer, Damage> dmgList = new HashMap<>();
            for (Bullet b : entity.bulletDamageList) {
                if (b.packet instanceof DamagePacket) {
                    DamagePacket p = (DamagePacket) b.packet;
                    int ownerId = p.objectId;

                    addDamage(dmgList, b, p.damageAmount, ownerId);
                } else if (b.packet instanceof EnemyHitPacket) {
                    addDamage(dmgList, b, b.totalDmg, player.id);
                }
            }

            if (count < 10) {
                sb.append(String.format("%18s HP:%8d\n", entity, entity.maxHp()));
                appendEntityDmgText(sb, player, entity.maxHp(), dmgList, entityList);
            }
            count++;
        }

        return sb.toString();
    }

    /**
     * Adds the amount of damage hit on the entity.
     *
     * @param dmgList      List of all players
     * @param bullet       Bullet that hit the entity.
     * @param damageAmount Amount of damage the bullet did.
     * @param ownerId      Owner of the bullet hitting the target.
     */
    private void addDamage(HashMap<Integer, Damage> dmgList, Bullet bullet, int damageAmount, int ownerId) {
        Damage dmg;
        if (!dmgList.containsKey(ownerId)) {
            dmg = new Damage();
            dmgList.put(ownerId, dmg);
        } else {
            dmg = dmgList.get(ownerId);
        }

        if (bullet.oryx3GuardDmg) {
            dmg.guardDmg += damageAmount;
            dmg.guardHits++;
        } else if (bullet.chancellorDammahDmg) {
            dmg.dammahDmg += damageAmount;
            dmg.dammahHits++;
        } else {
            dmg.dmg += damageAmount;
            dmg.hits++;
        }
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
     * Appends sorted string output to sb and returns.
     */
    private void appendEntityDmgText(StringBuilder sb, Entity player, int entityHp, HashMap<Integer, Damage> dmgList, HashMap<Integer, Entity> entityList) {
        int num = 0;
        Stream<Map.Entry<Integer, Damage>> sorted2 = dmgList.entrySet().stream().sorted(comparingByValue());
        for (Map.Entry<Integer, Damage> m : sorted2.collect(Collectors.toList())) {
            String extra = "    ";
            num++;
            String isMe = m.getKey() == player.id ? "->" : "  ";
            Entity entityPlayer;
            if (m.getKey() == player.id) {
                entityPlayer = player;
            } else {
                entityPlayer = entityList.get(m.getKey());
            }
            if (entityPlayer == null || entityPlayer.getStat(31) == null) continue;
            String name = entityPlayer.getStat(31).stringStatValue;
            int index = name.indexOf(',');
            if (index != -1) name = name.substring(0, index);
            float pers = ((float) m.getValue().dmg * 100 / (float) entityHp);
            if (m.getValue().guardDmg > 0) {
                extra = String.format("[Guarded Hits:%d Dmg:%d]", m.getValue().guardHits, m.getValue().guardDmg);
            } else if (dammahCountered && m.getValue().dammahDmg > 0) {
                extra = String.format("[Dammah Counter Hits:%d Dmg:%d]", m.getValue().dammahHits, m.getValue().dammahDmg);
            }
            sb.append(String.format("%s %3d %10s DMG: %7d %6.3f%% %s %s\n", isMe, num, name, m.getValue().dmg, pers, extra, entityPlayer.showInv(serverFirstTime, serverTime)));
        }
        sb.append("\n");
    }

    /**
     * Item list to string parser.
     *
     * @param entity A player entity with items equipped needing to be parsed.
     * @return String of player equipped items.
     */
    private static String itemToString(Entity entity) {
        StringBuilder s = new StringBuilder();
        for (int k = 0; k < 256; k++) {
            if (k >= 8 && k <= 11 && entity.getStat(k) != null) {
                int itemID = entity.getStat(k).statValue;
                s.append(IdToName.name(itemID));
                if (k < 11) s.append(", ");
            }
        }
        return s.toString();
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
    private static void calculateBulletDamage(Bullet bullet, RNG rng, Entity player, int weaponId, int projectileId) {
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
        stringLogs.clear();
        stringIndex = 1;
        TomatoGUI.setTextAreaAndLabelDPS("", "1/1", false);
    }

    /**
     * Find the next dps log to display in the dps calculator.
     */
    public void nextDisplay() {
        if (stringIndex < (stringLogs.size() - 1)) {
            stringIndex++;
            String s = stringLogs.get(stringIndex);
            String l = (stringIndex + 1) + "/" + (stringLogs.size() + 1);
            TomatoGUI.setTextAreaAndLabelDPS(s, l, true);
        } else if (stringIndex < stringLogs.size()) {
            stringIndex++;
            String l = (stringIndex + 1) + "/" + (stringLogs.size() + 1);
            TomatoGUI.setTextAreaAndLabelDPS(firstPage, l, false);
        }
    }

    /**
     * Find the previous dps log to display in the dps calculator.
     */
    public void previousDisplay() {
        if (stringIndex > 0) {
            stringIndex--;
            String s = stringLogs.get(stringIndex);
            String l = (stringIndex + 1) + "/" + (stringLogs.size() + 1);
            TomatoGUI.setTextAreaAndLabelDPS(s, l, true);
        }
    }

    private void addToStringLogs() {
        boolean b = true;
        String s = null;
        if (stringIndex == stringLogs.size()) {
            stringIndex++;
            b = false;
            s = "";
        }
        firstPage = stringDmg();
        stringLogs.add(firstPage);
        String l = (stringIndex + 1) + "/" + (stringLogs.size() + 1);
        TomatoGUI.setTextAreaAndLabelDPS(s, l, b);
    }

    /**
     * Update the text area of the dps calculator.
     */
    private void updateStringLogs() {
        if (stringIndex != stringLogs.size()) return;
        String firstPage = stringDmg();
        TomatoGUI.setTextAreaAndLabelDPS(firstPage, null, false);
    }

    private static class Damage implements Comparable {
        public int dmg;
        public int hits;
        public int guardDmg;
        public int guardHits;
        public int dammahDmg;
        public int dammahHits;

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
        public boolean chancellorDammahDmg = false;
        Packet packet;
        int totalDmg;
        boolean armorPiercing;

        public Bullet(Packet p) {
            packet = p;
        }
    }

    /**
     * Class used for entity info.
     */
    private static class Entity {

        private final int id;
        private int objectType = -1;
        private final StatData[] stats = new StatData[256];
        private final Bullet[] bulletDmg = new Bullet[512];
        private final ArrayList<Bullet> bulletDamageList = new ArrayList<>();

        private final ArrayList<Pair<StatData, Long>>[] inv = new ArrayList[4];

        public Entity(int id) {
            this.id = id;
            for (int i = 0; i < inv.length; i++) {
                inv[i] = new ArrayList<>();
            }
        }

        public void setBullet(short bulletId, Bullet bullet) {
            bulletDmg[bulletId] = bullet;
        }

        public Bullet getBullet(short p) {
            return bulletDmg[p];
        }

        public void setStats(StatData[] stats, long serverTime) {
            for (StatData sd : stats) {
                if (sd.statTypeNum >= 8 && sd.statTypeNum <= 11) {
                    inv[sd.statTypeNum - 8].add(new Pair(sd, serverTime));
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

        public String showInv(long firstServertime, long endServerTime) {
            if (stats[31] == null) return "";
            String s = "";
            for (int inventory = 0; inventory < 4; inventory++) {
                s += "<";

                if (inv[inventory].size() == 1) {
                    s += String.format("%s %.1fsec %s\n", IdToName.name(inv[inventory].get(0).left().statValue), (float) (endServerTime - firstServertime) / 1000, "100% Equipped:1 ");
                } else {
                    HashMap<Integer, Equipment> gear = new HashMap<>();
                    Pair<StatData, Long> pair2 = null;
                    long firstTime = 0;
                    boolean sameItem = false;
                    for (int i = 1; i < inv[inventory].size(); i++) {
                        Pair<StatData, Long> pair1 = inv[inventory].get(i - 1);
                        pair2 = inv[inventory].get(i);
                        if (pair1.left().statValue == pair2.left().statValue) sameItem = true;
                        else sameItem = false;
                        long time1 = pair1.right();
                        if (time1 == 0) time1 = firstServertime;
                        if (firstTime == 0) firstTime = time1;
                        addGear(gear, time1, pair2.right(), pair1.left().statValue, sameItem);
                    }
                    long totalTime = endServerTime - firstTime;
                    addGear(gear, pair2.right(), endServerTime, pair2.left().statValue, false);

                    Stream<Map.Entry<Integer, Equipment>> sorted2 = gear.entrySet().stream().sorted(comparingByValue());
                    for (Map.Entry<Integer, Equipment> m : sorted2.collect(Collectors.toList())) {
                        s += String.format("%s %.1fsec %.2f%% Equipped:%d ,", IdToName.name(m.getKey()), ((float) m.getValue().time / 1000), ((float) m.getValue().time * 100 / totalTime), m.getValue().swaps);
                    }
                }
                s = s.substring(0, s.length() - 2);
                s += "> ";
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

        @Override
        public String toString() {
            return IdToName.name(objectType);
        }
    }

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
}
