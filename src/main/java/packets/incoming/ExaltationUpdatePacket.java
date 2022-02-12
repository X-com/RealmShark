package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;

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
    public byte attackProgress;
    public byte defenseProgress;
    public byte speedProgress;
    public byte dexterityProgress;
    public byte vitalityProgress;
    public byte wisdomProgress;
    public byte healthProgress;
    public byte manaProgress;

    @Override
    public void deserialize(PBuffer buffer) throws Exception {
        objType = buffer.readShort();
        dexterityProgress = buffer.readByte();
        speedProgress = buffer.readByte();
        vitalityProgress = buffer.readByte();
        wisdomProgress = buffer.readByte();
        defenseProgress = buffer.readByte();
        attackProgress = buffer.readByte();
        manaProgress = buffer.readByte();
        healthProgress = buffer.readByte();
    }
}