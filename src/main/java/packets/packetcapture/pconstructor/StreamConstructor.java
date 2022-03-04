package packets.packetcapture.pconstructor;

import example.gui.TomatoGUI;
import example.gui.TomatoMenuBar;
import packets.packetcapture.networktap.pcap4j.TcpPacket;
import util.Util;

import java.util.HashMap;

/**
 * Stream constructor ordering TCP packets in sequence. Packets are sent to the rotmg
 * constructor if they are in sequence.
 */
public class StreamConstructor implements PConstructor {

    HashMap<Integer, TcpPacket> packetMap = new HashMap();
    PConstructor packetConstructor;
    PReset packetReset;
    public int sequenseNumber;

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
    public void build(TcpPacket packet) {
        if (packet.getHeader().getSyn()) {
            reset();
            return;
        }
        if (sequenseNumber == 0) {
            sequenseNumber = packet.getHeader().getSequenceNumber();
        }

        packetMap.put(packet.getHeader().getSequenceNumber(), packet);

        if (packetMap.size() > 50) { // Temp hacky solution until better solution is found. TODO: fix this
            String errorMsg = "Error! Stream Constructor reached 50 packets. Shutting down.";
            Util.print(errorMsg);
            TomatoGUI.appendTextAreaText(errorMsg);
            TomatoMenuBar.stopPacketSniffer();
            reset();
        }

        while (packetMap.containsKey(sequenseNumber)) {
            TcpPacket packetSeqed = packetMap.remove(sequenseNumber);
            if (packet.getPayload() != null) {
                sequenseNumber += packetSeqed.getPayload().length();
                packetConstructor.build(packetSeqed);
            }
        }
    }

    /**
     * Reset method if a reset packet is retrieved.
     */
    public void reset() {
        packetReset.reset();
        packetMap.clear();
        sequenseNumber = 0;
    }
}
