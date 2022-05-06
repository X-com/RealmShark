package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Unknown packet
 */
public class UnknownPacket137 extends Packet {
    /**
     * The current client time.
     */
    public int time;
    /**
     * unknown ints
     */
    public int unknownInt1;
    public int unknownInt2;
    public int unknownInt3;
    public int unknownInt4;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        time = buffer.readInt();
        unknownInt1 = buffer.readInt();
        unknownInt2 = buffer.readInt();
        unknownInt3 = buffer.readInt();
        unknownInt4 = buffer.readInt();
    }
}
