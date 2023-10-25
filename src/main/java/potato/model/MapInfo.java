package potato.model;

public class MapInfo {
    public long seed;
    public int[] locations;

    public MapInfo() {
        this.seed = -1;
        locations = new int[160];
    }
}
