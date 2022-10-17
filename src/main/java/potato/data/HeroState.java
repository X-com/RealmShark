package potato.data;

import java.awt.*;

public enum HeroState {
    MARK_UNVISITED(0, Color.green),
    MARK_VISITED(1, Color.green),
    MARK_ACTIVE(2, Color.red),
    MARK_DEAD(3, Color.white);

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
