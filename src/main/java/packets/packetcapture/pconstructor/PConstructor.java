package packets.packetcapture.pconstructor;

import org.pcap4j.packet.TcpPacket;

/**
 * Packet constructor interface used in constructing raw TCP packets.
 */
public interface PConstructor {

    /**
     * Build method for constructing readable TCP packets.
     *
     * @param packet Raw TCP packet needed to be constructed into a readable packet.
     */
    void build(TcpPacket packet);

    /**
     * Start resets when starting the packet reading.
     */
    void startResets();
}
