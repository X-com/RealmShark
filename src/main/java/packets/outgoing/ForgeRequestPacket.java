package packets.outgoing;

import packets.Packet;
import packets.buffer.PBuffer;
import data.SlotObjectData;

/**
 * Forge packet sent when forging.
 */
public class ForgeRequestPacket extends Packet {
    /**
     * The object id of the item to forge.
     */
    public int objectId;
    /**
     * The items to dismantle.
     */
    public SlotObjectData slotsUsed;

    @Override
    public void deserialize(PBuffer buffer) throws Exception {
        objectId = buffer.readInt();
        slotsUsed = new SlotObjectData().deserialize(buffer);
    }
}
