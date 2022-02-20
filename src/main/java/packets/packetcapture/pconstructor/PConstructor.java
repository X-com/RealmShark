package packets.packetcapture.pconstructor;

import packets.packetcapture.networktap.TCPCustomPacket;

/**
 * Packet constructor interface used in constructing raw TCP packets.
 */
public interface PConstructor {

    /**
     * Build method for constructing readable TCP packets.
     *
     * @param packet Raw TCP packet needed to be constructed into a readable packet.
     */
    void build(TCPCustomPacket packet);

    /**
     * Start resets when starting the packet reading.
     */
    void startResets();
}
