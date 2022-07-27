package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;
import packets.data.WorldPosData;

/**
 * Sent when the player shoots a projectile.
 */
public class PlayerShootPacket extends Packet {
    /**
     * The current client time.
     */
    public int time;
     /**
     * Counts the number of bullets sense entering dungeon.
     */
    public short bulletId;
    /**
     * The item id of the weapon used to fire the projectile.
     */
    public int containerType;
    /**
     * ID of the projectile for weapons with multiple types of projectiles. (i.e. bows)
     * Warning: some projectileIDs can be -1 and should be treated as 0!
     */
    public byte projectileId;
    /**
     * The position of the starting point where the projectile was fired.
     */
    public WorldPosData startingPos;
    /**
     * The angle at which the projectile was fired.
     */
    public float angle;
    /**
     * If the projectile is related to a burst weapon projectile.
     */
    public boolean isBurst;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        time = buffer.readInt();
        bulletId = buffer.readShort();
        containerType = buffer.readUnsignedShort();
        projectileId = buffer.readByte();
        startingPos = new WorldPosData().deserialize(buffer);
        angle = buffer.readFloat();
        isBurst = buffer.readBoolean();
    }

    @Override
    public String toString() {
        return "PlayerShootPacket{" +
                "\n   time=" + time +
                "\n   bulletID=" + bulletId +
                "\n   containerType=" + containerType +
                "\n   projectileId=" + projectileId +
                "\n   startingPos=" + startingPos +
                "\n   angle=" + angle +
                "\n   isBurst=" + isBurst;
    }
}
