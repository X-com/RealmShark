package tomato.backend.data;

import assets.IdToAsset;
import java.io.Serializable;
import packets.data.enums.StatType;
import util.RNG;

public class Projectile implements Serializable {

    private int damage;
    private int summonerId;
    private boolean armorPiercing;
    private int containerType = -1;
    // originAbilityItem: optional id of the ability item (inventory slot 1) that originated this projectile.
    // This is set externally when available (e.g., in serverPlayerShoot or the RNG-based constructor)
    // so Damage creation can attribute proc hits directly to the correct item.
    private int originAbilityItem = -1;
    // Snapshot of the relevant stat value used for scaling (e.g., WIS/VIT) at shot time.
    // If not set, will be Integer.MIN_VALUE to indicate absence.
    private int originScalingStat = Integer.MIN_VALUE;

    public Projectile(int damage) {
        this.damage = damage;
    }

    public Projectile(int damage, boolean armorPiercing) {
        this.damage = damage;
        this.armorPiercing = armorPiercing;
    }

    public Projectile(short damage, int id, int type, int summonerId) {
        this.damage = damage;
        this.summonerId = summonerId;
        this.containerType = id; // Store containerType for proc projectile scaling
        try {
            armorPiercing = IdToAsset.getIdProjectileArmorPierces(id, type);
        } catch (Exception e) {}
    }

    /**
     * Calculates the projectiles damage.
     *
     * @param rng          Seed used with randomizer to find the exact value of used weapon from min to max range of weapon dmg.
     * @param player       The player entity.
     * @param weaponId     Weapon ID used (retrieved from packet being sent).
     * @param projectileId Projectile ID used (retrieved from packet being sent).
     */
    public Projectile(RNG rng, Entity player, int weaponId, int projectileId) {
        if (player == null || rng == null) return;

        if (projectileId == -1) {
            projectileId = 0;
        }
        int min = 0;
        int max = 0;
        boolean ap = false;
        int slot = 0;
        try {
            min = IdToAsset.getIdProjectileMinDmg(weaponId, projectileId);
            max = IdToAsset.getIdProjectileMaxDmg(weaponId, projectileId);
            ap = IdToAsset.getIdProjectileArmorPierces(weaponId, projectileId);
            slot = IdToAsset.getIdProjectileSlotType(weaponId);
        } catch (ArrayIndexOutOfBoundsException e) {
            // For main weapons with enchantment issues, skip entirely
            slot = IdToAsset.getIdProjectileSlotType(weaponId);
            boolean mainWeapon = isMainWeapon(slot);
            if (mainWeapon) {
                // Skip this projectile for main weapons with enchantment issues
                return;
            }
            // System.err.println(
            //     "ArrayIndexOutOfBoundsException in IdToAsset for weaponId: " +
            //         weaponId +
            //         ", projectileId: " +
            //         projectileId
            // );
            return;
        }
        boolean mainWeapon = isMainWeapon(slot);
        int dmg;
        if (min != max) {
            long r = rng.next();
            dmg = (int) (min + (r % (max - min)));
        } else {
            dmg = min;
        }

        // Add stat modifier bonus for abilities with scaling
        // Note: For multi-shot abilities, each individual projectile gets the full stat bonus
        // Only apply scaling to ability projectiles (not regular weapons)
        boolean isAbilityProjectile = !mainWeapon; // Abilities are not main weapons
        if (isAbilityProjectile) {
            AbilityScalingManager scalingManager =
                AbilityScalingManager.getInstance();
            if (scalingManager.hasScaling(weaponId)) {
                int statBonus = scalingManager.calculateStatBonus(
                    weaponId,
                    player
                );
                System.out.println(
                    "Projectile scaling - weaponId: " +
                        weaponId +
                        ", baseDmg: " +
                        dmg +
                        ", statBonus: " +
                        statBonus +
                        ", total: " +
                        (dmg + statBonus) +
                        ", numShots: " +
                        (scalingManager.getScalingData(weaponId) != null
                            ? scalingManager.getScalingData(weaponId).numShots
                            : 1)
                );
                dmg += statBonus;
            } else {
                System.out.println(
                    "Projectile no scaling - weaponId: " +
                        weaponId +
                        ", baseDmg: " +
                        dmg +
                        " (ability but no scaling data)"
                );
            }
        } else {
            // System.out.println("Projectile no scaling - weaponId: " + weaponId +", baseDmg: " +
            // dmg +" (weapon, not ability)");
        }

        float f = 1f;
        if (mainWeapon) f = player.playerStatsMultiplier();
        damage = (int) (dmg * f);
        armorPiercing = ap;
    }

    private boolean isMainWeapon(int slot) {
        switch (slot) {
            case 1:
            case 2:
            case 3:
            case 8:
            case 17:
            case 24:
                return true;
        }
        return false;
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
    public static int damageWithDefense(
        int damage,
        boolean armorPiercing,
        int defence,
        int[] conditions
    ) {
        return damageWithDefense(
            damage,
            armorPiercing,
            defence,
            conditions,
            -1,
            null
        );
    }

    /**
     * Used when an entity takes damage taking defence and other effects into account for final damage to entity.
     *
     * @param damage        the base damage the bullet can do.
     * @param armorPiercing if the bullet ignores defence.
     * @param defence       defence of the entity being hit.
     * @param conditions    condition effects the entity being shot can have.
     * @param weaponId      the weapon ID (kept for compatibility but no longer used for defense calculations).
     * @return final damage applied to the entity.
     */
    public static int damageWithDefense(
        int damage,
        boolean armorPiercing,
        int defence,
        int[] conditions,
        int weaponId,
        Entity player
    ) {
        if (damage == 0) return 0;

        if (armorPiercing || (conditions[0] & 0x4000000) != 0) {
            defence = 0;
        } else if ((conditions[0] & 0x2000000) != 0) {
            defence = (int) (defence * 1.5);
        }
        if ((conditions[1] & 0x20000) != 0) {
            defence = defence - 20;
        }
        int minDmg = (damage * 2) / 20;
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

    public int getDamage() {
        return damage;
    }

    public boolean isArmorPiercing() {
        return armorPiercing;
    }

    public int getContainerType() {
        return containerType;
    }

    public int getSummonerId() {
        return summonerId;
    }

    /**
     * Returns the ability item id (inventory slot 1) associated with the originating ability for this projectile.
     * May be -1 if unknown/not set.
     */
    public int getOriginAbilityItem() {
        return originAbilityItem;
    }

    /**
     * Set the originating ability item id for this projectile.
     * Other code (e.g., TomatoData.serverPlayerShoot or the RNG-based constructor)
     * should call this when the originating item id is known.
     */
    public void setOriginAbilityItem(int itemId) {
        this.originAbilityItem = itemId;
    }

    /**
     * Returns the snapshot of the scaling stat value that should be used for scaling/defense-ignore calculations.
     * If this is Integer.MIN_VALUE, no snapshot is available and callers should fall back to current Entity stats.
     */
    public int getOriginScalingStat() {
        return originScalingStat;
    }

    /**
     * Set the snapshot of the scaling stat value (e.g., WIS/VIT) for this projectile.
     */
    public void setOriginScalingStat(int statValue) {
        this.originScalingStat = statValue;
    }

    public void clear() {
        damage = 0;
        armorPiercing = false;
        originAbilityItem = -1;
        originScalingStat = Integer.MIN_VALUE;
    }
}
