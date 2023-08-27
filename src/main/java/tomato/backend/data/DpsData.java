package tomato.backend.data;

import packets.incoming.MapInfoPacket;
import packets.incoming.NotificationPacket;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class DpsData implements Serializable {

    public MapInfoPacket map;
    public HashMap<Integer, Entity> hitList;
    public ArrayList<NotificationPacket> deathNotifications;
    public long totalDungeonPcTime;
    public long dungeonStartTime;

    public DpsData(MapInfoPacket m, HashMap<Integer, Entity> entityHitList, ArrayList<NotificationPacket> deathNotifications, long totalDungeonPcTime, long timePcFirst) {
        this.map = m;
        this.hitList = entityHitList;
        this.deathNotifications = deathNotifications;
        this.totalDungeonPcTime = totalDungeonPcTime;
        this.dungeonStartTime = timePcFirst;
    }
}
