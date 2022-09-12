package packets.data;

import packets.reader.BufferReader;
import util.IdToName;

/**
 * Tile data class storing tile coordinates (x and y) and type of each tile.
 */
public class GroundTileData {
    /**
     * The X coordinate of this tile.
     */
    public short x;
    /**
     * The Y coordinate of this tile.
     */
    public short y;
    /**
     * The tile type of this tile.
     */
    public int type;

    /**
     * Deserializer method to extract data from the buffer.
     *
     * @param buffer Data that needs deserializing.
     * @return Returns this object after deserializing.
     */
    public GroundTileData deserialize(BufferReader buffer) {
        x = buffer.readShort();
        y = buffer.readShort();
        type = buffer.readUnsignedShort();
        return this;
    }

    @Override
    public String toString() {
        return "\n    Tile: " + IdToName.tileName(type) + " " + type + " (" + x + ", " + y + ")";
    }
}
