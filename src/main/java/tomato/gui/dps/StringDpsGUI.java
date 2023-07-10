package tomato.gui.dps;

import tomato.backend.data.Entity;
import tomato.gui.TomatoGUI;

import javax.swing.*;
import java.awt.*;

public class StringDpsGUI extends DisplayDpsGUI {

    private static JTextArea textAreaDPS;

    public StringDpsGUI() {
        setLayout(new BorderLayout());
        textAreaDPS = new JTextArea();
        add(TomatoGUI.createTextArea(textAreaDPS), BorderLayout.CENTER);
        textAreaDPS.setEnabled(false);
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
    protected void renderData(Entity[] data, boolean isLive) {
        setTextAreaAndLabelDPS(DpsToString.stringDmgRealtime(data), !isLive);
    }

    /**
     * Set font size or name of text area.
     */
    @Override
    protected void editFont(Font font) {
        textAreaDPS.setFont(font);
    }
}
