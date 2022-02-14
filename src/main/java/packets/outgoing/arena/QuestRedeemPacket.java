package packets.outgoing.arena;

import packets.Packet;
import packets.buffer.PBuffer;
import data.SlotObjectData;

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
    public void deserialize(PBuffer buffer) throws Exception {
        questId = buffer.readString();
        slots = new SlotObjectData[buffer.readShort()];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = new SlotObjectData().deserialize(buffer);
        }
    }
}
