package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Nothing is known about this packet
 */
public class UnknownPacket165 extends Packet {
    /**
     * Unknown byte
     */
    public int unknownByte;
    /**
     * Unknown int
     */
    public int unknownInt1;
    public int unknownInt2;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        unknownByte = buffer.readByte();
        unknownInt1 = buffer.readInt();
        unknownInt2 = buffer.readInt();
        Thread.dumpStack();
    }

    @Override
    public String toString() {
        return "UnknownPacket147{" +
                "\n   unknownByte=" + unknownByte +
                "\n   unknownInt1=" + unknownInt1 +
                "\n   unknownInt2=" + unknownInt2;
    }
}
