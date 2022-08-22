package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Unknown packet
 */
public class DashAckPacket extends Packet {
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
        return "DashAckPacket{" +
                "\n   time=" + time;
    }
}
