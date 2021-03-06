package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Received to notify the player that a new skin has been unlocked
 */
public class ReskinUnlockPacket extends Packet {
    /**
     * The id of the skin that was unlocked
     */
    public int skinId;
    /**
     * The id of the pet skin that was unlocked
     */
    public int isPetSkin;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        skinId = buffer.readInt();
        isPetSkin = buffer.readInt();
    }

    @Override
    public String toString() {
        return "ReskinUnlockPacket{" +
                "\n   skinId=" + skinId +
                "\n   isPetSkin=" + isPetSkin;
    }
}