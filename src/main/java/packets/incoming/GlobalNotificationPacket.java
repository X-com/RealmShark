package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

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
    public void deserialize(BufferReader buffer) throws Exception {
        notificationType = buffer.readInt();
        text = buffer.readString();
    }

    @Override
    public String toString() {
        return "GlobalNotificationPacket{" +
                "\n   notificationType=" + notificationType +
                "\n   text=" + text;
    }
}