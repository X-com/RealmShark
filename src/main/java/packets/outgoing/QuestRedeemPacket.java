package packets.outgoing;

import packets.Packet;
import packets.data.QuestData;
import packets.reader.BufferReader;
import packets.data.SlotObjectData;

import java.util.Arrays;

/**
 * Packet sent when redeeming a quest.
 */
public class QuestRedeemPacket extends Packet {
    /**
     * ID of the quest
     */
    String questIdString;
    /**
     * > Unknown int
     */
    public int questIdInt;
    /**
     * > Unknown slots
     */
    public SlotObjectData[] slots;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        questIdString = buffer.readString();
        questIdInt = buffer.readInt();
        slots = new SlotObjectData[buffer.readShort()];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = new SlotObjectData().deserialize(buffer);
        }
    }

    @Override
    public String toString() {
        return "QuestRedeemPacket{" +
                "\n   questIdString=" + questIdString +
                "\n   questIdInt=" + questIdInt +
                "\n   slots=" + Arrays.toString(slots);
    }
}
