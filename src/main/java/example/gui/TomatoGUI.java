package example.gui;

import example.ExampleModTomato;

import javax.swing.*;
import java.awt.*;

/**
 * Example GUI for Tomato mod.
 */
public class TomatoGUI {
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

    /**
     * Create main panel and initializes the GUI for the example Tomato.
     */
    public void create() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

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
        frame.setVisible(true);
    }

    /**
     * Creates the frame with icon.
     */
    public void makeFrame() {
        frame = new JFrame("Tomato");
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
}