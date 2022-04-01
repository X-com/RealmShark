package packets.packetcapture.sniff.assembly;

import example.gui.TomatoGUI;
import example.gui.TomatoMenuBar;
import packets.packetcapture.sniff.netpackets.TcpPacket;
import bugfixingtools.HackyPacketLoggerForABug;
import util.Util;

import java.util.HashMap;

/**
 * Stream constructor ordering TCP packets in sequence. Payload is extracted and sent back in its raw form.
 */
public class TcpStreamBuilder {

    HashMap<Long, TcpPacket> packetMap = new HashMap();
    PStream stream;
    PReset packetReset;
    public long sequenseNumber;

    /**
     * Constructor of StreamConstructor which needs a reset class to reset if reset
     * packet is retrieved and a constructor class to send ordered packets to.
     *
     * @param preset  Reset class if a reset packet is retrieved.
     * @param pstream Constructor class to send ordered packets to.
     */
    public TcpStreamBuilder(PReset preset, PStream pstream) {
        packetReset = preset;
        stream = pstream;
    }

    /**
     * Build method for ordering packets according to index used by TCP.
     *
     * @param packet TCP packets needing to be ordered.
     */
    public void streamBuilder(TcpPacket packet) {
        if (packet.isResetBit()) {
            reset();
            return;
        }
        if (sequenseNumber == 0) {
            sequenseNumber = packet.getSequenceNumber();
        }

        packetMap.put(packet.getSequenceNumber(), packet);

        if (packetMap.size() > 50) { // Temp hacky solution until better solution is found. TODO: fix this
            String errorMsg = "Error! Stream Constructor reached 50 packets. Shutting down.";
            Util.print(errorMsg);
            TomatoGUI.appendTextAreaChat(errorMsg);
            TomatoGUI.appendTextAreaKeypop(errorMsg);
            TomatoMenuBar.stopPacketSniffer();
            reset();
            HackyPacketLoggerForABug.dumpData();
        }

        while (packetMap.containsKey(sequenseNumber)) {
            TcpPacket packetSeqed = packetMap.remove(sequenseNumber);
            if (packet.getPayload() != null) {
                sequenseNumber += packetSeqed.getPayloadSize();
                stream.stream(packetSeqed.getPayload());
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
