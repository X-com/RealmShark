package packets;

import packets.reader.BufferReader;

/**
 * Abstract packet class for all incoming or outgoing packets.
 */
public abstract class Packet {

    private byte[] data;

    public byte[] getPayload() {
        return data;
    }
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * An interface to be used as a class factory for different packet types.
     */
    public interface IPacket {
        public Packet factory();
    }

    /**
     * Deserialize method to deserialize the data for each packet type.
     *
     * @param buffer The data of the packet in a rotmg buffer format.
     */
    public abstract void deserialize(BufferReader buffer) throws Exception;
}
