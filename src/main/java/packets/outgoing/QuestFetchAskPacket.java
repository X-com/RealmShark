package packets.outgoing;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Sent to request the latest quests.
 */
public class QuestFetchAskPacket extends Packet {
    @Override
    public void deserialize(PBuffer buffer) throws Exception {
    }
}
