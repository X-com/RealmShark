package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Unknown packet -87 / 169
 */
public class UnknownPacket169 extends Packet {
    /**
     * Unknown
     */
    public int unknownInt;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        unknownInt = buffer.readInt();
    }

    @Override
    public String toString() {
        return "UnknownPacket169{" +
                "\n   unknownInt=" + unknownInt;
    }
}