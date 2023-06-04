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
        slotFrom = new SlotObjectData().deserialize(buffer);
        slotTo = new SlotObjectData().deserialize(buffer);
    }

    @Override
    public String toString() {
        return "InvSwapPacket{" +
                "\n   time=" + time +
                "\n   slotFrom=" + slotFrom +
                "\n   slotTo=" + slotTo;
    }
}
