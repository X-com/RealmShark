package packets.outgoing;

import packets.Packet;
import packets.data.enums.UseItemType;
import packets.reader.BufferReader;
import packets.data.SlotObjectData;
import packets.data.WorldPosData;

/**
 * Sent when the player uses an item such as an ability or consumable.
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
     * The position where the item was used.
     */
    public WorldPosData useItemPosition;
    /**
     * The type of item usage. See the `UseItemType` enum class for details.
     */
    public UseItemType useItemType;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        time = buffer.readInt();
        slotObject = new SlotObjectData().deserialize(buffer);
        useItemPosition = new WorldPosData().deserialize(buffer);
        useItemType = UseItemType.fromCode(buffer.readByte());
    }
}
