package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Received when a global notification is sent out to all players.
 */
public class GlobalNotificationPacket extends Packet {
    /**
     * The type of notification received.
     */
    public int notificationType;
    /**
     * The notification message.
     */
    public String text;

    @Override
    public void deserialize(PBuffer buffer) {
        notificationType = buffer.readInt();
        text = buffer.readString();
    }
}