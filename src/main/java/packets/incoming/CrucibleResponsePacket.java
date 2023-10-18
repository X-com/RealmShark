package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

import java.util.Arrays;

/**
 * Crucible Json String packet.
 */
public class CrucibleResponsePacket extends Packet {

    public byte[] unknownBytes;
    public String crucibleJsonString1;
    public String crucibleJsonString2;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        unknownBytes = buffer.readBytes(12);
        crucibleJsonString1 = buffer.readString();
        crucibleJsonString2 = buffer.readString();
    }

    @Override
    public String toString() {
        return "CruciblePacket{" +
                "\n   unknownBytes=" + Arrays.toString(unknownBytes) +
                "\n   crucibleJsonString1=" + crucibleJsonString1 +
                "\n   crucibleJsonString2=" + crucibleJsonString2;
    }
}
