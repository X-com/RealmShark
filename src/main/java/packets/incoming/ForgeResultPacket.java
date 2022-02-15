package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;
import packets.data.SlotObjectData;

/**
 * Received when the player uses the item forge
 */
public class ForgeResultPacket extends Packet {
    /**
     * Whether the forge was successful
     */
    public boolean success;
    /**
     * The SlotObjectData of the items forged
     */
    public SlotObjectData[] results;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        success = buffer.readBoolean();

        results = new SlotObjectData[buffer.readByte()];
        for (int i = 0; i < results.length; i++) {
            results[i] = new SlotObjectData().deserialize(buffer);
        }
    }
}