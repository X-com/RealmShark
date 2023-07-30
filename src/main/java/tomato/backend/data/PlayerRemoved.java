package tomato.backend.data;

public class PlayerRemoved {
    public int dropId;
    public int hp;
    public int max;
    public String name;
    public long time;

    public PlayerRemoved(int dropId, int hp, int max, String name, long time) {
        this.dropId = dropId;
        this.hp = hp;
        this.max = max;
        this.name = name;
        this.time = time;
    }
}
