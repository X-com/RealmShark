package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * > Unknown
 */
public class NewCharacterInfoPacket extends Packet {

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
    }

    @Override
    public String toString() {
        return "NewCharacterInfoPacket{";
    }
}