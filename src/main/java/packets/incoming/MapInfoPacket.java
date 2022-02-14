package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Received in response to the `HelloPacket`
 */
public class MapInfoPacket extends Packet {
    /**
     * The width of the map
     */
    public int width;
    /**
     * The height of the map
     */
    public int height;
    /**
     * The name of the map
     */
    public String name;
    /**
     * > Unknown.
     */
    public String displayName;
    /**
     * The name of the realm
     */
    public String realmName;
    /**
     * The difficulty rating of the map
     */
    public float difficulty;
    /**
     * The seed value for the client's PRNG
     */
    public long seed;
    /**
     * > Unknown
     */
    public int background;
    /**
     * Whether or not players can teleport in the map
     */
    public boolean allowPlayerTeleport;
    /**
     * > Unknown
     */
    public boolean showDisplays;
    /**
     * unknown
     */
    public boolean unknownBoolean;
    /**
     * The int of players allowed in this map
     */
    public short maxPlayers;
    /**
     * The time the connection to the game was started
     */
    public long gameOpenedTime;
    /**
     * Build version
     */
    public String buildVersion;
    /**
     * unknown
     */
    public int unknownInt;
    /**
     * unknown
     */
    public String unknownString;

    @Override
    public void deserialize(PBuffer buffer) throws Exception {
        width = buffer.readInt();
        height = buffer.readInt();
        name = buffer.readString();
        displayName = buffer.readString();
        realmName = buffer.readString();
        seed = buffer.readUnsignedInt();
        background = buffer.readInt();
        difficulty = buffer.readFloat();
        allowPlayerTeleport = buffer.readBoolean();
        showDisplays = buffer.readBoolean();
        unknownBoolean = buffer.readBoolean();
        maxPlayers = buffer.readShort();
        gameOpenedTime = buffer.readUnsignedInt();
        buildVersion = buffer.readString();
        unknownInt = buffer.readInt();
        unknownString = buffer.readString();
        System.out.print(unknownString.equals("") ? "" : "\n"+unknownString);
//        public string[] BEJAFOJGEJJ; // 0x58
    }

    int d = 0;
    public void print() {
        System.out.println(d++);
    }
    public String toString() {
        return String.format("%d %d %s %s %s %d %d %f %b %b %b %d %d %s %d %s\n", width, height, name, displayName, realmName, seed, background, difficulty, allowPlayerTeleport, showDisplays, unknownBoolean, maxPlayers, gameOpenedTime, buildVersion, unknownInt, unknownString);
    }
}