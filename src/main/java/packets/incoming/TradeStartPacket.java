package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;
import packets.data.TradeItem;

/**
 * Received when a new active trade has been initiated
 */
public class TradeStartPacket extends Packet {
    /**
     * A description of the player's inventory. Items 0-3 are the hotbar items,
     * and 4-19 are the 8 inventory slots and 8 backpack slots
     */
    public TradeItem[] clientItems;
    /**
     * The trade partner's name.
     */
    public String partnerName;
    /**
     * A description of the trade partner's inventory. Items 0-3 are the
     * hotbar items, and 4-19 are the 8 inventory slots and 8 backpack slots
     */
    public TradeItem[] partnerItems;
    /**
     * Unknown int
     */
    public int unknownInt;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        clientItems = new TradeItem[buffer.readShort()];
        for (int i = 0; i < clientItems.length; i++) {
            clientItems[i] = new TradeItem().deserialize(buffer);
        }
        partnerName = buffer.readString();
        partnerItems = new TradeItem[buffer.readShort()];
        for (int i = 0; i < partnerItems.length; i++) {
            partnerItems[i] = new TradeItem().deserialize(buffer);
        }
        unknownInt = buffer.readInt();
    }
}