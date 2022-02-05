package packets.buffer.data;

import packets.buffer.PBuffer;

public class SlotObjectData {
    /**
     * The object id of the entity which owns the slot
     */
    public int objectId;
    /**
     * The index of the slot - e.g. the 4th inventory slot has the slot id 3
     */
    public int slotId;
    /**
     * The item id of the item in the slot, or -1 if it is empty
     */
    public long objectType;

    /**
     * Deserializer method to extract data from the buffer.
     *
     * @param buffer Data that needs deserializing.
     * @return Returns this object after deserializing.
     */
    public SlotObjectData deserialize(PBuffer buffer) {
        objectId = buffer.readInt();
        slotId = buffer.readInt();
        objectType = buffer.readUnsignedInt();

        return this;
    }
}
