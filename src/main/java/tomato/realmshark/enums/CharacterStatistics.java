package tomato.realmshark.enums;

import java.util.ArrayList;
import java.util.HashMap;

public enum CharacterStatistics {
    SHOTS_FIRED(24, -1, "Shots Fired"),
    HITS(25, -1, "Hits"),
    ABILITY_USED(26, -1, "Ability Used"),
    TILES_DISCOVERED(27, -1, "Tiles Discovered"),
    TELEPORTS(28, -1, "Teleports"),
    POTIONS_DRUNK(29, -1, "Potions Drunk"),
    KILLS(30, -1, "Kills"),
    ASSISTS(31, -1, "Assists"),
    PARTY_LEVEL_UPS(85, -1, "Party Level Ups"),
    LESSER_GODS_KILLS(86, -1, "Lesser Gods Kills"),
    ENCOUNTER_KILLS(87, -1, "Encounter Kills"),
    HERO_KILLS(72, -1, "Hero Kills"),
    CRITTER_KILLS(74, -1, "Critter Kills"),
    BEAST_KILLS(75, -1, "Beast Kills"),
    HUMANOID_KILLS(76, -1, "Humanoid Kills"),
    UNDEAD_KILLS(77, -1, "Undead Kills"),
    NATURE_KILLS(78, -1, "Nature Kills"),
    CONSTRUCT_KILLS(79, -1, "Construct Kills"),
    GROTESQUE_KILLS(64, -1, "Grotesque Kills"),
    STRUCTURE_KILLS(65, -1, "Structure Kills"),
    GOD_KILLS(16, -1, "God Kills"),
    ASSISTS_AGAINST_GODS(17, -1, "Assists Against Gods"),
    CUBE_KILLS(18, -1, "Cube Kills"),
    ORYX_KILLS(19, -1, "Oryx Kills"),
    QUESTS_COMPLETED(20, -1, "Quests Completed"),
    MINUTES_ACTIVE(12, -1, "Minutes Active"),
    DUNGEON_TYPES_COMPLETED(36, -1, "Dungeon Types Completed"),

    ABYSS_OF_DEMONS(23, 1819, "Abyss of Demons"),
    ANCIENT_RUINS(35, 9657, "Ancient Ruins"), // AncientRuinsCompleted
    BATTLE_FOR_THE_NEXUS(62, 1886, "Battle for the Nexus"),
    BEACHZONE(67, 1858, "Beachzone"),
    BELLADONNAS_GARDEN(48, 8849, "Belladonnas Garden"),
    CANDYLAND_HUNTING_GROUNDS(3, 1866, "Candyland Hunting Grounds"),
    CAVE_OF_THOUSAND_TREASURES(5, 24110, "Cave of Thousand Treasures"),
    CNIDARIAN_REEF(46, 2554, "Cnidarian Reef"),
    CRYSTAL_CAVERN(34, 10042, "Crystal Cavern"),
    CULTIST_HIDEOUT(40, 19459, "Cultist Hideout"),
    CURSED_LIBRARY(32, 43862, "Cursed Library"),
    DAVY_JONES_LOCKER(7, 1857, "Davy Jones Locker"),
    DEADWATER_DOCKS(59, 1885, "Deadwater Docks"),
    FORAX(37, 45771, "Forax"), // ForaxsCompleted
    FORBIDDEN_JUNGLE(15, 1843, "Forbidden Jungle"),
    FOREST_MAZE(1, 24372, "Forest Maze"),
    FUNGAL_CAVERN(33, 45679, "Fungal Cavern"),
    HAUNTED_CEMETERY(4, 1867, "Haunted Cemetery"),
    HEROIC_ABYSS_OF_DEMONES(38, 9324, "Heroic Abyss of Demones"),
    HEROIC_UNDEAD_LAIR(39, 9323, "Heroic Undead Lair"), // TODO verify
    HIDDEN_INTERREGNUM(68, 49768, "Hidden Interregnum"), // TODO verify
    HIGH_TECH_TERROR(88, 15730, "High Tech Terror"),
    ICE_CAVE(58, 29835, "Ice Cave"), // IceCavesCompleted
    ICE_TOMB(89, 32696, "Ice Tomb"),
    KATALUND(90, 45774, "Katalund"), // KatalundsCompleted
    KOGBOLD_STEAMWORKS(70, 49433, "Kogbold Steamworks"),
    LAIR_OF_DRACONIS(2, 30014, "Lair of Draconis"),
    LAIR_OF_SHAITAN(43, 28057, "Lair of Shaitan"),
    LOST_HALLS(55, 45092, "Lost Halls"), // LostHallsCompleted
    MAD_LAB(6, 2192, "Mad Lab"), // MadLabsCompleted
    MAGIC_WOODS(45, 2172, "Magic Woods"),
    MALOGIA(92, 45752, "Malogia"), // MalogiasCompleted
    MANOR_OF_THE_IMMORTALS(0, 1849, "Manor of the Immortals"),
    MOONLIGHT_VILLAGE(71, 20447, "Moonlight Village"),
    MOUNTAIN_TEMPLE(52, 311, "Mountain Temple"), // MountainTemplesCompleted
    OCEAN_TRENCH(14, 1840, "Ocean Trench"),
    MAD_GOD_MAYHEM(91, 3873, "Mad God Mayhem"), // MadGodMayhemsCompleted
    ORYXS_CASTLE(93, 3465, "Oryxs Castle"),
    ORYXS_CHAMBER(94, 3451, "Oryxs Chamber"),
    ORYXS_SANCTUARY(95, 6218, "Oryxs Sanctuary"),
    PARASITE_CHAMBERS(44, 1944, "Parasite Chambers"),
    PIRATE_CAVE(21, 1815, "Pirate Cave"),
    PUPPET_MASTERS_ENCORE(42, 29798, "Puppet Masters Encore"),
    PUPPET_MASTERS_THEATRE(49, 9043, "Puppet Masters Theatre"),
    RAINBOW_ROAD(80, 5704, "Rainbow Road"),
    SANTAS_WORKSHOP(81, 15566, "Santas Workshop"), //SantasWorkshopsCompleted
    SECLUDED_THICKET(47, 13983, "Secluded Thicket"), // SecludedThicketsCompleted
    SNAKE_PIT(8, 1816, "Snake Pit"),
    SPIDER_DEN(9, 1817, "Spider Den"),
    SPRITE_WORLD(10, 1804, "Sprite World"),
    SULFUROUS_WETLANDS(69, 25490, "Sulfurous Wetlands"),
    THE_CRAWLING_DEPTHS(60, 1838, "The Crawling Depths"),
    THE_HIVE(51, 285, "The Hive"),
    THE_MACHINE(82, 43986, "The Machine"), // TODO verify
    THE_NEST(53, 4259, "The Nest"),
    THE_SHATTERS(63, 29310, "The Shatters"),
    THE_THIRD_DIMENSION(66, 19302, "The Third Dimension"),
    THE_VOID(41, 45075, "The Void"),
    TOMB_OF_THE_ANCIENTS(13, 1844, "Tomb of the Ancients"),
    TOXIC_SEWERS(50, 574, "Toxic Sewers"),
    UNDEAD_LAIR(22, 1818, "Undead Lair"),
    UNTARIS(83, 45751, "Untaris"),
    WINE_CELLAR(84, 578, "Wine Cellar"),
    WOODLAND_LABYRINTH(61, 1884, "Woodland Labyrinth");

    int pcStatId;
    int spriteId;
    String name;

    private static final HashMap<Integer, String> NAME = new HashMap<>();
    public static final ArrayList<Integer> DUNGEONS = new ArrayList<>();

    static {
        for (CharacterStatistics o : CharacterStatistics.values()) {
            NAME.put(o.pcStatId, o.name);
            if (o.spriteId != -1) {
                DUNGEONS.add(o.spriteId);
            }
        }
    }

    CharacterStatistics(int pcStatId, int spriteId, String name) {
        this.pcStatId = pcStatId;
        this.spriteId = spriteId;
        this.name = name;
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
