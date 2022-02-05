package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;
import packets.buffer.data.TradeItem;

/**
 * Received when a new active trade has been initiated
 */
public class TradeStartPacket extends Packet {
    /**
     * A description of the player's inventory. Items 0-3 are the hotbar items,
     * and 4-12 are the 8 inventory slots
     */
    public TradeItem[] clientItems;
    /**
     * The trade partner's name.
     */
    public String partnerName;
    /**
     * A description of the trade partner's inventory. Items 0-3 are the
     * hotbar items, and 4-12 are the 8 inventory slots
     */
    public TradeItem[] partnerItems;

    @Override
    public void deserialize(PBuffer buffer) {
        clientItems = new TradeItem[buffer.readShort()];
        for (int i = 0; i < clientItems.length; i++) {
            clientItems[i] = new TradeItem().deserialize(buffer);
        }
        partnerName = buffer.readString();
        partnerItems = new TradeItem[buffer.readShort()];
        for (int i = 0; i < partnerItems.length; i++) {
            partnerItems[i] = new TradeItem().deserialize(buffer);
        }
    }
}