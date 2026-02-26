package tomato.realmshark.enums;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

import util.StringXML;

public class CharacterClass {
    private static final String PLAYERS_XML_PATH = "assets/xml/players.xml";

    public final int id;
    public final String name;
    public final int life;
    public final int mana;
    public final int atk;
    public final int def;
    public final int spd;
    public final int dex;
    public final int vit; // Equivalent to HpRegen
    public final int wis; // Equivalent to MpRegen
    public int[] weaponGroup; // Mutable for updating after parsing
    public final int[] maxStats;
    public final int[] baseGear;

    CharacterClass(int id, String name, int life, int mana, int atk, int def, int spd, int dex, int vit, int wis, int[] weaponGroup, int[] baseGear) {
        this.id = id;
        this.name = name;
        this.life = life;
        this.mana = mana;
        this.atk = atk;
        this.def = def;
        this.spd = spd;
        this.dex = dex;
        this.vit = vit;
        this.wis = wis;
        this.weaponGroup = weaponGroup;
        maxStats = new int[]{life, mana, atk, def, spd, dex, vit, wis};
        this.baseGear = baseGear;
    }

    // Static structures to hold dynamic data
    public static final CharacterClass[] CHAR_CLASS_LIST;
    private static final TreeMap<Integer, CharacterClass> CHARACTER_CLASS = new TreeMap<>();
    private static final TreeMap<Integer, String> CLASS_NAME = new TreeMap<>();
    private static final TreeMap<Integer, int[]> CLASS_MAX_STATS = new TreeMap<>();
    private static final TreeMap<Integer, int[]> WEAPON_CLASSES = new TreeMap<>();
    private static final TreeSet<Integer> CHARACTER_IDS = new TreeSet<>();

    // Static initialization block
    static {
        List<CharacterClass> charClassList = new ArrayList<>();
        try {
            FileInputStream file = new FileInputStream(PLAYERS_XML_PATH);
            populateFromXML(new BufferedReader(new InputStreamReader(file)).lines().collect(Collectors.joining("\n")), charClassList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        CHAR_CLASS_LIST = charClassList.toArray(new CharacterClass[0]);
        for (CharacterClass o : CHAR_CLASS_LIST) {
                CHARACTER_IDS.add(o.id);
                CHARACTER_CLASS.put(o.id, o);
            CLASS_NAME.put(o.id, o.name);
                WEAPON_CLASSES.put(o.id, o.weaponGroup);
                CLASS_MAX_STATS.put(o.id, o.maxStats);
        }
    }

    private static void populateFromXML(String rawXML, List<CharacterClass> charClassList) throws Exception {
        // Parse the XML using StringXML
        StringXML root = StringXML.getParsedXML(rawXML);

        // Map to group character ids by Equipment's first integer
        Map<Integer, List<Integer>> weaponGroups = new HashMap<>();
        Map<Integer, Integer> weaponCharacter = new HashMap<>();

        // Process each Object element
        for (StringXML object : root) {
            if (!object.name.equals("Object")) continue; // Skip non-Object nodes

            // Extract id and name
            String name = object.children.stream()
                    .filter(child -> child.name.equals("id"))
                    .map(child -> child.value)
                    .findFirst()
                    .orElse(null);

            int id = object.children.stream()
                    .filter(child -> child.name.equals("type"))
                    .mapToInt(child -> Integer.decode(child.value))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Missing type attribute"));

            // Extract individual max stats
            int life = getMaxValue(object, "MaxHitPoints");
            int mana = getMaxValue(object, "MaxMagicPoints");
            int atk = getMaxValue(object, "Attack");
            int def = getMaxValue(object, "Defense");
            int spd = getMaxValue(object, "Speed");
            int dex = getMaxValue(object, "Dexterity");
            int vit = getMaxValue(object, "HpRegen");
            int wis = getMaxValue(object, "MpRegen");

            // Extract Equipment and determine weapon group
            String baseEquipmentString = "";
            for (StringXML child : object.children) {
                if (child.name.equals("Equipment")) {
                    for (StringXML stringXML : child.children) {
                        baseEquipmentString = stringXML.value;
                        break;
            }
                    break;
                }
            }
            String[] equipmentItems = baseEquipmentString.split(",");
            int[] baseGear = new int[4];
            for (int baseGearIndex = 0; baseGearIndex < 3; baseGearIndex++)
                baseGear[baseGearIndex] = Integer.decode(equipmentItems[baseGearIndex].trim());
            baseGear[3] = 2639; // Add ring
            int weaponId = baseGear[0];
            weaponGroups.computeIfAbsent(weaponId, k -> new ArrayList<>()).add(id);
            weaponCharacter.put(id, weaponId);

            // Create CharacterClass instance
            charClassList.add(new CharacterClass(id, name, life, mana, atk, def, spd, dex, vit, wis, new int[0], baseGear)); // Weapon group will be set later
        }

        // Update weapon groups for each CharacterClass
        for (CharacterClass character : charClassList) {
            List<Integer> group = weaponGroups.get(weaponCharacter.get(character.id));
            if (group != null) {
                character.weaponGroup = group.stream().mapToInt(Integer::intValue).toArray();
        }
        }
    }

    private static int getMaxValue(StringXML object, String tagName) {
        return object.children.stream()
                .filter(child -> child.name.equals(tagName))
                .map(child -> child.children.stream()
                        .filter(attr -> attr.name.equals("max"))
                        .mapToInt(attr -> Integer.parseInt(attr.value))
                        .findFirst()
                        .orElse(0))
                .findFirst()
                .orElse(0);
    }

    /**
     * Character class id to class name.
     *
     * @param id Class id
     * @return Class name
     */
    public static String getName(int id) {
        return CLASS_NAME.get(id);
    }

    /**
     * Get character max stats from class id.
     * Order: life, mana, atk, def, spd, dex, vit, wis
     *
     * @param id Class id
     * @return Class max stat array
     */
    public static int[] getStats(int id) {
        return CLASS_MAX_STATS.get(id);
    }

    /**
     * Getters for character class stats.
     *
     * @param id Class id
     * @return Class max stat
     */
    public static int getLife(int id) {
        return CHARACTER_CLASS.get(id).life;
    }

    /**
     * Checks if the objectType id is of player type.
     *
     * @param objectType Object id
     * @return Is player object type
     */
    public static boolean isPlayerCharacter(int objectType) {
        return CHARACTER_IDS.contains(objectType);
    }

    /**
     * Gets the same weapon classes as requested.
     *
     * @param classId Id of one class sharing the same weapon.
     * @return int[] of classes sharing same weapons.
     */
    public static int[] weaponClasses(int classId) {
        return WEAPON_CLASSES.get(classId);
    }

    public static int getMana(int id) {
        return CHARACTER_CLASS.get(id).mana;
    }

    public static int getAtk(int id) {
        return CHARACTER_CLASS.get(id).atk;
    }

    public static int getDef(int id) {
        return CHARACTER_CLASS.get(id).def;
    }

    public static int getSpd(int id) {
        return CHARACTER_CLASS.get(id).spd;
    }

    public static int getDex(int id) {
        return CHARACTER_CLASS.get(id).dex;
    }

    public static int getVit(int id) {
        return CHARACTER_CLASS.get(id).vit;
    }

    public static int getWis(int id) {
        return CHARACTER_CLASS.get(id).wis;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }
}
