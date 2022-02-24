package packets.data;

import packets.reader.BufferReader;

/**
 * Movement data of entity moving to point x and y with delta time.
 */
public class MoveRecord {
    /**
     * The client time of this move record.
     */
    public int time;
    /**
     * The position where the entity is moving to.
     */
    public WorldPosData pos;

    /**
     * Deserializer method to extract data from the buffer.
     *
     * @param buffer Data that needs deserializing.
     * @return Returns this object after deserializing.
     */
    public MoveRecord deserialize(BufferReader buffer) {
        time = buffer.readInt();

        pos = new WorldPosData().deserialize(buffer);

        return this;
    }

    public String toString() {
        return String.format("Time:%d %s", time, pos);
    }
}
