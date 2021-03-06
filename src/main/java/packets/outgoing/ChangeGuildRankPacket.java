package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Sent to change the guild rank of a member in the player's guild.
 */
public class ChangeGuildRankPacket extends Packet {
    /**
     * The name of the player whose rank will change.
     */
    public String name;
    /**
     * The new rank of the player.
     */
    public int guildRank;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        name = buffer.readString();
        guildRank = buffer.readInt();
    }

    @Override
    public String toString() {
        return "ChangeGuildRankPacket{" +
                "\n   name=" + name +
                "\n   guildRank=" + guildRank;
    }
}
