package tomato.backend.data;

import assets.IdToAsset;
import packets.Packet;
import packets.data.ObjectData;
import packets.data.StatData;
import packets.data.WorldPosData;
import packets.data.enums.NotificationEffectType;
import packets.data.enums.StatType;
import packets.incoming.*;
import packets.outgoing.*;
import tomato.backend.SecurityAbilityUseCheck;
import tomato.gui.character.*;
import tomato.gui.chat.ChatGUI;
import tomato.gui.dps.DpsGUI;
import tomato.gui.keypop.KeypopGUI;
import tomato.gui.myinfo.MyInfoGUI;
import tomato.gui.security.ParsePanelGUI;
import tomato.gui.stats.LootGUI;
import tomato.realmshark.HttpCharListRequest;
import tomato.realmshark.RealmCharacter;
import tomato.realmshark.RealmCharacterStats;
import tomato.realmshark.Sound;
import tomato.realmshark.enums.CharacterClass;
import tomato.realmshark.enums.LootBags;
import util.PropertiesManager;
import util.RNG;

import java.io.IOException;
import java.util.*;

/**
 * Main data class storing all incoming packet data regarding an instance the user is in.
 * Resets the data after leaving the instance.
 */
public class TomatoData {
    private String token;
    public MapInfoPacket map;
    protected int worldPlayerId;
    protected int charId;
    public long time;
    public long timePc;
    private long timePcFirst;
    public Entity player;
    public Entity pet;
    protected final int[][] mapTiles = new int[2048][2048];
    public final HashMap<Integer, Entity> entityList = new HashMap<>();
    protected final HashMap<Integer, Entity> playerList = new HashMap<>();
    public final HashMap<Integer, Entity> playerListUpdated = new HashMap<>();
    protected final Projectile[] projectiles = new Projectile[512];
    protected RNG rng;
    protected HashSet<Integer> crystalTracker = new HashSet<>();
    private HashMap<Integer, Entity> entityHitList = new HashMap<>();
    public VaultData regularVault = new VaultData();
    public VaultData seasonalVault = new VaultData();
    public boolean vaultDataRecievedSeasonal, vaultDataRecievedRegular, characterDataRecieved;
    public ArrayList<RealmCharacter> chars;
    public HashMap<Integer, RealmCharacter> charMap;
    public ArrayList<DpsData> dpsData = new ArrayList<>();
    protected ArrayList<NotificationPacket> deathNotifications = new ArrayList<>();
    protected final HashMap<Integer, Entity> dropList = new HashMap<>();
    private ArrayList<Packet> dpsPacketLog = new ArrayList<>();
    private boolean petyard;
    private RealmCharacterStats currentCharacterStats;
    private final TreeSet<Integer> lootBags = new TreeSet<>();
    private int lootTickToggle = 0;
    private final ArrayList<Entity>[] lootTickContainer = new ArrayList[]{new ArrayList<>(), new ArrayList<>()};
    private final ArrayList<Entity> killedEntitys = new ArrayList<>();
    protected final HashMap<Long, Projectile> enemyProjectiles = new HashMap<>();
    private final DungeonStatData dungeonStatData = new DungeonStatData();
    private int moonlightFlames = 0;
    private static final int MOONLIGHT_BOSS_FLAME_ID = 20518;
    private boolean updatedExaltStats = false;
    private final HashMap<String, ArrayList<String>> propLists = new HashMap<>();
    private ArrayList<Integer> idEntityPing = new ArrayList<>();

    /**
     * Sets the current realm.
     *
     * @param map New realm to be set.
     */
    public void setNewRealm(MapInfoPacket map) {
        clear();
        ParsePanelGUI.clear();
        petYardCheck(map.displayName);
        this.map = map;
        rng = new RNG(map.seed);
    }

    /**
     * Sets the current realm users character id.
     *
     * @param objectId ID of the object in the world.
     * @param charId   Current character id loaded.
     * @param str
     */
    public void setUserId(int objectId, int charId, String str) {
        this.worldPlayerId = objectId;
        this.charId = charId;
        updateDungeonStats(charId, str);
    }

    public void petYardCheck(String displayName) {
        if (displayName.equals("Pet Yard")) {
            petyard = true;
            CharacterPetsGUI.clearPets();
        }
    }

    public void webRequest() {
        if (map.displayName.equals("Pet Yard")) {
            petyard = true;
            CharacterPetsGUI.clearPets();
            charListHttpRequest();
        } else if (map.displayName.equals("Daily Quest Room")) {
            charListHttpRequest();
        }
    }

    private void updateDungeonStats(int charId, String str) {
        if (currentCharacterStats == null || !currentCharacterStats.pcStats.equals(str)) {
            currentCharacterStats = new RealmCharacterStats();
            currentCharacterStats.decode(str);
        }

        if (charMap == null) {
            return;
        }
        RealmCharacter r = charMap.get(charId);
        if (r != null && r.charStats != null && !r.charStats.pcStats.equals(str)) {
            r.updateCharStats(currentCharacterStats);
            CharacterStatsGUI.updateRealmChars();
            CharacterCollectionGUI.updateRealmChars();
        }
    }

    /**
     * Gets the dungeion completion stats of the currently played character.
     * Note! only available if the instance have been changed at least ones from starting the app.
     *
     * @return Currently playing character stats.
     */
    public RealmCharacterStats getCurrentDungeonStats() {
        if (currentCharacterStats == null) return null;
        return currentCharacterStats;
    }

    /**
     * Sets the time of the server.
     *
     * @param serverRealTimeMS Server time in milliseconds.
     */
    public void setTime(long serverRealTimeMS) {
        time = serverRealTimeMS;
        timePc = System.currentTimeMillis();
        if (timePcFirst == -1) timePcFirst = timePc;
    }

    /**
     * Packet updating players position
     *
     * @param p Move packet data
     */
    public void updatePlayersPos(MovePacket p) {
        if (player != null && p.records != null && p.records.length > 0) {
            player.pos = p.records[p.records.length - 1].pos;
        }
    }

    /**
     * Main update packet.
     *
     * @param p Update packet
     */
    public void update(UpdatePacket p) {
        for (int i = 0; i < p.tiles.length; i++) {
            mapTiles[p.tiles[i].x][p.tiles[i].y] = p.tiles[i].type;
        }
        for (int i = 0; i < p.newObjects.length; i++) {
            ObjectData object = p.newObjects[i];
            entityUpdate(object);
        }
        for (int i = 0; i < p.drops.length; i++) {
            int dropId = p.drops[i];
            crystalTracker.remove(dropId);
            Entity e = entityList.get(dropId);
            dropList.put(dropId, e);
            if (e != null) {
//                e.entityDropped(timePc);
                if (isPlayerEntity(e.objectType)) {
                    for (Map.Entry<Integer, Entity> dropCheck : entityHitList.entrySet()) {
                        int k = dropCheck.getKey();
                        if (!dropList.containsKey(k)) {
                            dropCheck.getValue().addPlayerDrop(dropId, timePc);
                        }
                    }
                }
            }
            if (entityHitList.containsKey(dropId)) {
                killedEntitys.add(e);
            }

            playerListUpdated.remove(dropId);
            ParsePanelGUI.removePlayer(dropId);
        }
    }

    /**
     * Adds an entity to the entity lists as well as updates objects.
     *
     * @param object Entity object to be added or updated
     */
    private void entityUpdate(ObjectData object) {
        int id = object.status.objectId;
        boolean newObject = !entityList.containsKey(id);
        Entity entity = entityList.computeIfAbsent(id, idd -> new Entity(this, idd, timePc));
        int idType = object.objectType;
        entity.entityUpdate(idType, object.status, timePc);

        if (newObject) {
            moonLightFlameCounter(idType);
            SecurityAbilityUseCheck.decoy(entity);
            customSoundAlert(idType);
        }
        if (petyard) {
            addPet(object);
        } else if (isCrystal(idType)) {
            crystalTracker.add(id);
        } else if (isLootBag(idType) && !lootBags.contains(id)) {
            lootBags.add(id);
            lootTickContainer[lootTickToggle].add(entity);
        } else if (isPlayerEntity(idType)) {
            playerList.put(id, entity);
            playerListUpdated.put(id, entity);
            if (id == worldPlayerId) {
                player = entity;
                entity.setUser(charId);
                MyInfoGUI.updatePlayer(player);
            } else {
                entity.isPlayer();
            }
            ParsePanelGUI.addPlayer(id, entity);
        }
    }

    /**
     * Plays sound if user wants alerts when this type of entity shows up.
     *
     * @param idType ID of sound alert entity
     */
    private void customSoundAlert(int idType) {
        for(int id : idEntityPing) {
            if(idType == id) {
                Sound.custom.play();
                break;
            }
        }
    }

    /**
     * Method to connect loot bag drops to mobs that drop them.
     * If an entity drops a tick after loot bag shows up and is
     * close enough it is most likely the entity that dropped the bag.
     */
    private void lootTick() {
        lootTickToggle ^= 1;
        if (!lootTickContainer[lootTickToggle].isEmpty()) {
            try {
                for (Entity bag : lootTickContainer[lootTickToggle]) {
                    double dist = 0;
                    Entity mob = null;
                    for (Entity k : killedEntitys) {
                        double d = bag.distSqrd(k.pos);
                        if (mob == null) {
                            dist = d;
                            mob = k;
                        } else if (d < dist) {
                            dist = d;
                            mob = k;
                        }
                    }
                    if (map != null) {
                        dungeonStatData.updateItems(map.name, mob, bag);
                    }
                    LootGUI.update(map, bag, mob, player, timePc);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            lootTickContainer[lootTickToggle].clear();
        }
        if (!killedEntitys.isEmpty()) {
            killedEntitys.clear();
        }
    }

    private void addPet(ObjectData object) {
        CharacterPetsGUI.addPet(object);
    }

    /**
     * Checks if id of shatters king boss crystal type.
     *
     * @param id Entity id.
     * @return True if id matches a crystal.
     */
    private boolean isCrystal(int id) {
        return id == 46721 || id == 46771 || id == 29501 || id == 33656;
    }

    /**
     * Checks if id is a loot drop bag
     *
     * @param id Entity id type.
     * @return True if id is loot bag
     */
    private boolean isLootBag(int id) {
        return LootBags.isLootDropBag(id);
    }

    /**
     * Determines the floor pattern in shatters king fight.
     *
     * @return Gives a mask id indicating the crystal colors in the king fight.
     */
    public int floorPlanCrystals() {
        int mask = 0;
        for (int i : crystalTracker) {
            if (i == 46721) {
                mask |= 1;
            } else if (i == 46771) {
                mask |= 2;
            } else if (i == 29501) {
                mask |= 4;
            } else if (i == 33656) {
                mask |= 8;
            }
        }
        return mask;
    }

    /**
     * Checks if objectType is a player entity.
     *
     * @param objectType ID of the object
     * @return True if ID matches a player entity.
     */
    private boolean isPlayerEntity(int objectType) {
        return CharacterClass.isPlayerCharacter(objectType);
    }

    /**
     * Entity updates and server time from new tick packet.
     *
     * @param p New tick packet.
     */
    public void updateNewTick(NewTickPacket p) {
        setTime(p.serverRealTimeMS);
        for (int i = 0; i < p.status.length; i++) {
            int id = p.status[i].objectId;
            Entity entity = entityList.computeIfAbsent(id, idd -> new Entity(this, idd, timePc));
            entity.updateStats(p.status[i], timePc);
        }
        SecurityAbilityUseCheck.decreaseDecoyCounter();
        lootTick();
    }

    /**
     * Creates a new projectile from the outgoing packet.
     *
     * @param p Projectile info.
     */
    public void playerShoot(PlayerShootPacket p) {
        projectiles[p.bulletId] = new Projectile(rng, player, p.weaponId, p.projectileId);
    }

    /**
     * Handles special projectile creations from the outgoing packet.
     *
     * @param p Projectile info
     */
    public void serverPlayerShoot(ServerPlayerShootPacket p) {
        if (p.bulletCount > 1) {
            Projectile projectile = new Projectile(p.damage, p.containerType, p.bulletType, p.summonerId);
            for (int j = p.bulletId; j < p.bulletId + p.bulletCount; j++) {
                projectiles[j % 256 + 256] = projectile;
            }
        } else if (p.bulletId > 255 && p.bulletId < 512) {
            Projectile projectile = new Projectile(p.damage, p.containerType, p.bulletType, p.summonerId);
            projectiles[p.bulletId] = projectile;
        }
    }

    /**
     * Handles entity's being hit by users projectiles.
     *
     * @param p Info about what entity was hit by what projectile.
     */
    public void enemtyHit(EnemyHitPacket p) {
        Projectile projectile = projectiles[p.bulletId];
        int id = p.targetId;
        Entity target = entityList.computeIfAbsent(id, idd -> new Entity(this, idd, timePc));
        int shooterId = p.shooterID;
        if (projectile != null && projectile.getSummonerId() != 0) {
            shooterId = projectile.getSummonerId();
        }
        Entity attacker = playerList.get(shooterId);
        target.userProjectileHit(attacker, projectile, timePc);
        if (!entityHitList.containsKey(id)) {
            entityHitList.put(id, target);
            if (attacker != null && attacker.isUser() && map != null) {
                dungeonStatData.updateEntityDamage(map.name, target);
            }
        }
        target.updateDamageTaken(timePc);
    }

    /**
     * Info related to damage taken on entity's.
     *
     * @param p Info on entity taking damage, amount and by what player.
     */
    public void damage(DamagePacket p) {
        int id = p.targetId;
        Entity target = entityList.computeIfAbsent(id, idd -> new Entity(this, idd, timePc));
        Entity attacker = playerList.get(p.objectId);
        if (p.damageAmount > 0) {
            Projectile projectile = new Projectile(p.damageAmount);
            target.genericDamageHit(attacker, projectile, timePc);
            if (!entityHitList.containsKey(id)) {
                entityHitList.put(id, target);
                if (attacker != null && attacker.isUser() && map != null) {
                    dungeonStatData.updateEntityDamage(map.name, target);
                }
            }
        }
        target.updateDamageTaken(timePc);
    }

    /**
     * Packet indicating player taking damage by enemy.
     */
    public void userDamage(PlayerHitPacket p) {
        if (player != null) {
            Entity e = entityList.get(p.objectId);
            long id = p.objectId + ((long) p.bulletId << 24);
            Projectile p1 = enemyProjectiles.get(id);
            if (p1 != null) {
                player.userDamageTaken(e, timePc, p1);
            }
        }
    }

    /**
     * Packet for projectiles seen by user.
     *
     * @param p Enemy projectile packet
     */
    public void enemyProjectile(EnemyShootPacket p) {
        Entity e = entityList.get(p.ownerId);
        boolean ap = false;
        if (e != null) {
            int etype = e.objectType;
            int btype = p.bulletType;
            try {
                ap = IdToAsset.getIdProjectileArmorPierces(etype, btype);
            } catch (Exception ignored) {
            }
        }
        for (int i = 0; i < p.numShots; i++) {
            long id = p.ownerId + ((long) (p.bulletId + i) << 24);
            enemyProjectiles.put(id, new Projectile(p.damage, ap));
        }
    }

    /**
     * Aoe damage packet
     *
     * @param p Aoe packet
     */
    public void aoeDamage(AoePacket p) {
        if (player != null) {
            if (player.distSqrd(p.pos) < (p.radius * p.radius)) {
                player.userDamageTaken(null, timePc, new Projectile(p.damage, p.armorPiercing));
            }
        }
    }

    /**
     * Ground tile damage on player
     *
     * @param p Ground tile packet update
     */
    public void groundDamage(GroundDamagePacket p) {
        if (player != null) {
            WorldPosData pos = p.position;
            int id = mapTiles[(int) pos.x][(int) pos.y];
            int dmg = IdToAsset.getTileDamage(id);
            player.userDamageTaken(null, timePc, new Projectile(dmg, true));
        }
    }

    /**
     * Dungeons that should not be logged.
     *
     * @param dungName Map data name of the instance.
     * @return Dungeon that should be logged.
     */
    private static boolean isLoggedDungeon(String dungName) {
        switch (dungName) {
            case "{s.vault}":  // vault
            case "Daily Quest Room": // quest room
            case "Pet Yard": // pet yard
            case "{s.guildhall}": // guild hall
            case "{s.nexus}": // nexus
            case "Grand Bazaar": // bazaar
                return false;
            default:
                return true;
        }
    }

    /**
     * Clears all data as instance is changing.
     */
    public void clear() {
        worldPlayerId = -1;
        charId = -1;
        time = -1;
        if (map != null && isLoggedDungeon(map.displayName)) {
            dpsData.add(new DpsData(map, entityHitList, deathNotifications, dungeonTime(), timePcFirst, dpsPacketLog));
            DpsGUI.updateLabel();
        }
        if (map != null) {
            dungeonStatData.updateDungeon(map.name, dungeonTime());
        }
        dpsPacketLog = new ArrayList<>();
        timePc = -1;
        timePcFirst = -1;
        rng = null;
        player = null;
        entityList.clear();
        playerList.clear();
        crystalTracker.clear();
        playerListUpdated.clear();
        dropList.clear();
        lootBags.clear();
        enemyProjectiles.clear();
        deathNotifications = new ArrayList<>();
        entityHitList = new HashMap<>();
        for (int[] row : mapTiles) {
            Arrays.fill(row, 0);
        }
        for (Projectile p : projectiles) {
            if (p != null) p.clear();
        }
        petyard = false;
        moonlightFlames = 0;
    }

    public Entity[] getEntityHitList() {
        return entityHitList.values().toArray(new Entity[0]);
    }

    public void exaltUpdate(ExaltationUpdatePacket p) {
        int[] exalts = RealmCharacter.exalts.get((int) p.objType);
        if (exalts == null) return;
        int[] update = new int[]{p.dexterityProgress, p.speedProgress, p.vitalityProgress, p.wisdomProgress, p.defenseProgress, p.attackProgress, p.manaProgress, p.healthProgress};
        if (!Arrays.equals(exalts, update)) {
            RealmCharacter.exalts.put((int) p.objType, update);
            CharacterExaltGUI.updateExalts();
        }
    }

    public void vaultPacketUpdate(VaultContentPacket p) {
        if (player != null) {
            if (player.stat.get(StatType.SEASONAL).statValue == 1) {
                vaultDataRecievedSeasonal = true;
                seasonalVault.vaultPacketUpdate(p);
            } else {
                vaultDataRecievedRegular = true;
                regularVault.vaultPacketUpdate(p);
            }
            CharacterPanelGUI.vaultDataUpdate();
        }
    }

    public void characterListUpdate(ArrayList<RealmCharacter> chars) {
        characterDataRecieved = true;
        this.chars = chars;
        charMap = new HashMap<>();
        for (RealmCharacter r : chars) {
            charMap.put(r.charId, r);
        }
        seasonalVault.clearChar();
        regularVault.clearChar();
        for (RealmCharacter c : chars) {
            if (c.seasonal) {
                seasonalVault.updateCharInventory(c);
            } else {
                regularVault.updateCharInventory(c);
            }
        }
        RealmCharacter currentChar = charMap.get(charId);
        if (charId != -1 && currentChar != null) {
            makePet(currentChar);
            MyInfoGUI.updatePet(pet);
        }
        CharacterPanelGUI.updateRealmChars();
    }

    private void makePet(RealmCharacter currentChar) {
        pet = new Entity(this, -1, time);

        pet.stat.set(StatType.SKIN_ID, new StatData());
        pet.stat.set(StatType.PET_TYPE_STAT, new StatData());
        pet.stat.set(StatType.PET_NAME_STAT, new StatData());
        pet.stat.set(StatType.PET_RARITY_STAT, new StatData());
        pet.stat.set(StatType.PET_INSTANCE_ID_STAT, new StatData());
        pet.stat.set(StatType.PET_MAX_ABILITY_POWER_STAT, new StatData());
        pet.stat.set(StatType.PET_FIRST_ABILITY_POINT_STAT, new StatData());
        pet.stat.set(StatType.PET_FIRST_ABILITY_POWER_STAT, new StatData());
        pet.stat.set(StatType.PET_FIRST_ABILITY_TYPE_STAT, new StatData());
        pet.stat.set(StatType.PET_SECOND_ABILITY_POINT_STAT, new StatData());
        pet.stat.set(StatType.PET_SECOND_ABILITY_POWER_STAT, new StatData());
        pet.stat.set(StatType.PET_SECOND_ABILITY_TYPE_STAT, new StatData());
        pet.stat.set(StatType.PET_THIRD_ABILITY_POINT_STAT, new StatData());
        pet.stat.set(StatType.PET_THIRD_ABILITY_POWER_STAT, new StatData());
        pet.stat.set(StatType.PET_THIRD_ABILITY_TYPE_STAT, new StatData());

        pet.stat.get(StatType.SKIN_ID).statValue = currentChar.petSkin;
        pet.stat.get(StatType.PET_TYPE_STAT).statValue = currentChar.petType;
        pet.stat.get(StatType.PET_NAME_STAT).stringStatValue = currentChar.petName;
        pet.stat.get(StatType.PET_RARITY_STAT).statValue = currentChar.petRarity;
        pet.stat.get(StatType.PET_INSTANCE_ID_STAT).statValue = currentChar.petInstanceId;
        pet.stat.get(StatType.PET_MAX_ABILITY_POWER_STAT).statValue = currentChar.petMaxAbilityPower;

        pet.stat.get(StatType.PET_FIRST_ABILITY_POINT_STAT).statValue = currentChar.petAbilitys[0];
        pet.stat.get(StatType.PET_FIRST_ABILITY_POWER_STAT).statValue = currentChar.petAbilitys[1];
        pet.stat.get(StatType.PET_FIRST_ABILITY_TYPE_STAT).statValue = currentChar.petAbilitys[2];
        pet.stat.get(StatType.PET_SECOND_ABILITY_POINT_STAT).statValue = currentChar.petAbilitys[3];
        pet.stat.get(StatType.PET_SECOND_ABILITY_POWER_STAT).statValue = currentChar.petAbilitys[4];
        pet.stat.get(StatType.PET_SECOND_ABILITY_TYPE_STAT).statValue = currentChar.petAbilitys[5];
        pet.stat.get(StatType.PET_THIRD_ABILITY_POINT_STAT).statValue = currentChar.petAbilitys[6];
        pet.stat.get(StatType.PET_THIRD_ABILITY_POWER_STAT).statValue = currentChar.petAbilitys[7];
        pet.stat.get(StatType.PET_THIRD_ABILITY_TYPE_STAT).statValue = currentChar.petAbilitys[8];
    }

    /**
     * Handles character data by sending char list request to rotmg servers while in the daily quest room.
     * This is done here given pet yard and daily quest instance is the only instances where the char list
     * request can be done without being rejected by rotmg servers.
     * <p>
     * token Current client token string used in http request packet.
     */
    public void charListHttpRequest() {
        try {
            String httpString = HttpCharListRequest.getChartList(token);
            ArrayList<RealmCharacter> charList = RealmCharacter.getCharList(httpString);
            if (charList != null) characterListUpdate(charList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stores the token for char list requests.
     *
     * @param token Current client token.
     */
    public void updateToken(String token) {
        this.token = token;

        updateExalts(token);
    }

    private void updateExalts(String token) {
        if (updatedExaltStats) return;
        updatedExaltStats = true;
        try {
            String s = HttpCharListRequest.getPowerUpStats(token);
            if (RealmCharacter.checkExaltNew(s)) {
                LootGUI.updateExaltStats();
                CharacterExaltGUI.updateExalts();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Incoming text data.
     *
     * @param p Text info.
     */
    public void text(TextPacket p) {
        if (p.text.equals("I SAID DO NOT INTERRUPT ME! For this I shall hasten your end!")) {
            Entity e = entityList.get(p.objectId);
            if (e != null) {
                e.dammahCountered = true;
            }
        }
        ChatGUI.updateChat(p);
    }

    /**
     * Handles notification packets.
     *
     * @param packet Notification packet
     */
    public void notification(NotificationPacket packet) {
        if (packet.effect == NotificationEffectType.PlayerDeath) {
            deathNotifications.add(packet);
        }
        KeypopGUI.packet(this, packet);
    }

    public ArrayList<NotificationPacket> getDeathNotifications() {
        return deathNotifications;
    }

    public long dungeonTime() {
        return timePc - timePcFirst;
    }

    public void logPacket(Packet packet) {
        dpsPacketLog.add(packet);
    }

    public void bootload() {
        dungeonStatData.load();
    }

    /**
     * Counts the number of moonlight boss flames that are observed by the player in moonlight village.
     *
     * @param idType Object ID to check if it's a boss flame.
     */
    private void moonLightFlameCounter(int idType) {
        if (idType == MOONLIGHT_BOSS_FLAME_ID) moonlightFlames++;
    }

    /**
     * Gets the number of flames from moonlight village boss phases
     *
     * @return Moonlight village boss flames
     */
    public int getMoonlightFlameCount() {
        return moonlightFlames;
    }

    /**
     * Used to reset the flames in moonlight village to separate umi flames from other boss flames.
     */
    public void resetMoonlightFlames() {
        moonlightFlames = 0;
    }

    /** Get and set Entity ID's the player wants to ping when appearing.
     *
     * @param a Array of all the entity IDs
     */
    public void setIdEntityPing(ArrayList<Integer> a) {
        idEntityPing = a;
    }
    public ArrayList<Integer> getEntityIdPings() {
        return idEntityPing;
    }

    /** Get and set Items the player wants to ping when appearing.
     *
     * @param a Array of all the item values
     */
    public void setItemPing(ArrayList<String> a) {
        savePropList(a, "itemPings");
    }

    public ArrayList<String> getItemPings() {
        return propLists.get("itemPings");
    }

    public boolean isItemPing(String item) {
        ArrayList<String> itemPing = propLists.get("itemPings");
        for(String s : itemPing) {
            if(item.toLowerCase().contains(s.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get a list property from the data storage.
     *
     * @param propName  Property name to get
     * @param list      Property list to set
     */
    public void setPropList(String propName, ArrayList<String> list) {
        propLists.put(propName, list);
    }

    /**
     * Load a list property from the properties manager into the data storage.
     *
     * @param propName  Property name to load
     * @param delimiter Delimiter used to split the property string
     */
    public void loadPropList(String propName, String delimiter) {
        ArrayList<String> arr = new ArrayList<>();
        String messages = PropertiesManager.getProperty(propName);
        if (messages == null) return;
        for (String s : messages.split(delimiter)) {
            if (!s.isEmpty()) {
                arr.add(s);
            }
        }
        this.setPropList(propName, arr);
    }

    public void loadPropList(String propName) {
        loadPropList(propName, "§");
    }

    /**
     * Save a list property from the data storage into the properties manager.
     *
     * @param list      List to save
     * @param propName  Property name to save
     * @param delimiter Delimiter used to join the list into a string
     */
    public void savePropList(ArrayList<String> list, String propName, String delimiter) {
        this.setPropList(propName, list);
        if (list == null || list.isEmpty()) {
            PropertiesManager.setProperties(propName, "");
            return;
        }
        StringBuilder s = new StringBuilder();
        for (String i : list) {
            s.append(delimiter).append(i);
        }
        PropertiesManager.setProperties(propName, s.substring(1));
    }

    public void savePropList(ArrayList<String> list, String propName) {
        savePropList(list, propName, "§");
    }
}
