package example.gui;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.*;
import example.ExampleModTomato;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * Example GUI for Tomato mod.
 */
public class TomatoGUI {
    private static final int windowWidth = 600;
    private static final int windowHeight = 600;
    private static int fontSize = 12;
    private static Properties properties;
    private static JTextArea textAreaChat;
    private static JTextArea textAreaKeypop;
    private static JTextArea textAreaDPS;
    private static JTextArea textAreaQuests;
    private static JLabel statusLabel, dpsLabel;
    private static JFrame frame;
    private JMenuBar jMenuBar;
    private JPanel mainPanel, dpsPanel, dpsTopPanel;
    private TomatoMenuBar menuBar;
    private Point center;
    private Image icon;
    private JButton next, prev;

    /*
     * Load the properties as the GUI loads to set the preset options by the user.
     */
    static {
        properties = new Properties();
        try {
            FileReader reader = new FileReader("realmShark.properties");
            properties.load(reader);
        } catch (IOException ignored) {
        }
    }

    /**
     * Create main panel and initializes the GUI for the example Tomato.
     */
    public void create() {
        loadThemePreset();
        JTabbedPane tabbedPane = new JTabbedPane();
        textAreaChat = new JTextArea();
        tabbedPane.addTab("Chat", createTextArea(textAreaChat));

        textAreaKeypop = new JTextArea();
        tabbedPane.addTab("Key-pops", createTextArea(textAreaKeypop));

        textAreaQuests = new JTextArea();
        tabbedPane.addTab("Quests", createTextArea(textAreaQuests));

        next = new JButton("  Next  ");
        prev = new JButton("Previous");
        dpsLabel = new JLabel("1/1");

        dpsTopPanel = new JPanel();
        dpsTopPanel.setLayout(new BoxLayout(dpsTopPanel, BoxLayout.X_AXIS));
        dpsTopPanel.add(Box.createHorizontalGlue());
        dpsTopPanel.add(prev);
        dpsTopPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        dpsTopPanel.add(dpsLabel);
        dpsTopPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        dpsTopPanel.add(next);
        dpsTopPanel.add(Box.createHorizontalGlue());

        dpsPanel = new JPanel();
        dpsPanel.setLayout(new BorderLayout());
        dpsPanel.add(dpsTopPanel, BorderLayout.NORTH);
        textAreaDPS = new JTextArea();
        dpsPanel.add(createTextArea(textAreaDPS), BorderLayout.CENTER);
        textAreaDPS.setEnabled(false);
        tabbedPane.addTab("DPS Logger", dpsPanel);

        next.addActionListener(event -> ExampleModTomato.nextDpsLogDungeon());
        prev.addActionListener(event -> ExampleModTomato.previousDpsLogDungeon());

        center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        menuBar = new TomatoMenuBar();

        statusLabel = new JLabel(" Network Monitor: OFF");

        mainPanel = new JPanel();
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        icon = Toolkit.getDefaultToolkit().getImage(ExampleModTomato.imagePath);
        jMenuBar = menuBar.make();
        makeFrame();
        loadFontSizePreset();

        frame.setVisible(true);
    }

    /**
     * Method to create text areas.
     *
     * @param textArea Text area object.
     * @return Scroll pane object to add to a parent object.
     */
    private JScrollPane createTextArea(JTextArea textArea) {
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
    private void loadThemePreset() {
        if (properties == null) {
            LafManager.install(new DarculaTheme());
            return;
        }

        String theme = properties.getProperty("theme");
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
        String fontSize = properties.getProperty("fontSize");
        int fs = TomatoGUI.fontSize;
        try {
            fs = Integer.parseInt(fontSize);
        } catch (Exception ignored) {
        }

        fontSizeTextAreas(fs);
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
        textAreaDPS.setFont(new Font(f.getName(), f.getStyle(), size));
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
     * Sets the text of DPS logger text area.
     *
     * @param text       Sets the text of text area.
     * @param label      Sets the text label showing the page being viewed.
     * @param selectable Sets if the text area should be selectable.
     */
    public static void setTextAreaAndLabelDPS(String text, String label, boolean selectable) {
        if (textAreaDPS != null && text != null) textAreaDPS.setText(text);
        if (dpsLabel != null && label != null) dpsLabel.setText(label);
        if (textAreaDPS != null) textAreaDPS.setEnabled(selectable);
    }

    /**
     * Sets the text of Quests text area.
     *
     * @param s Sets the text of text area.
     */
    public static void setTextAreaQuests(String s) {
        if (textAreaQuests != null) textAreaQuests.setText(s);
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
     * Sets a preset needed when reloading the program.
     *
     * @param name  Name of the property.
     * @param value Value of the property.
     */
    public static void setProperties(String name, String value) {
        properties.setProperty(name, value);
        try {
            properties.store(new FileWriter("realmShark.properties"), "Realm shark properties");
        } catch (IOException ignored) {
        }
    }

    /**
     * Gets the property value by the name of the property.
     *
     * @param name Name of the property
     * @return Value of the property.
     */
    public static String getProperty(String name) {
        if (properties == null) return null;
        return properties.getProperty(name);
    }
}