package tomato;

import packets.Packet;
import packets.PacketType;
import packets.data.enums.NotificationEffectType;
import packets.incoming.NotificationPacket;
import packets.incoming.TextPacket;
import packets.packetcapture.PacketProcessor;
import packets.packetcapture.register.Register;
import tomato.damagecalc.DpsLogger;
import tomato.gui.TomatoGUI;
import tomato.logic.QuestPackets;
import tomato.security.Parse;
import util.assets.AssetMissingException;
import util.assets.IdToName;
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
public class Tomato {
    public static URL imagePath = Tomato.class.getResource("/icon/tomatoIcon.png");
    private static final Pattern popperName = Pattern.compile("[^ ]*\"player\":\"([A-Za-z]*)[^ ]*");
    private static PacketProcessor packetProcessor;
    private static final DpsLogger dpsLogger = new DpsLogger();
    private static final Parse parse = new Parse();

    public static void main(String[] args) {
        Util.setSaveLogs(true); // turns the logger to, save in to files.
        Tomato.example();
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
        Register.INSTANCE.register(PacketType.TEXT, Tomato::textPacket);

        Register.INSTANCE.register(PacketType.NOTIFICATION, Tomato::notificationPacket);

        Register.INSTANCE.register(PacketType.CREATE_SUCCESS, Tomato::dpsLoggerPacket);
        Register.INSTANCE.register(PacketType.ENEMYHIT, Tomato::dpsLoggerPacket);
        Register.INSTANCE.register(PacketType.PLAYERSHOOT, Tomato::dpsLoggerPacket);
        Register.INSTANCE.register(PacketType.DAMAGE, Tomato::dpsLoggerPacket);
        Register.INSTANCE.register(PacketType.SERVERPLAYERSHOOT, Tomato::dpsLoggerPacket);
        Register.INSTANCE.register(PacketType.UPDATE, Tomato::dpsLoggerPacket);
        Register.INSTANCE.register(PacketType.NEWTICK, Tomato::dpsLoggerPacket);
        Register.INSTANCE.register(PacketType.MAPINFO, Tomato::dpsLoggerPacket);
        Register.INSTANCE.register(PacketType.TEXT, Tomato::dpsLoggerPacket);

        Register.INSTANCE.register(PacketType.QUEST_FETCH_RESPONSE, QuestPackets::questPacket);
        Register.INSTANCE.register(PacketType.QUEST_REDEEM, QuestPackets::questPacket);
        Register.INSTANCE.register(PacketType.HELLO, QuestPackets::questPacket);

        Register.INSTANCE.register(PacketType.NEWTICK, parse::packetCapture);
        Register.INSTANCE.register(PacketType.UPDATE, parse::packetCapture);
        Register.INSTANCE.register(PacketType.CREATE_SUCCESS, parse::packetCapture);

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

    /**
     * Clear the DPS logs.
     */
    public static void clearDpsLogs() {
        dpsLogger.clearTextLogs();
    }

    /**
     * Sets the flag for saving all packets related to dps logs to file.
     *
     * @param save If the dps logs should be saved to file.
     */
    public static void saveDpsLogsToFile(boolean save) {
        dpsLogger.setSaveToFile(save);
    }

    /**
     * Dps logger packets
     *
     * @param packet packets for dps logger.
     */
    private static void dpsLoggerPacket(Packet packet) {
        dpsLogger.packetCapture(packet, true);
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
                    try {
                        TomatoGUI.appendTextAreaKeypop(String.format("%s [%s]: %s\n", Util.getHourTime(), playerName, IdToName.objectName(nPacket.pictureType)));
                    } catch (AssetMissingException e) {
                        e.printStackTrace();
                    }
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

    /**
     * Next dungeon displayed by dps calculator.
     */
    public static void nextDpsLogDungeon() {
        dpsLogger.nextDisplay();
    }

    /**
     * Previous dungeon displayed by dps calculator.
     */
    public static void previousDpsLogDungeon() {
        dpsLogger.previousDisplay();
    }

    /**
     * Previous dungeon displayed by dps calculator.
     */
    public static void updateDpsWindow() {
        dpsLogger.updateFilter();
    }


}
