package packets.incoming.pets;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Received to notify the player that a pet has been deleted.
 */
public class DeletePetMessage extends Packet {
    /**
     * The id of the pet which has been deleted.
     */
    public int petId;

    @Override
    public void deserialize(PBuffer buffer) {
        petId = buffer.readInt();
    }

}