package packets.outgoing;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Sent to reset the daily quests currently available.
 */
public class ResetDailyQuestsPacket extends Packet {
    @Override
    public void deserialize(PBuffer buffer) throws Exception {
    }
}
