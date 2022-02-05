package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;
import packets.buffer.data.SlotObjectData;

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
    public void deserialize(PBuffer buffer) {
        this.success = buffer.readBoolean();

        byte resultCount = buffer.readByte();
        results = new SlotObjectData[resultCount];
        for (int i = 0; i < resultCount; i++) {
            results[i] = new SlotObjectData().deserialize(buffer);
        }
    }
}
