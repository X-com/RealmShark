package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Sent to acknowledge an `EnemyShootPacket`.
 */
public class ShootAckPacket extends Packet {
    /**
     * The current client time.
     */
    public int time;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        time = buffer.readInt();
    }

    @Override
    public String toString() {
        return "ShootAckPacket{" +
                "\n   time=" + time;
    }
}
