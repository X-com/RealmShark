package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * > Unknown.
 */
public class KeyInfoRequestPacket extends Packet {
    /**
     * > Unknown.
     */
    public int itemType;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        itemType = buffer.readInt();
    }
}
