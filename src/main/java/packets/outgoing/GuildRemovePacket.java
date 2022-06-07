package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Sent to remove a player from the client's current guild.
 */
public class GuildRemovePacket extends Packet {
    /**
     * The name of the player to remove.
     */
    public String name;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        name = buffer.readString();
    }

    @Override
    public String toString() {
        return "GuildRemovePacket{" +
                "\n   name=" + name;
    }
}
