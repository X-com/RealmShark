package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Received in response to a `CreatePacket`
 */
public class CreateSuccessPacket extends Packet {
    /**
     * The object id of the player's character
     */
    public int objectId;
    /**
     * The character id of the player's character
     */
    public int charId;

    @Override
    public void deserialize(PBuffer buffer) {
        objectId = buffer.readInt();
        charId = buffer.readInt();
    }
}