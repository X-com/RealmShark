package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

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
    public void deserialize(BufferReader buffer) throws Exception {
        objectId = buffer.readInt();
    }

    @Override
    public String toString() {
        return "UsePortalPacket{" +
                "\n   objectId=" + objectId;
    }
}
