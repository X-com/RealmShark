package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Sent to create a new character.
 */
public class CreatePacket extends Packet {
    /**
     * The class to use for the new character.
     */
    public short classType;
    /**
     * The skin id to use for the new character.
     * The default skin id is `0`.
     */
    public short skinType;
    /**
     * Whether the character is in challenger mode.
     */
    public boolean isChallenger;
    /**
     * Whether the character is in seasonal mode.
     */
    public boolean isSeasonal;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        classType = buffer.readShort();
        skinType = buffer.readShort();
        isChallenger = buffer.readBoolean();
        isSeasonal = buffer.readBoolean();
    }

    @Override
    public String toString() {
        return "CreatePacket{" +
                "\n   classType=" + classType +
                "\n   skinType=" + skinType +
                "\n   isChallenger=" + isChallenger +
                "\n   isSeasonal=" + isSeasonal;
    }
}
