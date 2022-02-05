package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * > Unknown.
 */
public class GuildResultPacket extends Packet {
    /**
     * > Unknown.
     */
    public boolean success;
    /**
     * > Unknown.
     */
    public String lineBuilderJSON;

    @Override
    public void deserialize(PBuffer buffer) {
        success = buffer.readBoolean();
        lineBuilderJSON = buffer.readString();
    }
}