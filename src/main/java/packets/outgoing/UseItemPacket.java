package packets.outgoing;

import packets.Packet;
import packets.buffer.PBuffer;
import data.SlotObjectData;
import data.WorldPosData;

/**
 * Sent to use an item, such as an ability or consumable.
 */
public class UseItemPacket extends Packet {
    /**
     * The current client time.
     */
    public int time;
    /**
     * The slot of the item being used.
     */
    public SlotObjectData slotObject;
    /**
     * The position at which the item was used.
     */
    public WorldPosData itemUsePos;
    /**
     * The type of item usage.
     */
    public byte useType;

    @Override
    public void deserialize(PBuffer buffer) throws Exception {
        time = buffer.readInt();
        slotObject = new SlotObjectData().deserialize(buffer);
        itemUsePos = new WorldPosData().deserialize(buffer);
        useType = buffer.readByte();
    }
}
