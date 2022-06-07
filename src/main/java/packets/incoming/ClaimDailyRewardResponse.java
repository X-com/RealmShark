package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Received in response to a `ClaimDailyRewardMessage`.
 */
public class ClaimDailyRewardResponse extends Packet {
    /**
     * The item id of the reward received.
     */
    public int itemId;
    /**
     * The int of items received.
     */
    public int quantity;
    /**
     * Unknown.
     */
    public int gold;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        itemId = buffer.readInt();
        quantity = buffer.readInt();
        gold = buffer.readInt();
    }

    @Override
    public String toString() {
        return "ClaimDailyRewardResponse{" +
                "\n   itemId=" + itemId +
                "\n   quantity=" + quantity +
                "\n   gold=" + gold;
    }
}