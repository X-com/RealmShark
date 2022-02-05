package packets.outgoing;

import packets.Packet;
import packets.buffer.PBuffer;

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
    public int bulletId;
    /**
     * The object id of player who fired the projectile which hit the object.
     */
    public int objectId;
    /**
     * The object id of the object which was hit.
     */
    public int targetId;

    @Override
    public void deserialize(PBuffer buffer) {
        time = buffer.readInt();
        bulletId = buffer.readUnsignedByte();
        objectId = buffer.readInt();
        targetId = buffer.readInt();
    }
}
