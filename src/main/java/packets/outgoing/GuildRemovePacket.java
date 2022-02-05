package packets.outgoing;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Sent to remove a player from the client's current guild.
 */
public class GuildRemovePacket extends Packet {
    /**
     * The name of the player to remove.
     */
    public String name;

    @Override
    public void deserialize(PBuffer buffer) {
        name = buffer.readString();
    }
}
