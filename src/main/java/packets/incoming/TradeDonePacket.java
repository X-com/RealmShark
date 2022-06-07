package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;
import packets.data.enums.TradeResult;

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
    public void deserialize(BufferReader buffer) throws Exception {
        code = TradeResult.byOrdinal(buffer.readInt());
        description = buffer.readString();
    }

    @Override
    public String toString() {
        return "TradeDonePacket{" +
                "\n   code=" + code +
                "\n   description=" + description;
    }
}