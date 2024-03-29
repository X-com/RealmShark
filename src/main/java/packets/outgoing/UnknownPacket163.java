package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Packet related to battle pass missions.
 */
public class UnknownPacket163 extends Packet {
    /**
     * Unknown
     */
    int unknownInt;
    /**
     * Unknown
     */
    byte unknownByte1;
    /**
     * Unknown
     */
    byte unknownByte2;
    /**
     * Unknown
     */
    short unknownShort;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        unknownInt = buffer.readInt();
        unknownByte1 = buffer.readByte();
        unknownByte2 = buffer.readByte();
        unknownShort = buffer.readShort();
    }

    @Override
    public String toString() {
        return "UnknownPacket163{" +
                "\n   unknownInt=" + unknownInt +
                "\n   unknownByte1=" + unknownByte1 +
                "\n   unknownByte2=" + unknownByte2 +
                "\n   unknownShort=" + unknownShort;
    }
}
