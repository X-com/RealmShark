package tomato.gui.dps;

import util.PropertiesManager;

public class DpsDisplayOptions {
    public static int equipmentOption;
    public static int sortOption;
    public static String[] filteredStrings;
    public static boolean nameFilter;

    public static void loadProfileFilter() {
        String equipment = PropertiesManager.getProperty("equipment");
        String names = PropertiesManager.getProperty("nameFilter");
        String toggleFilter = PropertiesManager.getProperty("toggleFilter");
        String sort = PropertiesManager.getProperty("sortDps");

        if (equipment == null) {
            equipmentOption = 1;
        } else {
            equipmentOption = Integer.parseInt(equipment);
        }

        if (sort == null) {
            sortOption = 0;
        } else {
            sortOption = Integer.parseInt(sort);
        }

        if (toggleFilter != null) {
            nameFilter = toggleFilter.equals("T");
        } else {
            nameFilter = false;
        }

        if (names != null) {
            filteredStrings = names.split(" ");
        } else {
            filteredStrings = new String[0];
        }
    }
}
