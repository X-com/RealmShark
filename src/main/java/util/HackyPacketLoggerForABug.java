package util;

import packets.packetcapture.networktap.pcap4j.EthernetPacket;

import java.util.Arrays;

/**
 * Temp class to sniff out a bug. Ignore all this.
 */
public class HackyPacketLoggerForABug {
    private static int index = 0;
    private static int size = 500;
    private static EthernetPacket[] logList = new EthernetPacket[size];

    public static void logTCPPacket(EthernetPacket tcp) {
        logList[index] = tcp;
        index++;
        if (index >= size) index = 0;
    }

    public static void dumpData() {
        Util.print("Packet sync error. Dumping packets.");
        for (int i = index; i <= (index + size); i++) {
            int j = i%size;
            EthernetPacket packet = logList[j];
            if (packet != null) {
                Util.print(Arrays.toString(packet.getRawData()) + " " + j);
            }
        }
    }
}
