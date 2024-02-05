package tomato.gui;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.*;
import packets.data.QuestData;
import tomato.Tomato;
import tomato.gui.chat.ChatGUI;
import tomato.gui.dps.DpsDisplayOptions;
import tomato.gui.dps.DpsGUI;
import tomato.gui.fame.FameTrackerGUI;
import tomato.gui.character.CharacterPanelGUI;
import tomato.gui.keypop.KeypopGUI;
import tomato.gui.maingui.TomatoMenuBar;
import tomato.gui.mydmg.MyDamageGUI;
import tomato.gui.quest.QuestGUI;
import tomato.gui.security.ParsePanelGUI;
import tomato.gui.security.SecurityGUI;
import tomato.backend.data.TomatoData;
import util.PropertiesManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Example GUI for Tomato mod.
 */
public class TomatoGUI {
    private static final int windowWidth = 500;
    private static final int windowHeight = 500;
    private static int fontSize = 12;
    private static int fontStyle = 0;
    private static String fontName = "Monospaced";
    private static JLabel statusLabel;
    private static JFrame frame;
    private static SecurityGUI securityPanel;
    private static CharacterPanelGUI characterPanel;
    private static QuestGUI questPanel;
    private static MyDamageGUI myDmg;
    private static FameTrackerGUI fameTracker;
    private JMenuBar jMenuBar;
    private JPanel mainPanel, dpsPanel;
    private TomatoMenuBar menuBar;
    private Point center;
    private Image icon;
    private TomatoData data;

    public TomatoGUI(TomatoData data) {
        this.data = data;
    }

    /**
     * Create main panel and initializes the GUI for the example Tomato.
     */
    public void create() {
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Chat", new ChatGUI());

        tabbedPane.addTab("Key-pops", new KeypopGUI());

        securityPanel = new SecurityGUI();
        tabbedPane.addTab("Security", securityPanel);

        characterPanel = new CharacterPanelGUI(data);
        tabbedPane.addTab("Characters", characterPanel);

        fameTracker = new FameTrackerGUI();
        tabbedPane.addTab("Fame", fameTracker);

        questPanel = new QuestGUI();
        tabbedPane.addTab("Daily Quests", questPanel);

        myDmg = new MyDamageGUI(data);
        tabbedPane.addTab("My Dmg", myDmg);

        dpsPanel = new DpsGUI(data);
        tabbedPane.addTab("DPS Logger", dpsPanel);

        center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        menuBar = new TomatoMenuBar();

        statusLabel = new JLabel(" Network Monitor: OFF");

        mainPanel = new JPanel();
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        icon = Toolkit.getDefaultToolkit().getImage(Tomato.imagePath);
        loadChatPreset();
        loadFontSizePreset();
        loadFontNamePreset();
        DpsGUI.loadFilterPreset();
        DpsDisplayOptions.loadProfileFilter();
        jMenuBar = menuBar.make();
        makeFrame();

        frame.setVisible(true);
    }

    /**
     * Method to create text areas.
     *
     * @param textArea Text area object.
     * @return Scroll pane object to add to a parent object.
     */
    public static JScrollPane createTextArea(JTextArea textArea) {
        textArea.setEnabled(true);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollChat = new JScrollPane(textArea);
        scrollChat.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollChat.setAutoscrolls(true);
        new SmartScroller(scrollChat);
        return scrollChat;
    }

    /**
     * Loads chat presets
     */
    private void loadChatPreset() {
        String save = PropertiesManager.getProperty("saveChat");
        if (save != null) {
            ChatGUI.save = save.equals("true");
        }

        String ping = PropertiesManager.getProperty("chatPing");
        if (ping != null) {
            ChatGUI.ping = ping.equals("true");
        }
    }

    /**
     * Loads the theme preset chosen by the user.
     */
    public static void loadThemePreset() {
        String theme = PropertiesManager.getProperty("theme");
        if (theme == null) {
            LafManager.install(new DarculaTheme());
            return;
        }

        switch (theme) {
            case "contrastDark":
                LafManager.install(new HighContrastDarkTheme());
                break;
            case "contrastLight":
                LafManager.install(new HighContrastLightTheme());
                break;
            case "intelliJ":
                LafManager.install(new IntelliJTheme());
                break;
            case "solarizedDark":
                LafManager.install(new SolarizedDarkTheme());
                break;
            case "solarizedLight":
                LafManager.install(new SolarizedLightTheme());
                break;
            default:
            case "darcula":
                LafManager.install(new DarculaTheme());
                break;
        }
    }

    /**
     * Loads the font size preset chosen by the user.
     */
    private void loadFontSizePreset() {
        String fontSize = PropertiesManager.getProperty("fontSize");
        int fs = TomatoGUI.fontSize;
        if (fontSize != null) {
            try {
                fs = Integer.parseInt(fontSize);
            } catch (Exception ignored) {
            }
        }

        fontSizeTextAreas(fs);
    }

    /**
     * Loads the font size preset chosen by the user.
     */
    private void loadFontNamePreset() {
        String fontName = PropertiesManager.getProperty("fontName");
        if (fontName == null) {
            fontName = TomatoGUI.fontName;
        } else {
            TomatoGUI.fontName = fontName;
        }
        String fontStyle = PropertiesManager.getProperty("fontStyle");
        int fontStyleNum = TomatoGUI.fontStyle;
        if (fontStyle != null) {
            try {
                fontStyleNum = Integer.parseInt(fontStyle);
            } catch (Exception ignored) {
            }
        }
        fontNameTextAreas(fontName, fontStyleNum);
    }

    /**
     * Creates the frame with icon.
     */
    public void makeFrame() {
        frame = new JFrame("    Tomato    ");
        frame.setIconImage(icon);
        frame.setSize(windowWidth, windowHeight);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(jMenuBar);
        frame.setJMenuBar(jMenuBar);
        menuBar.setFrame(frame);
        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }

    /**
     * Set font size of text area.
     */
    public static void fontSizeTextAreas(int size) {
        fontSize = size;
        ChatGUI.editFont(new Font(fontName, fontStyle, size));
        KeypopGUI.editFont(new Font(fontName, fontStyle, size));
        DpsGUI.editFont(new Font(fontName, fontStyle, size));
        ParsePanelGUI.editFont(new Font(fontName, fontStyle, size));
    }

    /**
     * Set font size of text area.
     */
    public static void fontNameTextAreas(String name, int style) {
        fontName = name;
        fontStyle = style;
        ChatGUI.editFont(new Font(name, style, fontSize));
        KeypopGUI.editFont(new Font(name, style, fontSize));
        DpsGUI.editFont(new Font(name, style, fontSize));
        ParsePanelGUI.editFont(new Font(name, style, fontSize));
    }

    /**
     * Updates the questGUI with quest data.
     *
     * @param q Quest data received when visiting quest room.
     */
    public static void updateQuests(QuestData[] q) {
        questPanel.update(q);
    }

    /**
     * Updates the state of the sniffer at the bottom label to show if running or off.
     *
     * @param running Set the label to running or off.
     */
    public static void setStateOfSniffer(boolean running) {
        statusLabel.setText(" Network Monitor: " + (running ? "RUNNING" : "OFF"));
    }

    /**
     * Getter for the main object.
     *
     * @return The main tomato frame object.
     */
    public static JFrame getFrame() {
        return frame;
    }
}