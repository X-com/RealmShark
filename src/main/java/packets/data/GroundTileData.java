package packets.data;

import packets.reader.BufferReader;
import assets.AssetMissingException;
import assets.IdToAsset;

import java.io.Serializable;

/**
 * Tile data class storing tile coordinates (x and y) and type of each tile.
 */
public class GroundTileData implements Serializable {
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
            tile = IdToAsset.tileName(type);
        } catch (AssetMissingException e) {
            e.printStackTrace();
        }
        return "    Tile: " + tile + " " + type + " (" + x + ", " + y + ")";
    }
}
