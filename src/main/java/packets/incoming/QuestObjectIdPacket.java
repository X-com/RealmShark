package packets.incoming;

import data.GroundTileData;
import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Received to tell the player the object id of their current quest
 */
public class QuestObjectIdPacket extends Packet {
    /**
     * The object id of the current quest
     */
    public int objectId;
    /**
     * Quest list
     */
    public int[] list;

    @Override
    public void deserialize(PBuffer buffer) throws Exception {
        objectId = buffer.readInt();
        list = new int[buffer.readCompressedInt()];
        for (int i = 0; i < list.length; i++) {
            list[i] = buffer.readCompressedInt();
        }
    }
}