package packets.outgoing;

import packets.Packet;
import packets.buffer.PBuffer;

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
    public void deserialize(PBuffer buffer) {
        accountId = buffer.readString();
        token = buffer.readString();
    }

}