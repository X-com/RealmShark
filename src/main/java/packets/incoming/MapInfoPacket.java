package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Received in response to the `HelloPacket`
 */
public class MapInfoPacket extends Packet {
    public int width;
    public int height;
    public String name;
    public String displayName;
    public String realmName;
    public int fp;
    @Deprecated
    public int seed;
    public int background;
    public float difficulty;
    public boolean allowPlayerTeleport;
    public boolean noSave;
    public boolean showDisplays;
    public short maxPlayerCount;
    public int gameOpenedTime;
    public String versionNumber;
    public short unknown1;
    public short viewDistance;
    public boolean unknown2;
    public int unknownInt;

    /**
     * Dungeon modifiers (can be 0–4).
     */
    public String dungeonModifiers;
    public String dungeonModifiers2;
    public String dungeonModifiers3;
    public String dungeonModifiers4;

    /**
     * Dungeon grade (e.g. "S", "A", "B", "C").
     */
    public String dungeonGrade;

    public short bgColor;
    public int maxRealmScore = -1;
    public int currentRealmScore = -1;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        width = buffer.readInt();
        height = buffer.readInt();
        name = buffer.readString();
        displayName = buffer.readString();
        realmName = buffer.readString();

        fp = buffer.readInt();
        seed = fp;

        background = buffer.readInt();
        difficulty = buffer.readFloat();
        allowPlayerTeleport = buffer.readBoolean();
        noSave = buffer.readBoolean();
        showDisplays = buffer.readBoolean();
        maxPlayerCount = buffer.readShort();
        gameOpenedTime = buffer.readInt();
        versionNumber = buffer.readString();
        unknown1 = buffer.readShort();
        viewDistance = buffer.readShort();
        unknown2 = buffer.readBoolean();
        unknownInt = buffer.readInt();

        // Modifiers string(s)
        String modifiers = buffer.readString();
        String[] parts = modifiers.split(";", -1); // allow unlimited, keep empties

        dungeonModifiers = parts.length > 0 ? parts[0] : null;
        dungeonModifiers2 = parts.length > 1 ? parts[1] : null;
        dungeonModifiers3 = parts.length > 2 ? parts[2] : null;
        dungeonModifiers4 = parts.length > 3 ? parts[3] : null;

        // Handle grade safely
        if (parts.length > 4 && parts[4].startsWith("|")) {
            dungeonGrade = parts[4].substring(1); // remove leading "|"
        } else {
            String[] lastSplit = null;
            if (dungeonModifiers4 != null && dungeonModifiers4.contains("|")) {
                lastSplit = dungeonModifiers4.split("\\|", 2);
                dungeonModifiers4 = lastSplit[0];
            } else if (dungeonModifiers3 != null && dungeonModifiers3.contains("|")) {
                lastSplit = dungeonModifiers3.split("\\|", 2);
                dungeonModifiers3 = lastSplit[0];
            } else if (dungeonModifiers2 != null && dungeonModifiers2.contains("|")) {
                lastSplit = dungeonModifiers2.split("\\|", 2);
                dungeonModifiers2 = lastSplit[0];
            } else if (dungeonModifiers != null && dungeonModifiers.contains("|")) {
                lastSplit = dungeonModifiers.split("\\|", 2);
                dungeonModifiers = lastSplit[0];
            }
            if (lastSplit != null) {
                dungeonGrade = lastSplit[1]; // no leading "|"
            }
        }

        bgColor = buffer.readShort();

        if (buffer.getRemainingBytes() >= 8) {
            maxRealmScore = buffer.readInt();
            currentRealmScore = buffer.readInt();
        }
    }

    @Override
    public String toString() {
        return "MapInfoPacket{" +
                "\n   width=" + width +
                "\n   height=" + height +
                "\n   name=" + name +
                "\n   displayName=" + displayName +
                "\n   realmName=" + realmName +
                "\n   fp=" + fp +
                "\n   seed (alias)=" + seed +
                "\n   background=" + background +
                "\n   difficulty=" + difficulty +
                "\n   allowPlayerTeleport=" + allowPlayerTeleport +
                "\n   noSave=" + noSave +
                "\n   showDisplays=" + showDisplays +
                "\n   maxPlayerCount=" + maxPlayerCount +
                "\n   gameOpenedTime=" + gameOpenedTime +
                "\n   versionNumber=" + versionNumber +
                "\n   unknown1=" + unknown1 +
                "\n   viewDistance=" + viewDistance +
                "\n   unknown2=" + unknown2 +
                "\n   unknownInt=" + unknownInt +
                "\n   dungeonModifiers=" + dungeonModifiers +
                "\n   dungeonModifiers2=" + dungeonModifiers2 +
                "\n   dungeonModifiers3=" + dungeonModifiers3 +
                "\n   dungeonModifiers4=" + dungeonModifiers4 +
                "\n   dungeonGrade=" + dungeonGrade +
                "\n   bgColor=" + bgColor +
                "\n   maxRealmScore=" + maxRealmScore +
                "\n   currentRealmScore=" + currentRealmScore +
                "\n}";
    }
}
