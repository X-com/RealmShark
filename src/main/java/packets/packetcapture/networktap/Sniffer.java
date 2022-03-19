package packets.packetcapture.networktap;

import packets.packetcapture.PacketProcessor;
import packets.packetcapture.networktap.ardikars.NativeBridge;
import packets.packetcapture.networktap.ardikars.NativeMappings;
import packets.packetcapture.networktap.netpackets.EthernetPacket;
import packets.packetcapture.networktap.netpackets.Ip4Packet;
import packets.packetcapture.networktap.netpackets.TcpPacket;
import pcap.spi.Interface;
import pcap.spi.Pcap;
import pcap.spi.Service;
import pcap.spi.exception.ErrorException;
import pcap.spi.exception.error.*;
import pcap.spi.option.DefaultLiveOptions;
import util.HackyPacketLoggerForABug;

import java.util.Arrays;
import java.util.Iterator;

import static util.PacketCruncher.getByteArray;

/**
 * A sniffer used to tap packets out of the Windows OS network layer. Before sniffing
 * packets it needs to find what network interface the packets are sent or received from,
 * aka if proxies are used.
 */
public class Sniffer {
    private static boolean disableChecksum = true;
    private String filter = "tcp port 2050";
    private Pcap[] pcaps;
    private Pcap realmPcap;
    private PacketProcessor processor;
    private boolean stop;
    private Sniffer thisObject;
    private RingBuffer<TcpPacket> ringBuffer;

    /**
     * Constructor of a Windows sniffer.
     *
     * @param p Object of parent class calling the sniffer.
     */
    public Sniffer(PacketProcessor p) {
        processor = p;
        thisObject = this;
        ringBuffer = new RingBuffer(32);
    }

    /**
     * Main sniffer method to listen on the network tap for any packets filtered by port
     * 2050 (default port rotmg uses) and TCP packets only (the packet type rotmg uses).
     * All network interfaces are listen to given some users might have multiple. A thread
     * is created to listen to all interfaces until any packet of the correct type (port
     * 2050 of type TCP) is found. The all other channels are halted and only the correct
     * interface is listened on.
     *
     * @throws If any unexpected issues are found.
     */
    public void startSniffer() throws ErrorException, RadioFrequencyModeNotSupportedException,
            ActivatedException, InterfaceNotSupportTimestampTypeException,
            PromiscuousModePermissionDeniedException, InterfaceNotUpException,
            PermissionDeniedException, NoSuchDeviceException,
            TimestampPrecisionNotSupportedException {
        Service service = Service.Creator.create("PcapService");
        Interface[] interfaceList = NativeBridge.getInterfaces(service);
        pcaps = new Pcap[interfaceList.length];
        realmPcap = null;
        stop = false;

        for (int i = 0; i < interfaceList.length; i++) {
            DefaultLiveOptions defaultLiveOptions = new DefaultLiveOptions();
            defaultLiveOptions.timeout(60000);

            Pcap pcap = service.live(interfaceList[i], defaultLiveOptions);
            pcap.setFilter(filter, true);

            pcaps[i] = pcap;

            startPacketSniffer(pcap);
        }

        closeUnusedSniffers();
        processBufferedPackets();
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
    public void startPacketSniffer(Pcap pcap) {
        new Thread(new Runnable() {
            final Pcap p = pcap;

            @Override
            public void run() {
                NativeBridge.PacketListener listener = packet -> {
                    EthernetPacket ethernetPacket = packet.getNewEthernetPacket();
                    if (ethernetPacket != null) {
                        Ip4Packet ip4Packet = ethernetPacket.getNewIp4Packet();
                        if (ip4Packet != null) {
                            TcpPacket tcpPacket = ip4Packet.getNewTcpPacket();

                            HackyPacketLoggerForABug.logTCPPacket(packet);

                            if (tcpPacket != null && computeChecksum(packet.getPayload())) {
                                ringBuffer.push(tcpPacket);
                                realmPcap = pcap;
                                synchronized (thisObject) {
                                    thisObject.notifyAll();
                                }
                            }
                        }
                    }
                };
                NativeBridge.loop(p, -1, listener);
            }
        }).start();
        pause(1);
    }

    /**
     * Close threads of sniffer network interfaces not being used after
     * capturing at least one realm packet in the correct net-interface.
     */
    private void closeUnusedSniffers() {
        try {
            synchronized (thisObject) {
                thisObject.wait();
            }
            while (!stop) {
                for (int s = 0; s < pcaps.length; s++) {
                    if (realmPcap != null) {
                        for (int c = 0; c < pcaps.length; c++) {
                            if (s != c) {
                                pcaps[c].close();
                            }
                        }
                        return;
                    }
                }
                pause(100);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Processing waits until new packets are captured by the sniffer, wakes
     * up and processes the buffered packets in the ring buffer and goes
     * back to sleep.
     */
    private void processBufferedPackets() {
        try {
            while (!stop) {
                synchronized (thisObject) {
                    thisObject.wait();
                }
                while (!ringBuffer.isEmpty()) {
                    TcpPacket tcpPacket = ringBuffer.pop();
                    processor.receivedPackets(tcpPacket);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
     * Close all network interfaces sniffing the wire.
     */
    public void closeSniffers() {
        stop = true;
        if (realmPcap != null) {
            realmPcap.close();
        } else {
            for (Pcap c : pcaps) {
                if (c != null) {
                    c.close();
                }
            }
        }
    }
}