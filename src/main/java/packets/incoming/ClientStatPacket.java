package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;

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
    public void deserialize(PBuffer buffer) {
        name = buffer.readString();
        value = buffer.readInt();
    }
}