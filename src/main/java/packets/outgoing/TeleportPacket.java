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
    /**
     * The object name of the player to teleport to
     */
    public String name;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        objectId = buffer.readInt();
        name = buffer.readString();
    }
}
