package example.gui;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.*;
import example.ExampleModTomato;

import javax.swing.*;
import java.awt.*;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Example GUI for Tomato mod.
 */
public class TomatoGUI {
    private static Properties properties;
    private static JTextArea textAreaChat;
    private static JTextArea textAreaKeypop;
    private static JTextArea textAreaDPS;
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
        try {
            FileReader reader = new FileReader("realmShark.properties");
            properties = new Properties();
            properties.load(reader);
        } catch (IOException ignored) {
        }
    }

    /**
     * Create main panel and initializes the GUI for the example Tomato.
     */
    public void create() {
        JTabbedPane tabbedPane = new JTabbedPane();
        textAreaChat = new JTextArea();
        textAreaChat.setEnabled(true);
        textAreaChat.setEditable(false);
        textAreaChat.setLineWrap(true);
        textAreaChat.setWrapStyleWord(true);
        JScrollPane scrollChat = new JScrollPane(textAreaChat);
        scrollChat.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollChat.setAutoscrolls(true);
        new SmartScroller(scrollChat);
        tabbedPane.addTab("Chat", scrollChat);

        textAreaKeypop = new JTextArea();
        textAreaKeypop.setEnabled(true);
        textAreaKeypop.setEditable(false);
        textAreaKeypop.setLineWrap(true);
        textAreaKeypop.setWrapStyleWord(true);
        JScrollPane scrollKeypop = new JScrollPane(textAreaKeypop);
        scrollKeypop.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollKeypop.setAutoscrolls(true);
        new SmartScroller(scrollKeypop);
        tabbedPane.addTab("Key-pops", scrollKeypop);

        textAreaDPS = new JTextArea();
        textAreaDPS.setEnabled(true);
        textAreaDPS.setEditable(false);
        textAreaDPS.setLineWrap(true);
        textAreaDPS.setWrapStyleWord(true);
        JScrollPane scrollDPS = new JScrollPane(textAreaDPS);
        scrollDPS.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollDPS.setAutoscrolls(true);
        new SmartScroller(scrollDPS);
        next = new JButton("  Next  ");
        prev = new JButton("Previous");
        dpsLabel = new JLabel("0/0");
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
        dpsPanel.add(scrollDPS, BorderLayout.CENTER);
        tabbedPane.addTab("DPS Logger", dpsPanel);
        next.addActionListener(event -> ExampleModTomato.nextDpsLogDungeon());
        prev.addActionListener(event -> ExampleModTomato.previousDpsLogDungeon());


        center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        menuBar = new TomatoMenuBar();
        jMenuBar = menuBar.make();

        statusLabel = new JLabel(" Network Tap: OFF");

        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        icon = Toolkit.getDefaultToolkit().getImage(ExampleModTomato.imagePath);
        makeFrame();
        loadPresets();
        frame.setVisible(true);
    }

    /**
     * Loads the presets chosen by the user.
     */
    private void loadPresets() {
        if (properties == null) {
            LafManager.install(new DarculaTheme());
            return;
        }

        String theme = properties.getProperty("theme");

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

        String fontSize = properties.getProperty("fontSize");
        int fs = 0;
        try {
            fs = Integer.parseInt(fontSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        fontSizeTextAreas(fs);
    }

    /**
     * Creates the frame with icon.
     */
    public void makeFrame() {
        frame = new JFrame("    Tomato    ");
        frame.setIconImage(icon);
        frame.setLocation(center.x / 2, 25);
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(jMenuBar);
        frame.setJMenuBar(jMenuBar);
        menuBar.setFrame(frame);
        frame.setContentPane(mainPanel);
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
     * @param s Sets the text of text area.
     */
    public static void setTextAreaAndLabelDPS(String s, String l) {
        if (textAreaDPS != null) textAreaDPS.setText(s);
        if (dpsLabel != null) dpsLabel.setText(l);
    }

    /**
     * Updates the state of the sniffer at the bottom label to show if running or off.
     *
     * @param running Set the label to running or off.
     */
    public static void setStateOfSniffer(boolean running) {
        statusLabel.setText(" Network Tap: " + (running ? "RUNNING" : "OFF"));
    }

    /**
     * Sets a preset needed when reloading the program.
     *
     * @param name  Name of the property.
     * @param value Value of the property.
     */
    public static void setProperties(String name, String value) {
        properties.setProperty(name, value);
    }

    /**
     * Gets the property value by the name of the property.
     *
     * @param name Name of the property
     * @return Value of the property.
     */
    public static String getPropertie(String name) {
        if (properties == null) return null;
        return properties.getProperty(name);
    }
}