package packets.outgoing;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Sent in response to a `MapInfoPacket` to load a character into the map.
 */
public class LoadPacket extends Packet {
    /**
     * The id of the character to load.
     */
    public int charId;
    /**
     * Whether or not the `MapInfoPacket` being responded to is from the arena.
     */
    public boolean isFromArena;
    /**
     * Whether or not the character is in challenger mode.
     */
    public boolean isChallenger;

    @Override
    public void deserialize(PBuffer buffer) {
        charId = buffer.readInt();
        isFromArena = buffer.readBoolean();
        isChallenger = buffer.readBoolean();
    }
}
