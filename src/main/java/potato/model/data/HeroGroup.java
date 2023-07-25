package potato.model.data;

import java.awt.*;

public enum HeroGroup {
    GROUP_DEMON(1, "a", new Color(255, 0, 0)),
    GROUP_CYCLOPS(2, "b", new Color(255, 128, 0)),
    GROUP_GHOST_PARASITE_MANOR(4, "c", new Color(255, 0, 255)),
    GROUP_PHENIX_OASIS(8, "d", new Color(255, 255, 0)),
    GROUP_ENT_SNAKE(16, "e", new Color(0, 255, 0)),
    GROUP_LICH_GRAVE(32, "f", new Color(0, 128, 255)),
    GROUP_HOUSE(64, "g", new Color(165, 42, 42)),
    ;

    private int index;
    private String shape;
    private Color color;

    HeroGroup(int i, String s, Color c) {
        index = i;
        shape = s;
        color = c;
    }

    public static Color getColor(int i) {
        for (HeroGroup g : HeroGroup.values()) {
            if (i == g.index) {
                return g.color;
            }
        }
        return new Color(255, 255, 255);
    }

    public static String getShapeString(int i) {
        for (HeroGroup g : HeroGroup.values()) {
            if (i == g.index) {
                return g.shape;
            }
        }
        return "e";
    }
}
