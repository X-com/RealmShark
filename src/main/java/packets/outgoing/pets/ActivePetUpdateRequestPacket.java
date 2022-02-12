package packets.outgoing.pets;

import packets.Packet;
import packets.buffer.PBuffer;
import packets.buffer.data.enums.ActivePetUpdateType;

/**
 * Sent to make an update to the pet currently following the player
 */
public class ActivePetUpdateRequestPacket extends Packet {
    /**
     * The type of update to perform
     */
    public ActivePetUpdateType commandType;
    /**
     * The instance id of the pet to update
     */
    public int instanceId;

    @Override
    public void deserialize(PBuffer buffer) throws Exception {
        commandType = ActivePetUpdateType.byOrdinal(buffer.readByte());
        instanceId = buffer.readInt();
    }
}