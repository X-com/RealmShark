package packets.packetcapture.networktap;

import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.packet.TCPPacket;
import packets.packetcapture.PacketProcessor;

import java.io.IOException;

/**
 * A sniffer used to tap packets out of the Windows OS network layer. Before sniffing
 * packets it needs to find what network interface the packets are sent or received from,
 * aka if proxies are used.
 */
public class WindowsSniffer implements Sniffer {
    private String filter = "tcp port 2050";
    private static JpcapCaptor[] captors;
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
     * @throws IOException IO exceptions thrown if unexpected issues are found.
     */
    public void startSniffer() throws IOException {
        stop = false;
        NetworkInterface[] list = JpcapCaptor.getDeviceList();
        captors = new JpcapCaptor[list.length];
        sniffers = new boolean[list.length];
        for (int number = 0; number < list.length; number++) {
            captors[number] = JpcapCaptor.openDevice(list[number], 2000, false, 20);
            try {
                captors[number].setFilter(filter, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (captors[number] != null) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                }
                int finalNumber = number;
                new Thread(new Runnable() {
                    int num = finalNumber;

                    @Override
                    public void run() {
                        captors[num].loopPacket(-1, (packet) -> {
                            if (packet instanceof TCPPacket) {
                                sniffers[num] = true;
                                processor.receivedPackets((TCPPacket) packet);
                            }
                        });
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
                            captors[c].close();
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
        for (JpcapCaptor c : captors) {
            if (c != null) {
                c.close();
            }
        }
    }
}