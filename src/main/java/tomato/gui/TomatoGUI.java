package tomato.gui;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.*;
import packets.data.QuestData;
import tomato.Tomato;
import tomato.logic.backend.data.TomatoData;
import util.PropertiesManager;
import util.Pair;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

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
    private static JTextArea textAreaDPS;
    private static JTextField textFilter;
    private static JCheckBox textFilterToggle;
    private static JLabel statusLabel, dpsLabel;
    private static JFrame frame;
    private static SecurityGUI securityPanel;
    private static CharacterPanelGUI characterPanel;
    private static QuestGUI questPanel;
    private static FameTrackerGUI fameTracker;
    private JMenuBar jMenuBar;
    private JPanel mainPanel, dpsPanel, dpsTopPanel;
    private TomatoMenuBar menuBar;
    private Point center;
    private Image icon;
    private JButton next, prev;

    public TomatoGUI(TomatoData data) {
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

        securityPanel = new SecurityGUI();
        tabbedPane.addTab("Security", securityPanel);

        characterPanel = new CharacterPanelGUI();
        tabbedPane.addTab("Characters", characterPanel);

        fameTracker = new FameTrackerGUI();
        tabbedPane.addTab("Fame", fameTracker);

        questPanel = new QuestGUI();
        tabbedPane.addTab("Daily Quests", questPanel);

        next = new JButton("  Next  ");
        prev = new JButton("Previous");
        dpsLabel = new JLabel("1/1");
        textFilter = new JTextField();
        textFilter.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
                PropertiesManager.setProperties("nameFilter", textFilter.getText());
                Tomato.updateDpsWindow();
            }
        });
        textFilterToggle = new JCheckBox();
        textFilterToggle.setSelected(true);
        textFilterToggle.addActionListener(event -> {
            boolean selected = textFilterToggle.isSelected();
            textFilter.setEnabled(selected);
            PropertiesManager.setProperties("toggleFilter", selected ? "T" : "F");
            Tomato.updateDpsWindow();
        });

        dpsTopPanel = new JPanel();
        dpsTopPanel.setLayout(new BoxLayout(dpsTopPanel, BoxLayout.X_AXIS));
        dpsTopPanel.add(Box.createHorizontalGlue());
        dpsTopPanel.add(textFilterToggle);
        dpsTopPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        dpsTopPanel.add(textFilter);
        dpsTopPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        dpsTopPanel.add(prev);
        dpsTopPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        dpsTopPanel.add(dpsLabel);
        dpsTopPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        dpsTopPanel.add(next);
        dpsTopPanel.add(Box.createHorizontalGlue());

        dpsPanel = new JPanel();
        dpsPanel.setLayout(new BorderLayout());
        dpsPanel.add(dpsTopPanel, BorderLayout.NORTH);
        textAreaDPS = new JTextArea();
        dpsPanel.add(createTextArea(textAreaDPS), BorderLayout.CENTER);
        textAreaDPS.setEnabled(false);
        tabbedPane.addTab("DPS Logger", dpsPanel);

        next.addActionListener(event -> Tomato.nextDpsLogDungeon());
        prev.addActionListener(event -> Tomato.previousDpsLogDungeon());

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
        loadFilterPreset();
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
     * Loads the filter preset chosen by the user.
     */
    private void loadFilterPreset() {
        String nameFilter = PropertiesManager.getProperty("nameFilter");
        String toggleFilter = PropertiesManager.getProperty("toggleFilter");

        if (nameFilter != null) {
            textFilter.setText(nameFilter);
        }
        if (toggleFilter != null) {
            boolean toggled = toggleFilter.equals("T");
            textFilterToggle.setSelected(toggled);
            textFilter.setEnabled(toggled);
        }
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
     * Set font size of text area.
     */
    public static void fontNameTextAreas(String name, int style) {
        Font f = textAreaChat.getFont();
        textAreaChat.setFont(new Font(name, style, f.getSize()));
        textAreaKeypop.setFont(new Font(name, style, f.getSize()));
        textAreaDPS.setFont(new Font(name, style, f.getSize()));
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
     * Updates the questGUI with quest data.
     *
     * @param q Quest data received when visiting quest room.
     */
    public static void updateQuests(QuestData[] q) {
        questPanel.update(q);
    }

    /**
     * Sets the equipment of parse players.
     *
     * @param data Player data to set the equipment of parse players.
     */
    public static void setParsePlayers(ArrayList<Pair<String, int[]>> data) {
//        if (securityPanel != null) securityPanel.addPlayers(data);
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
     * Getter for the character panel.
     */
    public static CharacterPanelGUI getCharacterPanel() {
        return characterPanel;
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