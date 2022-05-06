package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Unknown packet
 */
public class UnknownPacket138 extends Packet {
    /**
     * The current client time.
     */
    public int time;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        time = buffer.readInt();
    }
}
