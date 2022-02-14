package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;
import data.WorldPosData;

/**
 * Received when a visible enemy shoots a projectile.
 */
public class EnemyShootPacket extends Packet {
    /**
     * The id of the bullet which was fired.
     */
    public short bulletId;
    /**
     * The object id of the enemy which fired the projectile.
     */
    public int ownerId;
    /**
     * The local identifier of the projectile.
     *
     * @see `ProjectileInfo.id`
     */
    public int bulletType;
    /**
     * The position at which the projectile was fired.
     */
    public WorldPosData startingPos;
    /**
     * The angle at which the projectile was fired.
     */
    public float angle;
    /**
     * The damage which the projectile will cause.
     */
    public short damage;
    /**
     * The int of projeciles fired.
     */
    public int numShots;
    /**
     * The angle in degrees between the projectiles if `numShots > 1`.
     */
    public float angleInc;

    @Override
    public void deserialize(PBuffer buffer) throws Exception {
        bulletId = buffer.readShort();
        ownerId = buffer.readInt();
        bulletType = buffer.readUnsignedByte();
        startingPos = new WorldPosData().deserialize(buffer);
        angle = buffer.readFloat();
        damage = buffer.readShort();
        if (buffer.getIndex() < buffer.size()) {
            numShots = buffer.readUnsignedByte();
            angleInc = buffer.readFloat();
        } else {
            numShots = 1;
            angleInc = 0;
        }
    }
}