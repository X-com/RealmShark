package tomato.realmshark.enums;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Character class enum to get class name and stats from class id.
 */
public enum CharacterClass {
    Rogue(768, 720, 252, 50, 25, 75, 75, 40, 50),
    Archer(775, 700, 252, 75, 25, 55, 50, 40, 50),
    Wizard(782, 670, 385, 75, 25, 50, 75, 40, 60),
    Priest(784, 670, 385, 50, 25, 55, 55, 40, 75),
    Warrior(797, 770, 252, 75, 25, 50, 50, 75, 50),
    Knight(798, 770, 252, 50, 40, 50, 50, 75, 50),
    Paladin(799, 770, 252, 55, 30, 55, 55, 60, 75),
    Assassin(800, 720, 305, 65, 25, 65, 75, 40, 60),
    Necromancer(801, 670, 385, 75, 25, 50, 60, 40, 75),
    Huntress(802, 700, 305, 65, 25, 50, 60, 40, 50),
    Trickster(804, 720, 252, 65, 25, 75, 75, 40, 60),
    Mystic(803, 670, 385, 65, 25, 60, 65, 40, 75),
    Sorcerer(805, 670, 385, 75, 25, 60, 65, 75, 60),
    Ninja(806, 720, 252, 70, 25, 60, 70, 60, 70),
    Samurai(785, 720, 252, 75, 30, 55, 50, 60, 60),
    Bard(796, 670, 385, 55, 25, 55, 70, 45, 75),
    Summoner(817, 670, 385, 50, 25, 60, 75, 40, 75),
    Kensei(818, 720, 252, 65, 25, 60, 65, 60, 50);
    public static final CharacterClass[] CHAR_CLASS_LIST;

    private final int life, mana, atk, def, spd, dex, vit, wis;
    private final int id;
    private final int[] maxStats;

    private static final HashMap<Integer, CharacterClass> CHARACTER_CLASS = new HashMap<>();
    private static final HashMap<Integer, String> CLASS_NAME = new HashMap<>();
    private static final HashMap<Integer, int[]> CLASS_MAX_STATS = new HashMap<>();
    private static final HashSet<Integer> CHARACTER_IDS = new HashSet<>();

    static {
        CHAR_CLASS_LIST = CharacterClass.values().clone();
        try {
            for (CharacterClass o : CharacterClass.values()) {
                CHARACTER_IDS.add(o.id);
                CHARACTER_CLASS.put(o.id, o);
                CLASS_NAME.put(o.id, o.toString());
                CLASS_MAX_STATS.put(o.id, o.maxStats);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    CharacterClass(int id, int life, int mana, int atk, int def, int spd, int dex, int vit, int wis) {
        this.id = id;
        this.life = life;
        this.mana = mana;
        this.atk = atk;
        this.def = def;
        this.spd = spd;
        this.dex = dex;
        this.vit = vit;
        this.wis = wis;
        maxStats = new int[]{life, mana, atk, def, spd, dex, vit, wis};
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
}
