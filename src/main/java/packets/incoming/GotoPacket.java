package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;
import packets.data.WorldPosData;

/**
 * Received when an entity has moved to a new position.
 */
public class GotoPacket extends Packet {
    /**
     * The object id of the entity which moved.
     */
    public int objectId;
    /**
     * The new position of the entity.
     */
    public WorldPosData position;
    /**
     * Unknown int
     */
    public int unknownInt;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        objectId = buffer.readInt();
        position = new WorldPosData().deserialize(buffer);
        unknownInt = buffer.readInt();
    }

    @Override
    public String toString() {
        return "GotoPacket{" +
                "\n   objectId=" + objectId +
                "\n   position=" + position +
                "\n   unknownInt=" + unknownInt;
    }
}