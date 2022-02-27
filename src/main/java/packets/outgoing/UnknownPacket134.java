package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Unknown packet that rarely is used.
 */
public class UnknownPacket134 extends Packet {
    /**
     * Unknown
     */
    int unknownInt;
    /**
     * Unknown
     */
    byte unknownByte;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        unknownInt = buffer.readInt();
        unknownByte = buffer.readByte();
    }
}
