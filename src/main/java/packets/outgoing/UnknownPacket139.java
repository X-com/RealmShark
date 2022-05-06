package packets.outgoing;

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
        unknownByte2 = buffer.readByte();
        if (buffer.size() > 15)
            unknownByte3 = buffer.readByte();
        unknownInt1 = buffer.readInt();
        unknownInt2 = buffer.readInt();
    }
}
