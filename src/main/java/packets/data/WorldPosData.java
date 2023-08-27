package packets.data;

import packets.reader.BufferReader;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Coordinate data of world objects.
 */
public class WorldPosData implements Serializable {
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
    public WorldPosData deserialize(BufferReader buffer) {
        x = buffer.readFloat();
        y = buffer.readFloat();

        return this;
    }

    @Override
    public String toString() {
        return String.format("WorldPosData (x:%f, y:%f)", x, y);
    }
}
