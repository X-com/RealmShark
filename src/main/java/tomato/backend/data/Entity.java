package tomato.backend.data;

import assets.AssetMissingException;
import assets.IdToAsset;
import assets.ImageBuffer;
import packets.data.ObjectStatusData;
import packets.data.WorldPosData;
import tomato.backend.StasisCheck;
import tomato.gui.character.CharacterStatMaxingGUI;
import tomato.gui.security.ParsePanelGUI;
import tomato.realmshark.RealmCharacter;
import tomato.realmshark.enums.CharacterClass;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Entity {
    private boolean isUser;
    public final Stat stat;
    private final TomatoData tomatoData;
    public final int id;
    public int objectType;
    private long creationTime;
    private WorldPosData pos;
    public final ArrayList<ObjectStatusData> statUpdates;
    private final ArrayList<Damage> damageList;
    private final HashMap<Integer, Damage> damagePlayer;
    public final HashMap<Integer, PlayerRemoved> playerDropped;
    private String name;
    private BufferedImage img;
    private long firstDamageTaken = -1;
    private long lastDamageTaken = -1;
    private int charId;
    public int[] baseStats;
    private boolean isPlayer;
    public long stasisCounter;
    public boolean dammahCountered;

    private final static int ORYX_THE_MAD_GOD = 45363;
    private final static int ORYX_THE_MAD_GOD_GUARD_ANIMATION = -935464302;
    private final static int ORYX_THE_MAD_GOD_GUARD_EXALTED_ANIMATION = -918686683;
    private final static int CHANCELLOR_DAMMAH = 9635;
    private final static int FORGOTTEN_KING = 29039;
    private final static int FORGOTTEN_KING_REFLECTOR_ANIMATION = -123818367;

    public Entity(TomatoData tomatoData, int id, long time) {
        this.tomatoData = tomatoData;
        this.id = id;
        creationTime = time;
        damageList = new ArrayList<>();
        statUpdates = new ArrayList<>();
        stat = new Stat();
        damagePlayer = new HashMap<>();
        playerDropped = new HashMap<>();
    }

    public void entityUpdate(int type, ObjectStatusData status, long time) {
        updateStats(status, time);
        this.objectType = type;
        try {
            if (type != -1) {
                name = IdToAsset.objectName(type);
                getImg(type);
            }
        } catch (Exception e) {
        }
    }

    private void getImg(int type) throws IOException, AssetMissingException {
        if (CharacterClass.isPlayerCharacter(type)) {
            type = stat.SKIN_ID.statValue == 0 ? type : stat.SKIN_ID.statValue;
        }
        img = ImageBuffer.getImage(type);
    }

    // TODO fix time
    public void updateStats(ObjectStatusData status, long time) {
        statUpdates.add(status);
        StasisCheck.checkManaFromStasis(this, status.stats);
        stat.setStats(status.stats);
        if (status.stats.length > 0) {
            if (isUser) {
                fame(time);
                tomatoData.player.charStat(charId, calculateBaseStats());
            } else if (isPlayer) {
                baseStats = calculateBaseStats();
            }
        }
        ParsePanelGUI.update(id, this);
    }

    public int maxHp() {
        if (stat.MAX_HP_STAT == null) return 0;
        return stat.MAX_HP_STAT.statValue;
    }

    public int hp() {
        if (stat.HP_STAT == null) return 0;
        return stat.HP_STAT.statValue;
    }

    public long getLastDamageTaken() {
        return lastDamageTaken;
    }

    public long getFirstDamageTaken() {
        return firstDamageTaken;
    }

    public String getFightTimerString() {
        long time = lastDamageTaken - firstDamageTaken;
        if (time == 0) return " [-]";
        long ms = time % 1000;
        long s = time / 1000 % 60;
        if (s == 0) return String.format(" [%dms]", ms);
        long m = time / 60000;
        if (m == 0) return String.format(" [%ds %dms]", s, ms);
        return String.format(" [%dm %ds %dms]", m, s, ms);
    }

    public long getFightTimer() {
        return lastDamageTaken - firstDamageTaken;
    }

    public void entityDropped(long time) {
//        updates.add(status); // TODO fix time
    }

    /**
     * Player entity stats multiplier such as attack, exalts and other buffs.
     *
     * @return damage multiplier from player stats.
     */
    public float playerStatsMultiplier() {
        boolean weak = (stat.CONDITION_STAT.statValue & 0x40) != 0;
        boolean damaging = (stat.CONDITION_STAT.statValue & 0x40000) != 0;
        int attack = stat.ATTACK_STAT.statValue;
        float exaltDmgBonus = (float) stat.EXALTATION_BONUS_DAMAGE.statValue / 1000;

        if (weak) {
            return 0.5f;
        }
        float number = (attack + 25) * 0.02f;
        if (damaging) {
            number *= 1.25;
        }
        return number * exaltDmgBonus;
    }

    public void userProjectileHit(Entity attacker, Projectile projectile, long time) {
        if (projectile == null || projectile.getDamage() == 0) return;

        int[] conditions = new int[2];

        conditions[0] = stat.CONDITION_STAT == null ? 0 : stat.CONDITION_STAT.statValue;
        conditions[1] = stat.NEW_CON_STAT == null ? 0 : stat.NEW_CON_STAT.statValue;
        int defence = stat.DEFENSE_STAT == null ? 0 : stat.DEFENSE_STAT.statValue;

        int dmg = Projectile.damageWithDefense(projectile.getDamage(), projectile.isArmorPiercing(), defence, conditions);

        if (dmg > 0) {
            Damage damage = new Damage(attacker, projectile, time, dmg);
            bossPhaseDamage(damage);
            addPlayerDmg(damage);
            if (firstDamageTaken == -1) {
                firstDamageTaken = time;
            }
            lastDamageTaken = time;
        }
    }

    public void genericDamageHit(Entity attacker, Projectile projectile, long time) {
        if (projectile == null || projectile.getDamage() == 0) return;
        Damage damage = new Damage(attacker, projectile, time);
        bossPhaseDamage(damage);
        addPlayerDmg(damage);
    }

    private void addPlayerDmg(Damage damage) {
        damageList.add(damage);
        if (damage.owner != null) {
            int id = damage.owner.id;
            Damage dmg = damagePlayer.computeIfAbsent(id, a -> new Damage(damage.owner));
            dmg.add(damage);
        }
    }

    private void bossPhaseDamage(Damage damage) {
        damage.oryx3GuardDmg = objectType == ORYX_THE_MAD_GOD && stat.ANIMATION_ID != null && (stat.ANIMATION_ID.statValue == ORYX_THE_MAD_GOD_GUARD_ANIMATION || stat.ANIMATION_ID.statValue == ORYX_THE_MAD_GOD_GUARD_EXALTED_ANIMATION);
        damage.walledGardenReflectors = objectType == FORGOTTEN_KING && stat.ANIMATION_ID != null && (stat.ANIMATION_ID.statValue == FORGOTTEN_KING_REFLECTOR_ANIMATION && tomatoData.floorPlanCrystals() == 12);
        damage.chancellorDammahDmg = objectType == CHANCELLOR_DAMMAH && !dammahCountered;
    }

    public String name() {
        if (CharacterClass.isPlayerCharacter(objectType) && stat.NAME_STAT != null) {
            return stat.NAME_STAT.stringStatValue.split(",")[0];
        }
        return name;
    }

    public String getStatName() {
        if (stat.NAME_STAT == null) return null;
        return stat.NAME_STAT.stringStatValue;
    }

    public ArrayList<Damage> getDamageList() {
        return damageList;
    }

    public List<Damage> getPlayerDamageList() {
        return Arrays.stream(damagePlayer.values().toArray(new Damage[0])).sorted(Comparator.comparingInt(Damage::getDamage).reversed()).collect(Collectors.toList());
    }

    public boolean isUser() {
        return isUser;
    }

    public void isPlayer() {
        isPlayer = true;
        baseStats = calculateBaseStats();
    }

    public void setUser(int charId) {
        isUser = true;
        this.charId = charId;
        baseStats = calculateBaseStats();
    }

    private int[] calculateBaseStats() {
        int[] base = new int[8];

        base[0] = stat.MAX_HP_STAT.statValue - stat.MAX_HP_BOOST_STAT.statValue;
        base[1] = stat.MAX_MP_STAT.statValue - stat.MAX_MP_BOOST_STAT.statValue;
        base[2] = stat.ATTACK_STAT.statValue - stat.ATTACK_BOOST_STAT.statValue;
        base[3] = stat.DEFENSE_STAT.statValue - stat.DEFENSE_BOOST_STAT.statValue;
        base[4] = stat.SPEED_STAT.statValue - stat.SPEED_BOOST_STAT.statValue;
        base[5] = stat.DEXTERITY_STAT.statValue - stat.DEXTERITY_BOOST_STAT.statValue;
        base[6] = stat.VITALITY_STAT.statValue - stat.VITALITY_BOOST_STAT.statValue;
        base[7] = stat.WISDOM_STAT.statValue - stat.WISDOM_BOOST_STAT.statValue;

        return base;
    }

    /**
     * Fame update from experience points.
     *
     * @param time
     */
    private void fame(long time) {
        long exp = Long.parseLong(stat.EXP_STAT.stringStatValue);
        FameTracker.trackFame(charId, exp, time);
        if (tomatoData.charMap != null) {
            long fame = (exp + 40071) / 2000;
            RealmCharacter r = tomatoData.charMap.get(charId);
            if (r != null) {
                r.fame = fame;
            }
        }
    }

    /**
     * Updates player stats when drinking potions.
     *
     * @param charId User character id that is loaded in.
     * @param stats  Current base stats of user character.
     */
    public void charStat(int charId, int[] stats) {
        if (tomatoData.charMap == null) return;
        RealmCharacter r = tomatoData.charMap.get(charId);

        if (r == null) return;

        if (r.hp != stats[0]) {
            r.hp = stats[0];
            CharacterStatMaxingGUI.updateRealmChars();
        } else if (r.mp != stats[1]) {
            r.mp = stats[1];
            CharacterStatMaxingGUI.updateRealmChars();
        } else if (r.atk != stats[2]) {
            r.atk = stats[2];
            CharacterStatMaxingGUI.updateRealmChars();
        } else if (r.def != stats[3]) {
            r.def = stats[3];
            CharacterStatMaxingGUI.updateRealmChars();
        } else if (r.spd != stats[4]) {
            r.spd = stats[4];
            CharacterStatMaxingGUI.updateRealmChars();
        } else if (r.dex != stats[5]) {
            r.dex = stats[5];
            CharacterStatMaxingGUI.updateRealmChars();
        } else if (r.vit != stats[6]) {
            r.vit = stats[6];
            CharacterStatMaxingGUI.updateRealmChars();
        } else if (r.wis != stats[7]) {
            r.wis = stats[7];
            CharacterStatMaxingGUI.updateRealmChars();
        }
    }

    public BufferedImage img() {
        return img;
    }

    public void addPlayerDrop(int dropId, long time) {
        int hp = hp();
        int max = maxHp();
        String name = name();
        PlayerRemoved pr = new PlayerRemoved(dropId, hp, max, name, time);
        playerDropped.put(dropId, pr);
    }
}
