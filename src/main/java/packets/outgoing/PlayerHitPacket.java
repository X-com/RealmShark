package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Sent when the player is hit.
 */
public class PlayerHitPacket extends Packet {
    /**
     * The id of the bullet which hit the player.
     */
    public short bulletId;
    /**
     * The object id of the enemy that hit the player.
     */
    public int objectId;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        bulletId = buffer.readShort();
        objectId = buffer.readInt();
    }

    @Override
    public String toString() {
        return "PlayerHitPacket{" +
                "\n   bulletId=" + bulletId +
                "\n   objectId=" + objectId;
    }
}
