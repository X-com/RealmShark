package tomato.backend.data;

import java.io.Serializable;
import packets.data.StatData;
import packets.data.enums.StatType;
import tomato.realmshark.ParseEnchants;

/**
 * Class used to store damage and counter info.
 */
public class Damage implements Serializable {

    public Entity owner;
    public int ownerObjectType;
    public String ownerObjectName;
    public int[] ownerInvntory;
    public String[] ownerEnchants;
    // Generic snapshot of the relevant scaling stat captured at damage-creation time.
    // If ownerScalingStatType == null, no snapshot was captured. ownerScalingStatValue may be null
    // when the stat type is known but the specific stat value wasn't available.
    public StatType ownerScalingStatType;
    public Integer ownerScalingStatValue;
    public Projectile projectile;
    public long time;
    public int damage;
    public int counterDmg;
    public int counterHits;
    public boolean oryx3GuardDmg;
    public boolean chancellorDammahDmg;
    public boolean walledGardenReflectors;

    public Damage(Entity o) {
        owner = o;
        setInv(o);
        // Debug: log owner inventory & enchants for damage entries created without a projectile
        // if (owner != null) {
        //     System.out.println(
        //         "[Damage] created (no projectile) owner=" +
        //             (owner != null ? owner.id : -1) +
        //             " inv=" +
        //             (ownerInvntory != null
        //                 ? java.util.Arrays.toString(ownerInvntory)
        //                 : "null") +
        //             " enchants=" +
        //             (ownerEnchants != null
        //                 ? java.util.Arrays.toString(ownerEnchants)
        //                 : "null") +
        //             " damage=" +
        //             damage
        //     );
        // }
    }

    public Damage(Entity o, Projectile p, long t) {
        owner = o;
        projectile = p;
        time = t;
        damage = projectile.getDamage();
        setInv(o);

        // If we have scaling data for the projectile, capture a damage-time scaling stat snapshot
        try {
            if (o != null && p != null && p.getContainerType() != -1) {
                AbilityScalingManager asm = AbilityScalingManager.getInstance();
                AbilityScalingManager.AbilityScalingData sd =
                    asm.getScalingData(p.getContainerType());
                if (sd != null && sd.scalingStat != null) {
                    ownerScalingStatType = sd.scalingStat;
                    if (o.stat != null && o.stat.get(sd.scalingStat) != null) {
                        ownerScalingStatValue = Integer.valueOf(
                            o.stat.get(sd.scalingStat).statValue
                        );
                    } else {
                        ownerScalingStatValue = null;
                    }
                } else {
                    ownerScalingStatType = null;
                    ownerScalingStatValue = null;
                }
            } else {
                ownerScalingStatType = null;
                ownerScalingStatValue = null;
            }
        } catch (Exception ignored) {}

        // Debug: log owner inventory, enchants, projectile details and captured scaling snapshot when created from a projectile
        // if (owner != null) {
        //     System.out.println(
        //         "[Damage] created (projectile) owner=" +
        //             (owner != null ? owner.id : -1) +
        //             " projContainer=" +
        //             (projectile != null ? projectile.getContainerType() : -1) +
        //             " projDamage=" +
        //             (projectile != null ? projectile.getDamage() : damage) +
        //             " inv=" +
        //             (ownerInvntory != null
        //                 ? java.util.Arrays.toString(ownerInvntory)
        //                 : "null") +
        //             " enchants=" +
        //             (ownerEnchants != null
        //                 ? java.util.Arrays.toString(ownerEnchants)
        //                 : "null") +
        //             " scalingStatType=" +
        //             (ownerScalingStatType != null
        //                 ? ownerScalingStatType.name()
        //                 : "null") +
        //             " scalingStatValue=" +
        //             (ownerScalingStatValue != null
        //                 ? ownerScalingStatValue
        //                 : "null")
        //     );
        // }
    }

    public Damage(Entity o, Projectile p, long t, int dmg) {
        owner = o;
        projectile = p;
        time = t;
        damage = dmg;
        setInv(o);

        // If we have scaling data for the projectile, capture a damage-time scaling stat snapshot
        try {
            if (o != null && p != null && p.getContainerType() != -1) {
                AbilityScalingManager asm = AbilityScalingManager.getInstance();
                AbilityScalingManager.AbilityScalingData sd =
                    asm.getScalingData(p.getContainerType());
                if (sd != null && sd.scalingStat != null) {
                    ownerScalingStatType = sd.scalingStat;
                    if (o.stat != null && o.stat.get(sd.scalingStat) != null) {
                        ownerScalingStatValue = Integer.valueOf(
                            o.stat.get(sd.scalingStat).statValue
                        );
                    } else {
                        ownerScalingStatValue = null;
                    }
                } else {
                    ownerScalingStatType = null;
                    ownerScalingStatValue = null;
                }
            } else {
                ownerScalingStatType = null;
                ownerScalingStatValue = null;
            }
        } catch (Exception ignored) {}

        // Debug: explicit-damage constructor logging (including captured scaling snapshot)
        // if (owner != null) {
        //     System.out.println(
        //         "[Damage] created (explicit) owner=" +
        //             (owner != null ? owner.id : -1) +
        //             " projContainer=" +
        //             (projectile != null ? projectile.getContainerType() : -1) +
        //             " projDamage=" +
        //             (projectile != null ? projectile.getDamage() : damage) +
        //             " inv=" +
        //             (ownerInvntory != null
        //                 ? java.util.Arrays.toString(ownerInvntory)
        //                 : "null") +
        //             " enchants=" +
        //             (ownerEnchants != null
        //                 ? java.util.Arrays.toString(ownerEnchants)
        //                 : "null") +
        //             " damage=" +
        //             damage +
        //             " scalingStatType=" +
        //             (ownerScalingStatType != null
        //                 ? ownerScalingStatType.name()
        //                 : "null") +
        //             " scalingStatValue=" +
        //             (ownerScalingStatValue != null
        //                 ? ownerScalingStatValue
        //                 : "null")
        //     );
        // }
    }

    public Damage(Entity o, long t, int dmg) {
        owner = o;
        time = t;
        damage = dmg;
        // Debug: generic damage hit (no projectile) log
        // if (owner != null) {
        //     System.out.println(
        //         "[Damage] created (generic) owner=" +
        //             owner.id +
        //             " inv=" +
        //             (ownerInvntory != null
        //                 ? java.util.Arrays.toString(ownerInvntory)
        //                 : "null") +
        //             " abilityItem=" +
        //             ownerAbilityItem +
        //             " enchants=" +
        //             (ownerEnchants != null
        //                 ? java.util.Arrays.toString(ownerEnchants)
        //                 : "null") +
        //             " damage=" +
        //             damage
        //     );
        // }
    }

    private void setInv(Entity o) {
        if (o != null && o.stat != null) {
            ownerObjectType = o.objectType;
            ownerObjectName = o.name();

            // Check if entity has inventory stats (players have them, pets/minions don't)
            StatData inv0 = o.stat.get(StatType.INVENTORY_0_STAT);
            StatData inv1 = o.stat.get(StatType.INVENTORY_1_STAT);
            StatData inv2 = o.stat.get(StatType.INVENTORY_2_STAT);
            StatData inv3 = o.stat.get(StatType.INVENTORY_3_STAT);

            if (inv0 != null && inv1 != null && inv2 != null && inv3 != null) {
                ownerInvntory = new int[] {
                    inv0.statValue,
                    inv1.statValue,
                    inv2.statValue,
                    inv3.statValue,
                };
            } else {
                // Entity doesn't have inventory (pet, minion, etc.)
                ownerInvntory = new int[] { -1, -1, -1, -1 };
            }
            ownerEnchants = ParseEnchants.getEnchantStrings(o);

            // Generic: clear any scaling stat snapshot here. Specific constructors that know the projectile
            // will populate ownerScalingStatType and ownerScalingStatValue after this call.
            ownerScalingStatType = null;
            ownerScalingStatValue = null;
        } else {
            ownerObjectType = -1;
            ownerObjectName = null;
            ownerInvntory = null;
            ownerScalingStatType = null;
            ownerScalingStatValue = null;
        }
    }

    public int getDamage() {
        return damage;
    }

    /**
     * Returns a snapshot of the owner's stat (if available) for the given StatType.
     * This method uses the generic scaling stat snapshot captured at damage creation time
     * (ownerScalingStatType / ownerScalingStatValue). Returns null when no snapshot is available.
     */
    public Integer getOwnerStatSnapshot(StatType statType) {
        if (statType == null) return null;
        if (ownerScalingStatType == null) return null;
        return ownerScalingStatType == statType ? ownerScalingStatValue : null;
    }

    public void add(Damage d) {
        damage += d.damage;

        addCounters(d);
    }

    private void addCounters(Damage d) {
        if (
            d.oryx3GuardDmg || d.walledGardenReflectors || d.chancellorDammahDmg
        ) {
            counterDmg += d.damage;
            counterHits++;
            oryx3GuardDmg = d.oryx3GuardDmg;
            walledGardenReflectors = d.walledGardenReflectors;
            chancellorDammahDmg = d.chancellorDammahDmg;
        }
    }
}
