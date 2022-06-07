package packets.incoming.pets;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Received to notify the player of a new pet.
 */
public class ActivePetPacket extends Packet {
    /**
     * The instance id of the active pet.
     */
    public int instanceId;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        instanceId = buffer.readInt();
    }

    @Override
    public String toString() {
        return "ActivePetPacket{" +
                "\n   instanceId=" + instanceId;
    }
}