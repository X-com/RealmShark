package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;
import packets.data.SlotObjectData;
import packets.data.WorldPosData;

import java.util.Arrays;

/**
 * Sent to swap the items of two slots.
 */
public class InvSwapPacket extends Packet {
    /**
     * unknown
     */
    public byte unknownByte1;
    /**
     * unknown
     */
    public byte unknownByte2;
    /**
     * The slot to swap from.
     */
    public SlotObjectData slotFrom;
    /**
     * The slot to swap to.
     */
    public SlotObjectData slotTo;
    /**
     * Unknown bits
     */
    byte[] bitArray;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        unknownByte1 = buffer.readByte();
        unknownByte2 = buffer.readByte();
        slotFrom = new SlotObjectData().deserialize(buffer);
        slotTo = new SlotObjectData().deserialize(buffer);
        bitArray = buffer.readBytes(8);
    }

    @Override
    public String toString() {
        return "InvSwapPacket{" +
                "\n   unknownByte1=" + unknownByte1 +
                "\n   unknownByte2=" + unknownByte2 +
                "\n   slotFrom=" + slotFrom +
                "\n   slotTo=" + slotTo +
                "\n   slotTo=" + Arrays.toString(bitArray);
    }
}
