package tomato.gui.dps;

import packets.incoming.NotificationPacket;
import tomato.backend.data.Entity;
import tomato.gui.TomatoGUI;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class StringDpsGUI extends DisplayDpsGUI {

    private static JTextArea textAreaDPS;

    public StringDpsGUI() {
        setLayout(new BorderLayout());
        textAreaDPS = new JTextArea();
        add(TomatoGUI.createTextArea(textAreaDPS), BorderLayout.CENTER);
        textAreaDPS.setEnabled(false);

//        JButton button = new JButton("Experimental Icon Display (laggy)");
//        button.addActionListener(e -> clicked());
//        add(button, BorderLayout.SOUTH);
    }

//    private void clicked() {
//        DpsGUI.setDisplayAsIcon();
//    }

    /**
     * Sets the text of DPS logger text area.
     *
     * @param text       Sets the text of text area.
     * @param selectable Sets if the text area should be selectable.
     */
    private void setTextAreaAndLabelDPS(String text, boolean selectable) {
        if (textAreaDPS != null && text != null) textAreaDPS.setText(text);
        if (textAreaDPS != null) textAreaDPS.setEnabled(selectable);
    }

    @Override
    protected void renderData(Entity[] data, ArrayList<NotificationPacket> notifications, boolean isLive) {
        setTextAreaAndLabelDPS(DpsToString.stringDmgRealtime(data, notifications), !isLive);
    }

    /**
     * Set font size or name of text area.
     */
    @Override
    protected void editFont(Font font) {
        textAreaDPS.setFont(font);
    }
}
