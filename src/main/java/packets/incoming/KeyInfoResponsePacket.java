package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

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
    public void deserialize(BufferReader buffer) throws Exception {
        name = buffer.readString();
        description = buffer.readString();
        creator = buffer.readString();
    }

    @Override
    public String toString() {
        return "KeyInfoResponsePacket{" +
                "\n   name=" + name +
                "\n   description=" + description +
                "\n   creator=" + creator;
    }
}