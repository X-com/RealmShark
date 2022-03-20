package packets.packetcapture.sniff.assembly;

/**
 * TCP packet stream interface used to send ordered TCP packets with bytes contained in their payload.
 */
public interface PStream {

    /**
     * Ordered TCP packet method to send the byte stream contained in the payload.
     *
     * @param data The stream contained in TCP packet bytes.
     */
    void stream(byte[] data);
}
