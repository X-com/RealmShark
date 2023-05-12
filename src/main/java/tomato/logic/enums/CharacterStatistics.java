package tomato.logic.enums;

import java.util.ArrayList;
import java.util.HashMap;

public enum CharacterStatistics {
    SHOTS_FIRED(24, -1),
    HITS(25, -1),
    ABILITY_USED(26, -1),
    TILES_DISCOVERED(27, -1),
    TELEPORTS(28, -1),
    POTIONS_DRUNK(29, -1),
    KILLS(30, -1),
    ASSISTS(31, -1),
    PARTY_LEVEL_UPS(85, -1),
    LESSER_GODS_KILLS(86, -1),
    ENCOUNTER_KILLS(87, -1),
    HERO_KILLS(72, -1),
    CRITTER_KILLS(74, -1),
    BEAST_KILLS(75, -1),
    HUMANOID_KILLS(76, -1),
    UNDEAD_KILLS(77, -1),
    NATURE_KILLS(78, -1),
    CONSTRUCT_KILLS(79, -1),
    GROTESQUE_KILLS(64, -1),
    STRUCTURE_KILLS(65, -1),
    GOD_KILLS(16, -1),
    ASSISTS_AGAINST_GODS(17, -1),
    CUBE_KILLS(18, -1),
    ORYX_KILLS(19, -1),
    QUESTS_COMPLETED(20, -1),
    MINUTES_ACTIVE(12, -1),
    DUNGEON_TYPES_COMPLETED(36, -1),

    ABYSS_OF_DEMONS(23, 1819),
    ANCIENT_RUINS(35, 9657), // AncientRuinsCompleted
    BATTLE_FOR_THE_NEXUS(62, 1886),
    BEACHZONE(67, 1858),
    BELLADONNAS_GARDEN(48, 8849),
    CANDYLAND_HUNTING_GROUNDS(3, 1866),
    CAVE_OF_THOUSAND_TREASURES(5, 24110),
    CNIDARIAN_REEF(46, 2554),
    CRYSTAL_CAVERN(34, 10042),
    CULTIST_HIDEOUT(40, 19459),
    CURSED_LIBRARY(32, 43862),
    DAVY_JONES_LOCKER(7, 1857),
    DEADWATER_DOCKS(59, 1885),
    FORAX(37, 45771), // ForaxsCompleted
    FORBIDDEN_JUNGLE(15, 1843),
    FOREST_MAZE(1, 24372),
    FUNGAL_CAVERN(33, 45679),
    HAUNTED_CEMETERY(4, 1867),
    HEROIC_ABYSS_OF_DEMONES(38, 9324),
    HEROIC_UNDEAD_LAIR(39, 9323), // TODO verify
    HIDDEN_INTERREGNUM(68, 49768), // TODO verify
    HIGH_TECH_TERROR(88, 15730),
    ICE_CAVE(58, 29835), // IceCavesCompleted
    ICE_TOMB(89, 32696),
    KATALUND(90, 45774), // KatalundsCompleted
    KOGBOLD_STEAMWORKS(70, 49433),
    LAIR_OF_DRACONIS(2, 30014),
    LAIR_OF_SHAITAN(43, 28057),
    LOST_HALLS(55, 45092), // LostHallsCompleted
    MAD_LAB(6, 2192), // MadLabsCompleted
    MAGIC_WOODS(45, 2172),
    MALOGIA(92, 45752), // MalogiasCompleted
    MANOR_OF_THE_IMMORTALS(0, 1849),
    MOONLIGHT_VILLAGE(71, 20447),
    MOUNTAIN_TEMPLE(52, 311), // MountainTemplesCompleted
    OCEAN_TRENCH(14, 1840),
    MAD_GOD_MAYHEM(91, 3873), // MadGodMayhemsCompleted
    ORYXS_CASTLE(93, 3465),
    ORYXS_CHAMBER(94, 3451),
    ORYXS_SANCTUARY(95, 6218),
    PARASITE_CHAMBERS(44, 1944),
    PIRATE_CAVE(21, 1815),
    PUPPET_MASTERS_ENCORE(42, 29798),
    PUPPET_MASTERS_THEATRE(49, 9043),
    RAINBOW_ROAD(80, 5704),
    SANTAS_WORKSHOP(81, 15566), //SantasWorkshopsCompleted
    SECLUDED_THICKET(47, 13983), // SecludedThicketsCompleted
    SNAKE_PIT(8, 1816),
    SPIDER_DEN(9, 1817),
    SPRITE_WORLD(10, 1804),
    SULFUROUS_WETLANDS(69, 25490),
    THE_CRAWLING_DEPTHS(60, 1838),
    THE_HIVE(51, 285),
    THE_MACHINE(82, 43986), // TODO verify
    THE_NEST(53, 4259),
    THE_SHATTERS(63, 29310),
    THE_THIRD_DIMENSION(66, 19302),
    THE_VOID(41, 45075),
    TOMB_OF_THE_ANCIENTS(13, 1844),
    TOXIC_SEWERS(50, 574),
    UNDEAD_LAIR(22, 1818),
    UNTARIS(83, 45751),
    WINE_CELLAR(84, 578),
    WOODLAND_LABYRINTH(61, 1884);

    int pcStatId;
    int spriteId;

    private static final HashMap<Integer, String> NAME = new HashMap<>();
    public static final ArrayList<Integer> DUNGEONS = new ArrayList<>();

    static {
        for (CharacterStatistics o : CharacterStatistics.values()) {
            NAME.put(o.pcStatId, o.toString());
            if (o.spriteId != -1) {
                DUNGEONS.add(o.spriteId);
            }
        }
    }

    CharacterStatistics(int pcStatId, int spriteId) {
        this.pcStatId = pcStatId;
        this.spriteId = spriteId;
    }

    /**
     * Statistics id to name.
     *
     * @param id Statistics id
     * @return Statistics name
     */
    public static String getName(int id) {
        return NAME.get(id);
    }

    /**
     * Returns the PcStat decoding bit index. Used to decode the pcStat string.
     *
     * @return The bit index in the pcStat string
     */
    public int getPcStatId() {
        return pcStatId;
    }
}
