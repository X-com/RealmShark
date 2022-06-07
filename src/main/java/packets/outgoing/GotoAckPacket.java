package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Sent to acknowledge a `GotoPacket`.
 */
public class GotoAckPacket extends Packet {
    /**
     * The current client time.
     */
    public int time;
    /**
     * Unknown boolean
     */
    private boolean unknownBoolean;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        time = buffer.readInt();
        unknownBoolean = buffer.readBoolean();
    }

    @Override
    public String toString() {
        return "GotoAckPacket{" +
                "\n   time=" + time +
                "\n   unknownBoolean=" + unknownBoolean;
    }
}
