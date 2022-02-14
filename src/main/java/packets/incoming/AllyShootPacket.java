package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Received when another player shoots a projectile.
 */
public class AllyShootPacket extends Packet {
    /**
     * The bullet id of the projectile which was produced.
     */
    public int bulletId;
    /**
     * The object id of the player who fired the projectile.
     */
    public int ownerId;
    /**
     * The item id of the weapon used to fire the projectile.
     */
    public short containerType;
    /**
     * The angle at which the projectile was fired.
     */
    public float angle;

    /**
     * Whether or not the shot is affected by the 'Inspired' buff (presumably).
     */
    public boolean bard;

    @Override
    public void deserialize(PBuffer buffer) throws Exception {
        bulletId = buffer.readUnsignedByte();
        ownerId = buffer.readInt();
        containerType = buffer.readShort();
        angle = buffer.readFloat();
        bard = buffer.readBoolean();
    }
}