package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Received when the players exaltation stats update
 */
public class ExaltationUpdatePacket extends Packet {
    /**
     * The object type of the player's class
     */
    public short objType;
    /**
     * The amount of stats to increase
     */
    public int attackProgress;
    public int defenseProgress;
    public int speedProgress;
    public int dexterityProgress;
    public int vitalityProgress;
    public int wisdomProgress;
    public int healthProgress;
    public int manaProgress;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        objType = buffer.readShort();
        dexterityProgress = buffer.readCompressedInt();
        speedProgress = buffer.readCompressedInt();
        vitalityProgress = buffer.readCompressedInt();
        wisdomProgress = buffer.readCompressedInt();
        defenseProgress = buffer.readCompressedInt();
        attackProgress = buffer.readCompressedInt();
        manaProgress = buffer.readCompressedInt();
        healthProgress = buffer.readCompressedInt();
    }
}