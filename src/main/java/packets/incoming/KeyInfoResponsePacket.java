package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * > Unknown.
 */
public class KeyInfoResponsePacket extends Packet {
    /**
     * > Unknown.
     */
    public String name;
    /**
     * > Unknown.
     */
    public String description;
    /**
     * > Unknown.
     */
    public String creator;

    @Override
    public void deserialize(PBuffer buffer) {
        this.name = buffer.readString();
        this.description = buffer.readString();
        this.creator = buffer.readString();
    }
}
