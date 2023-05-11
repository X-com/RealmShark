package tomato.logic.enums;

import java.util.HashMap;

public enum CharacterStatistics {
    SHOTS_FIRED(24),
    HITS(25),
    ABILITY_USED(26),
    TILES_DISCOVERED(27),
    TELEPORTS(28),
    POTIONS_DRUNK(29),
    KILLS(30),
    ASSISTS(31),
    PARTY_LEVEL_UPS(85),
    LESSER_GODS_KILLS(86),
    ENCOUNTER_KILLS(87),
    HERO_KILLS(72),
    CRITTER_KILLS(74),
    BEAST_KILLS(75),
    HUMANOID_KILLS(76),
    UNDEAD_KILLS(77),
    NATURE_KILLS(78),
    CONSTRUCT_KILLS(79),
    GROTESQUE_KILLS(64),
    STRUCTURE_KILLS(65),
    GOD_KILLS(16),
    ASSISTS_AGAINST_GODS(17),
    CUBE_KILLS(18),
    ORYX_KILLS(19),
    QUESTS_COMPLETED(20),
    MINUTES_ACTIVE(12),
    DUNGEON_TYPES_COMPLETED(36),

    ABYSS_OF_DEMONS(23),
    ANCIENT_RUINS(35), // AncientRuinsCompleted
    BATTLE_FOR_THE_NEXUS(62),
    BEACHZONE(67),
    BELLADONNAS_GARDEN(48),
    CANDYLAND_HUNTING_GROUNDS(3),
    CAVE_OF_THOUSAND_TREASURES(5),
    CNIDARIAN_REEF(46),
    CRYSTAL_CAVERN(34),
    CULTIST_HIDEOUT(40),
    CURSED_LIBRARY(32),
    DAVY_JONES_LOCKER(7),
    DEADWATER_DOCKS(59),
    FORAX(37), // ForaxsCompleted
    FORBIDDEN_JUNGLE(15),
    FOREST_MAZE(1),
    FUNGAL_CAVERN(33),
    HAUNTED_CEMETERY(4),
    HEROIC_ABYSS_OF_DEMONES(38),
    HEROIC_UNDEAD_LAIR(39), // TODO verify
    HIDDEN_INTERREGNUM(68), // TODO verify
    HIGH_TECH_TERROR(88),
    ICE_CAVE(58), // IceCavesCompleted
    ICE_TOMB(89),
    KATALUND(90), // KatalundsCompleted
    KOGBOLD_STEAMWORKS(70),
    LAIR_OF_DRACONIS(2),
    LAIR_OF_SHAITAN(43),
    LOST_HALLS(55), // LostHallsCompleted
    MAD_LAB(6), // MadLabsCompleted
    MAGIC_WOODS(45),
    MALOGIA(92), // MalogiasCompleted
    MANOR_OF_THE_IMMORTALS(0),
    MOONLIGHT_VILLAGE(71),
    MOUNTAIN_TEMPLE(52), // MountainTemplesCompleted
    OCEAN_TRENCH(14),
    MAD_GOD_MAYHEM(91), // MadGodMayhemsCompleted
    ORYXS_CASTLE(93),
    ORYXS_CHAMBER(94),
    ORYXS_SANCTUARY(95),
    PARASITE_CHAMBERS(44),
    PIRATE_CAVE(21),
    PUPPET_MASTERS_ENCORE(42),
    PUPPET_MASTERS_THEATRE(49),
    RAINBOW_ROAD(80),
    SANTAS_WORKSHOP(81), //SantasWorkshopsCompleted
    SECLUDED_THICKET(47), // SecludedThicketsCompleted
    SNAKE_PIT(8),
    SPIDER_DEN(9),
    SPRITE_WORLD(10),
    SULFUROUS_WETLANDS(69),
    THE_CRAWLING_DEPTHS(60),
    THE_HIVE(51),
    THE_MACHINE(82), // TODO verify
    THE_NEST(53),
    THE_SHATTERS(63),
    THE_THIRD_DIMENSION(66),
    THE_VOID(41),
    TOMB_OF_THE_ANCIENTS(13),
    TOXIC_SEWERS(50),
    UNDEAD_LAIR(22),
    UNTARIS(83),
    WINE_CELLAR(84),
    WOODLAND_LABYRINTH(61);

    int id;

    private static final HashMap<Integer, String> NAME = new HashMap<>();

    static {
        for (CharacterStatistics o : CharacterStatistics.values()) {
            NAME.put(o.id, o.toString());
        }
    }

    CharacterStatistics(int i) {
        id = i;
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


    public int getId() {
        return id;
    }
}
