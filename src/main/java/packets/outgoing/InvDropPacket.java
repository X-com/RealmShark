package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;
import packets.data.SlotObjectData;

/**
 * Sent to drop an item from the client's inventory.
 */
public class InvDropPacket extends Packet {
    /**
     * The slot to drop the item from.
     */
    public SlotObjectData slotObject;
    /**
     * Unknown
     */
    public static boolean unknownBoolean;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        slotObject = new SlotObjectData().deserialize(buffer);
        unknownBoolean = buffer.readBoolean();
    }

    public String toString() {
        return String.format("SlotObject:%s Unknown:%b", slotObject, unknownBoolean);
    }
}
