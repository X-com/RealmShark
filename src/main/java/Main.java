import packets.Packet;
import packets.PacketType;
import packets.buffer.PBuffer;
import packets.incoming.TextPacket;
import packets.packetcapture.PacketProcessor;
import packets.packetcapture.register.Register;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * This is an API used to unwrapped Realm of the Mad Gods packets. The
 * Packets are grabbed directly from the network tap using a sniffer.
 * It is not possible to modify, block or create packets to be sent.
 * <p>
 * The register should be used to sign up for packets. If said packet is
 * received then the lambda function passed in as the second argument can
 * be used to trigger any functions listening to registered packets.
 */
public class Main {

    // TODO: Add GUI
    public static void main(String[] args) {
        /*
            Subscribe for any packet wanting to be monitored. Use a lambda in
            the second argument for the action when registered packet is received.

            Example for subscribing for all packet types:
            Register.INSTANCE.register(Packet.class, System.out::println);

            Example 2: Subscribing to ping packets:
         */
//        Register.INSTANCE.register(PacketType.TEXT.getPacketClass(), (packet) -> text(packet));

        new PacketProcessor().run();

//        byte[] listB = new byte[listS.length];
//
//        for(int i = 0; i < listS.length; i++){
//            byte b = (byte)(listS[i] < 128 ? listS[i] : (listS[i]-256));
//            listB[i] = b;
//        }
//        byte[] listB = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 15, 0, 1, 3, 31, -93, -63, 6, 67, 5, -80, 119, 67, 10, -101, 63, 30, 56, 1, 65, 3, -102, 4, 65, 30, 57, 65, 0, -79, 14, 65, 1, -79, 14, 65, 2, -92, 1, 65, 7, 20, 65, 8, -108, 9, 65, 9, -104, -126, 1, 65, 10, -73, -62, 3, 65, 11, -72, -62, 3, 65, 29, 0, 65, 31, 0, 9, 77, 97, 116, 122, 71, 109, 110, 103, 114, 0, 32, -74, 73, 65, 33, -83, 77, 65, 39, -75, 1, 65, 57, -97, -117, 4, 65, 59, 65, 65, 62, 0, 12, 68, 97, 109, 109, 97, 104, 76, 111, 118, 101, 114, 115, 0, 63, 20, 65, 25, -109, 14, 65, 99, 0, 65, 101, -124, -11, 6, 65, 102, -88, 15, 65, 103, -88, 15, 65, 111, 15, 65, 112, 10, 65, 46, -81, 2, 65, 47, 30, 65, 28, -124, 1, 65, 0};
//        Packet packetType = PacketType.getPacket(62).factory();
//        PBuffer pData = new PBuffer(ByteBuffer.wrap(listB).order(ByteOrder.BIG_ENDIAN));
//        try {
//            packetType.deserialize(pData);
//            pData.errorCheck(PacketType.byOrdinal(61));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    /**
     * Example method called when text packets are received.
     *
     * @param packet The text packet.
     */
    static void text(Packet packet) {
        if (!(packet instanceof TextPacket)) return;
        TextPacket tPacket = (TextPacket) packet;
        System.out.printf("[%s]: %s\n", tPacket.name, tPacket.text);
    }
}
