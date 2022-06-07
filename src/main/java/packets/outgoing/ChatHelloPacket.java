package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Sent to initiate the chat stream (unnused)
 */
public class ChatHelloPacket extends Packet {
    /**
     * The clients account ID
     */
    public String accountId;
    /**
     * The chat initiation token
     */
    public String token;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        accountId = buffer.readString();
        token = buffer.readString();
    }

    @Override
    public String toString() {
        return "ChatHelloPacket{" +
                "\n   accountId=" + accountId +
                "\n   token=" + token;
    }
}