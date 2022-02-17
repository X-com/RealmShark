package example;

import example.gui.TomatoGUI;
import packets.Packet;
import packets.PacketType;
import packets.incoming.TextPacket;
import packets.packetcapture.PacketProcessor;
import packets.packetcapture.register.Register;
import util.LibExtractor;
import org.pcap4j.*;

import java.net.URL;

/**
 * Tomato is an EXAMPLE MOD built on top of RealmShark, an API used to
 * unwrapped Realm of the Mad Gods packets. The Packets are grabbed
 * directly from the network tap using a sniffer. It is not possible
 * to modify, block or create packets to be sent, similar to WireShark.
 * <p>
 * The register should be used to sign up for packets. If said packet is
 * received then the lambda function passed in as the second argument can
 * be used to trigger any functions listening to registered packets.
 * <p>
 * TODO: Add linux and mac support later in PacketProcessor
 */
public class ExampleModTomato {
    public static URL imagePath = ExampleModTomato.class.getResource("/icon/tomatoIcon.png");
    private static PacketProcessor packetProcessor;
    public static String version = "v1.0";

    public static void main(String[] args) {
//        LibExtractor.libraryExtractor(); // This is needed to extract the libs files into root dir.
        ExampleModTomato.example();
    }

    /**
     * Example mod main method.
     */
    public static void example() {
        /*
            Subscribe for any packet wanting to be monitored. Use a lambda in
            the second argument for the action when registered packet is received.

            Example for subscribing for all packet types:
            Register.INSTANCE.register(Packet.class, System.out::println);

            Example 2: Subscribing to TEXT packets
         */
//        Register.INSTANCE.register(PacketType.TEXT, (packet) -> text(packet));
//
//        new TomatoGUI().create();
        startPacketSniffer();
    }

    /**
     * Start the packet sniffer.
     */
    public static void startPacketSniffer() {
        if (packetProcessor == null) {
            packetProcessor = new PacketProcessor();
            packetProcessor.start();
        }
    }

    /**
     * Stop the packet sniffer.
     */
    public static void stopPacketSniffer() {
        if (packetProcessor != null) {
            packetProcessor.stopSniffer();
            packetProcessor = null;
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Example method called when text packets are received.
     *
     * @param packet The text packet.
     */
    private static void text(Packet packet) {
        if (!(packet instanceof TextPacket)) return;
        TextPacket tPacket = (TextPacket) packet;
        TomatoGUI.appendTextAreaText(String.format("[%s]: %s\n", tPacket.name, tPacket.text));
    }
}
