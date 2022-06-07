package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Sent to buy an item.
 */
public class BuyPacket extends Packet {
    /**
     * The object id of the item being purchased.
     */
    public int objectId;
    /**
     * The number of items being purchased.
     */
    public int quantity;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        objectId = buffer.readInt();
        quantity = buffer.readInt();
    }

    @Override
    public String toString() {
        return "BuyPacket{" +
                "\n   objectId=" + objectId +
                "\n   quantity=" + quantity;
    }
}
