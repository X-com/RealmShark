package packets.data;

import packets.reader.BufferReader;
import util.Util;

public class ObjectStatusData {
    /**
     * The object id of the object which this status is for
     */
    public int objectId;
    /**
     * The position of the object which this status is for
     */
    public WorldPosData pos;
    /**
     * A list of stats for the object which this status is for
     */
    public StatData[] stats;

    /**
     * Deserializer method to extract data from the buffer.
     *
     * @param buffer Data that needs deserializing.
     * @return Returns this object after deserializing.
     */
    public ObjectStatusData deserialize(BufferReader buffer) {
        objectId = buffer.readCompressedInt();
        pos = new WorldPosData().deserialize(buffer);

        stats = new StatData[buffer.readCompressedInt()];
        for (int i = 0; i < stats.length; i++) {
            stats[i] = new StatData().deserialize(buffer);
        }

        return this;
    }

    @Override
    public String toString() {
        return  "\n    Id=" + objectId + " Loc=(" + pos.x + ", " + pos.y + ")" +
                (stats.length == 0 ? "" : Util.showAll(stats));
    }
}
