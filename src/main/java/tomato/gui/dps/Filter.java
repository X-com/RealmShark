package tomato.gui.dps;

import tomato.backend.data.Entity;
import tomato.realmshark.enums.CharacterClass;

import javax.swing.*;
import java.util.Arrays;
import java.util.HashSet;

public class Filter {

    public static String name;
    public static int filter = 1;
    public static boolean myGuildFilter;
    public static boolean myClassFilter;
    public static HashSet<String> filterNames = new HashSet<>();
    public static HashSet<String> filterGuilds = new HashSet<>();
    public static HashSet<Integer> filterClasses = new HashSet<>();

    public static int filter(Entity owner, Entity player) {
        if (filter == 0) return 0;
        int classType = owner.objectType;
        String name = owner.name().toLowerCase();
        String guild = owner.getStatGuild().toLowerCase();
        String myGuild = player.getStatGuild().toLowerCase();

        if (myGuildFilter && myGuild.equals(guild)) {
            return filter;
        } else if (myClassFilter && player.objectType == classType) {
            return filter;
        } else if (filterNames.contains(name)) {
            return filter;
        } else if (filterGuilds.contains(guild)) {
            return filter;
        } else if (filterClasses.contains(classType)) {
            return filter;
        }

        return 0;
    }

    public static void disable() {
        filter = 0;
    }

    public static void selectFilter(String ss) {
        myGuildFilter = false;
        myClassFilter = false;
        filterNames.clear();
        filterGuilds.clear();
        filterClasses.clear();
        int part = 0;
        int fieldIndex = 0;
        for (String s : ss.split(",")) {
            if (s.equals("-")) {
                part++;
            } else if (part == 0) {
                name = s;
            } else if (part == 1) {
                if (s.equals("F")) filter = 1;
                if (s.equals("H")) filter = 2;
            } else if (part == 2) {
                filterNames.add(s);
            } else if (part == 3) {
                filterGuilds.add(s);
            } else if (part == 4) {
                if (fieldIndex == 0 && s.equals("1")) {
                    myGuildFilter = true;
                } else if (fieldIndex == 1 && s.equals("1")) {
                    myClassFilter = true;
                } else if (s.equals("1")) {
                    filterClasses.add(CharacterClass.CHAR_CLASS_LIST[fieldIndex - 2].getId());
                }
                fieldIndex++;
            }
        }
    }
}
