package packets.incoming;

import packets.data.SlotObjectData;
import packets.Packet;
import packets.data.WorldPosData;
import packets.reader.BufferReader;

/**
 * > Unknown.
 */
public class InvResultPacket extends Packet {
    /**
     * > Unknown.
     */
    public byte unknownByte1;
    /**
     * > Unknown.
     */
    public byte unknownByte2;
    /**
     * Player time at the time of editing inventory
     */
    public int time;
    /**
     * Player positions at the time of editing inventory.
     */
    public WorldPosData pos;
    /**
     * The slot the item in the inventory being transferred from.
     */
    public SlotObjectData slotFrom;
    /**
     * The slot the item in the inventory being transferred to.
     */
    public SlotObjectData slotTo;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        time = buffer.readInt();
        pos = new WorldPosData().deserialize(buffer);
        slotFrom = new SlotObjectData().deserialize(buffer);
        unknownByte1 = buffer.readByte();
        slotTo = new SlotObjectData().deserialize(buffer);
        unknownByte2 = buffer.readByte();
    }

    @Override
    public String toString() {
        return "InvResultPacket{" +
                "\n   unknownByte1=" + unknownByte1 +
                "\n   unknownByte2=" + unknownByte2 +
                "\n   unknownInt=" + time +
                "\n   unknownPos=" + pos +
                "\n   slotFrom=" + slotFrom +
                "\n   slotTo=" + slotTo;
    }
}