package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Received when a notification is received by the player
 */
public class NotificationPacket extends Packet {
    /**
     * The object id of the entity which the notification is for.
     */
    public int objectId;
    /**
     * The notification message
     */
    public String message;
    /**
     * The color of the notification text
     */
    public int color;

    @Override
    public void deserialize(PBuffer buffer) {
        objectId = buffer.readInt();
        message = buffer.readString();
        color = buffer.readInt();
    }
}