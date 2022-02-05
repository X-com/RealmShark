package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * > Unknown
 */
public class QuestRedeemResponsePacket extends Packet {
    /**
     * > Unknown
     */
    public boolean ok;
    /**
     * > Unknown
     */
    public String message;

    @Override
    public void deserialize(PBuffer buffer) {
        ok = buffer.readBoolean();
        message = buffer.readString();
    }
}
