package packets.packetcapture.pconstructor;

import jpcap.packet.TCPPacket;

/**
 * Packet constructor interface used in constructing raw TCP packets.
 */
public interface PConstructor {

    /**
     * Build method for constructing readable TCP packets.
     *
     * @param packet Raw TCP packet needed to be constructed into a readable packet.
     */
    void build(TCPPacket packet);

    /**
     * Start resets when starting the packet reading.
     */
    void startResets();
}
