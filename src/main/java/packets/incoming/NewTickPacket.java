package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;
import packets.buffer.data.ObjectStatusData;

/**
 * Received to notify the player of a new game tick
 */
public class NewTickPacket extends Packet {
    /**
     * The id of the tick
     */
    public int tickId;
    /**
     * The time between the last tick and this tick, in milliseconds
     */
    public int tickTime;
    /**
     * Server realtime in ms
     */
    public long serverRealTimeMS;
    /**
     * Last server realtime in ms
     */
    public int serverLastTimeRTTMS;
    /**
     * An array of statuses for objects which are currently visible to the player
     */
    public ObjectStatusData[] status;

    @Override
    public void deserialize(PBuffer buffer) throws Exception {
        tickId = buffer.readInt();
        tickTime = buffer.readInt();
        serverRealTimeMS = buffer.readUnsignedInt();
        serverLastTimeRTTMS = buffer.readUnsignedShort();
        status = new ObjectStatusData[buffer.readShort()];
        for (int i = 0; i < status.length; i++) {
            status[i] = new ObjectStatusData().deserialize(buffer);
        }
    }
}