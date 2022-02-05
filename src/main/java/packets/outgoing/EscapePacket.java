package packets.outgoing;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Sent to prompt the server to send a `ReconnectPacket` which
 * contains the reconnect information for the Nexus.
 */
public class EscapePacket extends Packet {
    @Override
    public void deserialize(PBuffer buffer) {
    }
}
