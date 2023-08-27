package packets.data;

import packets.reader.BufferReader;

import java.io.Serializable;

/**
 * Death fame data storing name and fame given on death
 */
public class FameData implements Serializable {

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

    @Override
    public String toString() {
        return "FameData{" +
                "\n   achievement=" + achievement +
                "\n   fameLevel=" + fameLevel +
                "\n   fameAdded=" + fameAdded;
    }
}
