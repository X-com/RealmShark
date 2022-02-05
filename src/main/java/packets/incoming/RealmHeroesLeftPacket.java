package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Received to tell the client how many heroes are left in the current realm
 */
public class RealmHeroesLeftPacket extends Packet {
    /**
     * The number of heroes remaining.
     */
    public int realmHeroesLeft;

    @Override
    public void deserialize(PBuffer buffer) {
        realmHeroesLeft = buffer.readInt();
    }
}
