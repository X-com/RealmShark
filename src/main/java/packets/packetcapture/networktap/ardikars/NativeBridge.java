package packets.packetcapture.networktap.ardikars;

import com.sun.jna.Pointer;
import packets.packetcapture.networktap.pcap4j.EthernetPacket;
import packets.packetcapture.networktap.pcap4j.TcpPacket;
import pcap.spi.Interface;
import pcap.spi.Pcap;
import pcap.spi.Service;
import pcap.spi.exception.ErrorException;
import pcap.spi.option.DefaultLiveOptions;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Bridge class to hook directly into native methods instead of using
 * preset methods used by the ardikars library.
 */
public class NativeBridge {

    /**
     * for testing
     */
    public static void main(String[] args) {
        System.out.println("clearconsole");
        Pcap pcap;
        try {
            Service service = Service.Creator.create("PcapService");
            Interface i = service.interfaces();
            pcap = service.live(i, new DefaultLiveOptions());
            pcap.setFilter("tcp port 2050", true);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        PacketListener listener = packet -> {
            try {
                EthernetPacket e = EthernetPacket.newPacket(packet.getRawData(), 0, packet.getRawData().length, null);
                TcpPacket tcpPacket = e.get(TcpPacket.class);
                System.out.println(tcpPacket);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        try {
            loop(pcap, 1, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The main looping function on the network tap.
     * This is method calls the wrapper method that
     * in turn starts the packet sniffing.
     *
     * @param pcap        Packet capture class wrapping the interface for sniffing the wire.
     * @param packetCount Number of packets to listen to. -1 loops infinitely.
     * @param listener    Lambda abstract interface used when packets are captured.
     */
    public static void loop(Pcap pcap, int packetCount, PacketListener listener) {
        try {
            Field field = pcap.getClass().getDeclaredField("pointer");
            field.setAccessible(true);
            Pointer p = (Pointer) field.get(pcap);

            NativeMappings.pcap_loop(p, packetCount, new GotPacketFuncExecutor(listener, SimpleExecutor.getInstance()), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a list of all interfaces on the device.
     *
     * @param service Service object used to grab the list of interfaces.
     * @return List of all interfaces on the device.
     * @throws ErrorException Error when attempting to grab interfaces.
     */
    public static Interface[] getInterfaces(Service service) throws ErrorException {
        List<Interface> list = new ArrayList<>();
        Interface i = service.interfaces();
        while (i != null) {
            list.add(i);
            i = i.next();
        }
        return list.toArray(new Interface[0]);
    }

    /**
     * Interface class for responding to captured packets.
     */
    public interface PacketListener {
        void gotPacket(EthernetPacket packet);
    }

    /**
     * Thread executor when packets are captured.
     */
    public static final class SimpleExecutor implements Executor {

        private SimpleExecutor() {
        }

        private static final SimpleExecutor INSTANCE = new SimpleExecutor();

        public static SimpleExecutor getInstance() {
            return INSTANCE;
        }

        @Override
        public void execute(Runnable command) {
            command.run();
        }
    }

    /**
     * Interface class for unwrapping captured packets from native
     * pointers to useful byte arrays and timestamps.
     */
    private static final class GotPacketFuncExecutor implements NativeMappings.pcap_handler {
        private final PacketListener listener;
        private final Executor executor;
        private final int timestampPrecision = 1;

        public GotPacketFuncExecutor(PacketListener listener, Executor executor) {
            this.listener = listener;
            this.executor = executor;
        }

        @Override
        public void got_packet(Pointer args, Pointer header, final Pointer packet) {
            final Instant ts = buildTimestamp(header);
            final int len = NativeMappings.pcap_pkthdr.getLen(header);
            final byte[] ba = packet.getByteArray(0, NativeMappings.pcap_pkthdr.getCaplen(header));

            try {
                executor.execute(() -> {
                    listener.gotPacket(EthernetPacket.newPacket(ba, 0, len, ts));
                });
            } catch (Throwable e) {
            }
        }

        private Instant buildTimestamp(Pointer header) {
            long epochSecond = NativeMappings.pcap_pkthdr.getTvSec(header).longValue();
            switch (timestampPrecision) {
                case 0:
                    return Instant.ofEpochSecond(epochSecond, NativeMappings.pcap_pkthdr.getTvUsec(header).intValue() * 1000);
                case 1:
                    return Instant.ofEpochSecond(epochSecond, NativeMappings.pcap_pkthdr.getTvUsec(header).intValue());
                default:
                    throw new AssertionError("Never get here.");
            }
        }
    }
}
