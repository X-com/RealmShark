package packets.data;

import packets.reader.BufferReader;

import java.io.Serializable;

public class TradeItem implements Serializable {
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
    public TradeItem deserialize(BufferReader buffer) {
        item = buffer.readInt();
        slotType = buffer.readInt();
        tradeable = buffer.readBoolean();
        included = buffer.readBoolean();

        return this;
    }

    @Override
    public String toString() {
        return "TradeItem{" +
                "\n   item=" + item +
                "\n   slotType=" + slotType +
                "\n   tradeable=" + tradeable +
                "\n   included=" + included;
    }
}
