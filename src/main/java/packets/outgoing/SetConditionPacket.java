package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Sent when the player inflicts a condition effect.
 */
public class SetConditionPacket extends Packet {
    /**
     * The condition effect being conflicted.
     */
    public byte conditionEffect;
    /**
     * The duration of the conditin effect.
     */
    public float conditionDuration;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        conditionEffect = buffer.readByte();
        conditionDuration = buffer.readFloat();
    }

    @Override
    public String toString() {
        return "SetConditionPacket{" +
                "\n   conditionEffect=" + conditionEffect +
                "\n   conditionDuration=" + conditionDuration;
    }
}
