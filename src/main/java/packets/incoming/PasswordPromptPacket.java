package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Received to prompt the player to enter their password
 */
public class PasswordPromptPacket extends Packet {
    public int cleanPasswordStatus;

    @Override
    public void deserialize(PBuffer buffer) {
        cleanPasswordStatus = buffer.readInt();
    }
}
