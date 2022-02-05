package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;
import packets.buffer.data.CompressedInt;

/**
 * Received when the player enters the nexus
 */
public class ForgeUnlockedBlueprints extends Packet {
    /**
     * The itemIds of unlocked blueprints in an array
     */
    public int[] unlockedBlueprints;

    @Override
    public void deserialize(PBuffer buffer) {
        int count = buffer.readByte();
        unlockedBlueprints = new int[count];
        for (int i = 0; i < count; i++) {
            unlockedBlueprints[i] = new CompressedInt().deserialize(buffer);
        }
    }
}
