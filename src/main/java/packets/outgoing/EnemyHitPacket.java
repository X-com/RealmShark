package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

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
    public short bulletId;
    /**
     * The object id of the enemy which was hit.
     */
    public int targetId;
    /**
     * ID of the shooter hitting the target.
     */
    public int shooterID;
    /**
     * Whether the projectile will kill the enemy.
     */
    public boolean kill;
    /**
     * Id of the main player hitting the target.
     */
    public int mainID;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        time = buffer.readInt();
        bulletId = buffer.readShort();
        shooterID = buffer.readInt();
        targetId = buffer.readInt();
        kill = buffer.readBoolean();
        mainID = buffer.readInt();
    }

    @Override
    public String toString() {
        return "EnemyHitPacket{" +
                "\n   time=" + time +
                "\n   bulletId=" + bulletId +
                "\n   shooterID=" + shooterID +
                "\n   targetId=" + targetId +
                "\n   kill=" + kill +
                "\n   mainID=" + mainID;
    }
}
