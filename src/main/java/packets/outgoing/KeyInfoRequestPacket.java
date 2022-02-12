package packets.outgoing;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * > Unknown.
 */
public class KeyInfoRequestPacket extends Packet {
    /**
     * > Unknown.
     */
    public int itemType;

    @Override
    public void deserialize(PBuffer buffer) throws Exception {
        itemType = buffer.readInt();
    }
}
