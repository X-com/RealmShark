package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * > Unknown
 */
public class QuestRedeemResponsePacket extends Packet {
    /**
     * > Unknown
     */
    public boolean ok;
    /**
     * > Unknown
     */
    public String message;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        ok = buffer.readBoolean();
        message = buffer.readString();
    }

    @Override
    public String toString() {
        return "QuestRedeemResponsePacket{" +
                "\n   ok=" + ok +
                "\n   message=" + message;
    }
}