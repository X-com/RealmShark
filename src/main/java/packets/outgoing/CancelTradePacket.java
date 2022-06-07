package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Sent to cancel the current active trade.
 */
public class CancelTradePacket extends Packet {
    @Override
    public void deserialize(BufferReader buffer) throws Exception {
    }

    @Override
    public String toString() {
        return "CancelTradePacket{}";
    }
}
