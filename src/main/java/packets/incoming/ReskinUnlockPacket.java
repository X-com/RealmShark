package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Received to notify the player that a new object has been unlocked
 */
public class ReskinUnlockPacket extends Packet {
    /**
     * The type of unlocked object
     */
    public int unlockType;
    /**
     * The id of object that was unlocked
     */
    public int unlockId;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        unlockType = buffer.readByte();
        unlockId = buffer.readInt();
    }

    @Override
    public String toString() {
        return "ReskinUnlockPacket{" +
                "\n   unlockType=" + unlockType +
                "\n   unlockId=" + unlockId;
    }
}