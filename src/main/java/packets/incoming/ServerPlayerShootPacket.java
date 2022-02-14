package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;
import data.WorldPosData;

/**
 * Received when another player shoots
 */
public class ServerPlayerShootPacket extends Packet {
    /**
     * The id of the bullet that was produced
     */
    public int bulletId;
    /**
     * The object id of the player who fired the projectile
     */
    public int ownerId;
    /**
     * The item id of the weapon used to fire the projectile
     */
    public int containerType;
    /**
     * The starting position of the projectile
     */
    public WorldPosData startingPos;
    /**
     * The angle at which the projectile was fired
     */
    public float angle;
    /**
     * The damage which will be dealt by the projectile
     */
    public short damage;
    /**
     * Unknown int
     */
    public int unknownInt;
    /**
     * Unkown byte 1
     */
    public byte unknownByte1;
    /**
     * Extra bytes sent to add unknownByte2 & unknownFloat
     */
    public boolean extraData = false;
    /**
     * Unkown byte 1
     */
    public byte unknownByte2;
    /**
     * Unkown byte 1
     */
    public float unknownFloat;

    @Override
    public void deserialize(PBuffer buffer) throws Exception {
        bulletId = buffer.readUnsignedShort();
        ownerId = buffer.readInt();
        containerType = buffer.readInt();
        startingPos = new WorldPosData().deserialize(buffer);
        angle = buffer.readFloat();
        damage = buffer.readShort();
        unknownInt = buffer.readInt();
        unknownByte1 = buffer.readByte();
        if(buffer.getRemainingBytes() > 4) {
            extraData = true;
            unknownByte2 = buffer.readByte();
            unknownFloat = buffer.readFloat();
        } else {
            unknownByte2 = 0;
            unknownFloat = 372.0f;
        }
    }

    public String toString(){
        return String.format("%d %d %d %s %f %d (%d %d %d %f)\n", bulletId, ownerId, containerType, startingPos, angle, damage, unknownInt, unknownByte1, unknownByte2, unknownFloat);
    }
}