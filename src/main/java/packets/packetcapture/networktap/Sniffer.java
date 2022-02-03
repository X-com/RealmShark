package packets.packetcapture.networktap;

import java.io.IOException;

/**
 * Interface for different sniffers for different operative systems.
 */
public interface Sniffer {
    /**
     * Method used to start the sniffer.
     *
     * @throws IOException Exception thrown by unexpected IO errors.
     */
    void startSniffer() throws IOException;
}
