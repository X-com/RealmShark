package packets.outgoing;

import packets.Packet;
import packets.buffer.PBuffer;
import data.SlotObjectData;

/**
 * Sent to make an update to the pet currently following the player
 */
public class ReskinPetPacket extends Packet {
    /**
     * The instance id of the pet to update
     */
    public int instanceId;
    /**
     * The pet type that the pet will become after the form change
     */
    public int newPetType;
    /**
     * The slot object of a pet stone if one is used
     */
    public SlotObjectData item;

    @Override
    public void deserialize(PBuffer buffer) throws Exception {
        instanceId = buffer.readInt();
        newPetType = buffer.readInt();
        item = new SlotObjectData().deserialize(buffer);
    }

}