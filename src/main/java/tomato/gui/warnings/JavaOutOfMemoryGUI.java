package tomato.gui.warnings;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class JavaOutOfMemoryGUI {

    /**
     * Dialog to display crash if tomato runs out of memory.
     * Most memory crashes related to 32-bit java suggesting 64-bit install.
     */
    public static void crashDialog() {
        JEditorPane ep = new JEditorPane("text/html", "<html>Out of memory crash.<br>Please uninstall 32-bit and install 64-bit Java.<br><a href=\\\\\\\"https://www.java.com/en/download/manual.jsp\\\\\\\">Download java 64-bit</a></html>");
        ep.addHyperlinkListener(e -> {
            if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                try {
                    Desktop.getDesktop().browse(new URI("https://www.java.com/en/download/manual.jsp"));
                } catch (IOException | URISyntaxException ex) {
                    ex.printStackTrace();
                }
            }
        });
        ep.setEditable(false);
        JOptionPane.showMessageDialog(null, ep);
    }
}