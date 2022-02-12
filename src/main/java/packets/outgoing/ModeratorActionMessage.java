package packets.outgoing;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Presumably received when the player needs moderation.
 */
public class ModeratorActionMessage extends Packet {
    /**
     * The moderation message.
     */
    public String message;

    @Override
    public void deserialize(PBuffer buffer) throws Exception {
        message = buffer.readString();
    }
}
