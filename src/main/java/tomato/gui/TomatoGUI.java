package tomato.gui;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.*;
import packets.data.QuestData;
import tomato.Tomato;
import tomato.gui.dps.DpsDisplayOptions;
import tomato.gui.dps.DpsGUI;
import tomato.gui.fame.FameTrackerGUI;
import tomato.gui.character.CharacterPanelGUI;
import tomato.gui.maingui.TomatoMenuBar;
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
    private static JTextArea textAreaChat;
    private static JTextArea textAreaKeypop;
    private static JLabel statusLabel;
    private static JFrame frame;
    private static SecurityGUI securityPanel;
    private static CharacterPanelGUI characterPanel;
    private static QuestGUI questPanel;
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
        textAreaChat = new JTextArea();
        tabbedPane.addTab("Chat", createTextArea(textAreaChat));

        textAreaKeypop = new JTextArea();
        tabbedPane.addTab("Key-pops", createTextArea(textAreaKeypop));

        securityPanel = new SecurityGUI();
        tabbedPane.addTab("Security", securityPanel);

        characterPanel = new CharacterPanelGUI(data);
        tabbedPane.addTab("Characters", characterPanel);

        fameTracker = new FameTrackerGUI();
        tabbedPane.addTab("Fame", fameTracker);

        questPanel = new QuestGUI();
        tabbedPane.addTab("Daily Quests", questPanel);

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
     * Add text to the chat text area.
     *
     * @param s The text to be added at the end of text area.
     */
    public static void appendTextAreaChat(String s) {
        if (textAreaChat != null) textAreaChat.append(s);
    }

    /**
     * Clears the chat text area.
     */
    public static void clearTextAreaChat() {
        textAreaChat.setText("");
    }

    /**
     * Set font size of text area.
     */
    public static void fontSizeTextAreas(int size) {
        Font f = textAreaChat.getFont();
        textAreaChat.setFont(new Font(f.getName(), f.getStyle(), size));
        textAreaKeypop.setFont(new Font(f.getName(), f.getStyle(), size));
        DpsGUI.editFont(new Font(f.getName(), f.getStyle(), size));
        ParsePanelGUI.editFont(new Font(f.getName(), f.getStyle(), size));
    }

    /**
     * Set font size of text area.
     */
    public static void fontNameTextAreas(String name, int style) {
        Font f = textAreaChat.getFont();
        textAreaChat.setFont(new Font(name, style, f.getSize()));
        textAreaKeypop.setFont(new Font(name, style, f.getSize()));
        DpsGUI.editFont(new Font(name, style, f.getSize()));
        ParsePanelGUI.editFont(new Font(name, style, f.getSize()));
    }

    /**
     * Add text to the key pop text area.
     *
     * @param s The text to be added at the end of text area.
     */
    public static void appendTextAreaKeypop(String s) {
        if (textAreaKeypop != null) textAreaKeypop.append(s);
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