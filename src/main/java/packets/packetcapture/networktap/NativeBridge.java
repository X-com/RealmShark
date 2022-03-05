package packets.packetcapture.networktap;

import com.sun.jna.Pointer;
import packets.packetcapture.networktap.pcap4j.*;
import pcap.spi.Interface;
import pcap.spi.Pcap;
import pcap.spi.Service;
import pcap.spi.option.DefaultLiveOptions;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.concurrent.Executor;

public class NativeBridge {

    public static void main(String[] args) {
        System.out.println("clearconsole");
//        String s = "56, 44, 74, 116, 11, -119, 4, -110, 38, -59, -15, -116, 8, 0, 69, 0, 0, 61, 52, -93, 64, 0, -9, 6, 106, -110, 54, -21, -21, -116, -64, -88, 1, 101, 8, 2, -28, -94, -83, 5, 36, -126, -114, 109, 81, 99, 80, 24, 1, -26, 61, 3, 0, 0, 0, 0, 0, 21, 9, 20, 123, -116, -27, 83, -110, -9, -89, 86, 84, 102, 10, -17, 31, -98, -53";
//        byte[] data = getByteArray(s);
//        try {
//            EthernetPacket packet = EthernetPacket.newPacket(data, 0, data.length);
//            for (Iterator<packets.packetcapture.networktap.pcap4j.Packet> it = packet.iterator(); it.hasNext(); ) {
//                Packet p = (Packet) it.next();
//                System.out.println(it.next().getClass());
//            }
//            TcpPacket tcp = packet.get(TcpPacket.class);
//        } catch (IllegalRawDataException e) {
//            e.printStackTrace();
//        }
//        if (true) return;
        System.out.println("start");
        PacketListener listener = packet -> {

            try {
//                EthernetPacket ethernetPacket = EthernetPacket.newPacket(data, 0, data.length);
//                System.out.println(packet);
                TcpPacket tcpPacket = packet.get(TcpPacket.class);
                System.out.println(tcpPacket);
                if(tcpPacket == null) {
                    System.out.println("--------------------------");
                    return;
                }
//                System.out.println(Arrays.toString(data));
            } catch (Exception e) {
                e.printStackTrace();
            }

//            if (tcpPacket != null && computeChecksum(packet.getRawData())) {
//            }
        };
        try {
            loop(-1, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loop(int packetCount, PacketListener listener) {
        startLoop(packetCount, listener, SimpleExecutor.getInstance());
    }

    private static void startLoop(int packetCount, PacketListener listener, Executor executor) {
        try {
            Service service = Service.Creator.create("PcapService");
            Interface i = service.interfaces();
            for(Interface j : service.interfaces()) {
                System.out.println(j);
            }
            Pcap pcap = service.live(i, new DefaultLiveOptions());
//            pcap.setFilter("tcp port 2050", true);
            pcap.setFilter("tcp", true);

            Field field = pcap.getClass().getDeclaredField("pointer");
            field.setAccessible(true);

            NativeMappings.pcap_loop((Pointer) field.get(pcap), packetCount, new GotPacketFuncExecutor(listener, executor), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    public interface PacketListener {
        public void gotPacket(EthernetPacket packet);
    }

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
//                    new PcapPacket(ba, dlt, ts, len);
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
