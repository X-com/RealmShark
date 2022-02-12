package packets.outgoing;

import packets.Packet;
import packets.buffer.PBuffer;
import packets.buffer.data.WorldPosData;

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
    public void deserialize(PBuffer buffer) throws Exception {
        time = buffer.readInt();
        position = new WorldPosData().deserialize(buffer);
    }
}
