package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Received in response to a `ChooseNamePacket`
 */
public class NameResultPacket extends Packet {
    /**
     * Whether or not the name change was successful
     */
    public boolean success;
    /**
     * The error which occurred, if the result was not successful
     */
    public String errorText;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        success = buffer.readBoolean();
        errorText = buffer.readString();
    }
}