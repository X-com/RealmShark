package packets.outgoing;

import packets.Packet;
import packets.buffer.PBuffer;
import packets.buffer.data.SlotObjectData;
import packets.buffer.data.WorldPosData;

/**
 * Sent to swap the items of two slots.
 */
public class InvSwapPacket extends Packet {
    /**
     * The current client time.
     */
    public int time;
    /**
     * The current client position.
     */
    public WorldPosData position;
    /**
     * The slot to swap from.
     */
    public SlotObjectData slotObject1;
    /**
     * The slot to swap to.
     */
    public SlotObjectData slotObject2;

    @Override
    public void deserialize(PBuffer buffer) {
        time = buffer.readInt();
        position = new WorldPosData().deserialize(buffer);
        slotObject1 = new SlotObjectData().deserialize(buffer);
        slotObject2 = new SlotObjectData().deserialize(buffer);
    }
}
