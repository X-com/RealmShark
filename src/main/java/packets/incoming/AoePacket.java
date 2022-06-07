package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;
import packets.data.WorldPosData;

/**
 * Received when an AoE grenade has hit the ground.
 */
public class AoePacket extends Packet {
    /**
     * The position which the grenade landed at.
     */
    public WorldPosData pos;
    /**
     * The radius of the grenades area of effect, in game tiles.
     */
    public float radius;
    /**
     * The damage dealt by the grenade.
     */
    public int damage;
    /**
     * The condition effect applied by the grenade.
     */
    public int effect;
    /**
     * The duration of the effect applied.
     *
     * @see `AoePacket.effect`.
     */
    public float duration;
    /**
     * > Unknown.
     */
    public int origType;
    /**
     * The color of the grenade's explosion particles.
     * > The encoding of the color is unknown.
     */
    public int color;
    /**
     * Whether or not the damage of this grenade pierces armor.
     */
    public boolean armorPiercing;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        pos = new WorldPosData().deserialize(buffer);
        radius = buffer.readFloat();
        damage = buffer.readUnsignedShort();
        effect = buffer.readUnsignedByte();
        duration = buffer.readFloat();
        origType = buffer.readUnsignedShort();
        color = buffer.readInt();
        armorPiercing = buffer.readBoolean();
    }

    @Override
    public String toString() {
        return "AoePacket{" +
                "\n   pos=" + pos +
                "\n   radius=" + radius +
                "\n   damage=" + damage +
                "\n   effect=" + effect +
                "\n   duration=" + duration +
                "\n   origType=" + origType +
                "\n   color=" + color +
                "\n   armorPiercing=" + armorPiercing;
    }
}