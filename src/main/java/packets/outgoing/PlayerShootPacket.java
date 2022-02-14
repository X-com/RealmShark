package packets.outgoing;

import packets.Packet;
import packets.buffer.PBuffer;
import data.WorldPosData;

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
    public byte unknownByte;
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
     * The speed multiplier for the projectile.
     */
    public int speedMult;
    /**
     * The lifetime MS multiplier for the projectile.
     */
    public int lifeMult;
    /**
     * If the projectile is related to a burst weapon projectile.
     */
    public boolean isBurst;

    @Override
    public void deserialize(PBuffer buffer) throws Exception {
        time = buffer.readInt();
        bulletId = buffer.readByte();
        unknownByte = buffer.readByte();
        containerType = buffer.readShort();
        startingPos = new WorldPosData().deserialize(buffer);
        angle = buffer.readFloat();
        isBurst = buffer.readBoolean();
    }
}
