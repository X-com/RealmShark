package packets.packetcapture.sniff.assembly;

/**
 * Reset interface used in stream constructor for sending reset updates when
 * receiving reset packets.
 */
public interface PReset {

    /**
     * Method called when a reset packet is received.
     */
    void reset();
}
