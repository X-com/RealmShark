package packets.incoming;

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

    @Override
    public void deserialize(PBuffer buffer) throws Exception {
        objectId = buffer.readInt();
    }
}