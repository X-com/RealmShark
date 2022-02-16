package example;

import example.gui.TomatoGUI;
import packets.Packet;
import packets.PacketType;
import packets.incoming.TextPacket;
import packets.packetcapture.PacketProcessor;
import packets.packetcapture.register.Register;

import java.io.File;

/**
 * This is an API used to unwrapped Realm of the Mad Gods packets. The
 * Packets are grabbed directly from the network tap using a sniffer.
 * It is not possible to modify, block or create packets to be sent.
 * <p>
 * The register should be used to sign up for packets. If said packet is
 * received then the lambda function passed in as the second argument can
 * be used to trigger any functions listening to registered packets.
 * <p>
 * TODO: Add linux and mac support later in PacketProcessor
 */
public class TomatoExample {
    private static PacketProcessor packetProcessor;
    public static String version = "1.0";
    public static String tomatoIconURL = "icon/tomatoIcon.png";

    public static void main(String[] args) {
        TomatoExample.example();
    }

    /**
     * Example mod main method.
     */
    public static void example() {
        System.out.println(System.getProperty("java.library.path").replaceAll(";", "\n"));
        /*
            Subscribe for any packet wanting to be monitored. Use a lambda in
            the second argument for the action when registered packet is received.

            Example for subscribing for all packet types:
            Register.INSTANCE.register(Packet.class, System.out::println);

            Example 2: Subscribing to TEXT packets
         */
        Register.INSTANCE.register(PacketType.TEXT, (packet) -> text(packet));

        new TomatoGUI().create();
//        File f = new File(tomatoIconURL);
//        System.out.println(f.getAbsolutePath());
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
