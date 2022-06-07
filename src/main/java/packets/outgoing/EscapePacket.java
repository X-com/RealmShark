package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Sent to prompt the server to send a `ReconnectPacket` which
 * contains the reconnect information for the Nexus.
 */
public class EscapePacket extends Packet {
    @Override
    public void deserialize(BufferReader buffer) throws Exception {
    }

    @Override
    public String toString() {
        return "EscapePacket{}";
    }
}
