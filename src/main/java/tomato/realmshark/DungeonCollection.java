package tomato.realmshark;

import java.util.ArrayList;

public class DungeonCollection {

    public static final String[] collection = {
            "Tunnel Rat",
            "Explosive Journey",
            "Travel of the Decade",
            "First Steps",
            "King of the Mountains",
            "Conqueror of the Realm",
            "Enemy of the Court",
            "Epic Battles",
            "Far Out",
            "Hero of the Nexus",
            "Season's Beatins",
            "Realm of the Mad God",};

    public static final String[] collectionBonus = {
            "7.5%, 3,000 Fame",
            "7.5%, 3,000 Fame",
            "10%, 5,000 Fame",
            "2.5%, 100 Fame",
            "5%, 1,000 Fame",
            "10%, 4,000 Fame",
            "7.5%, 3,000 Fame",
            "7.5%, 2,000 Fame",
            "5%, 2,000 Fame",
            "12.5%, 5,000 Fame",
            "12.5%, 5,000 Fame",
            "25%, 10,000 Fame",};

    public static final String[] collectionShort = {"TR", "EJ", "TD", "FS", "KM", "CR", "EC", "EB", "FO", "HN", "SB", "RMG",};

    public static ArrayList<String> collection(String t, RealmCharacter c) {
        if (t.equals(collection[0])) {
            return tunnelRat(c.charStats);
        } else if (t.equals(collection[1])) {
            return explosiveJourney(c.charStats);
        } else if (t.equals(collection[2])) {
            return travelOfTheDecade(c.charStats);
        } else if (t.equals(collection[3])) {
            return firstSteps(c.charStats);
        } else if (t.equals(collection[4])) {
            return kingOfTheMountains(c.charStats);
        } else if (t.equals(collection[5])) {
            return conquerorOfTheRealm(c.charStats);
        } else if (t.equals(collection[6])) {
            return enemyOfTheCourt(c.charStats);
        } else if (t.equals(collection[7])) {
            return epicBattles(c.charStats);
        } else if (t.equals(collection[8])) {
            return farOut(c.charStats);
        } else if (t.equals(collection[9])) {
            return heroOfTheNexus(c.charStats);
        } else if (t.equals(collection[10])) {
            return seasonsBeatins(c.charStats);
        } else if (t.equals(collection[11])) {
            return realmOfTheMadGod(c.charStats);
        }
        return null;
    }

    public static ArrayList<String> tunnelRat(RealmCharacterStats c) {
        ArrayList<String> list = new ArrayList<>();

        if (c.abyss_of_demons == 0) list.add("Abyss of Demons");
        if (c.forbidden_jungle == 0) list.add("Forbidden Jungle");
        if (c.manor_of_the_immortals == 0) list.add("Manor of the Immortals");
        if (c.ocean_trench == 0) list.add("Ocean Trench");
        if (c.oryxs_castle == 0) list.add("Oryx's Castle");
        if (c.oryxs_chamber == 0) list.add("Oryx's Chamber");
        if (c.pirate_cave == 0) list.add("Pirate Cave");
        if (c.snake_pit == 0) list.add("Snake Pit");
        if (c.spider_den == 0) list.add("Spider Den");
        if (c.tomb_of_the_ancients == 0) list.add("Tomb of the Ancients");
        if (c.undead_lair == 0) list.add("Undead Lair");
        if (c.wine_cellar == 0) list.add("Wine Cellar");

        return list;
    }

    private static ArrayList<String> explosiveJourney(RealmCharacterStats c) {
        ArrayList<String> list = new ArrayList<>();

        if (c.candyland_hunting_grounds == 0) list.add("Candyland Hunting Grounds");
        if (c.cave_of_thousand_treasures == 0) list.add("Cave of a Thousand Treasures");
        if (c.davy_jones_locker == 0) list.add("Davy Jones' Locker");
        if (c.deadwater_docks == 0) list.add("Deadwater Docks");
        if (c.haunted_cemetery == 0) list.add("Haunted Cemetery");
        if (c.ice_cave == 0) list.add("Ice Cave");
        if (c.lair_of_draconis == 0) list.add("Lair of Draconis");
        if (c.lair_of_shaitan == 0) list.add("Lair of Shaitan");
        if (c.mad_lab == 0) list.add("Mad Lab");
        if (c.puppet_masters_theatre == 0) list.add("Puppet Master's Theatre");
        if (c.the_crawling_depths == 0) list.add("The Crawling Depths");
        if (c.the_shatters == 0) list.add("The Shatters");
        if (c.woodland_labyrinth == 0) list.add("Woodland Labyrinth");

        return list;
    }

    private static ArrayList<String> travelOfTheDecade(RealmCharacterStats c) {
        ArrayList<String> list = new ArrayList<>();

        if (c.ancient_ruins == 0) list.add("Ancient Ruins");
        if (c.cnidarian_reef == 0) list.add("Cnidarian Reef");
        if (c.cultist_hideout == 0) list.add("Cultist Hideout");
        if (c.cursed_library == 0) list.add("Cursed Library");
        if (c.high_tech_terror == 0) list.add("High Tech Terror");
        if (c.lost_halls == 0) list.add("Lost Halls");
        if (c.magic_woods == 0) list.add("Magic Woods");
        if (c.mountain_temple == 0) list.add("Mountain Temple");
        if (c.oryxs_sanctuary == 0) list.add("Oryx's Sanctuary");
        if (c.parasite_chambers == 0) list.add("Parasite Chambers");
        if (c.puppet_masters_encore == 0) list.add("Puppet Master's Encore");
        if (c.secluded_thicket == 0) list.add("Secluded Thicket");
        if (c.sulfurous_wetlands == 0) list.add("Sulfurous Wetlands");
        if (c.the_hive == 0) list.add("The Hive");
        if (c.the_nest == 0) list.add("The Nest");
        if (c.the_third_dimension == 0) list.add("The Third Dimension");
        if (c.the_void == 0) list.add("The Void");
        if (c.toxic_sewers == 0) list.add("Toxic Sewers");

        return list;
    }

    private static ArrayList<String> firstSteps(RealmCharacterStats c) {
        ArrayList<String> list = new ArrayList<>();

        if (c.forbidden_jungle == 0) list.add("Forbidden Jungle");
        if (c.forest_maze == 0) list.add("Forest Maze");
        if (c.pirate_cave == 0) list.add("Pirate Cave");
        if (c.spider_den == 0) list.add("Spider Den");
        if (c.the_hive == 0) list.add("The Hive");

        return list;
    }

    private static ArrayList<String> kingOfTheMountains(RealmCharacterStats c) {
        ArrayList<String> list = new ArrayList<>();

        if (c.abyss_of_demons == 0) list.add("Abyss of Demons");
        if (c.ancient_ruins == 0) list.add("Ancient Ruins");
        if (c.cursed_library == 0) list.add("Cursed Library");
        if (c.haunted_cemetery == 0) list.add("Haunted Cemetery");
        if (c.mad_lab == 0) list.add("Mad Lab");
        if (c.magic_woods == 0) list.add("Magic Woods");
        if (c.puppet_masters_theatre == 0) list.add("Puppet Master's Theatre");
        if (c.snake_pit == 0) list.add("Snake Pit");
        if (c.sprite_world == 0) list.add("Sprite World");
        if (c.sulfurous_wetlands == 0) list.add("Sulfurous Wetlands");
        if (c.toxic_sewers == 0) list.add("Toxic Sewers");

        return list;
    }

    private static ArrayList<String> conquerorOfTheRealm(RealmCharacterStats c) {
        ArrayList<String> list = new ArrayList<>();

        if (c.crystal_cavern == 0) list.add("Crystal Cavern");
        if (c.davy_jones_locker == 0) list.add("Davy Jones' Locker");
        if (c.fungal_cavern == 0) list.add("Fungal Cavern");
        if (c.ice_cave == 0) list.add("Ice Cave");
        if (c.kogbold_steamworks == 0) list.add("Kogbold Steamworks");
        if (c.lair_of_draconis == 0) list.add("Lair of Draconis");
        if (c.lost_halls == 0) list.add("Lost Halls");
        if (c.mountain_temple == 0) list.add("Mountain Temple");
        if (c.ocean_trench == 0) list.add("Ocean Trench");
        if (c.the_nest == 0) list.add("The Nest");
        if (c.the_shatters == 0) list.add("The Shatters");
        if (c.the_third_dimension == 0) list.add("The Third Dimension");
        if (c.tomb_of_the_ancients == 0) list.add("Tomb of the Ancients");

        return list;
    }

    private static ArrayList<String> enemyOfTheCourt(RealmCharacterStats c) {
        ArrayList<String> list = new ArrayList<>();

        if (c.cnidarian_reef == 0) list.add("Cnidarian Reef");
        if (c.high_tech_terror == 0) list.add("High Tech Terror");
        if (c.lair_of_shaitan == 0) list.add("Lair of Shaitan");
        if (c.puppet_masters_encore == 0) list.add("Puppet Master's Encore");
        if (c.secluded_thicket == 0) list.add("Secluded Thicket");

        return list;
    }

    private static ArrayList<String> epicBattles(RealmCharacterStats c) {
        ArrayList<String> list = new ArrayList<>();

        if (c.deadwater_docks == 0) list.add("Deadwater Docks");
        if (c.secluded_thicket == 0) list.add("Secluded Thicket");
        if (c.the_crawling_depths == 0) list.add("The Crawling Depths");
        if (c.the_nest == 0) list.add("The Nest");
        if (c.woodland_labyrinth == 0) list.add("Woodland Labyrinth");

        return list;
    }

    private static ArrayList<String> farOut(RealmCharacterStats c) {
        ArrayList<String> list = new ArrayList<>();

        if (c.forax == 0) list.add("Forax");
        if (c.katalund == 0) list.add("Katalund");
        if (c.malogia == 0) list.add("Malogia");
        if (c.untaris == 0) list.add("Untaris");

        return list;
    }

    private static ArrayList<String> heroOfTheNexus(RealmCharacterStats c) {
        ArrayList<String> list = new ArrayList<>();

        if (c.abyss_of_demons == 0) list.add("Abyss of Demons");
        if (c.ancient_ruins == 0) list.add("Ancient Ruins");
        if (c.candyland_hunting_grounds == 0) list.add("Candyland Hunting Grounds");
        if (c.cave_of_thousand_treasures == 0) list.add("Cave of a Thousand Treasures");
        if (c.cnidarian_reef == 0) list.add("Cnidarian Reef");
        if (c.crystal_cavern == 0) list.add("Crystal Cavern");
        if (c.cultist_hideout == 0) list.add("Cultist Hideout");
        if (c.cursed_library == 0) list.add("Cursed Library");
        if (c.davy_jones_locker == 0) list.add("Davy Jones' Locker");
        if (c.deadwater_docks == 0) list.add("Deadwater Docks");
        if (c.forbidden_jungle == 0) list.add("Forbidden Jungle");
        if (c.forest_maze == 0) list.add("Forest Maze");
        if (c.fungal_cavern == 0) list.add("Fungal Cavern");
        if (c.haunted_cemetery == 0) list.add("Haunted Cemetery");
        if (c.high_tech_terror == 0) list.add("High Tech Terror");
        if (c.ice_cave == 0) list.add("Ice Cave");
        if (c.kogbold_steamworks == 0) list.add("Kogbold Steamworks");
        if (c.lair_of_draconis == 0) list.add("Lair of Draconis");
        if (c.lair_of_shaitan == 0) list.add("Lair of Shaitan");
        if (c.lost_halls == 0) list.add("Lost Halls");
        if (c.mad_lab == 0) list.add("Mad Lab");
        if (c.magic_woods == 0) list.add("Magic Woods");
        if (c.manor_of_the_immortals == 0) list.add("Manor of the Immortals");
        if (c.mountain_temple == 0) list.add("Mountain Temple");
        if (c.ocean_trench == 0) list.add("Ocean Trench");
        if (c.oryxs_castle == 0) list.add("Oryx's Castle");
        if (c.oryxs_chamber == 0) list.add("Oryx's Chamber");
        if (c.oryxs_sanctuary == 0) list.add("Oryx's Sanctuary");
        if (c.parasite_chambers == 0) list.add("Parasite Chambers");
        if (c.pirate_cave == 0) list.add("Pirate Cave");
        if (c.puppet_masters_encore == 0) list.add("Puppet Master's Encore");
        if (c.puppet_masters_theatre == 0) list.add("Puppet Master's Theatre");
        if (c.secluded_thicket == 0) list.add("Secluded Thicket");
        if (c.snake_pit == 0) list.add("Snake Pit");
        if (c.spider_den == 0) list.add("Spider Den");
        if (c.sprite_world == 0) list.add("Sprite World");
        if (c.sulfurous_wetlands == 0) list.add("Sulfurous Wetlands");
        if (c.the_crawling_depths == 0) list.add("The Crawling Depths");
        if (c.the_hive == 0) list.add("The Hive");
        if (c.the_nest == 0) list.add("The Nest");
        if (c.the_shatters == 0) list.add("The Shatters");
        if (c.the_third_dimension == 0) list.add("The Third Dimension");
        if (c.the_void == 0) list.add("The Void");
        if (c.tomb_of_the_ancients == 0) list.add("Tomb of the Ancients");
        if (c.toxic_sewers == 0) list.add("Toxic Sewers");
        if (c.undead_lair == 0) list.add("Undead Lair");
        if (c.wine_cellar == 0) list.add("Wine Cellar");
        if (c.woodland_labyrinth == 0) list.add("Woodland Labyrinth");

        return list;
    }

    private static ArrayList<String> seasonsBeatins(RealmCharacterStats c) {
        ArrayList<String> list = new ArrayList<>();

        if (c.battle_for_the_nexus == 0) list.add("Battle for the Nexus");
        if (c.beachzone == 0) list.add("Beachzone");
        if (c.belladonnas_garden == 0) list.add("Belladonna's Garden");
        if (c.forax == 0) list.add("Forax");
        if (c.ice_tomb == 0) list.add("Ice Tomb");
        if (c.katalund == 0) list.add("Katalund");
        if (c.mad_god_mayhem == 0) list.add("Mad God Mayhem");
        if (c.malogia == 0) list.add("Malogia");
        if (c.rainbow_road == 0) list.add("Rainbow Road");
        if (c.santas_workshop == 0) list.add("Santa's Workshop");
        if (c.the_machine == 0) list.add("The Machine");
        if (c.untaris == 0) list.add("Untaris");

        return list;
    }

    private static ArrayList<String> realmOfTheMadGod(RealmCharacterStats c) {
        ArrayList<String> list = new ArrayList<>();

        if (c.abyss_of_demons == 0) list.add("Abyss of Demons");
        if (c.ancient_ruins == 0) list.add("Ancient Ruins");
        if (c.battle_for_the_nexus == 0) list.add("Battle for the Nexus");
        if (c.beachzone == 0) list.add("Beachzone");
        if (c.belladonnas_garden == 0) list.add("Belladonna's Garden");
        if (c.candyland_hunting_grounds == 0) list.add("Candyland Hunting Grounds");
        if (c.cave_of_thousand_treasures == 0) list.add("Cave of a Thousand Treasures");
        if (c.cnidarian_reef == 0) list.add("Cnidarian Reef");
        if (c.crystal_cavern == 0) list.add("Crystal Cavern");
        if (c.cultist_hideout == 0) list.add("Cultist Hideout");
        if (c.cursed_library == 0) list.add("Cursed Library");
        if (c.davy_jones_locker == 0) list.add("Davy Jones' Locker");
        if (c.deadwater_docks == 0) list.add("Deadwater Docks");
        if (c.forax == 0) list.add("Forax");
        if (c.forbidden_jungle == 0) list.add("Forbidden Jungle");
        if (c.forest_maze == 0) list.add("Forest Maze");
        if (c.fungal_cavern == 0) list.add("Fungal Cavern");
        if (c.haunted_cemetery == 0) list.add("Haunted Cemetery");
        if (c.high_tech_terror == 0) list.add("High Tech Terror");
        if (c.ice_cave == 0) list.add("Ice Cave");
        if (c.ice_tomb == 0) list.add("Ice Tomb");
        if (c.katalund == 0) list.add("Katalund");
        if (c.kogbold_steamworks == 0) list.add("Kogbold Steamworks");
        if (c.lair_of_draconis == 0) list.add("Lair of Draconis");
        if (c.lair_of_shaitan == 0) list.add("Lair of Shaitan");
        if (c.lost_halls == 0) list.add("Lost Halls");
        if (c.mad_god_mayhem == 0) list.add("Mad God Mayhem");
        if (c.mad_lab == 0) list.add("Mad Lab");
        if (c.magic_woods == 0) list.add("Magic Woods");
        if (c.malogia == 0) list.add("Malogia");
        if (c.manor_of_the_immortals == 0) list.add("Manor of the Immortals");
        if (c.mountain_temple == 0) list.add("Mountain Temple");
        if (c.ocean_trench == 0) list.add("Ocean Trench");
        if (c.oryxs_castle == 0) list.add("Oryx's Castle");
        if (c.oryxs_chamber == 0) list.add("Oryx's Chamber");
        if (c.oryxs_sanctuary == 0) list.add("Oryx's Sanctuary");
        if (c.parasite_chambers == 0) list.add("Parasite Chambers");
        if (c.pirate_cave == 0) list.add("Pirate Cave");
        if (c.puppet_masters_encore == 0) list.add("Puppet Master's Encore");
        if (c.puppet_masters_theatre == 0) list.add("Puppet Master's Theatre");
        if (c.rainbow_road == 0) list.add("Rainbow Road");
        if (c.santas_workshop == 0) list.add("Santa's Workshop");
        if (c.secluded_thicket == 0) list.add("Secluded Thicket");
        if (c.snake_pit == 0) list.add("Snake Pit");
        if (c.spider_den == 0) list.add("Spider Den");
        if (c.sprite_world == 0) list.add("Sprite World");
        if (c.sulfurous_wetlands == 0) list.add("Sulfurous Wetlands");
        if (c.the_crawling_depths == 0) list.add("The Crawling Depths");
        if (c.the_hive == 0) list.add("The Hive");
        if (c.the_machine == 0) list.add("The Machine");
        if (c.the_nest == 0) list.add("The Nest");
        if (c.the_shatters == 0) list.add("The Shatters");
        if (c.the_third_dimension == 0) list.add("The Third Dimension");
        if (c.the_void == 0) list.add("The Void");
        if (c.tomb_of_the_ancients == 0) list.add("Tomb of the Ancients");
        if (c.toxic_sewers == 0) list.add("Toxic Sewers");
        if (c.undead_lair == 0) list.add("Undead Lair");
        if (c.untaris == 0) list.add("Untaris");
        if (c.wine_cellar == 0) list.add("Wine Cellar");
        if (c.woodland_labyrinth == 0) list.add("Woodland Labyrinth");

        return list;
    }
}