package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Received when the player enters the nexus
 */
public class ForgeUnlockedBlueprints extends Packet {
    /**
     * The itemIds of unlocked blueprints in an array
     */
    public int[] unlockedBlueprints;

    // TODO: currently bugged, fix this
    @Override
    public void deserialize(PBuffer buffer) throws Exception {
        unlockedBlueprints = new int[buffer.readCompressedInt()];
        for (int i = 0; i < unlockedBlueprints.length; i++) {
            unlockedBlueprints[i] = buffer.readCompressedInt();
        }
    }
}