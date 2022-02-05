package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;
import packets.buffer.data.StatType;

/**
 * Received when the active trade has completed, regardless of whether
 * it was accepted or cancelled
 */
public class TradeDonePacket extends Packet {
    /**
     * The result of the trade
     */
    public TradeResult code;
    /**
     * > Unknown
     */
    public String description;

    @Override
    public void deserialize(PBuffer buffer) {
        code = TradeResult.byOrdinal(buffer.readInt());
        description = buffer.readString();
    }

    public enum TradeResult {
        Successful(0),
        PlayerCancelled(1);

        private int index;

        TradeResult(int i) {
            index = i;
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
}
