package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Packet sent when using emotes
 */
public class EmotePacket extends Packet {
    /**
     * Unknown
     */
    private int emoteId;
    private int emoteTime;
    private byte unknownByte;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        emoteId = buffer.readInt();
        emoteTime = buffer.readInt();
        unknownByte = buffer.readByte();
    }

    @Override
    public String toString() {
        return "EmotePacket{" +
                "\n   emoteId=" + emoteId +
                "\n   serverTime=" + emoteTime +
                "\n   unknownByte=" + unknownByte;
    }
}
