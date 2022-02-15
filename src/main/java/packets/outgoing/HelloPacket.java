package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Sent to prompt the server to accept the connection of an account
 * and reply with a `MapInfoPacket`.
 */
public class HelloPacket extends Packet {
    /**
     * The current build version of RotMG.
     */
    public String buildVersion;
    /**
     * The id of the map to connect to.
     */
    public int gameId;
    /**
     * The access token from the AppEngine used to login
     */
    public String accessToken;
    /**
     * The key time of the `key` being used.
     */
    public int keyTime;
    /**
     * The key of the map to connect to.
     */
    public byte[] key;
    /**
     * > Unknown.
     */
    public String mapJSON;
    /**
     * > Unknown.
     */
    public String entryTag;
    /**
     * The platform the game is played on
     */
    public String gameNet;
    /**
     * > Unknown.
     */
    public String gameNetUserId;
    /**
     * The platform the game is played on
     */
    public String playPlatform;
    /**
     * > Unknown
     */
    public String platformToken;
    /**
     * > Unknown
     */
    public String userToken;
    /**
     * The client token (hwid) of the Unity client
     */
    public String clientToken;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        buildVersion = buffer.readString();
        gameId = buffer.readInt();
        accessToken = buffer.readString();
        keyTime = buffer.readInt();
        key = buffer.readByteArray();
        mapJSON = buffer.readStringUTF32();
        entryTag = buffer.readString();
        gameNet = buffer.readString();
        gameNetUserId = buffer.readString();
        playPlatform = buffer.readString();
        platformToken = buffer.readString();
        userToken = buffer.readString();
        clientToken = buffer.readString();
    }

}