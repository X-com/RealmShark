package tomato.backend.data;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class used to display player Equipment
 */
public class Equipment implements Serializable {
    public int id;
    public int count;
    public int dmg;
    public String enchant;
    public AtomicInteger totalDmg;

    public Equipment(int id, String enchant, AtomicInteger tot) {
        this.id = id;
        this.enchant = enchant;
        count = 0;
        totalDmg = tot;
    }

    public void add(int damage) {
        this.dmg += damage;
        count++;
        totalDmg.set(totalDmg.get() + damage);
    }
}
