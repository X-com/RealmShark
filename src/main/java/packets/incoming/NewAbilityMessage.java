package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Received when a new ability has been unlocked by the player.
 */
public class NewAbilityMessage extends Packet {
    /**
     * The type of ability which has been unlocked.
     */
    public int abilityType;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        abilityType = buffer.readInt();
    }

    @Override
    public String toString() {
        return "NewAbilityMessage{" +
                "\n   abilityType=" + abilityType;
    }
}