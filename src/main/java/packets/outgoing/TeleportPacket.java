package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Sent to teleport to another player.
 */
public class TeleportPacket extends Packet {
    /**
     * The object id of the player to teleport to.
     */
    public int objectId;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        objectId = buffer.readInt();
    }
}
