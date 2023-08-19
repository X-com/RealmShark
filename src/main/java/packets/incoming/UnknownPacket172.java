package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

import java.util.Arrays;

/**
 * Unknown packet -84 / 172
 */
public class UnknownPacket172 extends Packet {

    //[0, 0, 0, 11, -84, 0, 1, 0, 0, -58, -117]

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        System.out.println(Arrays.toString(buffer.readBytes(6)));
        Thread.dumpStack();
    }

    @Override
    public String toString() {
        return "UnknownPacket172{}";
    }
}
