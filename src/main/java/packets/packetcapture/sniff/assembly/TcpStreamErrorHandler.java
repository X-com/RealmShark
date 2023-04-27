package packets.packetcapture.sniff.assembly;

import packets.packetcapture.sniff.netpackets.RawPacket;
import packets.packetcapture.sniff.netpackets.TcpPacket;

import java.util.Arrays;

public class TcpStreamErrorHandler {
    public static TcpStreamErrorHandler INSTANCE = new TcpStreamErrorHandler();
    private static int index = 0;
    private static int size = 500;
    private static RawPacket[] logList = new RawPacket[size];
    private static PErrorMessage errorMessage;
    private static PErrorStop errorStop;

    /**
     * Raw packet logger for dumping error logs.
     *
     * @param tcp Raw TCP packets.
     */
    public void logTCPPacket(RawPacket tcp) {
        logList[index] = tcp;
        index++;
        if (index >= size) index = 0;
    }

    /**
     * TCP raw packet byte dump for error logging.
     *
     * @param error Message of the error.
     */
    public void dumpData(String error) {
        errorMessage(error, error + "\n" + getRawPacketDump());
    }

    /**
     * TCP stream error checker for instances where packets are missing in a TCP stream.
     *
     * @param tcpStreamBuilder TCP packet object to be checked.
     */
    void errorChecker(TcpStreamBuilder tcpStreamBuilder) {
        if (tcpStreamBuilder.packetMap.size() > 95) {
            long index = tcpStreamBuilder.sequenseNumber;
            int counter = 0;
            while (counter < 100000) {
                if (tcpStreamBuilder.packetMap.containsKey(index)) {
                    tcpStreamBuilder.sequenseNumber = index;
                    TcpPacket tempPack = tcpStreamBuilder.packetMap.get(index);
                    String errorMsg = "Packets missing id:" + (tcpStreamBuilder.idNumber - tempPack.getIp4Packet().getIdentification()) + " seq:" + (tcpStreamBuilder.sequenseNumber - tempPack.getSequenceNumber()) + " outgoing:" + (tempPack.getDstPort() == 2050);
                    errorMessage(errorMsg, errorMsg);
                    break;
                }
                index++;
                counter++;
            }
        } else if (tcpStreamBuilder.packetMap.size() >= 100) {
            stop();
            tcpStreamBuilder.reset();
        }
    }

    /**
     * Called when to many packets are missing in a TCP stream.
     */
    private void stop() {
        String errorMsg = "Error! Stream Constructor reached 100 packets. Shutting down.";
        String dump = errorMsg + "\n" + getRawPacketDump();
        errorMessage(errorMsg, dump);
        errorStop();
    }

    /**
     * Creates a string form of the raw packets in the buffer for error dumping into logs.
     *
     * @return String format of raw packets in the buffer.
     */
    private String getRawPacketDump() {
        StringBuilder sb = new StringBuilder();
        sb.append("Packet sync error. Dumping packets.\n");
        for (int i = index; i <= (index + size); i++) {
            int j = i % size;
            RawPacket packet = logList[j];
            if (packet != null) {
                sb.append(Arrays.toString(packet.getPayload()));
                sb.append(" ");
                sb.append(j);
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Sets the error message handler.
     *
     * @param errorMessageHandler Lambda method to be used as error message handler.
     */
    public void setErrorMessageHandler(PErrorMessage errorMessageHandler) {
        errorMessage = errorMessageHandler;
    }

    /**
     * Sets the error stop handler.
     *
     * @param errorStopHandler Lambda method to be used as error stop handler.
     */
    public void setErrorStopHandler(PErrorStop errorStopHandler) {
        errorStop = errorStopHandler;
    }

    /**
     * Error message caused by packets missing, if error message handler is set.
     *
     * @param errorMsg  Message of missing packets.
     * @param errorDump Error dump for debugger
     */
    public void errorMessage(String errorMsg, String errorDump) {
        if (errorMessage != null) errorMessage.errorLogs(errorMsg, errorDump);
    }

    /**
     * Triggers after to many packets are missing allowing the sniffer to stop,
     * if error stop handler is set.
     */
    public void errorStop() {
        if (errorStop != null) errorStop.errorStop();
    }
}
