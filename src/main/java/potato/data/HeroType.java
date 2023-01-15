package potato.data;


public enum HeroType {
    UNVISITED(0, "a", 16, new int[0]),
    DEMON(1, "h", 100, new int[]{IdData.DEMON}),
    PHENIX(2, "i", 16, new int[]{IdData.PHENIX}),
    CYCLOPS(3, "j", 16, new int[]{IdData.CYCLOPS}),
    GHOST(4, "k", 16, new int[]{IdData.GHOST_INVON, IdData.GHOST_KILLABLE}),
    OASIS(5, "l", 16, new int[]{IdData.OASIS_GIANT}),
    ENT(6, "m", 16, new int[]{IdData.ENT_SMALL, IdData.ENT_BIG}),
    LICH(7, "n", 16, new int[]{IdData.LICH, IdData.LICH_KILLABLE}),
    PARASITE(8, "o", 16, new int[0]),
    MANOR(9, "p", 16, new int[0]),
    SNAKE(10, "q", 16, new int[0]),
    GRAVE(11, "r", 16, new int[0]),
    HOUSE(12, "s", 16, new int[0]);

    /*
     *   Red Demon
     *   Phoenix Lord
     *   Cyclops God
     *   Ghost King
     *   Oasis Giant
     *   Ent Ancient
     *   Lich
     */

    private final int index;
    private final String shape;
    private final int[] typeID;
    private final float missingDist;

    HeroType(int i, String s, float d, int[] t) {
        this.index = i;
        this.shape = s;
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

    public String getShape() {
        return shape;
    }
}
