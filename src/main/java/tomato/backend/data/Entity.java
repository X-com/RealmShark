package tomato.backend.data;

import assets.IdToAsset;
import packets.data.ObjectStatusData;
import packets.data.WorldPosData;
import tomato.gui.character.CharacterStatMaxingGUI;
import tomato.gui.security.ParsePanelGUI;
import tomato.realmshark.RealmCharacter;

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
    private String name;
    private long lastDamageTaken;
    private int charId;
    public int[] baseStats;
    private boolean isPlayer;
    public long stasisTimer;

    public Entity(TomatoData tomatoData, int id, long time) {
        this.tomatoData = tomatoData;
        this.id = id;
        creationTime = time;
        damageList = new ArrayList<>();
        statUpdates = new ArrayList<>();
        stat = new Stat();
        damagePlayer = new HashMap<>();
    }

    public void entityUpdate(int type, ObjectStatusData status, long time) {
        updateStats(status, time);
        this.objectType = type;
        try {
            if (type != -1) {
                name = IdToAsset.objectName(type);
            }
        } catch (Exception e) {
        }
    }

    // TODO fix time
    public void updateStats(ObjectStatusData status, long time) {
        statUpdates.add(status);
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

    public long getLastDamageTaken() {
        return lastDamageTaken;
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
        damage.oryx3GuardDmg = objectType == 45363 && stat.ANIMATION_ID != null && (stat.ANIMATION_ID.statValue == -935464302 || stat.ANIMATION_ID.statValue == -918686683);
        damage.walledGardenReflectors = objectType == 29039 && stat.ANIMATION_ID != null && (stat.ANIMATION_ID.statValue == -123818367 && tomatoData.floorPlanCrystals() == 12);
        damage.chancellorDammahDmg = objectType == 9635 && !Damage.dammahCountered;
    }

    public String name() {
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
}
