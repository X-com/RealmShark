package packets.outgoing;

import packets.Packet;
import packets.data.QuestData;
import packets.reader.BufferReader;
import packets.data.SlotObjectData;

/**
 * Packet sent when redeeming a quest.
 */
public class QuestRedeemPacket extends Packet {
    /**
     * Unknown string
     */
    String unknownString;
    /**
     * > Unknown int
     */
    public int questId;
    /**
     * > Unknown slots
     */
    public SlotObjectData[] slots;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        unknownString = buffer.readString();
        questId = buffer.readInt();
        slots = new SlotObjectData[buffer.readShort()];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = new SlotObjectData().deserialize(buffer);
        }
    }
}
