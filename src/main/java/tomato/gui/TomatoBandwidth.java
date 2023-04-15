package tomato.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Info panel frame builder.
 * TODO: fix this horrible code
 */
public class TomatoBandwidth extends JFrame {
    private static TomatoBandwidth bandwidth;
    private static JScrollPane scroll;
    private static JTextArea infoArea;
    private static boolean exists = false;

    /**
     * Bandwidth window to show data.
     *
     * @param frame Main frame of the tomato window.
     * @return The Info panel object.
     */
    public static void make(JFrame frame) {
        if(exists) return;
        exists = true;
        bandwidth = new TomatoBandwidth();
        bandwidth.setTitle("Net Traffic");
        bandwidth.setSize(350, 450);
        bandwidth.setLocation(frame.getX() + frame.getWidth() / 2 - bandwidth.getWidth() / 2,
                frame.getY() + frame.getHeight() / 2 - bandwidth.getHeight() / 2);

        infoArea = new JTextArea();
        infoArea.setEnabled(false);
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        Font f = infoArea.getFont();
        infoArea.setFont(new Font("Monospaced", f.getStyle(), f.getSize()));
        scroll = new JScrollPane(infoArea);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setAutoscrolls(true);
        new SmartScroller(scroll);

        bandwidth.add(scroll);

        bandwidth.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        bandwidth.setVisible(true);
    }

    public void dispose() {
        exists = false;
        infoArea = null;
        super.dispose();
    }

    /**
     * Setter method to set the text area.
     *
     * @param s String to be set in the info area.
     */
    public static void setInfo(String s) {
        if (infoArea != null) infoArea.setText(s);
    }
}
