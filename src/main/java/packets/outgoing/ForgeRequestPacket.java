package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;
import packets.data.SlotObjectData;

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
    public void deserialize(BufferReader buffer) throws Exception {
        objectId = buffer.readInt();
        slotsUsed = new SlotObjectData().deserialize(buffer);
    }

    @Override
    public String toString() {
        return "ForgeRequestPacket{" +
                "\n   objectId=" + objectId +
                "\n   slotsUsed=" + slotsUsed;
    }
}
