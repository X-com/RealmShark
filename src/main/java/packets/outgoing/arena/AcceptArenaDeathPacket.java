package packets.outgoing.arena;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Sent to accept a death in the arena.
 */
public class AcceptArenaDeathPacket extends Packet {
    @Override
    public void deserialize(PBuffer buffer) throws Exception {
    }
}
