package packets.packetcapture.sniff;

/**
 * Packet processing class calling the sniffer.
 */
public interface PProcessor {

    /**
     * Reset called when a TCP reset packet is received.
     */
    void reset();

    /**
     * Incoming stream from the TCP payload.
     *
     * @param data TCP packet payload byte data containing the stream.
     */
    void incomingStream(byte[] data);

    /**
     * Outgoing stream from the TCP payload.
     *
     * @param data TCP packet payload byte data containing the stream.
     */
    void outgoingStream(byte[] data);
}
