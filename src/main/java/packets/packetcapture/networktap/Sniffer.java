package packets.packetcapture.networktap;

import org.pcap4j.core.*;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import org.pcap4j.packet.TcpPacket;
import packets.packetcapture.PacketProcessor;

/**
 * A sniffer used to tap packets out of the Windows OS network layer. Before sniffing
 * packets it needs to find what network interface the packets are sent or received from,
 * aka if proxies are used.
 */
public class Sniffer {
    private static boolean disableChecksum = true;
    private String filter = "tcp port 2050";
    private PcapHandle[] handlers;
    private PacketProcessor processor;
    private boolean[] sniffers;
    private boolean stop;

    /**
     * Constructor of a Windows sniffer.
     *
     * @param p Object of parent class calling the sniffer.
     */
    public Sniffer(Object p) {
        processor = (PacketProcessor) p;
    }

    /**
     * Main sniffer method to listen on the network tap for any packets filtered by port
     * 2050 (default port rotmg uses) and TCP packets only (the packet type rotmg uses).
     * All network interfaces are listen to given some users might have multiple. A thread
     * is created to listen to all interfaces until any packet of the correct type (port
     * 2050 of type TCP) is found. The all other channels are halted and only the correct
     * interface is listened on.
     *
     * @throws PcapNativeException or NotOpenException are thrown if unexpected issues are found.
     */
    public void startSniffer() throws PcapNativeException, NotOpenException {
        stop = false;
        PcapNetworkInterface[] list = Pcaps.findAllDevs().toArray(new PcapNetworkInterface[0]);
        handlers = new PcapHandle[list.length];
        sniffers = new boolean[list.length];

        for (int number = 0; number < list.length; number++) {
            int snapshotLength = 65536; // in bytes
            int readTimeout = 4000000; // in milliseconds, set to hour long to never ignore packets
            handlers[number] = list[number].openLive(snapshotLength, PromiscuousMode.PROMISCUOUS, readTimeout);
            handlers[number].setFilter(filter, BpfProgram.BpfCompileMode.OPTIMIZE);

            if (handlers[number] != null) {
                pause(1);
                startPacketSniffer(number);
            }
        }

        closeUnusedSniffers();
    }

    /**
     * Small pauses for async to finish tasks.
     *
     * @param ms Millisecond of pause
     */
    private static void pause(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
        }
    }

    /**
     * Start a packet sniffers on different threads
     * and close any sniffer not being used.
     *
     * @param number Index of the network interface.
     */
    public void startPacketSniffer(int number) {
        new Thread(new Runnable() {
            final int num = number;

            @Override
            public void run() {
                PacketListener listener = packet -> {
                    TcpPacket tcpPacket = packet.get(TcpPacket.class);

                    if (tcpPacket != null && computeChecksum(packet.getRawData())) {
                        processor.receivedPackets(tcpPacket);
                        sniffers[num] = true;
                    }
                };
                try {
                    handlers[num].loop(-1, listener);
                } catch (PcapNativeException | InterruptedException | NotOpenException e) {
                }
            }
        }).start();
    }

    /**
     * Verify checksum of TCP packets. This does however not checksum the Ip4Header
     * given only the data of the TCP packet is vital. Not the header data.
     * <p>
     * WARNING! Don't use this checksum given the router handles checksums. Filtering packets
     * with checksum results in packets being lost. Even if the checksum fails the packets
     * pass the RC4 cipher meaning the packets are fine, even if the checksum miss matches.
     *
     * @param bytes Raw bytes of the packet being received.
     * @return true if the checksum is similar to the TCP checksum sent in the packet.
     * <p>
     * TODO: fix checksum not messing with the system.
     */
    private static boolean computeChecksum(byte[] bytes) {
        if (disableChecksum) return true;
        int tcpLen = (Byte.toUnsignedInt(bytes[17]) + (Byte.toUnsignedInt(bytes[16]) << 8)) - ((bytes[14] & 15) * 4);
        int sum = 6 + tcpLen; // add tcp num + length of tcp

        for (int i = 26; i < tcpLen + 33; i += 2) { // compute all byte pairs starting from ip dest/src to end of tcp payload
            if (i == 50) continue; // skip the TCP checksum values at address 50 & 51
            sum += (Byte.toUnsignedInt(bytes[i + 1]) + (Byte.toUnsignedInt(bytes[i]) << 8));
        }

        if ((tcpLen & 1) == 1) // add the last odd pair as if the whole packet had a zero byte added to the end
            sum += (Byte.toUnsignedInt(bytes[bytes.length - 1]) << 8);

        while ((sum >> 16) != 0) // one compliment
            sum = (sum & 0xFFFF) + (sum >> 16);

        sum = ~sum; // invert bits
        sum = sum & 0xFFFF; // remove upper bits

        int checksumTCP = (Byte.toUnsignedInt(bytes[51]) + (Byte.toUnsignedInt(bytes[50]) << 8));
        if (checksumTCP == 0xFFFF) checksumTCP = 0; // get checksum from tcp packet and set to 0 if value is FFFF,
        //                                                                              FFFF is impossible to have.

        return checksumTCP == sum;
    }

    /**
     * Close threads of sniffer channels not being used.
     */
    private void closeUnusedSniffers() {
        pause(100);
        while (true) {
            for (int s = 0; s < sniffers.length; s++) {
                if (stop) {
                    return;
                } else if (sniffers[s]) {
                    for (int c = 0; c < sniffers.length; c++) {
                        if (s != c) {
                            handlers[c].close();
                        }
                    }
                    return;
                }
            }
            pause(100);
        }
    }

    /**
     * Close all network interfaces sniffing the wire.
     */
    public void closeSniffers() {
        stop = true;
        for (PcapHandle c : handlers) {
            if (c != null) {
                try {
                    if (c.isOpen()) c.breakLoop();
                } catch (NotOpenException e) {
                }
                c.close();
            }
        }
    }
}