package packets.packetcapture.sniff.assembly;

/**
 * Class handling large errors stopping the sniffer.
 */
public interface PErrorStop {
    /**
     * Called when a large error is detected attempting to stop sniffing packets.
     */
    void errorStop();
}
