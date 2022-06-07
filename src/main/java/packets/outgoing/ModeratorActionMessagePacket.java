package packets.outgoing;

import packets.Packet;
import packets.data.enums.ModeratorActionType;
import packets.reader.BufferReader;

/**
 * A packet reserved for staff accounts to punish other players.
 */
public class ModeratorActionMessagePacket extends Packet {
    /**
     * The type of action requested.
     */
    public ModeratorActionType actionCode;
    /**
     * The message received from the action.
     */
    public String actionMessage;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        actionCode = ModeratorActionType.byOrdinal(buffer.readInt());
        actionMessage = buffer.readString();
    }

    @Override
    public String toString() {
        return "ModeratorActionMessagePacket{" +
                "\n   actionCode=" + actionCode +
                "\n   actionMessage=" + actionMessage;
    }
}