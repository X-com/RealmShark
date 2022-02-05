package packets.buffer.data;

import packets.buffer.PBuffer;

/**
 * Movement data of entity moving to point x and y with delta time.
 */
public class MoveRecord {
    /**
     * The client time of this move record.
     */
    public int time;
    /**
     * The X coordinate of this move record.
     */
    public float x;
    /**
     * The Y coordinate of this move record.
     */
    public float y;

    /**
     * Deserializer method to extract data from the buffer.
     *
     * @param buffer Data that needs deserializing.
     * @return Returns this object after deserializing.
     */
    public MoveRecord deserialize(PBuffer buffer) {
        time = buffer.readInt();
        x = buffer.readFloat();
        y = buffer.readFloat();

        return this;
    }
}
