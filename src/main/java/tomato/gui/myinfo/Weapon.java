package tomato.gui.myinfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Weapon {

    int id;
    String name;
    String displayName = null;
    String labels;
    HashMap<Integer, Projectile> projectiles = new HashMap();
    ArrayList<Bullet> bullets = new ArrayList<>();

    String imgFile;
    int imgIndex;
    float rof = -1;
    public int numProj;

    public void fix() {
        if (rof != -1 && bullets.isEmpty()) {
            Bullet b = new Bullet();
            b.id = 0;
            if (numProj < 1) numProj = 1;
            b.numProj = numProj;
            b.rof = rof;
            bullets.add(b);
        } else if (rof != -1) {
            for (Bullet b : bullets) {
                if (b.rof == -1) {
                    b.rof = this.rof;
                }
            }
        } else if (rof == -1 && bullets.isEmpty() && !projectiles.isEmpty()) {
            // Populate one bullet per projectile when no Subattack/RateOfFire is defined
            for (Projectile p : projectiles.values()) {
                Bullet b = new Bullet();
                b.id = p.id;
                b.numProj = 1;
                b.rof = 1f; // default to 1 shot per second-equivalent for display purposes
                bullets.add(b);
            }
        }

        for (Bullet b : bullets) {
            Projectile p = projectiles.get(b.id);
            b.min = p.min;
            b.max = p.max;
        }
    }

    @Override
    public String toString() {
        return (
            "Weapon{" +
            "\n   id=" +
            id +
            "\n   name=" +
            name +
            "\n   labels=" +
            labels +
            //                "\n   projectiles=" + Arrays.toString(projectiles.values().toArray()) +
            "\n   bullets=" +
            Arrays.toString(bullets.toArray()) +
            "\n   imgFile=" +
            imgFile +
            "\n   imgIndex=" +
            imgIndex
        );
    }
}
