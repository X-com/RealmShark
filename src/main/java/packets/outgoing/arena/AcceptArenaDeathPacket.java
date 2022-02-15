package packets.outgoing.arena;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Sent to accept a death in the arena.
 */
public class AcceptArenaDeathPacket extends Packet {
    @Override
    public void deserialize(BufferReader buffer) throws Exception {
    }
}
