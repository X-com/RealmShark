package example;

import example.damagecalc.DpsLogger;
import example.gui.TomatoGUI;
import packets.Packet;
import packets.PacketType;
import packets.data.enums.NotificationEffectType;
import packets.incoming.NotificationPacket;
import packets.incoming.TextPacket;
import packets.packetcapture.PacketProcessor;
import packets.packetcapture.register.Register;
import util.IdToName;
import util.Util;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tomato is an EXAMPLE MOD built on top of RealmShark, an API used to
 * unwrapped Realm of the Mad Gods packets. The Packets are grabbed
 * directly from the network tap using a sniffer. It is not possible
 * to modify, block or create packets to be sent, similar to WireShark.
 * <p>
 * The register should be used to sign up for packets. If said packet is
 * received then the lambda function passed in as the second argument can
 * be used to trigger any functions listening to registered packets.
 */
public class ExampleModTomato {
    public static URL imagePath = ExampleModTomato.class.getResource("/icon/tomatoIcon.png");
    private static final Pattern popperName = Pattern.compile("[^ ]*\"player\":\"([A-Za-z]*)[^ ]*");
    private static PacketProcessor packetProcessor;
    private static DpsLogger dpsLogger;

    public static void main(String[] args) {
        Util.setSaveLogs(true); // turns the logger to, save in to files.
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
            Register.INSTANCE.registerAll(System.out::println);

            Example 2: Subscribing to TEXT packets
         */
        // [ExampleModTomato::text] is the same as [(packet) - > text(packet)]
        Register.INSTANCE.register(PacketType.TEXT, ExampleModTomato::textPacket);

        Register.INSTANCE.register(PacketType.NOTIFICATION, ExampleModTomato::notificationPacket);

        Register.INSTANCE.register(PacketType.CREATE_SUCCESS, ExampleModTomato::dpsLoggerPacket);
        Register.INSTANCE.register(PacketType.ENEMYHIT, ExampleModTomato::dpsLoggerPacket);
        Register.INSTANCE.register(PacketType.PLAYERSHOOT, ExampleModTomato::dpsLoggerPacket);
        Register.INSTANCE.register(PacketType.DAMAGE, ExampleModTomato::dpsLoggerPacket);
        Register.INSTANCE.register(PacketType.SERVERPLAYERSHOOT, ExampleModTomato::dpsLoggerPacket);
        Register.INSTANCE.register(PacketType.UPDATE, ExampleModTomato::dpsLoggerPacket);
        Register.INSTANCE.register(PacketType.NEWTICK, ExampleModTomato::dpsLoggerPacket);
        Register.INSTANCE.register(PacketType.MAPINFO, ExampleModTomato::dpsLoggerPacket);
        new TomatoGUI().create();
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
            dpsLogger.clear();
        }
    }

    public static boolean isRunning() { // TODO remove in release version
        return packetProcessor != null;
    }

    private static void dpsLoggerPacket(Packet packet) {
        dpsLogger.packetCapture(packet);
    }

    /**
     * Example method called when text packets are received.
     *
     * @param packet The text packet.
     */
    private static void textPacket(Packet packet) {
        if (packet instanceof TextPacket) {
            TextPacket tPacket = (TextPacket) packet;
            TomatoGUI.appendTextAreaChat(String.format("[%s]: %s\n", tPacket.name, tPacket.text));
        }
    }

    /**
     * Example method called when event notifier packets are received.
     *
     * @param packet The event notifier packet.
     */
    private static void notificationPacket(Packet packet) {
        if (packet instanceof NotificationPacket) {
            NotificationPacket nPacket = (NotificationPacket) packet;
            if (nPacket.effect == NotificationEffectType.DungeonOpened) {
                String msg = nPacket.message;
                Matcher m = popperName.matcher(msg);
                if (m.matches()) {
                    String playerName = m.group(1);
                    TomatoGUI.appendTextAreaKeypop(String.format("%s [%s]: %s\n", Util.getHourTime(), playerName, IdToName.name(nPacket.pictureType)));
                }
            } else if (nPacket.effect == NotificationEffectType.ServerMessage) {
                String msg = nPacket.message;
                if (msg.startsWith("Wine Cellar")) {
                    String[] list = msg.split(" ");
                    String playerName = list[list.length - 1];
                    TomatoGUI.appendTextAreaKeypop(String.format("%s [%s]: Inc\n", Util.getHourTime(), playerName));
                } else if (msg.contains("Monument has been activated by")) {
                    String[] list = msg.split(" ");
                    String playerName = list[list.length - 1];
                    String type = list[1];
                    TomatoGUI.appendTextAreaKeypop(String.format("%s [%s]: %s Rune\n", Util.getHourTime(), playerName, type));
                }
            }
        }
    }
}
