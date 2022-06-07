package packets.incoming.arena;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Received when the player has been killed in the arena.
 */
public class ArenaDeathPacket extends Packet {
    /**
     * The cost in gold to be revived.
     */
    public int cost;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        cost = buffer.readInt();
    }

    @Override
    public String toString() {
        return "ArenaDeathPacket{" +
                "\n   cost=" + cost;
    }
}