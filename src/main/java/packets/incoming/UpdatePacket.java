package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;
import data.GroundTileData;
import data.ObjectData;
import data.WorldPosData;

/**
 * Received when an update even occurs. Some events include
 * + One or more new objects have entered the map (become visible)
 * + One or more objects have left the map (become invisible)
 * + New tiles are visible
 */
public class UpdatePacket extends Packet {
    /**
     * Unkown level byte
     */
    public byte levelType;
    /**
     * Pos unknown
     */
    public WorldPosData pos;
    /**
     * The new tiles which are visible.
     */
    public GroundTileData[] tiles;
    /**
     * The new objects which have entered the map (become visible).
     */
    public ObjectData[] newObjects;
    /**
     * The visible objects which have left the map (become invisible).
     */
    public int[] drops;

    // TODO: currently bugged, fix this
    @Override
    public void deserialize(PBuffer buffer) throws Exception {
        levelType = buffer.readByte();
        pos = new WorldPosData().deserialize(buffer);

        tiles = new GroundTileData[buffer.readCompressedInt()];
        for (int i = 0; i < tiles.length; i++) {
            tiles[i] = new GroundTileData().deserialize(buffer);
        }

        newObjects = new ObjectData[buffer.readCompressedInt()];
        for (int i = 0; i < newObjects.length; i++) {
            newObjects[i] = new ObjectData().deserialize(buffer);
        }

        drops = new int[buffer.readCompressedInt()];
        for (int i = 0; i < drops.length; i++) {
            drops[i] = buffer.readCompressedInt();
        }
    }
}