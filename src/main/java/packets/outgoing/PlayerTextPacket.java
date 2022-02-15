package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Sent when the client sends a chat message.
 */
public class PlayerTextPacket extends Packet {
    /**
     * The message to send.
     */
    public String text;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        text = buffer.readString();
    }
}
