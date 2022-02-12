package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;
import packets.buffer.data.enums.FailureCode;

/**
 * Received when an error has occurred
 */
public class FailurePacket extends Packet {
    /**
     * The error ID code of the failure
     */
    public FailureCode errorId;
    /**
     * A description of the error
     */
    public String errorDescription;

    @Override
    public void deserialize(PBuffer buffer) throws Exception {
        errorId = FailureCode.byOrdinal(buffer.readInt());
        errorDescription = buffer.readString();
    }
}