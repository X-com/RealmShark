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
    PacketProcessor processor;

    String filter = "tcp port 2050";

    /**
     * Constructor of a Windows sniffer.
     *
     * @param p Object of parent class calling the sniffer.
     */
    public WindowsSniffer(Object p) {
        processor = (PacketProcessor) p;
    }

    private boolean[] sniffers;

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
        NetworkInterface[] list = JpcapCaptor.getDeviceList();
        JpcapCaptor[] captors = new JpcapCaptor[list.length];
        sniffers = new boolean[list.length];
        for (final int[] number = {0}; number[0] < list.length; number[0]++) {
            captors[number[0]] = JpcapCaptor.openDevice(list[number[0]], 2000, false, 20);
            try {
                captors[number[0]].setFilter(filter, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (captors[number[0]] != null) {
                new Thread(new Runnable() {
                    int num = number[0];

                    @Override
                    public void run() {
                        captors[num].loopPacket(-1, (packet) -> {
                            if (packet instanceof TCPPacket) {
                                processor.receivedPackets((TCPPacket) packet);
                            }
                        });
                    }
                }).start();
            }
        }
        while (true) {
            for (int s = 0; s < sniffers.length; s++) {
                if (sniffers[s]) {
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
}