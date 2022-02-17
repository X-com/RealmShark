package util;

import jpcap.packet.TCPPacket;

import java.util.Arrays;

/**
 * Temp class to sniff out a bug. Ignore all this.
 */
public class HackyPacketLoggerForABug {
    private static int index = 0;
    private static TCPPacket[] logList = new TCPPacket[200];

    public static void logTCPPacket(TCPPacket tcp) {
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
                Util.print(Arrays.toString(logList[i].data));
            }
        }
    }
}
