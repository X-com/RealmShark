package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Received to prompt the player to enter their password
 */
public class PasswordPromptPacket extends Packet {
    /**
     * unknown
     */
    long cleanPasswordStatus;

    @Override
    public void deserialize(PBuffer buffer) throws Exception {
        cleanPasswordStatus = buffer.readUnsignedInt();
    }
}