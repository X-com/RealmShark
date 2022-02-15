package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * > Unknown
 */
public class NewCharacterInfoPacket extends Packet {
    /**
     * Unknown
     */
    String charXML;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        charXML = buffer.readString();
    }
}