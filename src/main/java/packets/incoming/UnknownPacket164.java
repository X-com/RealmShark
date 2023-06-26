package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Packet related to battle pass missions.
 */
public class UnknownPacket164 extends Packet {
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
        unknownByte1 = buffer.readByte();
        unknownByte2 = buffer.readByte();
        unknownShort = buffer.readShort();
    }

    @Override
    public String toString() {
        return "UnknownPacket164{" +
                "\n   unknownByte1=" + unknownByte1 +
                "\n   unknownByte2=" + unknownByte2 +
                "\n   unknownShort=" + unknownShort;
    }
}
