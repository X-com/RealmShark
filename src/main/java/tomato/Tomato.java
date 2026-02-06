package tomato;

import assets.AssetExtractor;
import java.io.File;
import java.net.URL;
import java.nio.file.AccessDeniedException;
import javax.swing.*;
import packets.PacketType;
import packets.packetcapture.PacketProcessor;
import packets.packetcapture.register.Register;
import packets.packetcapture.sniff.assembly.TcpStreamErrorHandler;
import tomato.backend.TomatoPacketCapture;
import tomato.backend.TomatoRootController;
import tomato.backend.data.AbilityScalingManager;
import tomato.backend.data.TomatoData;
import tomato.gui.TomatoGUI;
import tomato.gui.chat.ChatGUI;
import tomato.gui.maingui.TomatoBandwidth;
import tomato.gui.maingui.TomatoMenuBar;
import tomato.gui.warnings.JavaOutOfMemoryGUI;
import tomato.realmshark.CrashLogger;
import tomato.version.Version;
import util.Util;

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

    public static URL imagePath = Tomato.class.getResource(
        "/icon/tomatoIcon.png"
    );
    private static PacketProcessor packetProcessor;
    private static TomatoRootController rootController;

    public static void main(String[] args) {
        System.out.println(
            "Java Version: " +
                System.getProperty("java.version") +
                " : (" +
                System.getProperty("sun.arch.data.model") +
                " - bit)"
        );
        parseArgs(args);

        parseCustomAssetPath(args);

        parseCustomAssetPath(args);
        Util.setSaveLogs(false); // turns the logger to, save in to files.
        TcpStreamErrorHandler.INSTANCE.setErrorMessageHandler(
            Tomato::errorMessageHandler
        );
        TcpStreamErrorHandler.INSTANCE.setErrorStopHandler(
            TomatoMenuBar::stopPacketSniffer
        );

        // Initialize crucible data from API on startup
        initializeCrucibleData();

        load();
    }

    private static void parseArgs(String[] args) {
        for (String arg : args) {
            if (arg.equals("--help") || arg.equals("-h")) {
                usage();
                System.exit(0);
            }
        }

        parseCustomAssetPath(args);
    }

    private static void usage() {
        System.out.println("Usage: java -jar Tomato.jar [options]");
        System.out.println("Options:");
        System.out.println("  --help, -h          Show this help message");
        System.out.println(
            "  --path <file_path>  Specify custom resources.assets file path"
        );
    }

    /**
     * Initializes crucible data by fetching from API on startup
     * This provides pre-launch crucible bonuses without waiting for game packets
     */
    private static void initializeCrucibleData() {
        long startTime = System.currentTimeMillis();
        tomato.backend.data.CrucibleBonusManager.fetchCrucibleDataFromApi();
        long endTime = System.currentTimeMillis();

        boolean apiDataLoaded =
            tomato.backend.data.CrucibleBonusManager.isApiDataLoaded();
        if (apiDataLoaded) {
            System.out.println(
                "[Crucible] API data loaded (" + (endTime - startTime) + "ms)"
            );
        } else {
            System.out.println(
                "[Crucible] Using packet data (" + (endTime - startTime) + "ms)"
            );
        }
    }

    private static void parseCustomAssetPath(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--path") && i + 1 < args.length) {
                String customPath = args[i + 1];
                File customFile = new File(customPath);
                if (customFile.exists() && customFile.isFile()) {
                    AssetExtractor.setRealmResPath(customPath);
                    System.out.println(
                        "Using custom resources.assets path: " + customPath
                    );
                } else {
                    System.err.println("Invalid path provided: " + customPath);
                    System.err.println("Falling back to default paths.");
                }
                break;
            }
        }
    }

    /**
     * Main boot up method to create data storage, start controllers and attach
     * the data to the controllers and link the data to the view to be displayed.
     */
    public static void load() {
        try {
            CrashLogger.loadThisClass();
            TomatoGUI.loadThemePreset();
            CheckVersion.checkVersion();
            AssetExtractor.checkForExtraction(Version.VERSION);
            TomatoData data = new TomatoData();
            loadControllers(data);
            new TomatoGUI(data).create();
            bootload(data);
        } catch (OutOfMemoryError | StackOverflowError e) {
            JavaOutOfMemoryGUI.crashDialog();
            System.exit(0);
        } catch (AccessDeniedException e) {
            JOptionPane.showMessageDialog(
                null,
                "<html>Extraction access denied, failed to extract!<br/>Please move Tomato to a different folder,<br/>Windows is blocking access in current folder.</html>\""
            );
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            CrashLogger.printCrash(e);
        } catch (Throwable e) {
            e.printStackTrace();
            CrashLogger.printCrash((Exception) e);
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

        // Initialize ability scaling manager
        AbilityScalingManager.getInstance().initialize();
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
        ChatGUI.appendTextAreaChat(errorMsg);
        Util.printLogs(dump);
    }

    /**
     * Packet register for listening to incoming or outgoing packets from realm client.
     *
     * @param packCap Packet capture controller.
     */
    private static void packetRegister(TomatoPacketCapture packCap) {
        Register.INSTANCE.subscribePacketLogger(TomatoBandwidth::setInfo);

        Register.INSTANCE.register(
            PacketType.CREATE_SUCCESS,
            packCap::packetCapture
        );
        Register.INSTANCE.register(PacketType.ENEMYHIT, packCap::packetCapture);
        Register.INSTANCE.register(
            PacketType.PLAYERSHOOT,
            packCap::packetCapture
        );
        Register.INSTANCE.register(PacketType.DAMAGE, packCap::packetCapture);
        Register.INSTANCE.register(
            PacketType.PLAYERHIT,
            packCap::packetCapture
        );
        Register.INSTANCE.register(
            PacketType.ENEMYSHOOT,
            packCap::packetCapture
        );
        Register.INSTANCE.register(
            PacketType.GROUNDDAMAGE,
            packCap::packetCapture
        );
        Register.INSTANCE.register(PacketType.AOE, packCap::packetCapture);
        Register.INSTANCE.register(PacketType.MOVE, packCap::packetCapture);
        Register.INSTANCE.register(
            PacketType.SERVERPLAYERSHOOT,
            packCap::packetCapture
        );
        Register.INSTANCE.register(PacketType.UPDATE, packCap::packetCapture);
        Register.INSTANCE.register(PacketType.NEWTICK, packCap::packetCapture);
        Register.INSTANCE.register(PacketType.MAPINFO, packCap::packetCapture);
        Register.INSTANCE.register(PacketType.STASIS, packCap::packetCapture);
        Register.INSTANCE.register(PacketType.TEXT, packCap::packetCapture);
        Register.INSTANCE.register(
            PacketType.NOTIFICATION,
            packCap::packetCapture
        );
        Register.INSTANCE.register(
            PacketType.EXALTATION_BONUS_CHANGED,
            packCap::packetCapture
        );
        Register.INSTANCE.register(
            PacketType.VAULT_UPDATE,
            packCap::packetCapture
        );
        Register.INSTANCE.register(
            PacketType.QUEST_FETCH_RESPONSE,
            packCap::packetCapture
        );
        Register.INSTANCE.register(PacketType.HELLO, packCap::packetCapture);
        Register.INSTANCE.register(
            PacketType.TRADEREQUESTED,
            packCap::packetCapture
        );

        // Register CrucibleResponsePacket if it exists
        try {
            Class<?> cruciblePacketClass = Class.forName(
                "packets.incoming.CrucibleResponsePacket"
            );
            Register.INSTANCE.register(
                PacketType.CRUCIBLE_RESPONSE,
                packCap::packetCapture
            );
        } catch (ClassNotFoundException e) {
            // CrucibleResponsePacket not available in this version
            System.out.println("CrucibleResponsePacket not available");
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
     * Load all presets
     */
    private static void bootload(TomatoData data) {
        data.bootload();
        data.loadPropList("chatPingMessages");
        data.loadPropList("entityIdPings");
        data.loadPropList("itemPings");
    }
}
