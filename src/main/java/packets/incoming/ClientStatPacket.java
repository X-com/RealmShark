package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Received to give the player information about their stats.
 */
public class ClientStatPacket extends Packet {
    /**
     * The name of the stat.
     */
    public String name;
    /**
     * The value of the stat.
     */
    public int value;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        name = buffer.readString();
        value = buffer.readInt();
    }

    @Override
    public String toString() {
        return "ClientStatPacket{" +
                "\n name=" + name +
                "\n value=" + value;
    }
}