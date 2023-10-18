package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

import java.util.Arrays;

/**
 * Sent to request Crucible json info.
 * Unknown packet -74 / 182
 */
public class CrucibleRequestPacket extends Packet {

    /**
     * Unknown
     */
    byte[] unknownBytes;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        unknownBytes = buffer.readBytes(10);
    }

    @Override
    public String toString() {
        return "UnknownPacket182{" +
                "\n   unknownBytes=" + Arrays.toString(unknownBytes);
    }
}
