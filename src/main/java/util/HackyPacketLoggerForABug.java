package util;

import org.pcap4j.packet.TcpPacket;

import java.util.Arrays;

/**
 * Temp class to sniff out a bug. Ignore all this.
 */
public class HackyPacketLoggerForABug {
    private static int index = 0;
    private static TcpPacket[] logList = new TcpPacket[200];

    public static void logTCPPacket(TcpPacket tcp) {
        logList[index] = tcp;
        index++;
        if (index >= 200) index = 0;
    }

    public static void dumpData() {
        Util.print("Packet sync error. Dumping packets.");
        for (int i = index; i <= (index + 200); i++) {
            if (i >= 200) i = 0;
            if (logList[i] != null) {
                Util.print(logList[i].toString());
                Util.print(Arrays.toString(logList[i].getRawData()));
            }
        }
    }
}
