package packets.buffer.data;

import packets.buffer.PBuffer;

/**
 * Coordinate data of world objects.
 */
public class WorldPosData {
    /**
     * Position x
     */
    public float x;
    /**
     * Position y
     */
    public float y;

    /**
     * Deserializer method to extract data from the buffer.
     *
     * @param buffer Data that needs deserializing.
     * @return Returns this object after deserializing.
     */
    public WorldPosData deserialize(PBuffer buffer) {
        x = buffer.readFloat();
        y = buffer.readFloat();

        return this;
    }

    public String toString() {
        return String.format("(%f,%f)", x, y);
    }
}
