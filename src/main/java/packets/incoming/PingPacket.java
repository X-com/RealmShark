package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Received in response to a `BuyPacket`.
 */
public class PingPacket extends Packet {
    /**
     * A nonce value which is expected to be present in the reply
     */
    public int serial;

    @Override
    public void deserialize(PBuffer buffer) {
        serial = buffer.readInt();
    }
}
