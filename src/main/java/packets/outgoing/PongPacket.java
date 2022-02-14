package packets.outgoing;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Sent to acknowledge the `PingPacket.`
 */
public class PongPacket extends Packet {
    /**
     * The serial value received in the `PingPacket` which this acknowledges.
     */
    public int serial;
    /**
     * The current client time.
     */
    public int time;

    @Override
    public void deserialize(PBuffer buffer) throws Exception {
        serial = buffer.readInt();
        time = buffer.readInt();
    }
}
