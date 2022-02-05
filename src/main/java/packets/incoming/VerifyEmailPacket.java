package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Received to prompt the player to verify their email.
 */
public class VerifyEmailPacket extends Packet {
    @Override
    public void deserialize(PBuffer buffer) {
    }
}