package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Received to tell the player about damage done to other players and enemies.
 */
public class DamagePacket extends Packet {
    /**
     * The object id of the entity receiving the damage.
     */
    public int targetId;
    /**
     * An array of status effects which were applied with the damage.
     */
    public int[] effects;
    /**
     * The amount of damage taken.
     */
    public int damageAmount;
    /**
     * Whether or not the damage resulted in killing the entity.
     */
    public boolean kill;
    /**
     * Whether or not the damage was armor piercing.
     */
    public boolean armorPierce;
    /**
     * The id of the bullet which caused the damage.
     */
    public int bulletId;
    /**
     * The object id of the entity which owned the bullet that caused the damage.
     */
    public int objectId;

    @Override
    public void deserialize(PBuffer buffer) {
        targetId = buffer.readInt();
        effects = new int[buffer.readUnsignedByte()];
        for (int i = 0; i < effects.length; i++) {
            effects[i] = buffer.readUnsignedByte();
        }
        damageAmount = buffer.readUnsignedShort();
        kill = buffer.readBoolean();
        armorPierce = buffer.readBoolean();
        bulletId = buffer.readUnsignedByte();
        objectId = buffer.readInt();
    }
}