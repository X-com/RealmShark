package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;
import packets.buffer.data.QuestData;

/**
 * Received to tell the player about new quests
 */
public class QuestFetchResponsePacket extends Packet {
    /**
     * The quests which were fetched
     */
    public QuestData[] quests;
    /**
     * The cost in gold of the next quest refresh
     */
    public short nextRefreshPrice;

    @Override
    public void deserialize(PBuffer buffer) throws Exception {
        quests = new QuestData[buffer.readShort()];
        for (int i = 0; i < quests.length; i++) {
            quests[i] = new QuestData().deserialize(buffer);
        }
        nextRefreshPrice = buffer.readShort();
    }
}