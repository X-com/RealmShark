package packets.packetcapture.networktap;

//import jpcap.JpcapCaptor;
//import jpcap.NetworkInterface;
//import jpcap.packet.TCPPacket;

import org.pcap4j.core.*;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.IllegalRawDataException;
import org.pcap4j.packet.TcpPacket;
import packets.packetcapture.PacketProcessor;
import util.Util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

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
            int readTimeout = 50; // in milliseconds
            handlers[number] = list[number].openLive(snapshotLength, PromiscuousMode.PROMISCUOUS, readTimeout);
            handlers[number].setFilter(filter, BpfProgram.BpfCompileMode.OPTIMIZE);

            if (handlers[number] != null) {
                pause(1);
                startPacketSniffer(number);
            }
        }

//        NetworkInterface ni = JpcapCaptor.getDeviceList()[3];
//        try {
//            JpcapCaptor cap = JpcapCaptor.openDevice(ni, 2000, false, 20);
//            cap.setFilter(filter, true);
//            cap.loopPacket(-1, (packet) -> {
//                if (packet instanceof TCPPacket) {
//                    System.out.println((TCPPacket) packet + " " + packet.data.length+ " " + packet.data.hashCode());
//                }
//            });
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        closeUnusedSniffers();
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
                    processRawPacket(packet, num);
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
     * Raw packet processor to TCP packets.
     *
     * @param packet Raw packet
     * @param num    Index of network interface
     */
    public void processRawPacket(PcapPacket packet, int num) {
        ByteBuffer packetData = ByteBuffer.wrap(packet.getRawData()).order(ByteOrder.BIG_ENDIAN);
        if (packet.getRawData().length > 53) {
            int ethernetType = packetData.getShort(12);
            if (ethernetType == 2048) {
                int protocal = packetData.get(23);
                if (protocal == 6) {
                    createTCPpacket(packet, packetData, num);
                }
            }
        }
    }

    private void createTCPpacket(PcapPacket packet, ByteBuffer packetData, int num) {
        int portSrc = Short.toUnsignedInt(packetData.getShort(34));
        int portDst = Short.toUnsignedInt(packetData.getShort(36));
        int identifier = (0xFFFF & packetData.getShort(18));
        long sequenceNum = Integer.toUnsignedLong(packetData.getInt(38));
        long ackNum = Integer.toUnsignedLong(packetData.getInt(42));
        short codeBits = packetData.getShort(46);
        boolean psh = (codeBits & 0x8) != 0;
        byte[] rawBytes = packet.getRawData();
        int size = rawBytes.length;
        if (portSrc == 2050) { // Ignore all but incoming packets from rotmg servers. Outgoing not implemented.
            byte[] tcpBytes = new byte[0];
            if (size != 60 && (psh || size == 1514)) tcpBytes = Arrays.copyOfRange(packet.getRawData(), 54, size);
            TCPCustomPacket tcpPacket = new TCPCustomPacket(identifier, portSrc, portDst, rawBytes, tcpBytes);

            try {
                EthernetPacket ep = EthernetPacket.newPacket(tcpPacket.getRawData(), 0, tcpPacket.getRawData().length);
                IpV4PacketCustom ip = IpV4PacketCustom.newPacket(ep.getPayload().getRawData(), 0, ep.getPayload().length());
                TcpPacket tcp = TcpPacket.newPacket(ip.getPayload().getRawData(), 0, ip.getPayload().length());
                byte[] pcap4j = (tcp.getPayload() != null ? tcp.getPayload().getRawData() : new byte[0]);
                byte[] medata = tcpPacket.tcpData();
                if (!arrayMatches(pcap4j, medata)) {
                    System.out.println("Missmatch");
                    System.out.println(Util.byteArrayPrint(tcp.getPayload().getRawData()));
                    System.out.println(Util.byteArrayPrint(tcpPacket.tcpData()));
                    System.out.println();
                }
            } catch (IllegalRawDataException e) {
                e.printStackTrace();
            }

            processor.receivedPackets(tcpPacket);
            sniffers[num] = true;
        }
    }

    private void testBytes(TCPCustomPacket tcpPacket) {
        try {
            EthernetPacket ep = EthernetPacket.newPacket(tcpPacket.getRawData(), 0, tcpPacket.getRawData().length);
            IpV4PacketCustom ip = IpV4PacketCustom.newPacket(ep.getPayload().getRawData(), 0, ep.getPayload().length());
            TcpPacket tcp = TcpPacket.newPacket(ip.getPayload().getRawData(), 0, ip.getPayload().length());
            byte[] pcap4j = (tcp.getPayload() != null ? tcp.getPayload().getRawData() : new byte[0]);
            byte[] medata = tcpPacket.tcpData();
            if (!arrayMatches(pcap4j, medata)) {
                System.out.println("Missmatch");
                System.out.println(Util.byteArrayPrint(tcp.getPayload().getRawData()));
                System.out.println(Util.byteArrayPrint(tcpPacket.tcpData()));
            }
        } catch (IllegalRawDataException e) {
            e.printStackTrace();
        }
    }

    private boolean arrayMatches(byte[] pcap4j, byte[] medata) {
        if (pcap4j.length != medata.length) return false;
        for (int i = 0; i < pcap4j.length; i++) {
            if (pcap4j[i] != medata[i]) return false;
        }
        return true;
    }

    /**
     * Close all network interfaces sniffing the wire.
     */
    public void closeSniffers() {
        stop = true;
        for (PcapHandle c : handlers) {
            if (c != null) {
                c.close();
            }
        }
    }
}