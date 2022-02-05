package packets.buffer.data.enums;

public enum TradeResult {
    Successful(0),
    PlayerCancelled(1);

    private final int index;

    TradeResult(int i) {
        index = i;
    }

    public int get() {
        return index;
    }

    public static TradeResult byOrdinal(int ord) {
        for (TradeResult o : TradeResult.values()) {
            if (o.index == ord) {
                return o;
            }
        }
        return null;
    }
}
