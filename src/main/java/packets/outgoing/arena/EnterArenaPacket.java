package packets.outgoing.arena;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Sent to enter the arena.
 */
public class EnterArenaPacket extends Packet {
    /**
     * > Unknown.
     */
    public int currency;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        currency = buffer.readInt();
    }
}
