package packets.data;

import packets.reader.BufferReader;
import assets.AssetMissingException;
import assets.IdToName;

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
        String tile = "";
        try {
            tile = IdToName.tileName(type);
        } catch (AssetMissingException e) {
            e.printStackTrace();
        }
        return "\n    Tile: " + tile + " " + type + " (" + x + ", " + y + ")";
    }
}
