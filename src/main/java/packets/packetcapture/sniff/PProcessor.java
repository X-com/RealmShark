package packets.packetcapture.sniff;

/**
 * Packet processing class calling the sniffer.
 */
public interface PProcessor {

    /**
     * Reset called when a TCP reset packet is received on incoming packets.
     */
    void resetIncoming();

    /**
     * Reset called when a TCP reset packet is received on outgoing packets.
     */
    void resetOutgoing();

    /**
     * Incoming stream from the TCP payload.
     *
     * @param data TCP packet payload byte data containing the stream.
     * @param srcAddr
     */
    void incomingStream(byte[] data, byte[] srcAddr);

    /**
     * Outgoing stream from the TCP payload.
     *
     * @param data TCP packet payload byte data containing the stream.
     */
    void outgoingStream(byte[] data);
}
