package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * > Unknown.
 */
public class InvitedToGuildPacket extends Packet {
    /**
     * > Unknown.
     */
    public int result;

    @Override
    public void deserialize(PBuffer buffer) {
        result = buffer.readInt();
    }
}
