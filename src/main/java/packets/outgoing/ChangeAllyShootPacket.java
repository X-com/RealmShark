package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Sent to tell the server if you would like to receive ally (other player) projectiles.
 */
public class ChangeAllyShootPacket extends Packet {
    /**
     * Whether the server will send ally projectiles.
     * 0 = disable, 1 = enable.
     */
    public int isEnabledIfOne;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        isEnabledIfOne = buffer.readInt();
    }

    @Override
    public String toString() {
        return "ChangeAllyShootPacket{" +
                "\n   toggle=" + isEnabledIfOne;
    }
}
