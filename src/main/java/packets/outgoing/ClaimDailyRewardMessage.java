package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

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
    public void deserialize(BufferReader buffer) throws Exception {
        claimKey = buffer.readString();
        claimType = buffer.readString();
    }
}
