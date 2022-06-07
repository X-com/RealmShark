package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Nothing is known about this packet
 */
public class UnknownPacket146 extends Packet {
    /**
     * Unknown int
     */
    public int unknownInt1;
    public int unknownInt2;
    public int unknownInt3;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        unknownInt1 = buffer.getIndex();
        unknownInt2 = buffer.getIndex();
        unknownInt3 = buffer.getIndex();
    }

    @Override
    public String toString() {
        return "UnknownPacket146{" +
                "\n   unknownInt1=" + unknownInt1 +
                "\n   unknownInt2=" + unknownInt2 +
                "\n   unknownInt3=" + unknownInt3;
    }
}
