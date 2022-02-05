package packets.outgoing;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Sent to acknowledge an `EnemyShootPacket`.
 */
public class ShootAckPacket extends Packet {
    /**
     * The current client time.
     */
    public int time;

    @Override
    public void deserialize(PBuffer buffer) {
        time = buffer.readInt();
    }
}
