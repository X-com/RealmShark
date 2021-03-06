package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Received when the player's exaltation stats are updated.
 */
public class ExaltationUpdatePacket extends Packet {
    /**
     * The object type of the player's class.
     */
    public short objType;
    /**
     * The amount of stats to increase.
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

    @Override
    public String toString() {
        return "ExaltationUpdatePacket{" +
                "\n   objType=" + objType +
                "\n   attackProgress=" + attackProgress +
                "\n   defenseProgress=" + defenseProgress +
                "\n   speedProgress=" + speedProgress +
                "\n   dexterityProgress=" + dexterityProgress +
                "\n   vitalityProgress=" + vitalityProgress +
                "\n   wisdomProgress=" + wisdomProgress +
                "\n   healthProgress=" + healthProgress +
                "\n   manaProgress=" + manaProgress;
    }
}