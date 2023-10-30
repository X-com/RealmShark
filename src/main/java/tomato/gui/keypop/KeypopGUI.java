package tomato.gui.keypop;

import assets.AssetMissingException;
import assets.IdToAsset;
import packets.data.enums.NotificationEffectType;
import packets.incoming.NotificationPacket;
import util.Util;

import javax.swing.*;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//message={"k":"s.dungeon_opened_by","t":{"player":"PLAYERNAME",}}   key pop
//message={"k":"s.something_by_player","t":{"name":"The Shield Monument has been activated","player":"PLAYERNAME",}}  rune pop
//message={"k":"s.dungeon_unlocked_by","t":{"name":"The Void","player":"PLAYERNAME",}}   vial pop
//message={"k":"s.dungeon_unlocked_by","t":{"name":"Wine Cellar","player":"PLAYERNAME",}}   Inc pop

/**
 * GUI class for popping dungeons.
 */
public class KeypopGUI extends JPanel {

    private static JTextArea textAreaKeypop;

    private static final Pattern keypopParse = Pattern.compile("[^ ]*\"player\":\"([A-Za-z]*)[^ ]*");
    private static final Pattern nonkeypopParse = Pattern.compile("[^ ]*\"name\":\"([A-Za-z ]*)\",\"player\":\"([A-Za-z]*)[^ ]*");

    public KeypopGUI() {
        setLayout(new BorderLayout());
        textAreaKeypop = new JTextArea();
        textAreaKeypop.setEditable(false);
        add(textAreaKeypop);
    }

    /**
     * Packet parser for notification packets that will be used to add key, vial, rune or inc pops.
     *
     * @param packet Notification packet containing info about who pops keys, vial, runes or inc pops.
     */
    public static void packet(NotificationPacket packet) {
        if (packet.effect == NotificationEffectType.PortalOpened) {
            String msg = packet.message;
            Matcher m = keypopParse.matcher(msg);
            if (m.matches()) {
                String playerName = m.group(1);
                try {
                    appendTextAreaKeypop(String.format("%s [%s]: %s\n", Util.getHourTime(), playerName, IdToAsset.objectName(packet.pictureType)));
                } catch (AssetMissingException e) {
                    e.printStackTrace();
                }
            }
        } else if (packet.effect == NotificationEffectType.ServerMessage && packet.message != null) {
            String msg = packet.message;
            Matcher m = nonkeypopParse.matcher(msg);
            if (m.matches()) {
                String type = m.group(1);
                String playerName = m.group(2);
                String pop = null;
                if (type.contains("Monument has been activated")) {
                    pop = type.split(" ")[1] + " Rune";
                } else if (type.equals("The Void")) {
                    pop = "Vial";
                } else if (type.equals("Wine Cellar")) {
                    pop = "Inc";
                }
                if (pop != null) {
                    appendTextAreaKeypop(String.format("%s [%s]: %s\n", Util.getHourTime(), playerName, pop));
                }
            }
        }
    }

    /**
     * Add text to the key pop text area.
     *
     * @param s The text to be added at the end of text area.
     */
    public static void appendTextAreaKeypop(String s) {
        if (textAreaKeypop != null) textAreaKeypop.append(s);
    }

    /**
     * Sets the font of the text area.
     *
     * @param font Font to be set.
     */
    public static void editFont(Font font) {
        textAreaKeypop.setFont(font);
    }
}