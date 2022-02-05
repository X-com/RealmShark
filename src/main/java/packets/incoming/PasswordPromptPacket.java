package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Received to prompt the player to enter their password
 */
public class PasswordPromptPacket extends Packet {
    @Override
    public void deserialize(PBuffer buffer) {
    }
}