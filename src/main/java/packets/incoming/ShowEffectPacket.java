package packets.incoming;

import packets.Packet;
import packets.data.WorldPosData;
import packets.reader.BufferReader;

/**
 * Received to tell the player to display an effect such as an AOE grenade
 */
public class ShowEffectPacket extends Packet {
    /**
     * The type of effect to display
     */
    public byte effectType;
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
    public int color;
    /**
     * The duration of the effect
     */
    public float duration;

    /**
     * unknown
     */
    public byte unknownByte;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        pos1 = new WorldPosData();
        pos2 = new WorldPosData();
        effectType = buffer.readByte();
        byte ignore = buffer.readByte();

        if ((ignore & 0x40) != 0) {
            targetObjectId = buffer.readCompressedInt();
        } else {
            targetObjectId = 0;
        }
        if ((ignore & 0x2) != 0) {
            pos1.x = buffer.readFloat();
        } else {
            pos1.x = 0.0f;
        }
        if ((ignore & 0x4) != 0) {
            pos1.y = buffer.readFloat();
        } else {
            pos1.y = 0.0f;
        }
        if ((ignore & 0x8) != 0) {
            pos2.x = buffer.readFloat();
        } else {
            pos2.x = 0.0f;
        }
        if ((ignore & 0x10) != 0) {
            pos2.y = buffer.readFloat();
        } else {
            pos2.y = 0.0f;
        }
        if ((ignore & 0x1) != 0) {
            color = buffer.readInt();
        } else {
            color = 0xFFFFFF;
        }
        if ((ignore & 0x20) != 0) {
            duration = buffer.readFloat();
        } else {
            duration = 1.0f;
        }
        if (ignore >= 0) {
            unknownByte = 100;
        } else {
            unknownByte = buffer.readByte();
        }
    }
}