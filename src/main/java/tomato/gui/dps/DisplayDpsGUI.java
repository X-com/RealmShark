package tomato.gui.dps;

import tomato.backend.data.Entity;

import javax.swing.*;
import java.awt.*;

public abstract class DisplayDpsGUI extends JPanel {

    abstract protected void renderData(Entity[] data, boolean isLive);

    abstract protected void editFont(Font font);
}
