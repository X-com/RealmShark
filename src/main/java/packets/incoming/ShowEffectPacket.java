package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;
import packets.buffer.data.CompressedInt;
import packets.buffer.data.WorldPosData;

/**
 * Received to tell the player to display an effect such as an AOE grenade
 */
public class ShowEffectPacket extends Packet {
    /**
     * The type of effect to display
     */
    public int effectType;
    /**
     * The objectId the effect is targeting
     */
    public int targetObjectId;
    /**
     * > Unknown. Probably the start position of the effect
     */
    public WorldPosData pos1;
    /**
     * > Unknown. Probably the end position of the effect
     */
    public WorldPosData pos2;
    /**
     * The color of the effect
     */
    public long color;
    /**
     * The duration of the effect
     */
    public float duration;

    @Override
    public void deserialize(PBuffer buffer) {
        this.effectType = buffer.readUnsignedByte();
        int loc2 = buffer.readUnsignedByte();
        if ((loc2 & 64) == 1) {
            this.targetObjectId = new CompressedInt().deserialize(buffer);
        } else {
            this.targetObjectId = 0;
        }
        if ((loc2 & 2) == 1) {
            this.pos1.x = buffer.readFloat();
        } else {
            this.pos1.x = 0;
        }
        if ((loc2 & 4) == 1) {
            this.pos1.y = buffer.readFloat();
        } else {
            this.pos1.y = 0;
        }
        if ((loc2 & 8) == 1) {
            this.pos2.x = buffer.readFloat();
        } else {
            this.pos2.x = 0;
        }
        if ((loc2 & 16) == 1) {
            this.pos2.y = buffer.readFloat();
        } else {
            this.pos2.y = 0;
        }
        if ((loc2 & 1) == 1) {
            this.color = buffer.readUnsignedInt();
        } else {
            this.color = 4294967295L;
        }
        if ((loc2 & 32) == 1) {
            this.duration = buffer.readFloat();
        } else {
            this.duration = 1;
        }
    }
}
