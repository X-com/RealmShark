package potato.model.data;

public enum HeroState {
    MARK_UNVISITED(0),
    MARK_VISITED(1),
    MARK_ACTIVE(2),
    MARK_DEAD(3);

    private final int index;

    HeroState(int i) {
        index = i;
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
}
