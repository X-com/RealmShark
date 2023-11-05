package tomato.gui.mydmg;

public class Bullet {
    int id;
    float rof = -1;
    int numProj;

    int min;
    int max;

    @Override
    public String toString() {
        return "Bullet{" +
                "\n      id=" + id +
                "\n      min=" + min +
                "\n      max=" + max +
                "\n      rof=" + rof +
                "\n      numProj=" + numProj;
    }
}
