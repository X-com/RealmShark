package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Received in response to a `BuyPacket`.
 */
public class BuyResultPacket extends Packet {
    /**
     * The result code.
     */
    public int result;
    /**
     * > Unknown.
     */
    public String resultString;

    @Override
    public void deserialize(PBuffer buffer) {
        result = buffer.readInt();
        resultString = buffer.readString();
    }
}