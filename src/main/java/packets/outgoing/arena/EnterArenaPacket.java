package packets.outgoing.arena;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Sent to enter the arena.
 */
public class EnterArenaPacket extends Packet {
    /**
     * > Unknown.
     */
    public int currency;

    @Override
    public void deserialize(PBuffer buffer) throws Exception {
        currency = buffer.readInt();
    }
}
