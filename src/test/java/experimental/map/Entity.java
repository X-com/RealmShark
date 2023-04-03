package experimental.map;

import packets.data.StatData;
import packets.data.enums.StatType;
import util.IdToName;

import java.util.Arrays;

public class Entity {

    int type;
    float x;
    float y;
    StatData[] stats;

    public Entity(String t, String x, String y, String[] s) {
        this.type = Integer.parseInt(t);
        this.x = Float.parseFloat(x);
        this.y = Float.parseFloat(y);
        this.stats = new StatData[s.length / 4];
        for (int i = 0; i < stats.length; i++) {
            StatData sd = new StatData();
            sd.statValue = Integer.parseInt(s[i * 4]);
            sd.statValueTwo = Integer.parseInt(s[i * 4 + 1]);
            sd.stringStatValue = s[i * 4 + 2];
            sd.statTypeNum = Integer.parseInt(s[i * 4 + 3]);
            sd.statType = StatType.byOrdinal(sd.statTypeNum);
            stats[i] = sd;
        }
    }

    @Override
    public String toString() {
        return "Entity{" +
                "\n   type=" + type + " " + IdToName.objectName(type) +
                "\n   x=" + x +
                "\n   y=" + y +
                "\n   stats=" + Arrays.toString(stats);
    }

    public double dist(float x, float y) {
        return Math.sqrt(Math.pow(this.x - x, 2) * Math.pow(this.y - y, 2));
    }

}
