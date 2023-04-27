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
    public byte unknownByte1;
    /**
     * Unknown
     */
    public byte unknownByte2;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        slotObject = new SlotObjectData().deserialize(buffer);
        unknownByte1 = buffer.readByte();
        unknownByte2 = buffer.readByte();
    }

    @Override
    public String toString() {
        return "InvDropPacket{" +
                "\n   slotObject=" + slotObject +
                "\n   unknownByte1=" + unknownByte1 +
                "\n   unknownByte2=" + unknownByte2;
    }
}
