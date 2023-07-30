package tomato.gui.dps;

import packets.incoming.NotificationPacket;
import tomato.backend.data.Entity;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public abstract class DisplayDpsGUI extends JPanel {

    abstract protected void renderData(Entity[] data, ArrayList<NotificationPacket> notifications, boolean isLive);

    abstract protected void editFont(Font font);
}
