package packets.incoming;

import packets.data.SlotObjectData;
import packets.Packet;
import packets.reader.BufferReader;

/**
 * > Unknown.
 */
public class InvResultPacket extends Packet {
    /**
     * > Unknown.
     */
    public boolean unknownBool;
    /**
     * > Unknown.
     */
    public byte unknownByte;
    /**
     * The slot the item in the inventory being transferred from.
     */
    public SlotObjectData slotFrom;
    /**
     * The slot the item in the inventory being transferred to.
     */
    public SlotObjectData slotTo;
    /**
     * > Unknown.
     */
    public int unknownInt1;
    /**
     * > Unknown.
     */
    public int unknownInt2;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        unknownBool = buffer.readBoolean();
        unknownByte = buffer.readByte();
        slotFrom = new SlotObjectData().deserialize(buffer);
        slotTo = new SlotObjectData().deserialize(buffer);
        unknownInt1 = buffer.readInt();
        unknownInt2 = buffer.readInt();
    }

    @Override
    public String toString() {
        return "InvResultPacket{" +
                "\n   unknownBool=" + unknownBool +
                "\n   unknownByte=" + unknownByte +
                "\n   slotFrom=" + slotFrom +
                "\n   slotTo=" + slotTo +
                "\n   unknownInt1=" + unknownInt1 +
                "\n   unknownInt2=" + unknownInt2;
    }
}