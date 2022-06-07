package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * > Unknown.
 */
public class CheckCreditsPacket extends Packet {
    @Override
    public void deserialize(BufferReader buffer) throws Exception {
    }

    @Override
    public String toString() {
        return "CheckCreditsPacket{}";
    }
}
