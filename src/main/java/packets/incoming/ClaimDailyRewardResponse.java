package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Received in response to a `ClaimDailyRewardMessage`.
 */
public class ClaimDailyRewardResponse extends Packet {
    /**
     * The item id of the reward received.
     */
    public int itemId;
    /**
     * The number of items received.
     */
    public int quantity;
    /**
     * Unknown.
     */
    public int gold;

    @Override
    public void deserialize(PBuffer buffer) {
        itemId = buffer.readInt();
        quantity = buffer.readInt();
        gold = buffer.readInt();
    }
}
