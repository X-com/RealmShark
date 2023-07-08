package tomato.backend.data;

import packets.incoming.MapInfoPacket;

import java.util.HashMap;

public class DpsData {

    public MapInfoPacket map;
    public HashMap<Integer, Entity> hitList;

    public DpsData(MapInfoPacket m, HashMap<Integer, Entity> entityHitList) {
        map = m;
        hitList = entityHitList;

    }
}
