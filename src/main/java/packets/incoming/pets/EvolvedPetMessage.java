package packets.incoming.pets;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Received to give the player information about a newly evolved pet.
 */
public class EvolvedPetMessage extends Packet {
    /**
     * The id of the pet which has evolved.
     */
    public int petId;
    /**
     * The current skin id of the pet.
     */
    public int initialSkin;
    /**
     * The skin id of the pet after its evolution.
     */
    public int finalSkin;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        petId = buffer.readInt();
        initialSkin = buffer.readInt();
        finalSkin = buffer.readInt();
    }

    @Override
    public String toString() {
        return "EvolvedPetMessage{" +
                "\n   petId=" + petId +
                "\n   initialSkin=" + initialSkin +
                "\n   finalSkin=" + finalSkin;
    }
}