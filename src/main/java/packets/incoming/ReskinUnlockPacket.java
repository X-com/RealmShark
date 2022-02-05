package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Received to notify the player that a new skin has been unlocked
 */
public class ReskinUnlockPacket extends Packet {
    /**
     * The id of the skin that was unlocked
     */
    public int skinId;

    @Override
    public void deserialize(PBuffer buffer) {
        skinId = buffer.readInt();
    }
}