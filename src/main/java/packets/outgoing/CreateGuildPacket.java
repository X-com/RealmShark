package packets.outgoing;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Sent to create a new guild.
 */
public class CreateGuildPacket extends Packet {
    /**
     * The name of the guild being created.
     */
    public String name;

    @Override
    public void deserialize(PBuffer buffer) {
        name = buffer.readString();
    }
}
