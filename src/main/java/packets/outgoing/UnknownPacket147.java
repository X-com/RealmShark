package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Nothing is known about this packet
 */
public class UnknownPacket147 extends Packet {
    /**
     * Unknown byte
     */
    public int unknownByte;
    /**
     * Unknown int
     */
    public int unknownInt1;
    public int unknownInt2;
    public int unknownInt3;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        unknownByte = buffer.getIndex();
        unknownInt1 = buffer.getIndex();
        unknownInt2 = buffer.getIndex();
        unknownInt3 = buffer.getIndex();
    }
}
