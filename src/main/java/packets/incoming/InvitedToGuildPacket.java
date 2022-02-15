package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Received when the player is invited to a guild.
 */
public class InvitedToGuildPacket extends Packet {
    /**
     * The name of the player who sent the invite.
     */
    public String name;
    /**
     * The name of the guild which the invite is for.
     */
    public String guildName;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        name = buffer.readString();
        guildName = buffer.readString();
    }
}