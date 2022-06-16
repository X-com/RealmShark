package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Sent when an object or other player has been hit by an enemy projectile.
 */
public class OtherHitPacket extends Packet {
    /**
     * The current client time.
     */
    public int time;
    /**
     * The id of the bullet which hit the object.
     */
    public short bulletId;
    /**
     * The object id of player who fired the projectile which hit the object.
     */
    public int objectId;
    /**
     * The object id of the object which was hit.
     */
    public int targetId;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        time = buffer.readInt();
        bulletId = buffer.readShort();
        objectId = buffer.readInt();
        targetId = buffer.readInt();
    }

    @Override
    public String toString() {
        return "OtherHitPacket{" +
                "\n   time=" + time +
                "\n   bulletId=" + bulletId +
                "\n   objectId=" + objectId +
                "\n   targetId=" + targetId;
    }
}
