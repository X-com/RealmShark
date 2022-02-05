package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;
import packets.buffer.data.CompressedInt;
import packets.buffer.data.GroundTileData;
import packets.buffer.data.ObjectData;

/**
 * Received when an update even occurs. Some events include
 * + One or more new objects have entered the map (become visible)
 * + One or more objects have left the map (become invisible)
 * + New tiles are visible
 */
public class UpdatePacket extends Packet {
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

    @Override
    public void deserialize(PBuffer buffer) {
        tiles = new GroundTileData[new CompressedInt().deserialize(buffer)];
        for (int i = 0; i < tiles.length; i++) {
            tiles[i] = new GroundTileData().deserialize(buffer);
        }

        newObjects = new ObjectData[new CompressedInt().deserialize(buffer)];
        for (int i = 0; i < newObjects.length; i++) {
            newObjects[i] = new ObjectData().deserialize(buffer);
        }

        drops = new int[new CompressedInt().deserialize(buffer)];
        for (int i = 0; i < drops.length; i++) {
            drops[i] = new CompressedInt().deserialize(buffer);
        }
    }
}