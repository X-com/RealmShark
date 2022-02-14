package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;
import data.enums.NotificationEffectType;

/**
 * Received when a notification is received by the player
 */
public class NotificationPacket extends Packet {
    /**
     * Notification effect type
     */
    public NotificationEffectType effect;
    /**
     * Unknown
     */
    public byte extra;
    /**
     * The object id of the object which this status is for
     */
    public int objectId;
    /**
     * The notification message
     */
    public String message;
    /**
     * ... no idea
     */
    public int uiExtra;
    /**
     * Position in the queue when queueing for servers
     */
    public int queuePos;
    /**
     * The color of the notification text
     */
    public int color;
    /**
     * The picture type of the notification
     */
    public int pictureType;
    /**
     * unknown
     */
    public int unknown1;
    /**
     * unknown
     */
    public int unknown2;

    @Override
    public void deserialize(PBuffer buffer) throws Exception {
        effect = NotificationEffectType.byOrdinal(buffer.readByte());
        extra = buffer.readByte();
        message = buffer.readString();

        switch (effect.get()) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 9:
                return;
            case 4:
                uiExtra = buffer.readShort();
                return;
            case 5:
                objectId = buffer.readInt();
                queuePos = buffer.readShort();
                return;
            case 6:
                objectId = buffer.readInt();
                color = buffer.readInt();
                return;
            case 7:
            case 8:
                pictureType = buffer.readInt();
                return;
            case 10:
                unknown1 = buffer.readInt();
                unknown2 = buffer.readByte();
                return;
            default:
        }
    }
}