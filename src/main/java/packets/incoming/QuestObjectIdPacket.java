package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

import java.util.Arrays;

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
    public void deserialize(BufferReader buffer) throws Exception {
        objectId = buffer.readInt();
        list = new int[buffer.readCompressedInt()];
        for (int i = 0; i < list.length; i++) {
            list[i] = buffer.readCompressedInt();
        }
    }

    @Override
    public String toString() {
        return "QuestObjectIdPacket{" +
                "\n   objectId=" + objectId +
                "\n   list=" + Arrays.toString(list);
    }
}