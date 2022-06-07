package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Received when a chat message is sent by another player or NPC
 */
public class TextPacket extends Packet {
    /**
     * The sender of the message
     */
    public String name;
    /**
     * The object id of the sender
     */
    public int objectId;
    /**
     * The int of stars of the sender
     */
    public short numStars;
    /**
     * The length of time to display the chat bubble for
     */
    public int bubbleTime;
    /**
     * The recipient of the message
     */
    public String recipient;
    /**
     * The content of the message
     */
    public String text;
    /**
     * > Unknown.
     */
    public String cleanText;
    /**
     * Whether or not the sender of the message is a supporter
     */
    public boolean isSupporter;
    /**
     * The star background of the player
     */
    public int starBackground;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        name = buffer.readString();
        objectId = buffer.readInt();
        numStars = buffer.readShort();
        bubbleTime = buffer.readUnsignedByte();
        recipient = buffer.readString();
        text = buffer.readString();
        cleanText = buffer.readString();
        isSupporter = buffer.readBoolean();
        starBackground = buffer.readInt();
    }

    @Override
    public String toString() {
        return "TextPacket{" +
                "\n   name=" + name +
                "\n   objectId=" + objectId +
                "\n   numStars=" + numStars +
                "\n   bubbleTime=" + bubbleTime +
                "\n   recipient=" + recipient +
                "\n   text=" + text +
                "\n   cleanText=" + cleanText +
                "\n   isSupporter=" + isSupporter +
                "\n   starBackground=" + starBackground;
    }
}