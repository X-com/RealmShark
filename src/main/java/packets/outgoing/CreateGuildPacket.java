package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Sent to create a new guild.
 */
public class CreateGuildPacket extends Packet {
    /**
     * The name of the guild being created.
     */
    public String name;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        name = buffer.readString();
    }
}
