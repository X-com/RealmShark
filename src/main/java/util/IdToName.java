package util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * Id to name class. Used to convert incoming realm IDs to the resources names.
 */
public class IdToName {
    private final int id;
    private final String idName;
    private final String display;
    private final String clazz;
    private final String group;
    private int minDmg;
    private int maxDmg;
    private static final HashMap<Integer, IdToName> ID = new HashMap<>();

    /**
     * Constructor for the resources.
     *
     * @param id      Id of the resource
     * @param idName  Name of the resource
     * @param display Display name of the resource
     * @param clazz   Class of the resource
     * @param minDmg  Min damage of weapons
     * @param maxDmg  Max damage of weapons
     * @param group   Group of the resource
     */
    public IdToName(int id, String idName, String display, String clazz, String minDmg, String maxDmg, String group) {
        this.id = id;
        this.idName = idName;
        this.display = display;
        this.clazz = clazz;
        if(!minDmg.equals("")) this.minDmg = Integer.parseInt(minDmg);
        if(!maxDmg.equals("")) this.maxDmg = Integer.parseInt(maxDmg);
        this.group = group;
    }

    /**
     * Construct the list on start of using this class.
     */
    static {
        readList();
    }

    /**
     * Method to grab the full list of resource's from file and construct the hashmap.
     */
    private static void readList() {
        String fileName = "ID2.list";

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(Util.resourceFilePath(fileName), StandardCharsets.UTF_8));
            String line;

            while ((line = br.readLine()) != null) {
                String[] l = line.split(":");
                int id = Integer.parseInt(l[0]);
                String display = l[1];
                String clazz = l[2];
                String group = l[3];
                String minDmg = l[4];
                String maxDmg = l[5];
                String idName = l[6];
                ID.put(id, new IdToName(id, idName, display, clazz, minDmg, maxDmg, group));
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to grab the name of the resource.
     * If display name is not present, use the regular name.
     *
     * @param id Id of the object.
     * @return Best descriptive name of the resource
     */
    public static String name(int id) {
        IdToName i = ID.get(id);
        if (i == null) return "";
        if (i.display.equals("")) return i.idName;
        return i.display;
    }

    /**
     * Common name of the object.
     *
     * @param id Id of the object.
     * @return Regular name of the object.
     */
    public static String getIdName(int id) {
        IdToName i = ID.get(id);
        return i.idName;
    }

    /**
     * Display name of the object.
     *
     * @param id Id of the object.
     * @return Display name of the object.
     */
    public static String getDisplayName(int id) {
        IdToName i = ID.get(id);
        return i.display;
    }

    /**
     * Class of the object.
     *
     * @param id Id of the object.
     * @return Class name of the object.
     */
    public static String getClazz(int id) {
        IdToName i = ID.get(id);
        return i.clazz;
    }

    /**
     * Group of the object.
     *
     * @param id Id of the object.
     * @return Group name of the object.
     */
    public static String getIdGroup(int id) {
        IdToName i = ID.get(id);
        return i.group;
    }

    /**
     * Minimum damage of weapon.
     *
     * @param id Id of the object.
     * @return Minimum damage
     */
    public static int getIdWeaponMin(int id) {
        IdToName i = ID.get(id);
        return i.minDmg;
    }

    /**
     * Maximum damage of weapon.
     *
     * @param id Id of the object.
     * @return Maximum damage
     */
    public static int getIdWeaponMax(int id) {
        IdToName i = ID.get(id);
        return i.maxDmg;
    }
}
