package tomato.backend.data;

/**
 * Class used to store damage and counter info.
 */
public class Damage {
    public Entity owner;
    public int[] ownerInv;
    public Projectile projectile;
    public long time;
    public int damage;
    public int counterDmg;
    public int counterHits;
    public boolean oryx3GuardDmg;
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
        if (o != null && o.stat != null) {
            ownerInv = new int[]{o.stat.INVENTORY_0_STAT.statValue, o.stat.INVENTORY_1_STAT.statValue, o.stat.INVENTORY_2_STAT.statValue, o.stat.INVENTORY_3_STAT.statValue};
        } else {
            ownerInv = null;
        }
    }

    public int getDamage() {
        return damage;
    }

    public void add(Damage d) {
        damage += d.damage;

        addCounters(d);
    }

    private void addCounters(Damage d) {
        if (d.oryx3GuardDmg || d.walledGardenReflectors || d.chancellorDammahDmg) {
            counterDmg += d.damage;
            counterHits++;
            oryx3GuardDmg = d.oryx3GuardDmg;
            walledGardenReflectors = d.walledGardenReflectors;
            chancellorDammahDmg = d.chancellorDammahDmg;
        }
    }
}
