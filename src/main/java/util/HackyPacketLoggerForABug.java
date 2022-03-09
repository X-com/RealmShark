package util;

import packets.packetcapture.networktap.pcap4j.EthernetPacket;

import java.util.Arrays;

/**
 * Temp class to sniff out a bug. Ignore all this.
 */
public class HackyPacketLoggerForABug {
    private static int index = 0;
    private static int size = 300;
    private static EthernetPacket[][] logList = new EthernetPacket[size][2];
    private static int type = 0;

    public static void logTCPPacket(EthernetPacket tcp, int t) {
        type = t;
        logList[index][type] = tcp;
        index++;
        if (index >= size) index = 0;
    }

    public static void dumpData() {
        Util.print("Packet sync error. Dumping packets.");
        for (int i = index; i <= (index + size); i++) {
            int j = i%size;
            EthernetPacket packet = logList[j][type];
            if (packet != null) {
                Util.print(Arrays.toString(packet.getRawData()) + " " + j);
            }
        }
    }
}
