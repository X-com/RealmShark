package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Nothing is known about this packet
 */
public class UnknownPacket145 extends Packet {
    /**
     * Unknown int
     */
    public int unknownInt;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        unknownInt = buffer.getIndex();
    }
}
