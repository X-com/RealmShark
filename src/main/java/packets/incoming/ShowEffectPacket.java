package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;
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
    public int color;
    /**
     * The duration of the effect
     */
    public float duration;

    /**
     * unknown
     */
    public byte unknown;

    // TODO: currently bugged, fix this
    @Override
    public void deserialize(PBuffer buffer) throws Exception {
        pos1 = new WorldPosData();
        pos2 = new WorldPosData();
        effectType = buffer.readUnsignedByte();
        int ignore = buffer.readUnsignedByte();
//        System.out.println("show effect: " + buffer.getArray());
//        System.out.println(ignore + " " + Integer.toBinaryString(ignore));
        if ((ignore & 64) == 64) {
            targetObjectId = buffer.readCompressedInt();
        } else {
            targetObjectId = 0;
        }
        if ((ignore & 2) == 2) {
            pos1.x = buffer.readFloat();
        } else {
            pos1.x = 0;
        }
        if ((ignore & 4) == 4) {
            pos1.y = buffer.readFloat();
        } else {
            pos1.y = 0;
        }
        if ((ignore & 8) == 8) {
            pos2.x = buffer.readFloat();
        } else {
            pos2.x = 0;
        }
        if ((ignore & 16) == 16) {
            pos2.y = buffer.readFloat();
        } else {
            pos2.y = 0;
        }
        if ((ignore & 1) == 1) {
            color = buffer.readInt();
        } else {
            color = 0;
        }
        if ((ignore & 32) == 32) {
            duration = buffer.readFloat();
        } else {
            duration = 0;
        }
        if(effectType == 15){
            unknown = buffer.readByte();
        }
//        String s = String.format("%06X",color);
//        System.out.println("targetObjectId:" + targetObjectId + " (" + pos1.x + "," + pos1.y +") (" + pos2.x + "," + pos2.y +") " + " color:" + s + " duration:" + duration);
//        buffer.readByte(); // TODO: This is not a fix. Just to stop the screaming.
    }
}