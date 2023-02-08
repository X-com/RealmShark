package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

import java.util.Arrays;

/**
 * Received when the player enters the nexus
 */
public class ForgeUnlockedBlueprints extends Packet {
    /**
     * Unknown Byte
     */
    public byte unknownByte;
    /**
     * The itemIds of unlocked blueprints in an array
     */
    public int[] unlockedBlueprints;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        unknownByte = buffer.readByte();
        unlockedBlueprints = new int[buffer.readCompressedInt()];
        for (int i = 0; i < unlockedBlueprints.length; i++) {
            unlockedBlueprints[i] = buffer.readCompressedInt();
        }
    }

    @Override
    public String toString() {
        return "ForgeUnlockedBlueprints{" +
                "\n   unknownByte=" + unknownByte +
                "\n   unlockedBlueprints=" + Arrays.toString(unlockedBlueprints);
    }
}