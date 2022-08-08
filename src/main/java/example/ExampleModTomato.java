package example;

import example.damagecalc.DpsLogger;
import example.gui.TomatoGUI;
import packets.Packet;
import packets.PacketType;
import packets.data.QuestData;
import packets.data.enums.NotificationEffectType;
import packets.incoming.NotificationPacket;
import packets.incoming.QuestFetchResponsePacket;
import packets.incoming.TextPacket;
import packets.packetcapture.PacketProcessor;
import packets.packetcapture.register.Register;
import util.IdToName;
import util.Util;

import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private static final DpsLogger dpsLogger = new DpsLogger();

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
        Register.INSTANCE.register(PacketType.TEXT, ExampleModTomato::dpsLoggerPacket);

        Register.INSTANCE.register(PacketType.QUEST_FETCH_RESPONSE, ExampleModTomato::questPacket);
        Register.INSTANCE.register(PacketType.QUEST_REDEEM, ExampleModTomato::questPacket);

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
     * Quest packets to be processed.
     *
     * @param packet The event notifier packet.
     */
    private static void questPacket(Packet packet) {
        if (packet instanceof QuestFetchResponsePacket) {
            QuestFetchResponsePacket p = (QuestFetchResponsePacket) packet;
            StringBuilder s = new StringBuilder();
            Stream<QuestData> list = Arrays.stream(p.quests).sorted(Comparator.comparing(questData -> questData.category));
            for (QuestData qd : list.collect(Collectors.toList())) {
                StringBuilder sbReqs = new StringBuilder();
                int memId = 0;
                int memCount = 0;
                boolean first = true;
                if (qd.requirements[0] == 34734 || // Ancient schematics
                        qd.requirements[0] == 34683 || // Schematics
                        qd.requirements[0] == 10023 || // Rune shield
                        qd.requirements[0] == 10024 || // Rune helm
                        qd.requirements[0] == 10022 || // Rune sword
                        qd.requirements[0] == 23547 || // Shard weapons
                        qd.requirements[0] == 23583 || // Shard armor
                        qd.requirements[0] == 32403 || // Shard ring
                        qd.requirements[0] == 23619 || // Shard ability
                        qd.requirements[0] == 47360 || // Common ore
                        qd.requirements[0] == 19210 || // Red crystal
                        qd.requirements[0] == 12055) // Blue crystal
                    continue; // ignore list

                for (int i = 0; i < qd.requirements.length; i++) {
                    int id = qd.requirements[i];
                    if (first) {
                        first = false;
                        memId = id;
                        memCount = 1;
                    } else if (memId != id) {
                        if (memCount > 1) sbReqs.append(memCount).append("x ");
                        sbReqs.append(IdToName.name(memId));
                        if (i < qd.requirements.length - 1) sbReqs.append(", ");
                        memId = id;
                        memCount = 1;
                    } else {
                        memCount++;
                    }
                }
                if (memCount > 1) sbReqs.append(memCount).append("x ");
                sbReqs.append(IdToName.name(memId));
                StringBuilder sbRew = new StringBuilder();
                for (int i = 0; i < qd.rewards.length; i++) {
                    int id = qd.rewards[i];
                    sbRew.append(IdToName.name(id));
                    if (i < qd.rewards.length - 1) sbRew.append(", ");
                }

                s.append(String.format("%s\n%s\n[%s] for [%s]\n\n", qd.name, qd.description, sbReqs, sbRew));
            }
            TomatoGUI.setTextAreaQuests(s.toString());
        }
    }
}
