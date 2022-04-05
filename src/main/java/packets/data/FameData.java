package packets.data;

import packets.reader.BufferReader;

/**
 * Death fame data storing name and fame given on death
 */
public class FameData {

    /**
     * Name of the achievement.
     */
    String achievement;
    /**
     * Fame level
     */
    int fameLevel;
    /**
     * Fame
     */
    int fameAdded;

    public FameData deserialize(BufferReader buffer) {
        achievement = buffer.readString();
        fameLevel = buffer.readCompressedInt();
        fameAdded = buffer.readCompressedInt();
        return this;
    }
}
