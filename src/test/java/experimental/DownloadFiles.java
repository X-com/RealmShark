package experimental;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class DownloadFiles {

    private static String xml = "https://rotmg-mirror.github.io/rotmg-metadata/assets/production/xml/";
    private static String xmlEquip = "equip.xml";
    private static String xmlEquipmentsets = "equipmentsets.xml";
    private static String xmlSkins = "skins.xml";
    private static String xmlPlayers = "players.xml";

    private static String atlases = "https://rotmg-mirror.github.io/rotmg-metadata/assets/production/atlases/";
    private static String atlasManifest = "assets_manifest.xml";
    private static String atlasCharacters = "characters.png";
    private static String atlasMasks = "characters_masks.png";
    private static String atlasTiles = "groundTiles.png";
    private static String atlasObjects = "mapObjects.png";
    private static String atlasSpritesheet = "spritesheet.json";

    public void downloadFile(String url, String out) {
        try {
            File f = new File(out);

            if (!f.exists()) {
                f.getParentFile().mkdirs();
            } else {
                return;
            }

            URL website = new URL(url);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(out);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new DownloadFiles().downloadFile(xml + xmlEquip, "assets/" + xmlEquip);
        new DownloadFiles().downloadFile(xml + xmlEquipmentsets, "assets/" + xmlEquipmentsets);
        new DownloadFiles().downloadFile(atlases + atlasObjects, "assets/" + atlasObjects);
        new DownloadFiles().downloadFile(atlases + atlasSpritesheet, "assets/" + atlasSpritesheet);
    }
}
