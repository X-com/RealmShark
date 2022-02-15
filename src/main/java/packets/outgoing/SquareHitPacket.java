package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * > Unknown.
 */
public class SquareHitPacket extends Packet {
    /**
     * The current client time.
     */
    public int time;
    /**
     * > Unknown.
     */
    public byte bulletId;
    /**
     * > Unknown.
     */
    public int objectId;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        time = buffer.readInt();
        bulletId = buffer.readByte();
        objectId = buffer.readInt();
    }
}
