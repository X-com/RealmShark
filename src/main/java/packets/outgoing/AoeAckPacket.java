package packets.outgoing;

import packets.Packet;
import packets.buffer.PBuffer;
import data.WorldPosData;

/**
 * Sent to acknowledge an `AoePacket`.
 */
public class AoeAckPacket extends Packet {
    /**
     * The current client time.
     */
    public int time;
    /**
     * The position of the AoE which this packet is acknowledging.
     */
    public WorldPosData position;

    @Override
    public void deserialize(PBuffer buffer) throws Exception {
        time = buffer.readInt();
        position = new WorldPosData().deserialize(buffer);
    }
}
