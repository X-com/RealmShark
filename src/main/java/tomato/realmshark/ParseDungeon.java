package tomato.realmshark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private static final String XML_DIR_PATH = "assets/xml";
    private static final HashMap<Integer, String> ID_TO_NAME_MODS =
        new HashMap<>();
    private static final HashMap<String, Integer> NAME_TO_ID_MODS =
        new HashMap<>();
    private static final HashMap<String, Integer> NAME_TO_ID_PORTAL =
        new HashMap<>();

    // Reverse of NAME_TO_ID_PORTAL: portal objectType -> dungeon name. Lets a
    // portal entity seen in the world be identified as a specific dungeon.
    private static final HashMap<Integer, String> ID_TO_NAME_PORTAL =
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
        try {
            parseAllPortalObjects();
        } catch (Throwable t) {
            System.out.println("[ParseDungeon] portal scan failed: " + t);
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
                        ID_TO_NAME_PORTAL.put(id, name);
                    }
                }
            }
            NAME_TO_ID_PORTAL.put("Realm of the Mad God", 1796);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * portals.xml only defines the classic dungeons - every modern one (Lost
     * Halls, Cultist Hideout, The Void, Moonlight Village, The Nest, ...) lives
     * in its own per-dungeon objects file. Reading only portals.xml finds 82 of
     * the ~160 dungeon portals in the game, so this sweeps every extracted XML
     * for objects carrying a DungeonName.
     *
     * A light regex pass rather than a DOM parse of 230+ files.
     */
    private static void parseAllPortalObjects() {
        File dir = new File(XML_DIR_PATH);
        File[] files = dir.listFiles((d, n) -> n.toLowerCase().endsWith(".xml"));
        if (files == null) return;

        Pattern objectBlock = Pattern.compile(
            "<Object\\s[^>]*type=\"([^\"]+)\"[^>]*>(.*?)</Object>",
            Pattern.DOTALL
        );
        Pattern dungeonName = Pattern.compile(
            "<DungeonName>(.*?)</DungeonName>",
            Pattern.DOTALL
        );

        for (File f : files) {
            String content;
            try {
                content = new String(
                    Files.readAllBytes(f.toPath()),
                    StandardCharsets.UTF_8
                );
            } catch (IOException e) {
                continue; // skip unreadable file, keep going
            }
            if (!content.contains("<DungeonName>")) continue;

            Matcher m = objectBlock.matcher(content);
            while (m.find()) {
                Matcher dn = dungeonName.matcher(m.group(2));
                if (!dn.find()) continue;
                String name = dn.group(1).trim();
                if (name.isEmpty()) continue;
                try {
                    int type = Integer.decode(m.group(1).trim());
                    ID_TO_NAME_PORTAL.put(type, name);
                    // Do not clobber portals.xml, which stays authoritative for
                    // the loot-tab dungeon icons.
                    NAME_TO_ID_PORTAL.putIfAbsent(name, type);
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    /**
     * Resolves a world entity's objectType to a dungeon name, if that entity is
     * a dungeon portal.
     *
     * @param objectType Entity objectType seen in the world.
     * @return The dungeon name, or null if this objectType is not a portal.
     */
    public static String getDungeonNameByPortalType(int objectType) {
        return ID_TO_NAME_PORTAL.get(objectType);
    }

    /**
     * Every known dungeon name, sorted, for populating selection UIs.
     *
     * Fewer entries than there are portal objectTypes: some dungeons ship
     * several portal variants (Cultist Hideout has a normal and a temp portal),
     * which is why selections are keyed by NAME - picking the name covers every
     * variant of that dungeon.
     */
    public static java.util.List<String> allDungeonNames() {
        java.util.TreeSet<String> names = new java.util.TreeSet<>(
            String.CASE_INSENSITIVE_ORDER
        );
        names.addAll(ID_TO_NAME_PORTAL.values());
        return new java.util.ArrayList<>(names);
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
