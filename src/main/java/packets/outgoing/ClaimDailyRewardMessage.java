package packets.outgoing;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Sent to claim rewards from the login calendar.
 */
public class ClaimDailyRewardMessage extends Packet {
    /**
     * The key of the item being claimed.
     */
    public String claimKey;
    /**
     * The type of claim being made.
     */
    public String claimType;

    @Override
    public void deserialize(PBuffer buffer) {
        claimKey = buffer.readString();
        claimType = buffer.readString();
    }
}
