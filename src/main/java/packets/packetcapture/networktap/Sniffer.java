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
                    }
                };
                try {
                    handlers[num].loop(-1, listener);
                } catch (PcapNativeException | InterruptedException | NotOpenException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Verify checksum of TCP packets. This does however not checksum the Ip4Header
     * given only the data of the TCP packet is vital. Not the header daita.
     *
     * @param bytes Raw bytes of the packet being received.
     * @return true if the checksum is similar to the TCP checksum sent in the packet.
     */
    private static boolean computeChecksum(byte[] bytes) {
        long sum = 0;
        long checksumTCP = (Byte.toUnsignedInt(bytes[51]) + (Byte.toUnsignedInt(bytes[50]) << 8));
        int tcpLen = (Byte.toUnsignedInt(bytes[17]) + (Byte.toUnsignedInt(bytes[16]) << 8)) - ((bytes[14] & 15) * 4);
        for (int i = 26; i < 34; i += 2) {
            sum += (Byte.toUnsignedInt(bytes[i + 1]) + (Byte.toUnsignedInt(bytes[i]) << 8));
        }
        sum += 6;
        sum += tcpLen;
        for (int i = 34; i < tcpLen + 33; i += 2) {
            if (i == 50) continue;
            sum += (Byte.toUnsignedInt(bytes[i + 1]) + (Byte.toUnsignedInt(bytes[i]) << 8));
        }
        if ((tcpLen & 1) == 1) {
            sum += (Byte.toUnsignedInt(bytes[bytes.length - 1]) << 8) & 0xFF00;
        }
        while ((sum >> 16) != 0) {
            sum = (sum & 0xffff) + (sum >> 16);
        }
        sum = ~sum;
        sum = sum & 0xFFFF;
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