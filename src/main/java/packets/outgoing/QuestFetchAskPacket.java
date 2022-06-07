package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Sent to request the latest quests.
 */
public class QuestFetchAskPacket extends Packet {
    @Override
    public void deserialize(BufferReader buffer) throws Exception {
    }

    @Override
    public String toString() {
        return "QuestFetchAskPacket{}";
    }
}
