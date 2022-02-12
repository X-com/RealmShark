package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;
import packets.buffer.data.WorldPosData;

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
     * Unknown
     */
    public int unknownInt;
    /**
     *
     */


    @Override
    public void deserialize(PBuffer buffer) throws Exception {
        bulletId = buffer.readUnsignedShort();
        ownerId = buffer.readInt();
        containerType = buffer.readInt();
        startingPos = new WorldPosData().deserialize(buffer);
        angle = buffer.readFloat();
        damage = buffer.readShort();
        unknownInt = buffer.readInt();

        switch ()

//        public short GELANIFLOON; // 0x10
//        public int BHBLILNECJL; // 0x14
//        public int IPDJPBMBAOC; // 0x18
//        public float NJCBCFBLAAB; // 0x1C
//        public short BKEILPIFPMB; // 0x20
//        public OHPIBKLOFIN CBLBGAHHEHF; // 0x28
//        public int KKIKDIHKKPE; // 0x30
//        public byte BPLFFFJLNFF; // 0x34
//        public byte OKLLNCONDHI; // 0x35
//        public float IICAHAHELEF; // 0x38
    }

    public String toString(){
        return String.format("%d %d %d %s %f %d\n", bulletId, ownerId, containerType, startingPos, angle, damage);
    }
}