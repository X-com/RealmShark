package packets.incoming.pets;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Received to give the player information about a newly hatched pet
 */
public class HatchPetMessage extends Packet {
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
    public void deserialize(BufferReader buffer) throws Exception {
        petName = buffer.readString();
        petSkin = buffer.readInt();
        petType = buffer.readInt();
    }

    @Override
    public String toString() {
        return "HatchPetMessage{" +
                "\n   petName=" + petName +
                "\n   petSkin=" + petSkin +
                "\n   petType=" + petType;
    }
}