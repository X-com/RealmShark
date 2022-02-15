package example.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Example GUI for Tomato mod.
 */
public class TomatoGUI {
    private static JTextArea textArea;
    private static JLabel statusLabel;
    private JMenuBar jMenuBar;
    private JPanel mainPanel;
    private MenuBar menuBar;
    private JFrame frame;
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
        mainPanel = new JPanel();
        textArea = new JTextArea();

        center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        icon = Toolkit.getDefaultToolkit().getImage("tomatoIcon.png");
        menuBar = new example.gui.MenuBar();
        jMenuBar = menuBar.make();

        statusLabel = new JLabel(" Network Tap: OFF");
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(textArea, BorderLayout.CENTER);
        textArea.setEnabled(true);
        textArea.setEditable(false);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

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
     * Add text to the text area.
     *
     * @param s The text to be added at the end of text area.
     */
    public static void appendTextAreaText(String s) {
        textArea.append(s);
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