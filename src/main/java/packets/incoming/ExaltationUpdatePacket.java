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
    public void deserialize(PBuffer buffer) {
        this.objType = buffer.readShort();
        this.dexterityProgress = buffer.readByte();
        this.speedProgress = buffer.readByte();
        this.vitalityProgress = buffer.readByte();
        this.wisdomProgress = buffer.readByte();
        this.defenseProgress = buffer.readByte();
        this.attackProgress = buffer.readByte();
        this.manaProgress = buffer.readByte();
        this.healthProgress = buffer.readByte();
    }
}
