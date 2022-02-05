package packets.incoming.arena;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Received when the player has been killed in the arena.
 */
public class ArenaDeathPacket extends Packet {
    /**
     * The cost in gold to be revived.
     */
    public int cost;

    @Override
    public void deserialize(PBuffer buffer) {
        cost = buffer.readInt();
    }
}