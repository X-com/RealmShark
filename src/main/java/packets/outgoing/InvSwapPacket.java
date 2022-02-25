package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;
import packets.data.SlotObjectData;
import packets.data.WorldPosData;

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
    public SlotObjectData slotFrom;
    /**
     * The slot to swap to.
     */
    public SlotObjectData slotTo;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        time = buffer.readInt();
        position = new WorldPosData().deserialize(buffer);
        slotFrom = new SlotObjectData().deserialize(buffer);
        slotTo  = new SlotObjectData().deserialize(buffer);
    }
}
