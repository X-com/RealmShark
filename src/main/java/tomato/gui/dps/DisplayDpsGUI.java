package tomato.gui.dps;

import packets.incoming.NotificationPacket;
import tomato.backend.data.Entity;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class DisplayDpsGUI extends JPanel {

    abstract protected void renderData(List<Entity> sortedEntityHitList, ArrayList<NotificationPacket> notifications, boolean isLive);

    abstract protected void editFont(Font font);
}
