package packets.outgoing;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Sent to prompt the server to send a `ReconnectPacket` which
 * contains the reconnect information for the Quest Room.
 */
public class GoToQuestRoomPacket extends Packet {
    @Override
    public void deserialize(PBuffer buffer) throws Exception {
    }
}
