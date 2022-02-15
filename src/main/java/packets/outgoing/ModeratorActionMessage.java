package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Presumably received when the player needs moderation.
 */
public class ModeratorActionMessage extends Packet {
    /**
     * The moderation message.
     */
    public String message;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        message = buffer.readString();
    }
}
