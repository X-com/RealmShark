package tomato.backend.data;

import javax.swing.plaf.synth.SynthUI;

/**
 * Class used to store damage and counter info.
 */
public class Damage implements Comparable {
    public Entity owner;
    public int[] ownerInv;
    public Projectile projectile;
    public long time;
    public int damage;
    public int counterDmg;
    public int counterHits;
    public boolean chancellorDammahDmg;
    public boolean walledGardenReflectors;
    public static boolean dammahCountered;

    public Damage(Entity o) {
        owner = o;
        setInv(o);
    }

    public Damage(Entity o, Projectile p, long t) {
        owner = o;
        projectile = p;
        time = t;
        damage = projectile.getDamage();
        setInv(o);
    }

    public Damage(Entity o, Projectile p, long t, int dmg) {
        owner = o;
        projectile = p;
        time = t;
        damage = dmg;
        setInv(o);
    }

    private void setInv(Entity o) {
        try {
            if (o != null) {
                ownerInv = new int[]{o.stat.INVENTORY_0_STAT.statValue, o.stat.INVENTORY_1_STAT.statValue, o.stat.INVENTORY_2_STAT.statValue, o.stat.INVENTORY_3_STAT.statValue};
            } else {
                ownerInv = null;
            }
        } catch (Exception e) {
            System.out.println(o);
            System.out.println(o.stat);
            System.out.println(o.stat.INVENTORY_0_STAT);
            System.out.println(o.stat.INVENTORY_0_STAT.statValue);
        }
    }

    @Override
    public int compareTo(Object o) {
        return ((Damage) o).damage - damage;
    }

    public int getDamage() {
        return damage;
    }

    public void add(Damage d) {
        damage += d.damage;
    }
}
