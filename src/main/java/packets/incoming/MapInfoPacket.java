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
    public int difficulty;
    /**
     * The seed value for the client's PRNG
     */
    public long fp;
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
     * The number of players allowed in this map
     */
    public short maxPlayers;
    /**
     * The time the connection to the game was started
     */
    public long gameOpenedTime;

    @Override
    public void deserialize(PBuffer buffer) {
        width = buffer.readInt();
        height = buffer.readInt();
        name = buffer.readString();
        displayName = buffer.readString();
        realmName = buffer.readString();
        fp = buffer.readUnsignedInt();
        background = buffer.readInt();
        difficulty = buffer.readInt();
        allowPlayerTeleport = buffer.readBoolean();
        showDisplays = buffer.readBoolean();
        maxPlayers = buffer.readShort();
        gameOpenedTime = buffer.readUnsignedInt();
    }
}
