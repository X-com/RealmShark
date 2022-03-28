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
    private static JLabel statusLabel;
    private static JFrame frame;
    private JMenuBar jMenuBar;
    private JPanel mainPanel;
    private TomatoMenuBar menuBar;
    private Point center;
    private Image icon;

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
     * Add text to the key pop text area.
     *
     * @param s The text to be added at the end of text area.
     */
    public static void appendTextAreaKeypop(String s) {
        if (textAreaKeypop != null) textAreaKeypop.append(s);
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