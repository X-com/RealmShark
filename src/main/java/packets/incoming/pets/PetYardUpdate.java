package packets.incoming.pets;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Received to give the player information about a newly hatched pet
 */
public class PetYardUpdate extends Packet {
    /**
     * The name of the hatched pet
     */
    public String petName;
    /**
     * The skin id of the hatched pet
     */
    public int petSkin;
    /**
     * The object type of the pet
     */
    public int petType;

    @Override
    public void deserialize(PBuffer buffer) {
        this.petName = buffer.readString();
        this.petSkin = buffer.readInt();
        this.petType = buffer.readInt();
    }
}
