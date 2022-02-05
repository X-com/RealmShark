package packets.outgoing;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Sent to cancel the current active trade.
 */
public class CancelTradePacket extends Packet {
    @Override
    public void deserialize(PBuffer buffer) {
    }
}
