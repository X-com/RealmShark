package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;
import data.WorldPosData;

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
    public int color;
    /**
     * The duration of the effect
     */
    public float duration;

    /**
     * unknown
     */
    public byte unknown;

    @Override
    public void deserialize(PBuffer buffer) throws Exception {
        pos1 = new WorldPosData();
        pos2 = new WorldPosData();
        effectType = buffer.readUnsignedByte();
        int ignore = buffer.readUnsignedByte();

        if ((ignore & 64) != 0) {
            targetObjectId = buffer.readCompressedInt();
        } else {
            targetObjectId = 0;
        }
        if ((ignore & 2) != 0) {
            pos1.x = buffer.readFloat();
        } else {
            pos1.x = 0.0f;
        }
        if ((ignore & 4) != 0) {
            pos1.y = buffer.readFloat();
        } else {
            pos1.y = 0.0f;
        }
        if ((ignore & 8) != 0) {
            pos2.x = buffer.readFloat();
        } else {
            pos2.x = 0.0f;
        }
        if ((ignore & 16) != 0) {
            pos2.y = buffer.readFloat();
        } else {
            pos2.y = 0.0f;
        }
        if ((ignore & 1) != 0) {
            color = buffer.readInt();
        } else {
            color = 0xFFFFFF;
        }
        if ((ignore & 32) != 0) {
            duration = buffer.readFloat();
        } else {
            duration = 1.0f;
        }
        if (effectType == 15) {
            unknown = buffer.readByte();
        } else {
            unknown = 100;
        }

//        String s = String.format("%06X",color);
//        System.out.println("targetObjectId:" + targetObjectId + " (" + pos1.x + "," + pos1.y +") (" + pos2.x + "," + pos2.y +") " + " color:" + s + " duration:" + duration);
//        buffer.readByte(); // TODO: This is not a fix. Just to stop the screaming.
    }
}