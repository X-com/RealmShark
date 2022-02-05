package packets.outgoing;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Sent to acknowledge an `UpdatePacket`.
 */
public class UpdateAckPacket extends Packet {
    @Override
    public void deserialize(PBuffer buffer) {
    }
}
