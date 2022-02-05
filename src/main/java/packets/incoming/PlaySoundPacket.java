package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Received to tell the client to play a sound
 */
public class PlaySoundPacket extends Packet {
    /**
     * The object id of the origin of the sound
     */
    public int ownerId;
    /**
     * The id of the sound to play
     */
    public int soundId;

    @Override
    public void deserialize(PBuffer buffer) {
        ownerId = buffer.readInt();
        soundId = buffer.readUnsignedByte();
    }
}
