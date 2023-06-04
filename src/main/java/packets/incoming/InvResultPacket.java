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
     * Player time at the time of editing inventory
     */
    public int time;
    /**
     * Player positions at the time of editing inventory.
     */
    public WorldPosData playerWorldPos;
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
        playerWorldPos = new WorldPosData().deserialize(buffer);
        slotFrom = new SlotObjectData().deserialize(buffer);
        slotTo = new SlotObjectData().deserialize(buffer);
    }

    @Override
    public String toString() {
        return "InvResultPacket{" +
                "\n   time=" + time +
                "\n   playerWorldPos=" + playerWorldPos +
                "\n   slotFrom=" + slotFrom +
                "\n   slotTo=" + slotTo;
    }
}