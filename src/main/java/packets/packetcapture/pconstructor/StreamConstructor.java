package packets.packetcapture.pconstructor;

import packets.packetcapture.networktap.TCPCustomPacket;

import java.util.HashMap;

/**
 * Stream constructor ordering TCP packets in sequence. Packets are sent to the rotmg
 * constructor if they are in sequence.
 */
public class StreamConstructor implements PConstructor {

    HashMap<Integer, TCPCustomPacket> packetMap = new HashMap();
    PConstructor packetConstructor;
    PReset packetReset;
    public int identifier;

    /**
     * Constructor of StreamConstructor which needs a reset class to reset if reset
     * packet is retrieved and a constructor class to send ordered packets to.
     *
     * @param pr Reset class if a reset packet is retrieved.
     * @param pc Constructor class to send ordered packets to.
     */
    public StreamConstructor(PReset pr, PConstructor pc) {
        packetReset = pr;
        packetConstructor = pc;
    }

    /**
     * No start resets are needed.
     */
    @Override
    public void startResets() {
    }

    /**
     * Build method for ordering packets according to index used by TCP.
     *
     * @param packet TCP packets needing to be ordered.
     */
    @Override
    public void build(TCPCustomPacket packet) {
        int packetIdentifier = packet.getIdentifier();
        if (packetIdentifier == 0) {
            if (packet.length() != 0) {
                throw new IllegalStateException();
            }
            reset();
            return;
        }
        if (identifier == 0) {
            identifier = packetIdentifier;
        } else if (identifier == 65536) {
            identifier = 0;
        }

        packetMap.put(packetIdentifier, packet);
        while (packetMap.containsKey(identifier)) {
            TCPCustomPacket packetSeqed = packetMap.remove(identifier);
            identifier++;
            packetConstructor.build(packetSeqed);
        }
    }

    /**
     * Reset method if a reset packet is retrieved.
     */
    public void reset() {
        packetReset.reset();
        packetMap.clear();
        identifier = 0;
    }
}
