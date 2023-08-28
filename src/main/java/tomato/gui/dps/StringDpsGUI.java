package tomato.gui.dps;

import packets.incoming.MapInfoPacket;
import packets.incoming.NotificationPacket;
import tomato.backend.data.Entity;
import tomato.backend.data.TomatoData;
import tomato.gui.TomatoGUI;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StringDpsGUI extends DisplayDpsGUI {

    private static JTextArea textAreaDPS;
    private final TomatoData data;
    private final JButton button;
    private boolean freeze;

    public StringDpsGUI(TomatoData data) {
        this.data = data;

        setLayout(new BorderLayout());
        textAreaDPS = new JTextArea();
        add(TomatoGUI.createTextArea(textAreaDPS), BorderLayout.CENTER);
        textAreaDPS.setEnabled(false);

        button = new JButton("Freeze");
        button.addActionListener(e -> clicked());
        add(button, BorderLayout.SOUTH);
    }

    private void clicked() {
        if (freeze) {
            button.setText("Freeze");
            freeze = false;
        } else {
            button.setText("Unfreeze");
            freeze = true;
        }
    }

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
    protected void renderData(MapInfoPacket map, List<Entity> sortedEntityHitList, ArrayList<NotificationPacket> notifications, long totalDungeonPcTime, boolean isLive) {
        if (freeze && isLive && button.isVisible()) {
            return;
        }
        button.setVisible(isLive);
        setTextAreaAndLabelDPS(DpsToString.stringDmgRealtime(map, sortedEntityHitList, notifications, data.player, totalDungeonPcTime), !isLive);
    }

    /**
     * Set font size or name of text area.
     */
    @Override
    protected void editFont(Font font) {
        textAreaDPS.setFont(font);
    }
}
