package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;
import packets.data.MoveRecord;
import packets.data.WorldPosData;

import java.util.Arrays;

/**
 * Sent to acknowledge a `NewTickPacket`, and to notify the
 * server of the client's current position and time.
 */
public class MovePacket extends Packet {
    /**
     * The tick id of the `NewTickPacket` which this is acknowledging.
     */
    public int tickId;
    /**
     * The serverRealTimeMS.
     */
    public int time;
    /**
     * The move records of the client.
     * <p>
     * This property can be an empty array.
     */
    public MoveRecord[] records;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        tickId = buffer.readInt();
        time = buffer.readInt();
        records = new MoveRecord[buffer.readShort()];
        for (int i = 0; i < records.length; i++) {
            records[i] = new MoveRecord().deserialize(buffer);
        }
    }

    @Override
    public String toString() {
        return "MovePacket{" +
                "\n   tickId=" + tickId +
                "\n   time=" + time +
                "\n   records=" + Arrays.toString(records);
    }
}
