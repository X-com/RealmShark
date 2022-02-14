package packets.outgoing;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Sent to invite a player to the client's current guild.
 */
public class GuildInvitePacket extends Packet {
    /**
     * The name of the player to invite.
     */
    public String name;

    @Override
    public void deserialize(PBuffer buffer) throws Exception {
        name = buffer.readString();
    }
}
