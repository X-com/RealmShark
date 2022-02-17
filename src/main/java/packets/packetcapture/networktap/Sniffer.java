package packets.packetcapture.networktap;

import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapNativeException;

import java.io.IOException;

/**
 * Interface for different sniffers for different operative systems.
 */
public interface Sniffer {
    /**
     * Method used to start the sniffer.
     *
     * @throws PcapNativeException, NotOpenException Exceptions thrown if errors show up.
     */
    void startSniffer() throws PcapNativeException, NotOpenException;

    /**
     * Close all network interfaces sniffing the wire.
     */
    void closeSniffers();
}
