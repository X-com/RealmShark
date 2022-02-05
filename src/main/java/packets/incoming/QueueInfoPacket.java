package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Received when the client connects to a server with a queue.
 */
public class QueueInfoPacket extends Packet {
    /**
     * The current position of the client in the queue
     */
    public int currentPosition;

    /**
     * The maximum amount of clients allowed in the queue
     */
    public int maxPosition;

    @Override
    public void deserialize(PBuffer buffer) {
        currentPosition = buffer.readUnsignedShort();
        maxPosition = buffer.readUnsignedShort();
    }
}
