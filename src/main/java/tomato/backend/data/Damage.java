package tomato.backend.data;

/**
 * Class used to store damage and counter info.
 */
public class Damage implements Comparable {
    public Entity owner;
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
    }

    public Damage(Entity o, Projectile p, long t) {
        owner = o;
        projectile = p;
        time = t;
        damage = projectile.getDamage();
    }

    public Damage(Entity o, Projectile p, long t, int dmg) {
        owner = o;
        projectile = p;
        time = t;
        damage = dmg;
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
