package data;

import packets.buffer.PBuffer;

public class TradeItem {
    /**
     * The item id
     */
    public int item;
    /**
     * The slot type the item is stored in
     */
    public int slotType;
    /**
     * Whether or not the item is tradeable
     */
    public boolean tradeable;
    /**
     * Whether or not the item is included in an active trade
     */
    public boolean included;

    /**
     * Deserializer method to extract data from the buffer.
     *
     * @param buffer Data that needs deserializing.
     * @return Returns this object after deserializing.
     */
    public TradeItem deserialize(PBuffer buffer) {
        item = buffer.readInt();
        slotType = buffer.readInt();
        tradeable = buffer.readBoolean();
        included = buffer.readBoolean();

        return this;
    }
}
