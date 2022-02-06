import packets.Packet;
import packets.PacketType;
import packets.incoming.TextPacket;
import packets.packetcapture.PacketProcessor;
import packets.packetcapture.register.Register;

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
        Register.INSTANCE.register(PacketType.TEXT.getPacketClass(), (packet) -> text(packet));

        new PacketProcessor().run();
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
