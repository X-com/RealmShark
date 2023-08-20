package tomato.gui.dps;

import packets.incoming.MapInfoPacket;
import packets.incoming.NotificationPacket;
import tomato.backend.data.Entity;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class DisplayDpsGUI extends JPanel {

    abstract protected void renderData(MapInfoPacket map, List<Entity> sortedEntityHitList, ArrayList<NotificationPacket> notifications, long totalDungeonPcTime, boolean isLive);

    abstract protected void editFont(Font font);
}
