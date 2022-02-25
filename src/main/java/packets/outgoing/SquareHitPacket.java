package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Type of packet sent when some type of hostiles fire.
 */
public class SquareHitPacket extends Packet {
    /**
     * The current client time.
     */
    public int time;
    /**
     * The id of the bullet which hit the object.
     */
    public short bulletId;
    /**
     * The id of the object shooting.
     */
    public int objectId;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        time = buffer.readInt();
        bulletId = buffer.readShort();
        objectId = buffer.readInt();
    }

    public String toString() {
        return String.format("Time:%d BulletId:%d ObjectId:%d", time, bulletId, objectId);
    }
}
