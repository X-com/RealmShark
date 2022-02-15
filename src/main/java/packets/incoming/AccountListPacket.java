package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Received to provide lists of accounts ids which are
 * those of players who have been locked, ignored, etc.
 */
public class AccountListPacket extends Packet {
    /**
     * The id of the account id list.
     */
    public int accountListId;
    /**
     * The account ids included in the list.
     */
    public String[] accountIds;
    /**
     * > Unknown.
     */
    public int lockAction;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        accountListId = buffer.readInt();
        accountIds = new String[buffer.readShort()];
        for (int i = 0; i < accountIds.length; i++) {
            accountIds[i] = buffer.readString();
        }
        lockAction = buffer.readInt();
    }
}