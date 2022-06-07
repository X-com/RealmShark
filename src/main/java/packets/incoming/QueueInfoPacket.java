package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

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
    public void deserialize(BufferReader buffer) throws Exception {
        currentPosition = buffer.readUnsignedShort();
        maxPosition = buffer.readUnsignedShort();
    }

    @Override
    public String toString() {
        return "QueueInfoPacket{" +
                "\n   currentPosition=" + currentPosition +
                "\n   maxPosition=" + maxPosition;
    }
}