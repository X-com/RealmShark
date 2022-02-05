package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;

import java.security.PublicKey;

/**
 * Received to instruct the client to connect to a new host
 */
public class ReconnectPacket extends Packet {
    /**
     * The name of the new host.
     */
    public String name;
    /**
     * The address of the new host
     */
    public String host;
    /**
     * The port of the new host
     */
    public int port;
    /**
     * The `gameId` to send in the next `HelloPacket`
     */
    public int gameId;
    /**
     * The `keyTime` to send in the next `HelloPacket`
     */
    public int keyTime;
    /**
     * Whether the new host is from the arena
     */
    public boolean isFromArena;
    /**
     * The `key` to send in the next `HelloPacket`
     */
    public byte[] key;

    @Override
    public void deserialize(PBuffer buffer) {
        name = buffer.readString();
        host = buffer.readString();
        port = buffer.readInt();
        gameId = buffer.readInt();
        keyTime = buffer.readInt();
        isFromArena = buffer.readBoolean();
        key = buffer.readByteArray();
    }
}
