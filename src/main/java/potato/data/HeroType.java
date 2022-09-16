package potato.data;


public enum HeroType {
    UNVISITED(0, 16, new int[0]),
    DEMON(1, 100, new int[]{IdData.DEMON}),
    PHENIX(2, 16, new int[]{IdData.PHENIX}),
    CYCLOPS(3, 16, new int[]{IdData.CYCLOPS}),
    GHOST(4, 16, new int[]{IdData.GHOST_INVON, IdData.GHOST_KILLABLE}),
    OASIS(5, 16, new int[]{IdData.OASIS_GIANT}),
    ENT(6, 16, new int[]{IdData.ENT_SMALL, IdData.ENT_BIG}),
    LICH(7, 16, new int[]{IdData.LICH, IdData.LICH_KILLABLE}),
    PARASITE(8, 16, new int[0]),
    MANOR(9, 16, new int[0]),
    SNAKE(10, 16, new int[0]),
    GRAVE(11, 16, new int[0]),
    HOUSE(12, 16, new int[0]);

    /*
     *   Red Demon
     *   Phoenix Lord
     *   Cyclops God
     *   Ghost King
     *   Oasis Giant
     *   Ent Ancient
     *   Lich
     */

    private int index;
    private int[] typeID;
    private float missingDist;

    HeroType(int i, float d, int[] t) {
        this.index = i;
        this.typeID = t;
        this.missingDist = d;
    }

    public static HeroType byOrdinal(int ord) {
        for (HeroType o : HeroType.values()) {
            if (o.index == ord) {
                return o;
            }
        }
        return null;
    }

    public boolean typeMatch(int objectType) {
        for (int i : typeID) {
            if (i == objectType) return true;
        }
        return false;
    }

    public float missing() {
        return missingDist;
    }

    public int getIndex() {
        return index;
    }
}
