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
        this.x = buffer.readFloat();
        this.y = buffer.readFloat();

        return this;
    }
}
