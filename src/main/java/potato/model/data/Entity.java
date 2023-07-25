package potato.model.data;

import packets.data.WorldPosData;

public class Entity {
    public float x;
    public float y;
    public String shape;
    public float size;

    public Entity(float x, float y, String s, float size) {
        this.x = x;
        this.y = y;
        shape = s;
        this.size = size;
    }

    public void move(WorldPosData pos) {
        this.x = pos.x;
        this.y = pos.y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
