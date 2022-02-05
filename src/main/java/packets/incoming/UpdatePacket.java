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
        int groundTileDataLen = new CompressedInt().deserialize(buffer);
        tiles = new GroundTileData[groundTileDataLen];
        for (int i = 0; i < tiles.length; i++) {
            tiles[i] = new GroundTileData().deserialize(buffer);
        }

        int newObjectsLen = new CompressedInt().deserialize(buffer);
        newObjects = new ObjectData[newObjectsLen];
        for (int i = 0; i < newObjectsLen; i++) {
            newObjects[i] = new ObjectData().deserialize(buffer);
        }

        int dropsLen = new CompressedInt().deserialize(buffer);
        drops = new int[dropsLen];
        for (int i = 0; i < dropsLen; i++) {
            drops[i] = new CompressedInt().deserialize(buffer);
        }
    }
}
