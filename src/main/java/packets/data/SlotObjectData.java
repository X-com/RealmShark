package packets.data;

import packets.reader.BufferReader;

public class SlotObjectData {
    /**
     * The object id of the entity which owns the slot
     */
    public int objectId;
    /**
     * The index of the slot - weapon=0, ability=1, armor=2, ring=3
     * and inventory going from 4 to 11, backpack from 12 to 19
     */
    public int slotId;
    /**
     * The item id of the item in the slot, or -1 if it is empty
     */
    public int objectType;

    /**
     * Deserializer method to extract data from the buffer.
     *
     * @param buffer Data that needs deserializing.
     * @return Returns this object after deserializing.
     */
    public SlotObjectData deserialize(BufferReader buffer) {
        objectId = buffer.readInt();
        slotId = buffer.readInt();
        objectType = buffer.readInt();

        return this;
    }

    @Override
    public String toString() {
        return "SlotObjectData{" +
                "\n   objectId=" + objectId +
                "\n   slotId=" + slotId +
                "\n   objectType=" + objectType;
    }
}
