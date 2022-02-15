package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Sent to edit an account id list.
 */
public class EditAccountListPacket extends Packet {
    /**
     * The id of the account id list being edited.
     */
    public int accountListId;
    /**
     * Whether the edit is to add to the list or remove from it.
     */
    public boolean add;
    /**
     * The object id of the player to add to the list.
     */
    public int objectId;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        accountListId = buffer.readInt();
        add = buffer.readBoolean();
        objectId = buffer.readInt();
    }
}
