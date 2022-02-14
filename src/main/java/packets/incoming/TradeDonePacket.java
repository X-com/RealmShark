package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;
import data.enums.TradeResult;

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
    public void deserialize(PBuffer buffer) throws Exception {
        code = TradeResult.byOrdinal(buffer.readInt());
        description = buffer.readString();
    }
}