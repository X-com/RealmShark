package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

import java.util.StringJoiner;

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
    /**
     * Unknown
     */
    public String str;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        objectId = buffer.readInt();
        charId = buffer.readInt();
        str = buffer.readString();
    }

    @Override
    public String toString() {
        return "CreateSuccessPacket{" +
                "\n   objectId=" + objectId +
                "\n   charId=" + charId +
                "\n   str=" + str;
    }
}