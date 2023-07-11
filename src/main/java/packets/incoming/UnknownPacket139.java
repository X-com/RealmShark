package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Unknown packet
 */
public class UnknownPacket139 extends Packet {
    /**
     * Unknown
     */
    int unknownInt1;
    int unknownInt2;
    /**
     * Unknown
     */
    byte unknownByte1;
    byte unknownByte2;
    byte unknownByte3;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        unknownByte1 = buffer.readByte();
        if (buffer.size() > 14)
            unknownByte2 = buffer.readByte();
        if (buffer.size() > 15)
            unknownByte3 = buffer.readByte();
        unknownInt1 = buffer.readInt();
        unknownInt2 = buffer.readInt();
    }

    @Override
    public String toString() {
        return "UnknownPacket139{" +
                "\n   unknownInt1=" + unknownInt1 +
                "\n   unknownInt2=" + unknownInt2 +
                "\n   unknownByte1=" + unknownByte1 +
                "\n   unknownByte2=" + unknownByte2 +
                "\n   unknownByte3=" + unknownByte3;
    }
}
