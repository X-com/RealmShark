package util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * Id to name class. Used to convert incoming realm IDs to the resources names.
 */
public class IdToName {
    private final int iD;
    private final String idName;
    private final String display;
    private final String clazz;
    private final String group;
    private static final HashMap<Integer, IdToName> ID = new HashMap<>();

    /**
     * Constructor for the resources.
     *
     * @param i Id of the resource
     * @param n Name of the resource
     * @param d Display name of the resource
     * @param c Class of the resource
     * @param g Group of the resource
     */
    public IdToName(int i, String n, String d, String c, String g) {
        iD = i;
        idName = n;
        display = d;
        clazz = c;
        group = g;
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
        String fileName = "ID.list";

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(Util.resourceFilePath(fileName), StandardCharsets.UTF_8));
            String line;

            while ((line = br.readLine()) != null) {
                String[] l = line.split(":");
                int i = Integer.parseInt(l[0]);
                String d = l[1];
                String c = l[2];
                String g = l[3];
                String n = l[4];
                ID.put(i, new IdToName(i, n, d, c, g));
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
}
