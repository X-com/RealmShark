package packets.outgoing.arena;

import packets.Packet;
import packets.reader.BufferReader;
import packets.data.SlotObjectData;

/**
 * > Unknown.
 */
public class QuestRedeemPacket extends Packet {
    /**
     * > Unknown.
     */
    public String questId;
    /**
     * > Unknown.
     */
    public SlotObjectData[] slots;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        questId = buffer.readString();
        slots = new SlotObjectData[buffer.readShort()];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = new SlotObjectData().deserialize(buffer);
        }
    }
}
