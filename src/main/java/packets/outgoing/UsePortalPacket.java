package packets.outgoing;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Sent to prompt the server to send a `ReconnectPacket` which
 * contains the reconnect information for the used portal.
 */
public class UsePortalPacket extends Packet {
    /**
     * The object id of the portal to enter.
     */
    public int objectId;

    @Override
    public void deserialize(PBuffer buffer) {
        objectId = buffer.readInt();
    }
}
