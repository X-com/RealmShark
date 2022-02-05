package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Received when the server sends the client a chat token to use (unnused)
 */
public class ChatToken extends Packet {
    /**
     * The chat token for the current client
     */
    public String token;
    /**
     * The host address of the chat server
     */
    public String host;
    /**
     * The port of the chat server
     */
    public int port;

    @Override
    public void deserialize(PBuffer buffer) {
        token = buffer.readString();
        host = buffer.readString();
        port = buffer.readInt();
    }
}