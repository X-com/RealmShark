package packets.outgoing;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Sent to accept a pending guild invite.
 */
public class JoinGuildPacket extends Packet {
    /**
     * The name of the guild for which there is a pending invite.
     */
    public String guildName;

    @Override
    public void deserialize(PBuffer buffer) {
        guildName = buffer.readString();
    }
}
