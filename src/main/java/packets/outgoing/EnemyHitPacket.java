package packets.outgoing;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Sent when an enemy has been hit by the player.
 */
public class EnemyHitPacket extends Packet {
    /**
     * The current client time.
     */
    public int time;
    /**
     * The id of the bullet which hit the enemy.
     */
    public int bulletId;
    /**
     * The object id of the enemy which was hit.
     */
    public int targetId;
    /**
     * Whether or not the projectile will kill the enemy.
     */
    public boolean kill;

    @Override
    public void deserialize(PBuffer buffer) throws Exception {
        time = buffer.readInt();
        bulletId = buffer.readUnsignedByte();
        targetId = buffer.readInt();
        kill = buffer.readBoolean();
    }
}
