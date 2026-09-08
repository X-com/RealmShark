package tomato.realmshark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import packets.incoming.MapInfoPacket;
import util.StringXML;

public class ParseDungeon {

    private static final String MODS_XML_PATHS[] = {
        "assets/xml/mods.xml",
        "assets/xml/mods2.xml",
    };
    private static final String PORTAL_XML_PATH = "assets/xml/portals.xml";
    private static final HashMap<Integer, String> ID_TO_NAME_MODS =
        new HashMap<>();
    private static final HashMap<String, Integer> NAME_TO_ID_MODS =
        new HashMap<>();
    private static final HashMap<String, Integer> NAME_TO_ID_PORTAL =
        new HashMap<>();

    /**
     * Load Dungeon modifiers XML data to get names from file.
     */
    static {
        loadAssets();
    }

    /**
     * Parses the XML assets this class needs.
     *
     * Nothing in here may propagate an exception. This runs from a static
     * initializer, so a throw leaves the class permanently unusable for the
     * rest of the JVM's life - every later reference fails with
     * NoClassDefFoundError, which is how a single missing asset file took out
     * the whole loot display. Assets are legitimately absent before extraction,
     * so empty maps are a valid state: names simply resolve to nothing.
     */
    private static void loadAssets() {
        try {
            parseDungeonModifier();
        } catch (Throwable t) {
            System.out.println("[ParseDungeon] modifier parse failed: " + t);
        }
        try {
            parseDungeonPortalId();
        } catch (Throwable t) {
            System.out.println("[ParseDungeon] portal parse failed: " + t);
        }
    }

    private static void parseDungeonModifier() {
        for (String path : MODS_XML_PATHS) {
            // The client does not ship every mods file in every build -
            // mods2.xml is absent as of the current release. A missing
            // supplementary file must not be fatal.
            if (!new File(path).isFile()) {
                System.out.println(
                    "[ParseDungeon] " + path + " not present, skipping"
                );
                continue;
            }
            try {
                FileInputStream file = new FileInputStream(path);
                String result = new BufferedReader(new InputStreamReader(file))
                    .lines()
                    .collect(Collectors.joining("\n"));
                StringXML base = StringXML.getParsedXML(result);
                for (StringXML xml : base) {
                    if (Objects.equals(xml.name, "DungeonModifier")) {
                        DungeonModifier modifier = new DungeonModifier();

                        for (StringXML info : xml) {
                            if (Objects.equals(info.name, "id")) {
                                modifier.name = info.value;
                            }
                            if (Objects.equals(info.name, "type")) {
                                modifier.modId = Short.decode(info.value);
                            }
                        }
                        for (StringXML x : xml) {
                            if (Objects.equals(x.name, "Description")) {
                                modifier.description = x.children.get(0).value;
                            }
                        }
                        ID_TO_NAME_MODS.put(modifier.modId, modifier.name);
                        NAME_TO_ID_MODS.put(modifier.name, modifier.modId);
                    }
                }
            } catch (
                ParserConfigurationException
                | IOException
                | SAXException e
            ) {
                throw new RuntimeException(e);
            }
        }
        NAME_TO_ID_MODS.put("|S", -11);
        NAME_TO_ID_MODS.put("|A", -12);
        NAME_TO_ID_MODS.put("|B", -13);
        NAME_TO_ID_MODS.put("|C", -14);
        NAME_TO_ID_MODS.put("|D", -15);
    }

    private static void parseDungeonPortalId() {
        // Absent on a fresh install until assets are extracted. Same treatment
        // as the mods files: skip rather than throw.
        if (!new File(PORTAL_XML_PATH).isFile()) {
            System.out.println(
                "[ParseDungeon] " + PORTAL_XML_PATH + " not present, skipping"
            );
            return;
        }
        try {
            FileInputStream file = new FileInputStream(PORTAL_XML_PATH);
            String result = new BufferedReader(new InputStreamReader(file))
                .lines()
                .collect(Collectors.joining("\n"));
            StringXML base = StringXML.getParsedXML(result);
            for (StringXML xml : base) {
                if (Objects.equals(xml.name, "Object")) {
                    int id = 0;
                    String name = null;

                    for (StringXML info : xml) {
                        if (Objects.equals(info.name, "type")) {
                            id = Integer.decode(info.value);
                        }
                    }
                    for (StringXML x : xml) {
                        if (Objects.equals(x.name, "DungeonName")) {
                            name = x.children.get(0).value;
                        }
                    }
                    if (name != null && id != 0) {
                        NAME_TO_ID_PORTAL.put(name, id);
                    }
                }
            }
            NAME_TO_ID_PORTAL.put("Realm of the Mad God", 1796);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    public static int[] getModIds(String dungeonString) {
        if (dungeonString == null || dungeonString.isEmpty()) return new int[0];

        String[] split = dungeonString.split(";");

        int[] array = new int[split.length];
        for (int i = 0; i < split.length; i++) {
            String key = split[i];

            Integer integer = NAME_TO_ID_MODS.get(key);
            array[i] = integer;
        }

        return array;
    }

    /**
     * Returns the ID of the portal from the dungeon name.
     *
     * @param name Name of the portal.
     * @return ID of the dungeon portal
     */
    public static int getPortalId(String name) {
        Integer id = NAME_TO_ID_PORTAL.get(name);
        return id != null ? id : -1;
    }

    private static class DungeonModifier {

        public int modId;

        public String name;

        public String description;
    }

    /**
     * Build the canonical modifiers string from a MapInfoPacket by combining up to four modifier fields
     * and the optional dungeon grade. Example output: "BONUSCONSUMABLES;ENERGIZEDMINIONS_1;|D".
     */
    public static String getModifiersString(MapInfoPacket map) {
        if (map == null) return "";
        StringBuilder sb = new StringBuilder();
        // Append non-empty modifier fields in order
        if (map.dungeonModifiers != null && !map.dungeonModifiers.isEmpty()) {
            sb.append(map.dungeonModifiers);
        }
        if (map.dungeonModifiers2 != null && !map.dungeonModifiers2.isEmpty()) {
            if (sb.length() > 0) sb.append(';');
            sb.append(map.dungeonModifiers2);
        }
        if (map.dungeonModifiers3 != null && !map.dungeonModifiers3.isEmpty()) {
            if (sb.length() > 0) sb.append(';');
            sb.append(map.dungeonModifiers3);
        }
        if (map.dungeonModifiers4 != null && !map.dungeonModifiers4.isEmpty()) {
            if (sb.length() > 0) sb.append(';');
            sb.append(map.dungeonModifiers4);
        }
        // Append grade as separate token like "|D"
        if (map.dungeonGrade != null && !map.dungeonGrade.isEmpty()) {
            if (sb.length() > 0) sb.append(';');
            sb.append('|').append(map.dungeonGrade);
        }
        return sb.toString();
    }
}
