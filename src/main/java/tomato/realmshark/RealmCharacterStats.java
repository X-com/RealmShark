package tomato.realmshark;

import packets.reader.BufferReader;
import tomato.realmshark.enums.CharacterStatistics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Character statistics and dungeon completes
 */
public class RealmCharacterStats {
    /**
     * Stats
     */
    public int shots_fired;
    public int hits;
    public int ability_used;
    public int tiles_discovered;
    public int teleports;
    public int potions_drunk;
    public int kills;
    public int assists;
    public int party_level_ups;
    public int lesser_gods_kills;
    public int encounter_kills;
    public int hero_kills;
    public int critter_kills;
    public int beast_kills;
    public int humanoid_kills;
    public int undead_kills;
    public int nature_kills;
    public int construct_kills;
    public int grotesque_kills;
    public int structure_kills;
    public int god_kills;
    public int assists_against_gods;
    public int cube_kills;
    public int oryx_kills;
    public int quests_completed;
    public int minutes_active;
    public int dungeon_types_completed;
    public int stat_potion_consumed;
    public int ruins_enemy_kills;
    public int beach_enemy_kills;
    public int undead_forest_enemy_kills;
    public int forest_enemy_kills;
    public int plains_enemy_kills;
    public int wither_enemy_kills;
    public int dark_forest_enemy_kills;
    public int desert_enemy_kills;
    public int coral_reefs_enemy_kills;
    public int sprite_forest_enemy_kills;
    public int haunted_hallows_enemy_kills;
    public int shipwreck_cove_enemy_kills;
    public int dead_church_enemy_kills;
    public int risen_hells_enemy_kills;
    public int abandoned_city_enemy_kills;
    public int sea_abyss_enemy_kills;
    public int carboniferous_enemy_kills;
    public int floral_escape_enemy_kills;
    public int sanguine_forest_enemy_kills;
    public int runic_tundra_kills;

    /**
     * Dungeon completes
     */
    public int abyss_of_demons;
    public int advanced_kogbold_steamworks;
    public int ancient_ruins;
    public int battle_for_the_nexus;
    public int beachzone;
    public int belladonnas_garden;
    public int candyland_hunting_grounds;
    public int cave_of_thousand_treasures;
    public int cnidarian_reef;
    public int crystal_cavern;
    public int cultist_hideout;
    public int cursed_library;
    public int davy_jones_locker;
    public int deadwater_docks;
    public int forax;
    public int forbidden_jungle;
    public int forest_maze;
    public int fungal_cavern;
    public int haunted_cemetery;
    public int heroic_undead_lair;
    public int hidden_interregnum;
    public int high_tech_terror;
    public int ice_citadel;
    public int ice_tomb;
    public int infernal_abyss_of_demons;
    public int katalund;
    public int kogbold_steamworks;
    public int lair_of_draconis;
    public int lair_of_shaitan;
    public int legacy_heroic_abyss_of_demons;
    public int legacy_heroic_undead_lair;
    public int lost_halls;
    public int mad_lab;
    public int magic_woods;
    public int malogia;
    public int manor_of_the_immortals;
    public int mgm2;
    public int moonlight_village;
    public int mountain_temple;
    public int ocean_trench;
    public int mad_god_mayhem;
    public int oryxs_castle;
    public int oryxs_chamber;
    public int oryxs_sanctuary;
    public int parasite_chambers;
    public int pirate_cave;
    public int plagued_nest;
    public int puppet_masters_encore;
    public int puppet_masters_theatre;
    public int queen_bunny_chamber;
    public int rainbow_road;
    public int santas_workshop;
    public int secluded_thicket;
    public int snake_pit;
    public int spectral_penitentiary;
    public int spider_den;
    public int sprite_world;
    public int sulfurous_wetlands;
    public int the_crawling_depths;
    public int the_hive;
    public int the_machine;
    public int the_nest;
    public int the_shatters;
    public int the_tavern;
    public int the_third_dimension;
    public int the_void;
    public int tomb_of_the_ancients;
    public int toxic_sewers;
    public int undead_lair;
    public int untaris;
    public int white_snake_invasion_i;
    public int white_snake_invasion_ii;
    public int white_snake_invasion_iii;
    public int wine_cellar;
    public int woodland_labyrinth;

    public int[] regularStats;
    public int[] dungeonStats;
    public String pcStats;

    private static boolean debugMessage = false;

    public static void main(String[] args) {
        String s = "";
        debugMessage = true;
        new RealmCharacterStats().decode(s);
    }

    public void decode(String pcStats) {
        this.pcStats = pcStats;
        byte[] data = PcStatsDecoder.sixBitStringToBytes(pcStats);
        BufferReader reader = new BufferReader(ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN));

        int flag = reader.readInt();
        if (flag != 0) {
            try {
                boolean[] bitArray = parseBits(reader);
                if (debugMessage) {
                    for (int i = 0; i < bitArray.length; i++) {
                        if (CharacterStatistics.PC_NAME.get(i) == null && bitArray[i]) {
                            System.out.printf("%3d %s\n", i, CharacterStatistics.PC_NAME.get(i));
                        }
                    }
                }
                parseStats(reader, bitArray);
            } catch (Exception e) {
                System.out.println(pcStats);
            }
        }
        updateStats();
        if (debugMessage) {
            System.out.println(flag + " " + pcStats);
            for (int i = 0; i < CharacterStatistics.DUNGEON_NAMES.size(); i++) {
                if (dungeonStats[i] > 0) {
                    System.out.print(CharacterStatistics.DUNGEON_NAMES.get(i) + "=" + dungeonStats[i] + ", ");
                }
            }
            System.out.println();
        }
    }

    private void updateStats() {
        regularStats = new int[]{
                shots_fired,
                hits,
                ability_used,
                tiles_discovered,
                teleports,
                potions_drunk,
                kills,
                assists,
                party_level_ups,
                lesser_gods_kills,
                encounter_kills,
                hero_kills,
                critter_kills,
                beast_kills,
                humanoid_kills,
                undead_kills,
                nature_kills,
                construct_kills,
                grotesque_kills,
                structure_kills,
                god_kills,
                assists_against_gods,
                cube_kills,
                oryx_kills,
                quests_completed,
                minutes_active,
                dungeon_types_completed,
                stat_potion_consumed,
                ruins_enemy_kills,
                beach_enemy_kills,
                undead_forest_enemy_kills,
                forest_enemy_kills,
                plains_enemy_kills,
                wither_enemy_kills,
                dark_forest_enemy_kills,
                desert_enemy_kills,
                coral_reefs_enemy_kills,
                sprite_forest_enemy_kills,
                haunted_hallows_enemy_kills,
                shipwreck_cove_enemy_kills,
                dead_church_enemy_kills,
                risen_hells_enemy_kills,
                abandoned_city_enemy_kills,
                sea_abyss_enemy_kills,
                carboniferous_enemy_kills,
                floral_escape_enemy_kills,
                sanguine_forest_enemy_kills,
                runic_tundra_kills,
        };

        dungeonStats = new int[]{
                abyss_of_demons,
                advanced_kogbold_steamworks,
                ancient_ruins,
                battle_for_the_nexus,
                beachzone,
                belladonnas_garden,
                candyland_hunting_grounds,
                cave_of_thousand_treasures,
                cnidarian_reef,
                crystal_cavern,
                cultist_hideout,
                cursed_library,
                davy_jones_locker,
                deadwater_docks,
                forax,
                forbidden_jungle,
                forest_maze,
                fungal_cavern,
                haunted_cemetery,
                heroic_undead_lair,
                hidden_interregnum,
                high_tech_terror,
                ice_citadel,
                ice_tomb,
                infernal_abyss_of_demons,
                katalund,
                kogbold_steamworks,
                lair_of_draconis,
                lair_of_shaitan,
                legacy_heroic_abyss_of_demons,
                legacy_heroic_undead_lair,
                lost_halls,
                mad_god_mayhem,
                mad_lab,
                magic_woods,
                malogia,
                manor_of_the_immortals,
                mgm2,
                moonlight_village,
                mountain_temple,
                ocean_trench,
                oryxs_castle,
                oryxs_chamber,
                oryxs_sanctuary,
                parasite_chambers,
                pirate_cave,
                plagued_nest,
                puppet_masters_encore,
                puppet_masters_theatre,
                queen_bunny_chamber,
                rainbow_road,
                santas_workshop,
                secluded_thicket,
                snake_pit,
                spectral_penitentiary,
                spider_den,
                sprite_world,
                sulfurous_wetlands,
                the_crawling_depths,
                the_hive,
                the_machine,
                the_nest,
                the_shatters,
                the_tavern,
                the_third_dimension,
                the_void,
                tomb_of_the_ancients,
                toxic_sewers,
                undead_lair,
                untaris,
                white_snake_invasion_i,
                white_snake_invasion_ii,
                white_snake_invasion_iii,
                wine_cellar,
                woodland_labyrinth,
        };
    }

    private boolean[] parseBits(BufferReader reader) {
        boolean[] bitArray = new boolean[16 * 8];
        for (int i = 0; i < 16; i++) {
            int j = i * 8;
            byte b = reader.readByte();
            bitArray[j] = (b & 0x1) > 0;
            bitArray[j + 1] = (b & 0x2) > 0;
            bitArray[j + 2] = (b & 0x4) > 0;
            bitArray[j + 3] = (b & 0x8) > 0;
            bitArray[j + 4] = (b & 0x10) > 0;
            bitArray[j + 5] = (b & 0x20) > 0;
            bitArray[j + 6] = (b & 0x40) > 0;
            bitArray[j + 7] = (b & 0x80) > 0;
        }
        return bitArray;
    }

    private void parseStats(BufferReader reader, boolean[] bitArray) {
        if (readStat(bitArray, CharacterStatistics.SHOTS_FIRED.getPcStatId())) {
            shots_fired = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.HITS.getPcStatId())) {
            hits = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.ABILITY_USED.getPcStatId())) {
            ability_used = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.TILES_DISCOVERED.getPcStatId())) {
            tiles_discovered = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.TELEPORTS.getPcStatId())) {
            teleports = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.POTIONS_DRUNK.getPcStatId())) {
            potions_drunk = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.KILLS.getPcStatId())) {
            kills = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.ASSISTS.getPcStatId())) {
            assists = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.GOD_KILLS.getPcStatId())) {
            god_kills = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.ASSISTS_AGAINST_GODS.getPcStatId())) {
            assists_against_gods = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.CUBE_KILLS.getPcStatId())) {
            cube_kills = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.ORYX_KILLS.getPcStatId())) {
            oryx_kills = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.QUESTS_COMPLETED.getPcStatId())) {
            quests_completed = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.PIRATE_CAVE.getPcStatId())) {
            pirate_cave = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.UNDEAD_LAIR.getPcStatId())) {
            undead_lair = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.ABYSS_OF_DEMONS.getPcStatId())) {
            abyss_of_demons = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.SNAKE_PIT.getPcStatId())) {
            snake_pit = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.SPIDER_DEN.getPcStatId())) {
            spider_den = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.SPRITE_WORLD.getPcStatId())) {
            sprite_world = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.MINUTES_ACTIVE.getPcStatId())) {
            minutes_active = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.TOMB_OF_THE_ANCIENTS.getPcStatId())) {
            tomb_of_the_ancients = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.OCEAN_TRENCH.getPcStatId())) {
            ocean_trench = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.FORBIDDEN_JUNGLE.getPcStatId())) {
            forbidden_jungle = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.MANOR_OF_THE_IMMORTALS.getPcStatId())) {
            manor_of_the_immortals = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.FOREST_MAZE.getPcStatId())) {
            forest_maze = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.LAIR_OF_DRACONIS.getPcStatId())) {
            lair_of_draconis = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.CANDYLAND_HUNTING_GROUNDS.getPcStatId())) {
            candyland_hunting_grounds = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.HAUNTED_CEMETERY.getPcStatId())) {
            haunted_cemetery = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.CAVE_OF_THOUSAND_TREASURES.getPcStatId())) {
            cave_of_thousand_treasures = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.MAD_LAB.getPcStatId())) {
            mad_lab = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.DAVY_JONES_LOCKER.getPcStatId())) {
            davy_jones_locker = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.ICE_CITADEL.getPcStatId())) {
            ice_citadel = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.DEADWATER_DOCKS.getPcStatId())) {
            deadwater_docks = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.THE_CRAWLING_DEPTHS.getPcStatId())) {
            the_crawling_depths = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.WOODLAND_LABYRINTH.getPcStatId())) {
            woodland_labyrinth = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.BATTLE_FOR_THE_NEXUS.getPcStatId())) {
            battle_for_the_nexus = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.THE_SHATTERS.getPcStatId())) {
            the_shatters = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.BELLADONNAS_GARDEN.getPcStatId())) {
            belladonnas_garden = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.PUPPET_MASTERS_THEATRE.getPcStatId())) {
            puppet_masters_theatre = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.TOXIC_SEWERS.getPcStatId())) {
            toxic_sewers = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.THE_HIVE.getPcStatId())) {
            the_hive = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.MOUNTAIN_TEMPLE.getPcStatId())) {
            mountain_temple = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.THE_NEST.getPcStatId())) {
            the_nest = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.LOST_HALLS.getPcStatId())) {
            lost_halls = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.CULTIST_HIDEOUT.getPcStatId())) {
            cultist_hideout = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.THE_VOID.getPcStatId())) {
            the_void = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.PUPPET_MASTERS_ENCORE.getPcStatId())) {
            puppet_masters_encore = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.LAIR_OF_SHAITAN.getPcStatId())) {
            lair_of_shaitan = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.PARASITE_CHAMBERS.getPcStatId())) {
            parasite_chambers = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.MAGIC_WOODS.getPcStatId())) {
            magic_woods = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.CNIDARIAN_REEF.getPcStatId())) {
            cnidarian_reef = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.SECLUDED_THICKET.getPcStatId())) {
            secluded_thicket = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.CURSED_LIBRARY.getPcStatId())) {
            cursed_library = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.FUNGAL_CAVERN.getPcStatId())) {
            fungal_cavern = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.CRYSTAL_CAVERN.getPcStatId())) {
            crystal_cavern = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.ANCIENT_RUINS.getPcStatId())) {
            ancient_ruins = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.DUNGEON_TYPES_COMPLETED.getPcStatId())) {
            dungeon_types_completed = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.FORAX.getPcStatId())) {
            forax = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.LEGACY_HEROIC_ABYSS_OF_DEMONS.getPcStatId())) {
            legacy_heroic_abyss_of_demons = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.LEGACY_HEROIC_UNDEAD_LAIR.getPcStatId())) {
            legacy_heroic_undead_lair = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.HIGH_TECH_TERROR.getPcStatId())) {
            high_tech_terror = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.ICE_TOMB.getPcStatId())) {
            ice_tomb = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.KATALUND.getPcStatId())) {
            katalund = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.MAD_GOD_MAYHEM.getPcStatId())) {
            mad_god_mayhem = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.MALOGIA.getPcStatId())) {
            malogia = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.ORYXS_CASTLE.getPcStatId())) {
            oryxs_castle = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.ORYXS_CHAMBER.getPcStatId())) {
            oryxs_chamber = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.ORYXS_SANCTUARY.getPcStatId())) {
            oryxs_sanctuary = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.RAINBOW_ROAD.getPcStatId())) {
            rainbow_road = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.SANTAS_WORKSHOP.getPcStatId())) {
            santas_workshop = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.THE_MACHINE.getPcStatId())) {
            the_machine = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.UNTARIS.getPcStatId())) {
            untaris = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.WINE_CELLAR.getPcStatId())) {
            wine_cellar = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.PARTY_LEVEL_UPS.getPcStatId())) {
            party_level_ups = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.LESSER_GODS_KILLS.getPcStatId())) {
            lesser_gods_kills = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.ENCOUNTER_KILLS.getPcStatId())) {
            encounter_kills = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.HERO_KILLS.getPcStatId())) {
            hero_kills = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.CRITTER_KILLS.getPcStatId())) {
            critter_kills = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.BEAST_KILLS.getPcStatId())) {
            beast_kills = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.HUMANOID_KILLS.getPcStatId())) {
            humanoid_kills = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.UNDEAD_KILLS.getPcStatId())) {
            undead_kills = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.NATURE_KILLS.getPcStatId())) {
            nature_kills = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.CONSTRUCT_KILLS.getPcStatId())) {
            construct_kills = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.GROTESQUE_KILLS.getPcStatId())) {
            grotesque_kills = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.STRUCTURE_KILLS.getPcStatId())) {
            structure_kills = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.THE_THIRD_DIMENSION.getPcStatId())) {
            the_third_dimension = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.BEACHZONE.getPcStatId())) {
            beachzone = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.HIDDEN_INTERREGNUM.getPcStatId())) {
            hidden_interregnum = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.SULFUROUS_WETLANDS.getPcStatId())) {
            sulfurous_wetlands = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.KOGBOLD_STEAMWORKS.getPcStatId())) {
            kogbold_steamworks = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.MOONLIGHT_VILLAGE.getPcStatId())) {
            moonlight_village = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.ADVANCED_KOGBOLD_STEAMWORKS.getPcStatId())) {
            advanced_kogbold_steamworks = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.PLAGUED_NEST.getPcStatId())) {
            plagued_nest = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.THE_TAVERN.getPcStatId())) {
            the_tavern = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.QUEEN_BUNNY_CHAMBER.getPcStatId())) {
            queen_bunny_chamber = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.STAT_POTION_CONSUMED.getPcStatId())) {
            stat_potion_consumed = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.SPECTRAL_PENITENTIARY.getPcStatId())) {
            spectral_penitentiary = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.RUINS_ENEMY_KILLS.getPcStatId())) {
            ruins_enemy_kills = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.BEACH_ENEMY_KILLS.getPcStatId())) {
            beach_enemy_kills = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.UNDEAD_FOREST_ENEMY_KILLS.getPcStatId())) {
            undead_forest_enemy_kills = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.FOREST_ENEMY_KILLS.getPcStatId())) {
            forest_enemy_kills = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.PLAINS_ENEMY_KILLS.getPcStatId())) {
            plains_enemy_kills = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.WITHER_ENEMY_KILLS.getPcStatId())) {
            wither_enemy_kills = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.DARK_FOREST_ENEMY_KILLS.getPcStatId())) {
            dark_forest_enemy_kills = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.DESERT_ENEMY_KILLS.getPcStatId())) {
            desert_enemy_kills = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.CORAL_REEFS_ENEMY_KILLS.getPcStatId())) {
            coral_reefs_enemy_kills = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.SPRITE_FOREST_ENEMY_KILLS.getPcStatId())) {
            sprite_forest_enemy_kills = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.HAUNTED_HALLOWS_ENEMY_KILLS.getPcStatId())) {
            haunted_hallows_enemy_kills = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.SHIPWRECK_COVE_ENEMY_KILLS.getPcStatId())) {
            shipwreck_cove_enemy_kills = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.DEAD_CHURCH_ENEMY_KILLS.getPcStatId())) {
            dead_church_enemy_kills = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.RISEN_HELLS_ENEMY_KILLS.getPcStatId())) {
            risen_hells_enemy_kills = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.ABANDONED_CITY_ENEMY_KILLS.getPcStatId())) {
            abandoned_city_enemy_kills = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.SEA_ABYSS_ENEMY_KILLS.getPcStatId())) {
            sea_abyss_enemy_kills = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.CARBONIFEROUS_ENEMY_KILLS.getPcStatId())) {
            carboniferous_enemy_kills = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.FLORAL_ESCAPE_ENEMY_KILLS.getPcStatId())) {
            floral_escape_enemy_kills = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.SANGUINE_FOREST_ENEMY_KILLS.getPcStatId())) {
            sanguine_forest_enemy_kills = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.RUNIC_TUNDRA_KILLS.getPcStatId())) {
            runic_tundra_kills = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.WHITE_SNAKE_INVASION_I.getPcStatId())) {
            white_snake_invasion_i = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.WHITE_SNAKE_INVASION_II.getPcStatId())) {
            white_snake_invasion_ii = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.WHITE_SNAKE_INVASION_III.getPcStatId())) {
            white_snake_invasion_iii = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.MGM2.getPcStatId())) {
            mgm2 = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.HEROIC_UNDEAD_LAIR.getPcStatId())) {
            heroic_undead_lair = reader.readCompressedInt();
        }
        if (readStat(bitArray, CharacterStatistics.INFERNAL_ABYSS_OF_DEMONS.getPcStatId())) {
            infernal_abyss_of_demons = reader.readCompressedInt();
        }
    }

    public int getDungeonInfoByName(String dungeonName) {
        int index = CharacterStatistics.getDungeonIndex(dungeonName);
        if (index == -1) return -1;
        return dungeonStats[index];
    }

    private static boolean readStat(boolean[] bitArray, int id) {
        if (id == -1) return false;
        return bitArray[id];
    }

    @Override
    public String toString() {
        return "RealmCharacterStats{" +
                "\n   shots_fired=" + shots_fired +
                "\n   hits=" + hits +
                "\n   ability_used=" + ability_used +
                "\n   tiles_discovered=" + tiles_discovered +
                "\n   teleports=" + teleports +
                "\n   potions_drunk=" + potions_drunk +
                "\n   kills=" + kills +
                "\n   assists=" + assists +
                "\n   party_level_ups=" + party_level_ups +
                "\n   lesser_gods_kills=" + lesser_gods_kills +
                "\n   encounter_kills=" + encounter_kills +
                "\n   hero_kills=" + hero_kills +
                "\n   critter_kills=" + critter_kills +
                "\n   beast_kills=" + beast_kills +
                "\n   humanoid_kills=" + humanoid_kills +
                "\n   undead_kills=" + undead_kills +
                "\n   nature_kills=" + nature_kills +
                "\n   construct_kills=" + construct_kills +
                "\n   grotesque_kills=" + grotesque_kills +
                "\n   structure_kills=" + structure_kills +
                "\n   god_kills=" + god_kills +
                "\n   assists_against_gods=" + assists_against_gods +
                "\n   cube_kills=" + cube_kills +
                "\n   oryx_kills=" + oryx_kills +
                "\n   quests_completed=" + quests_completed +
                "\n   minutes_active=" + minutes_active +
                "\n   dungeon_types_completed=" + dungeon_types_completed +
                "\n   stat_potion_consumed=" + stat_potion_consumed +
                "\n   ruins_enemy_kills=" + ruins_enemy_kills +
                "\n   beach_enemy_kills=" + beach_enemy_kills +
                "\n   undead_forest_enemy_kills=" + undead_forest_enemy_kills +
                "\n   forest_enemy_kills=" + forest_enemy_kills +
                "\n   plains_enemy_kills=" + plains_enemy_kills +
                "\n   wither_enemy_kills=" + wither_enemy_kills +
                "\n   dark_forest_enemy_kills=" + dark_forest_enemy_kills +
                "\n   desert_enemy_kills=" + desert_enemy_kills +
                "\n   coral_reefs_enemy_kills=" + coral_reefs_enemy_kills +
                "\n   sprite_forest_enemy_kills=" + sprite_forest_enemy_kills +
                "\n   haunted_hallows_enemy_kills=" + haunted_hallows_enemy_kills +
                "\n   shipwreck_cove_enemy_kills=" + shipwreck_cove_enemy_kills +
                "\n   dead_church_enemy_kills=" + dead_church_enemy_kills +
                "\n   risen_hells_enemy_kills=" + risen_hells_enemy_kills +
                "\n   abandoned_city_enemy_kills=" + abandoned_city_enemy_kills +
                "\n   sea_abyss_enemy_kills=" + sea_abyss_enemy_kills +
                "\n   carboniferous_enemy_kills=" + carboniferous_enemy_kills +
                "\n   floral_escape_enemy_kills=" + floral_escape_enemy_kills +
                "\n   sanguine_forest_enemy_kills=" + sanguine_forest_enemy_kills +
                "\n   runic_tundra_kills=" + runic_tundra_kills +
                "\n   abyss_of_demons=" + abyss_of_demons +
                "\n   advanced_kogbold_steamworks=" + advanced_kogbold_steamworks +
                "\n   ancient_ruins=" + ancient_ruins +
                "\n   battle_for_the_nexus=" + battle_for_the_nexus +
                "\n   beachzone=" + beachzone +
                "\n   belladonnas_garden=" + belladonnas_garden +
                "\n   candyland_hunting_grounds=" + candyland_hunting_grounds +
                "\n   cave_of_thousand_treasures=" + cave_of_thousand_treasures +
                "\n   cnidarian_reef=" + cnidarian_reef +
                "\n   crystal_cavern=" + crystal_cavern +
                "\n   cultist_hideout=" + cultist_hideout +
                "\n   cursed_library=" + cursed_library +
                "\n   davy_jones_locker=" + davy_jones_locker +
                "\n   deadwater_docks=" + deadwater_docks +
                "\n   forax=" + forax +
                "\n   forbidden_jungle=" + forbidden_jungle +
                "\n   forest_maze=" + forest_maze +
                "\n   fungal_cavern=" + fungal_cavern +
                "\n   haunted_cemetery=" + haunted_cemetery +
                "\n   heroic_undead_lair=" + heroic_undead_lair +
                "\n   hidden_interregnum=" + hidden_interregnum +
                "\n   high_tech_terror=" + high_tech_terror +
                "\n   ice_citadel=" + ice_citadel +
                "\n   ice_tomb=" + ice_tomb +
                "\n   infernal_abyss_of_demons=" + infernal_abyss_of_demons +
                "\n   katalund=" + katalund +
                "\n   kogbold_steamworks=" + kogbold_steamworks +
                "\n   lair_of_draconis=" + lair_of_draconis +
                "\n   lair_of_shaitan=" + lair_of_shaitan +
                "\n   legacy_heroic_abyss_of_demons=" + legacy_heroic_abyss_of_demons +
                "\n   legacy_heroic_undead_lair=" + legacy_heroic_undead_lair +
                "\n   lost_halls=" + lost_halls +
                "\n   mad_god_mayhem=" + mad_god_mayhem +
                "\n   mad_lab=" + mad_lab +
                "\n   magic_woods=" + magic_woods +
                "\n   malogia=" + malogia +
                "\n   manor_of_the_immortals=" + manor_of_the_immortals +
                "\n   mgm2=" + mgm2 +
                "\n   moonlight_village=" + moonlight_village +
                "\n   mountain_temple=" + mountain_temple +
                "\n   ocean_trench=" + ocean_trench +
                "\n   oryxs_castle=" + oryxs_castle +
                "\n   oryxs_chamber=" + oryxs_chamber +
                "\n   oryxs_sanctuary=" + oryxs_sanctuary +
                "\n   parasite_chambers=" + parasite_chambers +
                "\n   pirate_cave=" + pirate_cave +
                "\n   plagued_nest=" + plagued_nest +
                "\n   puppet_masters_encore=" + puppet_masters_encore +
                "\n   puppet_masters_theatre=" + puppet_masters_theatre +
                "\n   queen_bunny_chamber=" + queen_bunny_chamber +
                "\n   rainbow_road=" + rainbow_road +
                "\n   santas_workshop=" + santas_workshop +
                "\n   secluded_thicket=" + secluded_thicket +
                "\n   snake_pit=" + snake_pit +
                "\n   spectral_penitentiary=" + spectral_penitentiary +
                "\n   spider_den=" + spider_den +
                "\n   sprite_world=" + sprite_world +
                "\n   sulfurous_wetlands=" + sulfurous_wetlands +
                "\n   the_crawling_depths=" + the_crawling_depths +
                "\n   the_hive=" + the_hive +
                "\n   the_machine=" + the_machine +
                "\n   the_nest=" + the_nest +
                "\n   the_shatters=" + the_shatters +
                "\n   the_tavern=" + the_tavern +
                "\n   the_third_dimension=" + the_third_dimension +
                "\n   the_void=" + the_void +
                "\n   tomb_of_the_ancients=" + tomb_of_the_ancients +
                "\n   toxic_sewers=" + toxic_sewers +
                "\n   undead_lair=" + undead_lair +
                "\n   untaris=" + untaris +
                "\n   white_snake_invasion_i=" + white_snake_invasion_i +
                "\n   white_snake_invasion_ii=" + white_snake_invasion_ii +
                "\n   white_snake_invasion_iii=" + white_snake_invasion_iii +
                "\n   wine_cellar=" + wine_cellar +
                "\n   woodland_labyrinth=" + woodland_labyrinth;
    }
}
