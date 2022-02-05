package packets.outgoing;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Sent to teleport to another player.
 */
public class TeleportPacket extends Packet {
    /**
     * The object id of the player to teleport to.
     */
    public int objectId;

    @Override
    public void deserialize(PBuffer buffer) {
        objectId = buffer.readInt();
    }
}
