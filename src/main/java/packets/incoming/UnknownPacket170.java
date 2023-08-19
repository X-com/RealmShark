package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

import java.util.Arrays;

/**
 * Unknown packet -86 / 170
 */
public class UnknownPacket170 extends Packet {
    /**
     * Unknown byte
     */
    public byte unknownByte;
    /**
     * Unknown ints
     */
    public int[] unknownInts;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        Thread.dumpStack();
        unknownByte = buffer.readByte();
        short size = buffer.readShort();
        unknownInts = new int[size];
        for (int i = 0; i < size; i++) {
            unknownInts[i] = buffer.readInt();
        }
    }

    @Override
    public String toString() {
        return "UnknownPacket170{" +
                "\n   unknownInts=" + Arrays.toString(unknownInts);
    }
}
