package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Received when a trade is requested.
 */
public class TradeRequestedPacket extends Packet {
    /**
     * The name of the player who requested the trade.
     */
    public String name;

    @Override
    public void deserialize(PBuffer buffer) {
        name = buffer.readString();
    }
}
