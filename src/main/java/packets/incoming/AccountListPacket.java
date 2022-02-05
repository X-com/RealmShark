package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;

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
    public void deserialize(PBuffer buffer) {
        accountListId = buffer.readInt();
        short len = buffer.readShort();
        accountIds = new String[len];
        for (int i = 0; i < len; i++) {
            accountIds[i] = buffer.readString();
        }
        lockAction = buffer.readInt();
    }
}
