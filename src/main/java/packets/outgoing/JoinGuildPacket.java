package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Sent to accept a pending guild invite.
 */
public class JoinGuildPacket extends Packet {
    /**
     * The name of the guild for which there is a pending invite.
     */
    public String guildName;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        guildName = buffer.readString();
    }

    @Override
    public String toString() {
        return "JoinGuildPacket{" +
                "\n   guildName=" + guildName;
    }
}
