package tomato.backend.data;

import assets.AssetMissingException;
import assets.IdToAsset;
import packets.data.ObjectData;
import packets.incoming.*;
import packets.outgoing.EnemyHitPacket;
import packets.outgoing.PlayerShootPacket;
import tomato.gui.character.CharacterExaltGUI;
import tomato.gui.character.CharacterPanelGUI;
import tomato.gui.character.CharacterStatsGUI;
import tomato.gui.dps.DpsGUI;
import tomato.gui.security.ParsePanelGUI;
import tomato.gui.security.SecurityGUI;
import tomato.realmshark.HttpCharListRequest;
import tomato.realmshark.RealmCharacter;
import tomato.realmshark.RealmCharacterStats;
import tomato.realmshark.enums.CharacterClass;
import util.RNG;
import util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Main data class storing all incoming packet data regarding an instance the user is in.
 * Resets the data after leaving the instance.
 */
public class TomatoData {
    private String token;
    protected MapInfoPacket map;
    protected int worldPlayerId;
    protected int charId;
    protected long time;
    protected Entity player;
    protected final int[][] mapTiles = new int[2048][2048];
    protected final HashMap<Integer, Entity> entityList = new HashMap<>();
    protected final HashMap<Integer, Entity> playerList = new HashMap<>();
    protected final HashMap<Integer, Entity> playerListUpdated = new HashMap<>();
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

    /**
     * Sets the current realm.
     *
     * @param map New realm to be set.
     */
    public void setNewRealm(MapInfoPacket map) {
        clear();
        ParsePanelGUI.clear();
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

    private void updateDungeonStats(int charId, String str) {
        if (charMap == null) return;
        RealmCharacter r = charMap.get(charId);
        if (r == null) return;
        RealmCharacterStats newStats = new RealmCharacterStats();
        newStats.decode(str);
        if (!Arrays.equals(newStats.dungeonStats, r.charStats.dungeonStats)) {
            r.charStats = newStats;
            CharacterStatsGUI.updateRealmChars();
        }
    }

    /**
     * Sets the time of the server.
     *
     * @param serverRealTimeMS Server time in milliseconds.
     */
    public void setTime(long serverRealTimeMS) {
        this.time = serverRealTimeMS;
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
            if (e != null) {
                e.entityDropped(time);
            }
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
        Entity entity = entityList.computeIfAbsent(id, idd -> new Entity(this, idd, time));
        entity.entityUpdate(object.objectType, object.status, time);

        if (isCrystal(id)) {
            crystalTracker.add(id);
        }
        if (isPlayerEntity(object.objectType)) {
            playerList.put(id, entity);
            if (id == worldPlayerId) {
                player = entity;
                entity.setUser(charId);
            } else {
                entity.isPlayer();
            }
            ParsePanelGUI.addPlayer(id, entity);
        }
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
            Entity entity = entityList.computeIfAbsent(id, idd -> new Entity(this, idd, time));
            entity.updateStats(p.status[i], time);
        }
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
        if (p.spellBulletData) {
            Projectile projectile = new Projectile(p.damage, p.containerType);
            for (int j = p.bulletId; j < p.bulletId + p.bulletCount; j++) {
                projectiles[j % 256 + 256] = projectile;
            }
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
        Entity target = entityList.computeIfAbsent(id, idd -> new Entity(this, idd, time));
        Entity attacker = playerList.get(p.shooterID);
        target.userProjectileHit(attacker, projectile, time);
        if (!entityHitList.containsKey(id)) {
            entityHitList.put(id, target);
        }
    }

    /**
     * Info related to damage taken on entity's.
     *
     * @param p Info on entity taking damage, amount and by what player.
     */
    public void damage(DamagePacket p) {
        if (p.damageAmount > 0) {
            Projectile projectile = new Projectile(p.damageAmount);
            int id = p.targetId;
            Entity target = entityList.computeIfAbsent(id, idd -> new Entity(this, idd, time));
            Entity attacker = playerList.get(p.objectId);
            target.genericDamageHit(attacker, projectile, time);
            if (!entityHitList.containsKey(id) && !CharacterClass.isPlayerCharacter(id)) {
                entityHitList.put(id, target);
            }
        }
    }

    /**
     * Incoming packets updates after observing an entity becoming stasised.
     *
     * @param p Stasis packet
     */
    public void stasis(StasisPacket p) {
        float stasisDuration = p.stasisDuration;
//        System.out.println(p);

        int itemId = -1;
        if (stasisDuration == 4.0f) {
            itemId = 2627;
        }

        if (itemId == -1) return;

        for (Entity player : playerList.values()) {
            if (player.stat.INVENTORY_1_STAT.statValue == itemId) {
                System.out.println(player.stat.NAME_STAT.stringStatValue);
            }
        }
    }

    /**
     * Incoming text data.
     *
     * @param p Text info.
     */
    public void text(TextPacket p) {

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
        rng = null;
        player = null;
        entityList.clear();
        playerList.clear();
        crystalTracker.clear();
        playerListUpdated.clear();
        if (map != null && isLoggedDungeon(map.displayName)) {
            dpsData.add(new DpsData(map, entityHitList));
            DpsGUI.updateLabel();
        }
        entityHitList = new HashMap<>();
        for (int[] row : mapTiles) {
            Arrays.fill(row, 0);
        }
        for (Projectile p : projectiles) {
            if (p != null) p.clear();
        }
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
            if (player.stat.SEASONAL.statValue == 1) {
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
        CharacterPanelGUI.updateRealmChars();
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
            ArrayList<RealmCharacter> charList = HttpCharListRequest.getCharList(httpString);
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
    }
}
