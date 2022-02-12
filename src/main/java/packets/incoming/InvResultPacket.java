package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * > Unknown.
 */
public class InvResultPacket extends Packet {
    /**
     * > Unknown.
     */
    public int result;

    @Override
    public void deserialize(PBuffer buffer) throws Exception {
        result = buffer.readInt();
    }
}