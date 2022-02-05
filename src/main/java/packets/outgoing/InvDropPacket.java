package packets.outgoing;

import packets.Packet;
import packets.buffer.PBuffer;
import packets.buffer.data.SlotObjectData;

/**
 * Sent to drop an item from the client's inventory.
 */
public class InvDropPacket extends Packet {
    /**
     * The slot to drop the item from.
     */
    public SlotObjectData slotObject;

    @Override
    public void deserialize(PBuffer buffer) {
        slotObject = new SlotObjectData().deserialize(buffer);
    }
}
