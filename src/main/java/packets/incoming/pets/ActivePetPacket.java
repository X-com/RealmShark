package packets.incoming.pets;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Received to notify the player of a new pet.
 */
public class ActivePetPacket extends Packet {
    /**
     * The instance id of the active pet.
     */
    public int instanceId;

    @Override
    public void deserialize(PBuffer buffer) {
        instanceId = buffer.readInt();
    }

}