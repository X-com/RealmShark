package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Sent to invite a player to the client's current guild.
 */
public class GuildInvitePacket extends Packet {
    /**
     * The name of the player to invite.
     */
    public String name;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        name = buffer.readString();
    }

    @Override
    public String toString() {
        return "GuildInvitePacket{" +
                "\n   name=" + name;
    }
}
