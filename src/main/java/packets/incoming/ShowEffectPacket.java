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

    // TODO: currently bugged, fix this
    @Override
    public void deserialize(PBuffer buffer) {
        pos1 = new WorldPosData();
        pos2 = new WorldPosData();
        effectType = buffer.readUnsignedByte();
        int loc2 = buffer.readUnsignedByte();
        if ((loc2 & 64) == 64) {
            targetObjectId = new CompressedInt().deserialize(buffer);
        } else {
            targetObjectId = 0;
        }
        if ((loc2 & 2) == 2) {
            pos1.x = buffer.readFloat();
        } else {
            pos1.x = 0;
        }
        if ((loc2 & 4) == 4) {
            pos1.y = buffer.readFloat();
        } else {
            pos1.y = 0;
        }
        if ((loc2 & 8) == 8) {
            pos2.x = buffer.readFloat();
        } else {
            pos2.x = 0;
        }
        if ((loc2 & 16) == 16) {
            pos2.y = buffer.readFloat();
        } else {
            pos2.y = 0;
        }
        if ((loc2 & 1) == 1) {
            color = buffer.readInt();
        } else {
            color = 4294967295L;
        }
        if ((loc2 & 32) == 32) {
            duration = buffer.readFloat();
        } else {
            duration = 1;
        }
    }
}