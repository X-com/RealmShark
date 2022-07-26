package assembly;

public class GHPointer {
    final private long staticPointer;
    final private int[] offsets;

    public GHPointer(long staticPointer, int... offsets) {
        this.staticPointer = staticPointer;
        this.offsets = offsets;
    }

    public long getStaticPointer() {
        return staticPointer;
    }

    public int[] getOffsets() {
        return offsets;
    }
}