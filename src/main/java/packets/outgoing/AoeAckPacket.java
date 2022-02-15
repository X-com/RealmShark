package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;
import packets.data.WorldPosData;

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
    public void deserialize(BufferReader buffer) throws Exception {
        time = buffer.readInt();
        position = new WorldPosData().deserialize(buffer);
    }
}
