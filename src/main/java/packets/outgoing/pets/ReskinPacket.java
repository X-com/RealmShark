package packets.outgoing.pets;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Sent to activate a new skin for the current character.
 */
public class ReskinPacket extends Packet {
    /**
     * The id of the skin to activate.
     */
    public int skinId;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        skinId = buffer.readInt();
    }

    @Override
    public String toString() {
        return "ReskinPacket{" +
                "\n   skinId=" + skinId;
    }
}
