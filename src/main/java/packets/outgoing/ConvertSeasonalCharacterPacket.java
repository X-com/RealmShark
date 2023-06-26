package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Sent when converting a seasonal character to a regular character
 */
public class ConvertSeasonalCharacterPacket extends Packet {

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
    }

    @Override
    public String toString() {
        return "ConvertSeasonalCharacterPacket";
    }
}
