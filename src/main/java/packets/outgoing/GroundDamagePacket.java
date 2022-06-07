package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;
import packets.data.WorldPosData;

/**
 * Sent when the client takes damage from a ground source, such as lava.
 */
public class GroundDamagePacket extends Packet {
    /**
     * The current client time.
     */
    public int time;
    /**
     * The current client position.
     */
    public WorldPosData position;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        time = buffer.readInt();
        position = new WorldPosData().deserialize(buffer);
    }

    @Override
    public String toString() {
        return "GroundDamagePacket{" +
                "\n   time=" + time +
                "\n   position=" + position;
    }
}
