package packets.outgoing;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Sent to change the client's account name.
 */
public class ChooseNamePacket extends Packet {
    /**
     * The name to change the account's name to.
     */
    public String name;

    @Override
    public void deserialize(PBuffer buffer) throws Exception {
        name = buffer.readString();
    }
}
