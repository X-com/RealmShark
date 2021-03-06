package packets.incoming;

import packets.Packet;
import packets.data.FameData;
import packets.reader.BufferReader;

import java.util.Arrays;

/**
 * Received when a player dies
 */
public class DeathPacket extends Packet {
    /**
     * The account id of the player who died
     */
    public String accountId;
    /**
     * The character id of the player who died
     */
    public int unknownFameID1;
    /**
     * The cause of death
     */
    public String killedBy;
    /**
     * Unknown int
     */
    public int unknownFameID2;
    /**
     * Unknown short
     */
    public int unknownFameID3;
    /**
     * Death fame data
     */
    public FameData[] fameData;
    /**
     * Unknown String
     */
    public String unknownString;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        accountId = buffer.readString();
        unknownFameID1 = buffer.readCompressedInt();
        killedBy = buffer.readString();
        unknownFameID2 = buffer.readInt();
        unknownFameID3 = buffer.readCompressedInt();
        fameData = new FameData[buffer.readCompressedInt()];
        for (int i = 0; i < fameData.length; i++) {
            fameData[i] = new FameData().deserialize(buffer);
        }
        unknownString = buffer.readString();
    }

    @Override
    public String toString() {
        return "DeathPacket{" +
                "\n   accountId=" + accountId +
                "\n   unknownFameID1=" + unknownFameID1 +
                "\n   killedBy=" + killedBy +
                "\n   unknownFameID2=" + unknownFameID2 +
                "\n   unknownFameID3=" + unknownFameID3 +
                "\n   fameData=" + Arrays.toString(fameData) +
                "\n   unknownString=" + unknownString;
    }
}