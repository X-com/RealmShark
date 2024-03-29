package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

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
    public int containerType;
    /**
     * The angle at which the projectile was fired.
     */
    public float angle;

    /**
     * Inspired buff increasing the range of the weapon.
     */
    public boolean inspiredBuff;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        bulletId = buffer.readUnsignedShort();
        ownerId = buffer.readInt();
        containerType = buffer.readInt();
        angle = buffer.readFloat();
        inspiredBuff = buffer.readBoolean();
    }

    @Override
    public String toString() {
        return "AllyShootPacket{" +
                "\n   bulletId=" + bulletId +
                "\n   ownerId=" + ownerId +
                "\n   containerType=" + containerType +
                "\n   angle=" + angle +
                "\n   inspiredBuff=" + inspiredBuff;
    }
}