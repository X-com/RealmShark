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
     * The id of the bullet which was fired.
     */
    public byte bulletId;
    /**
     * (work-in-progress)
     */
    public short unknownShort;
    /**
     * The item id of the weapon used to fire the projectile.
     */
    public short containerType;
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
        bulletId = buffer.readByte();
        unknownShort = buffer.readShort();
        containerType = buffer.readShort();
        startingPos = new WorldPosData().deserialize(buffer);
        angle = buffer.readFloat();
        isBurst = buffer.readBoolean();
    }

    @Override
    public String toString() {
        return "PlayerShootPacket{" +
                "\n  time=" + time +
                "\n, bulletId=" + bulletId +
                "\n, unknownByte=" + unknownShort +
                "\n, containerType=" + containerType +
                "\n, startingPos=" + startingPos +
                "\n, angle=" + angle +
                "\n, isBurst=" + isBurst +
                "\n}";
    }
}
