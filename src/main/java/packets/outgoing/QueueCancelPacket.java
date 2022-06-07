package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Sent when the clients position in the queue should be cancelled
 */
public class QueueCancelPacket extends Packet {
    @Override
    public void deserialize(BufferReader buffer) throws Exception {
    }

    @Override
    public String toString() {
        return "QueueCancelPacket{}";
    }
}