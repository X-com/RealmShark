package tomato.gui.dps;

import util.PropertiesManager;

public class DpsDisplayOptions {
    public static int equipmentOption;
    public static String[] filteredStrings;
    public static boolean nameFilter;

    public static void loadProfileFilter() {
        String equipment = PropertiesManager.getProperty("equipment");
        String names = PropertiesManager.getProperty("nameFilter");
        String toggleFilter = PropertiesManager.getProperty("toggleFilter");

        if (equipment == null) {
            equipmentOption = 1;
        } else {
            equipmentOption = Integer.parseInt(equipment);
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
