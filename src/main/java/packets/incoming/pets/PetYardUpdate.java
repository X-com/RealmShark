package packets.incoming.pets;


import packets.Packet;
import packets.reader.BufferReader;
import packets.data.enums.PetYardType;

/**
 * Received when the pet yard is updated to a new type of yard
 */
public class PetYardUpdate extends Packet {
    /**
     * The type of the new yard
     */
    public PetYardType yardType;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        yardType = PetYardType.byOrdinal(buffer.readInt());
    }

    @Override
    public String toString() {
        return "PetYardUpdate{" +
                "\n   yardType=" + yardType;
    }
}