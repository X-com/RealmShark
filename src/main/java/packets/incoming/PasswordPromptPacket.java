package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Received to prompt the player to enter their password
 */
public class PasswordPromptPacket extends Packet {
    /**
     * unknown
     */
    long cleanPasswordStatus;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        cleanPasswordStatus = buffer.readUnsignedInt();
    }

    @Override
    public String toString() {
        return "PasswordPromptPacket{" +
                "\n   cleanPasswordStatus=" + cleanPasswordStatus;
    }
}