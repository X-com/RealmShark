package packets.outgoing;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Sent when the client sends a chat message.
 */
public class PlayerTextPacket extends Packet {
    /**
     * The message to send.
     */
    public String text;

    @Override
    public void deserialize(PBuffer buffer) {
        text = buffer.readString();
    }
}
