package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Sent to tell the server if you would like to receive ally (other player) projectiles.
 */
public class ChangeAllyShootPacket extends Packet {
    /**
     * Whether the server will send ally projectiles.
     * 0 = disable, 1 = enable.
     */
    public int toggle;
    /**
     * Unknown
     */
    public short unknownShort;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        toggle = buffer.readInt();
        unknownShort = buffer.readShort();
    }

    public String toString() {
        return String.format("%d %d", toggle, unknownShort);
    }
}
