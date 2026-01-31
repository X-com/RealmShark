package tomato.realmshark.enums;

import java.util.ArrayList;
import java.util.TreeMap;

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
    STAT_POTION_CONSUMED(124, -1, "Stat Potion Consumed"),
    RUINS_ENEMY_KILLS(126, -1, "Ruins Enemy Kills"),
    BEACH_ENEMY_KILLS(127, -1, "Beach Enemy Kills"),
    UNDEAD_FOREST_ENEMY_KILLS(112, -1, "Undead Forest Enemy Kills"),
    FOREST_ENEMY_KILLS(113, -1, "Forest Enemy Kills"),
    PLAINS_ENEMY_KILLS(114, -1, "Plains Enemy Kills"),
    WITHER_ENEMY_KILLS(115, -1, "Wither Enemy Kills"),
    DARK_FOREST_ENEMY_KILLS(116, -1, "Dark Forest Enemy Kills"),
    DESERT_ENEMY_KILLS(117, -1, "Desert Enemy Kills"),
    CORAL_REEFS_ENEMY_KILLS(118, -1, "Coral Reefs Enemy Kills"),
    SPRITE_FOREST_ENEMY_KILLS(119, -1, "Sprite Forest Enemy Kills"),
    HAUNTED_HALLOWS_ENEMY_KILLS(104, -1, "Haunted Hallows Enemy Kills"),
    SHIPWRECK_COVE_ENEMY_KILLS(105, -1, "Shipwreck Cove Enemy Kills"),
    DEAD_CHURCH_ENEMY_KILLS(106, -1, "Dead Church Enemy Kills"),
    RISEN_HELLS_ENEMY_KILLS(107, -1, "Risen Hells Enemy Kills"),
    ABANDONED_CITY_ENEMY_KILLS(108, -1, "Abandoned City Enemy Kills"),
    SEA_ABYSS_ENEMY_KILLS(109, -1, "Sea Abyss Enemy Kills"),
    CARBONIFEROUS_ENEMY_KILLS(110, -1, "Carboniferous Enemy Kills"),
    FLORAL_ESCAPE_ENEMY_KILLS(111, -1, "Floral Escape Enemy Kills"),
    SANGUINE_FOREST_ENEMY_KILLS(96, -1, "Sanguine Forest Enemy Kills"),
    RUNIC_TUNDRA_KILLS(97, -1, "Runic Tundra Kills"),

    ABYSS_OF_DEMONS(23, 1819, "Abyss of Demons"),
    ADVANCED_KOGBOLD_STEAMWORKS(120, 28822, "Advanced Kogbold Steamworks"),
    ANCIENT_RUINS(35, 9657, "Ancient Ruins"), // AncientRuinsCompleted
    BATTLE_FOR_THE_NEXUS(62, 1886, "Battle for the Nexus"),
    BEACHZONE(67, 1858, "Beachzone"),
    BELLADONNAS_GARDEN(48, 8849, "Belladonna's Garden"),
    CANDYLAND_HUNTING_GROUNDS(3, 1866, "Candyland Hunting Grounds"),
    CAVE_OF_THOUSAND_TREASURES(5, 24110, "Cave of A Thousand Treasures"),
    CNIDARIAN_REEF(46, 2554, "Cnidarian Reef"),
    CRYSTAL_CAVERN(34, 10042, "Crystal Cavern"),
    CULTIST_HIDEOUT(40, 19459, "Cultist Hideout"),
    CURSED_LIBRARY(32, 43862, "Cursed Library"),
    DAVY_JONES_LOCKER(7, 1857, "Davy Jones' Locker"),
    DEADWATER_DOCKS(59, 1885, "Deadwater Docks"),
    FORAX(37, 45771, "Forax"), // ForaxsCompleted
    FORBIDDEN_JUNGLE(15, 1843, "Forbidden Jungle"),
    FOREST_MAZE(1, 24372, "Forest Maze"),
    FUNGAL_CAVERN(33, 45679, "Fungal Cavern"),
    HAUNTED_CEMETERY(4, 1867, "Haunted Cemetery"),
    HEROIC_UNDEAD_LAIR(102, 14690, "Heroic Undead Lair"),
    HIDDEN_INTERREGNUM(68, 49768, "Hidden Interregnum"), // TODO verify
    HIGH_TECH_TERROR(88, 15730, "High Tech Terror"),
    ICE_CITADEL(58, 29835, "Ice Citadel"), // IceCavesCompleted
    ICE_TOMB(89, 32696, "Ice Tomb"),
    INFERNAL_ABYSS_OF_DEMONS(103, 14861, "Infernal Abyss of Demons"),
    KATALUND(90, 45774, "Katalund"), // KatalundsCompleted
    KOGBOLD_STEAMWORKS(70, 49433, "Kogbold Steamworks"),
    LAIR_OF_DRACONIS(2, 30014, "Lair of Draconis"),
    LAIR_OF_SHAITAN(43, 28057, "Lair of Shaitan"),
    LEGACY_HEROIC_ABYSS_OF_DEMONS(38, 9324, "Legacy Heroic Abyss of Demons"),
    LEGACY_HEROIC_UNDEAD_LAIR(39, 9323, "Legacy Heroic Undead Lair"),
    LOST_HALLS(55, 45092, "Lost Halls"), // LostHallsCompleted
    MAD_GOD_MAYHEM(91, 3873, "Mad God Mayhem"), // MadGodMayhemsCompleted
    MAD_LAB(6, 2192, "Mad Lab"), // MadLabsCompleted
    MAGIC_WOODS(45, 2172, "Magic Woods"),
    MALOGIA(92, 45752, "Malogia"), // MalogiasCompleted
    MANOR_OF_THE_IMMORTALS(0, 1849, "Manor of the Immortals"),
    MGM2(101, 13100, "The Trials of Cronus"),
    MOONLIGHT_VILLAGE(71, 20447, "Moonlight Village"),
    MOUNTAIN_TEMPLE(52, 311, "Mountain Temple"), // MountainTemplesCompleted
    OCEAN_TRENCH(14, 1840, "Ocean Trench"),
    ORYXS_CASTLE(93, 3465, "Oryx's Castle"),
    ORYXS_CHAMBER(94, 3451, "Oryx's Chamber"),
    ORYXS_SANCTUARY(95, 6218, "Oryx's Sanctuary"),
    PARASITE_CHAMBERS(44, 1944, "Parasite Chambers"),
    PIRATE_CAVE(21, 1815, "Pirate Cave"),
    PLAGUED_NEST(121, 17570, "Plagued Nest"),
    PUPPET_MASTERS_ENCORE(42, 29798, "Puppet Master's Encore"),
    PUPPET_MASTERS_THEATRE(49, 9043, "Puppet Master's Theatre"),
    QUEEN_BUNNY_CHAMBER(123, 1430, "Queen Bunny Chamber"),
    RAINBOW_ROAD(80, 5704, "Rainbow Road"),
    SANTAS_WORKSHOP(81, 15566, "Santas Workshop"), //SantasWorkshopsCompleted
    SECLUDED_THICKET(47, 13983, "Secluded Thicket"), // SecludedThicketsCompleted
    SNAKE_PIT(8, 1816, "Snake Pit"),
    SPIDER_DEN(9, 1817, "Spider Den"),
    SPECTRAL_PENITENTIARY(125, 23691, "Spectral Penitentiary"),
    SPRITE_WORLD(10, 1804, "Sprite World"),
    SULFUROUS_WETLANDS(69, 25490, "Sulfurous Wetlands"),
    THE_CRAWLING_DEPTHS(60, 1838, "The Crawling Depths"),
    THE_HIVE(51, 285, "The Hive"),
    THE_MACHINE(82, 43986, "The Machine"), // TODO verify
    THE_NEST(53, 4259, "The Nest"),
    THE_SHATTERS(63, 29310, "The Shatters"),
    THE_TAVERN(122, 17744, "The Tavern"),
    THE_THIRD_DIMENSION(66, 19302, "The Third Dimension"),
    THE_VOID(41, 45075, "The Void"),
    TOMB_OF_THE_ANCIENTS(13, 1844, "Tomb of the Ancients"),
    TOXIC_SEWERS(50, 574, "Toxic Sewers"),
    UNDEAD_LAIR(22, 1818, "Undead Lair"),
    UNTARIS(83, 45751, "Untaris"),
    WHITE_SNAKE_INVASION_I(98, 59886, "White Snake Invasion I"),
    WHITE_SNAKE_INVASION_II(99, 59887, "White Snake Invasion II"),
    WHITE_SNAKE_INVASION_III(100, 59888, "White Snake Invasion III"),
    WINE_CELLAR(84, 578, "Wine Cellar"),
    WOODLAND_LABYRINTH(61, 1884, "Woodland Labyrinth");

    int pcStatId;
    int spriteId;
    String name;

    public static final TreeMap<Integer, String> PC_NAME = new TreeMap<>();
    public static final TreeMap<Integer, String> ID_NAME = new TreeMap<>();
    public static final TreeMap<String, CharacterStatistics> NAME_CS = new TreeMap<>();
    public static final ArrayList<Integer> DUNGEONS = new ArrayList<>();
    public static final ArrayList<String> DUNGEON_NAMES = new ArrayList<>();

    static {
        for (CharacterStatistics o : CharacterStatistics.values()) {
            PC_NAME.put(o.pcStatId, o.name);
            ID_NAME.put(o.spriteId, o.name);
            if (o.spriteId != -1) {
                DUNGEONS.add(o.spriteId);
                DUNGEON_NAMES.add(o.name);
            }
            NAME_CS.put(o.name, o);
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
        return ID_NAME.get(id);
    }

    /**
     * Returns the dungeon index from name in the dungeon array.
     *
     * @param name Name of the dungeon.
     * @return The index in the dungeon list the dungeon is found.
     */
    public static int getDungeonIndex(String name) {
        return DUNGEON_NAMES.indexOf(name);
    }

    /**
     * Returns the PcStat decoding bit index. Used to decode the pcStat string.
     *
     * @return The bit index in the pcStat string
     */
    public int getPcStatId() {
        return pcStatId;
    }

    /**
     * Character statistics from stat name
     *
     * @param name Name of the stat
     * @return Character statistics tied to the stat name.
     */
    public static CharacterStatistics statByName(String name) {
        return NAME_CS.get(name);
    }

    /**
     * Returns sprite ID of stat.
     *
     * @return Sprite ID
     */
    public int getSpriteId() {
        return spriteId;
    }
}
