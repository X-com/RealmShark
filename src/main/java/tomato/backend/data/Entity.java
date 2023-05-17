package tomato.backend.data;

import assets.AssetMissingException;
import assets.IdToAsset;
import packets.data.ObjectStatusData;
import packets.data.WorldPosData;

import java.util.*;
import java.util.stream.Collectors;

public class Entity {
    private boolean isUser;
    public final Stat stat;
    private final TomatoData tomatoData;
    private int id;
    private int objectType;
    private long creationTime;
    private WorldPosData pos;
    private ArrayList<ObjectStatusData> statUpdates;
    private ArrayList<Damage> damageList;
    private HashMap<Integer, Damage> damagePlayer;
    private String name;
    private long lastDamageTaken;

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
        try {
            name = IdToAsset.objectName(type);
        } catch (AssetMissingException e) {
            e.printStackTrace();
        }
    }

    // TODO fix time
    public void updateStats(ObjectStatusData status, long time) {
        statUpdates.add(status);
        stat.setStats(status.stats);
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
        damage.walledGardenReflectors = objectType == 29039 && stat.UNKNOWN125 != null && (stat.UNKNOWN125.statValue == -123818367 && tomatoData.floorPlanCrystals() == 12);
        damage.chancellorDammahDmg = objectType == 9635 && !damage.dammahCountered;
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

    public void setUser(boolean b) {
        isUser = b;
    }
}
