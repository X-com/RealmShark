package potato.data;

import java.awt.*;

public enum HeroState {
    MARK_UNVISITED(0, new Color(0, 255, 0, 100)),
    MARK_VISITED(1, new Color(0, 255, 0, 100)),
    MARK_ACTIVE(2, new Color(255, 0, 0, 100)),
    MARK_DEAD(3, new Color(255, 255, 255, 100));

    private final int index;
    private Color color;

    HeroState(int i, Color c) {
        index = i;
        color = c;
    }

    public static HeroState byOrdinal(int ord) {
        for (HeroState o : HeroState.values()) {
            if (o.index == ord) {
                return o;
            }
        }
        return null;
    }

    public int getIndex() {
        return index;
    }

    public Color getColor() {
        return color;
    }
}
