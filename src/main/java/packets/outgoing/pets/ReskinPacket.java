package packets.outgoing.pets;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Sent to activate a new skin for the current character.
 */
public class ReskinPacket extends Packet {
    /**
     * The id of the skin to activate.
     */
    public int skinId;

    @Override
    public void deserialize(PBuffer buffer) {
        skinId = buffer.readInt();
    }
}
