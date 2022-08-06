package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Packet sent after a successful attempt to redeem a quest.
 */
public class QuestRedeemResponsePacket extends Packet {
    /**
     * If the quest was successfully accepted.
     */
    public boolean ok;
    /**
     * Message used in the response dialog.
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