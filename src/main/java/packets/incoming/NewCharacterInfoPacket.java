package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * > Unknown
 */
public class NewCharacterInfoPacket extends Packet {
    /**
     * Unknown
     */
    String charXML;

    @Override
    public void deserialize(PBuffer buffer) throws Exception {
        charXML = buffer.readString();
    }
}