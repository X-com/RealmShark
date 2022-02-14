package packets.outgoing;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Sent to request a trade with a player, as well as
 * to accept a pending trade with a player.
 */
public class RequestTradePacket extends Packet {
    /**
     * The name of the player to request the trade with.
     */
    public String name;

    @Override
    public void deserialize(PBuffer buffer) throws Exception {
        name = buffer.readString();
    }
}
