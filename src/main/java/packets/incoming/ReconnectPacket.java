package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

import java.util.Arrays;

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
     * Unknown short
     */
    public int unknownUnsignedShort;
    /**
     * The port of the new host
     */
    public int port;
    /**
     * The `gameId` to send in the next `HelloPacket`
     */
    public int gameId;
    /**
     * The `key` to send in the next `HelloPacket`
     */
    public byte[] key;
//    /**
//     * The `keyTime` to send in the next `HelloPacket`
//     */
//    public int keyTime;
//    /**
//     * Whether or not the new host is from the arena
//     */
//    public boolean isFromArena;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        name = buffer.readString();
        host = buffer.readString();
        unknownUnsignedShort = buffer.readUnsignedShort();
        port = buffer.readInt();
        gameId = buffer.readInt();
        key = buffer.readByteArray();
    }

    @Override
    public String toString() {
        return "ReconnectPacket{" +
                "\n   name='" + name + '\'' +
                "\n   host='" + host + '\'' +
                "\n   unknownUnsignedShort=" + unknownUnsignedShort +
                "\n   port=" + port +
                "\n   gameId=" + gameId +
                "\n   key=" + Arrays.toString(key);
    }
}