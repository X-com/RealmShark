package packets.outgoing.pets;

import packets.Packet;
import packets.buffer.PBuffer;
import packets.buffer.data.PaymentType;
import packets.buffer.data.PetUpgradeType;
import packets.buffer.data.SlotObjectData;

/**
 * Sent when you are feeding and fusing pets or upgrading your pet yard
 */
public class PetUpgradeRequestPacket extends Packet {
    /**
     * The upgrade transaction type
     */
    public PetUpgradeType petTransType;
    /**
     * The object ID of the first pet
     */
    public int pIdOne;
    /**
     * The object ID of the second pet
     */
    public int pIdTwo;
    /**
     * The owner's object ID
     */
    public int objectId;
    /**
     * The items which will be used to upgrade the pet
     */
    public SlotObjectData[] slotObjects;
    /**
     * The type of currency which will be used to purchase the upgrade
     */
    public PaymentType paymentType;

    @Override
    public void deserialize(PBuffer buffer) {
        petTransType = PetUpgradeType.byOrdinal(buffer.readByte());
        pIdOne = buffer.readInt();
        pIdTwo = buffer.readInt();
        objectId = buffer.readInt();
        paymentType = PaymentType.byOrdinal(buffer.readByte());
        slotObjects = new SlotObjectData[buffer.readShort()];
        for (int i = 0; i < slotObjects.length; i++) {
            slotObjects[i] = new SlotObjectData().deserialize(buffer);
        }
    }

}