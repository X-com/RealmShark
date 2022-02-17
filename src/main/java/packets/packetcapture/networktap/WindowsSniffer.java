package packets.packetcapture.networktap;

import org.pcap4j.core.*;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import org.pcap4j.packet.IllegalRawDataException;
import org.pcap4j.packet.Packet;
import packets.packetcapture.PacketProcessor;
import org.pcap4j.packet.TcpPacket;

import java.util.Arrays;

/**
 * A sniffer used to tap packets out of the Windows OS network layer. Before sniffing
 * packets it needs to find what network interface the packets are sent or received from,
 * aka if proxies are used.
 */
public class WindowsSniffer implements Sniffer {
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
    public WindowsSniffer(Object p) {
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
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                }
                int finalNumber = number;
                new Thread(new Runnable() {
                    int num = finalNumber;

                    @Override
                    public void run() {
                        PacketListener listener = packet -> {
                            Packet p = packet.getPacket();
                            byte[] b = p.getRawData();
                            System.out.println("packets " + Arrays.toString(p.getRawData()));
                            try {
                                System.out.println(TcpPacket.newPacket(b, 1, b.length).getPayload());
                            } catch (IllegalRawDataException e) {
                                e.printStackTrace();
                            }
                            if (p instanceof TcpPacket) {
                                sniffers[num] = true;
                                processor.receivedPackets((TcpPacket) p);
                            }
                        };
                        try {
                            handlers[num].loop(-1, listener);
                        } catch (PcapNativeException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (NotOpenException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }
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
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
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