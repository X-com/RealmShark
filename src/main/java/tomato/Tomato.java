package tomato;

import assets.AssetExtractor;
import assets.AssetMissingException;
import assets.IdToAsset;
import packets.Packet;
import packets.PacketType;
import packets.data.enums.NotificationEffectType;
import packets.incoming.MapInfoPacket;
import packets.incoming.NotificationPacket;
import packets.incoming.TextPacket;
import packets.packetcapture.PacketProcessor;
import packets.packetcapture.register.IPacketListener;
import packets.packetcapture.register.Register;
import packets.packetcapture.sniff.assembly.TcpStreamErrorHandler;
import tomato.gui.warnings.JavaOutOfMemoryGUI;
import tomato.gui.maingui.TomatoBandwidth;
import tomato.gui.TomatoGUI;
import tomato.gui.maingui.TomatoMenuBar;
import tomato.realmshark.CrashLogger;
import tomato.backend.TomatoPacketCapture;
import tomato.backend.TomatoRootController;
import tomato.backend.data.TomatoData;
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
    private static final IPacketListener<Packet> loadAsset = Tomato::loadAssets;
    private static TomatoRootController rootController;

    public static void main(String[] args) {
        Util.setSaveLogs(true); // turns the logger to, save in to files.
        TcpStreamErrorHandler.INSTANCE.setErrorMessageHandler(Tomato::errorMessageHandler);
        TcpStreamErrorHandler.INSTANCE.setErrorStopHandler(TomatoMenuBar::stopPacketSniffer);
        load();
    }

    /**
     * Main boot up method to create data storage, start controllers and attach
     * the data to the controllers and link the data to the view to be displayed.
     */
    public static void load() {
        CrashLogger.loadThisClass();
        try {
            TomatoData data = new TomatoData();
            loadControllers(data);
            new TomatoGUI(data).create();
        } catch (OutOfMemoryError e) {
            JavaOutOfMemoryGUI.crashDialog();
        } catch (Exception e) {
            e.printStackTrace();
            CrashLogger.printCrash(e);
        } finally {
            dispose();
        }
    }

    /**
     * Loads controllers and adds them to the root controller list.
     *
     * @param data Main root controller.
     */
    private static void loadControllers(TomatoData data) {
        rootController = new TomatoRootController(data);
        // Create realm packet capture instance and add to root controller
        TomatoPacketCapture packCap = new TomatoPacketCapture(data);
        packetRegister(packCap);
        rootController.addController(packCap);
    }

    /**
     * Disposes all controllers
     */
    public static void dispose() {
        if (rootController != null) rootController.dispose();
//        if (packetProcessor != null) packetProcessor.stopSniffer();
    }

    /**
     * Error message handler from the TCP stream constructor.
     *
     * @param errorMsg Display message string
     * @param dump     Log dump string
     */
    private static void errorMessageHandler(String errorMsg, String dump) {
        TomatoGUI.appendTextAreaChat(errorMsg);
        Util.print(dump);
    }

    /**
     * Packet register for listening to incoming or outgoing packets from realm client.
     *
     * @param packCap Packet capture controller.
     */
    private static void packetRegister(TomatoPacketCapture packCap) {
        Register.INSTANCE.subscribePacketLogger(TomatoBandwidth::setInfo);

//        Register.INSTANCE.register(PacketType.MAPINFO, loadAsset);

//        Register.INSTANCE.register(PacketType.TEXT, Tomato::textPacket);

//        Register.INSTANCE.register(PacketType.NOTIFICATION, Tomato::notificationPacket);

        Register.INSTANCE.register(PacketType.CREATE_SUCCESS, packCap::packetCapture);
        Register.INSTANCE.register(PacketType.ENEMYHIT, packCap::packetCapture);
        Register.INSTANCE.register(PacketType.PLAYERSHOOT, packCap::packetCapture);
        Register.INSTANCE.register(PacketType.DAMAGE, packCap::packetCapture);
        Register.INSTANCE.register(PacketType.SERVERPLAYERSHOOT, packCap::packetCapture);
        Register.INSTANCE.register(PacketType.UPDATE, packCap::packetCapture);
        Register.INSTANCE.register(PacketType.NEWTICK, packCap::packetCapture);
        Register.INSTANCE.register(PacketType.MAPINFO, packCap::packetCapture);
        Register.INSTANCE.register(PacketType.STASIS, packCap::packetCapture);
        Register.INSTANCE.register(PacketType.TEXT, packCap::packetCapture);
        Register.INSTANCE.register(PacketType.EXALTATION_BONUS_CHANGED, packCap::packetCapture);
        Register.INSTANCE.register(PacketType.VAULT_UPDATE, packCap::packetCapture);
        Register.INSTANCE.register(PacketType.QUEST_FETCH_RESPONSE, packCap::packetCapture);
        Register.INSTANCE.register(PacketType.HELLO, packCap::packetCapture);
    }

    /**
     * Asset loader from realm resources.
     */
    private static void loadAssets(Packet packet) {
        if (packet instanceof MapInfoPacket) {
            MapInfoPacket p = (MapInfoPacket) packet;
            AssetExtractor.checkForExtraction(p.buildVersion);
            Register.INSTANCE.unregister(PacketType.MAPINFO, loadAsset);
        }
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
//            dpsLogger.clear(); // TODO clear tomatodata
        }
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
                        TomatoGUI.appendTextAreaKeypop(String.format("%s [%s]: %s\n", Util.getHourTime(), playerName, IdToAsset.objectName(nPacket.pictureType)));
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
}
