package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Received when a new ability has been unlocked by the player.
 */
public class NewAbilityMessage extends Packet {
    /**
     * The type of ability which has been unlocked.
     */
    public int abilityType;

    @Override
    public void deserialize(PBuffer buffer) {
        abilityType = buffer.readInt();
    }
}