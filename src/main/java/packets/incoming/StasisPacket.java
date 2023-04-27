package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Stasis blinking timer packet
 */
public class StasisPacket extends Packet {
    /**
     * Id of the entity put into stasis
     */
    public int entityId;
    /**
     * Unknown
     */
    public int unknownInt1;
    public int unknownInt2;
    public int unknownInt3;
    /**
     * Stasis duration in seconds
     */
    public float stasisDuration;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        entityId = buffer.readInt();
        unknownInt1 = buffer.readInt();
        unknownInt2 = buffer.readInt();
        unknownInt3 = buffer.readInt();
        stasisDuration = buffer.readFloat();
    }

    @Override
    public String toString() {
        return "StasisPacket{" +
                "\n   entityId=" + entityId +
                "\n   unknownInt1=" + unknownInt1 +
                "\n   unknownInt2=" + unknownInt2 +
                "\n   unknownInt3=" + unknownInt3 +
                "\n   stasisDuration=" + stasisDuration;
    }
}
