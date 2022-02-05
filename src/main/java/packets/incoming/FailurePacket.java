package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Received when an error has occurred
 */
public class FailurePacket extends Packet {
    /**
     * The error ID code of the failure
     */
    public int errorId;
    /**
     * A description of the error
     */
    public String errorDescription;
    /**
     * The place where the error occurred
     */
    public String errorPlace;
    /**
     * The ID of the connection in which the error occurred
     */
    public String errorConnectionId;

    @Override
    public void deserialize(PBuffer buffer) {
        errorId = buffer.readInt();
        errorDescription = buffer.readString();
        errorPlace = buffer.readString();
        errorConnectionId = buffer.readString();
    }
}
