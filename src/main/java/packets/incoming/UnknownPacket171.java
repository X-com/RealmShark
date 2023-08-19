package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

import java.util.Arrays;

/**
 * Unknown packet -85 / 171
 */
public class UnknownPacket171 extends Packet {

    //[0, 0, 0, 19, -85, 1, 0, 0, 2, 53, 0, 0, 0, 5, 0, 0, 97, 86, 0]

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        System.out.println(Arrays.toString(buffer.readBytes(14)));
        Thread.dumpStack();
    }

    @Override
    public String toString() {
        return "UnknownPacket171{}";
    }
}
