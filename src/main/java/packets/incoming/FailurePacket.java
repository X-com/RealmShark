package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;
import packets.data.enums.FailureCode;

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
    public void deserialize(BufferReader buffer) throws Exception {
        errorId = FailureCode.byOrdinal(buffer.readInt());
        errorDescription = buffer.readString();
    }

    @Override
    public String toString() {
        return "FailurePacket{" +
                "\n   errorId=" + errorId +
                "\n   errorDescription=" + errorDescription;
    }
}