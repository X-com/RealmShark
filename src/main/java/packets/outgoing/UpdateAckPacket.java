package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Sent to acknowledge an `UpdatePacket`.
 */
public class UpdateAckPacket extends Packet {
    @Override
    public void deserialize(BufferReader buffer) throws Exception {
    }

    @Override
    public String toString() {
        return "UpdateAckPacket{}";
    }
}
