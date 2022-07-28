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
    private final String projectile;
    private Projectile[] projectiles = null;
    private static final HashMap<Integer, IdToName> ID = new HashMap<>();

    /**
     * Constructor for the resources.
     *
     * @param id         Id of the resource
     * @param idName     Name of the resource
     * @param display    Display name of the resource
     * @param clazz      Class of the resource
     * @param projectile Projectile min,max,armorPiercing,(repeated) listed
     * @param group      Group of the resource
     */
    public IdToName(int id, String idName, String display, String clazz, String projectile, String group) {
        this.id = id;
        this.idName = idName;
        this.display = display;
        this.clazz = clazz;
        this.projectile = projectile;
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
        String fileName = "ID3.list";

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(Util.resourceFilePath(fileName), StandardCharsets.UTF_8));
            String line;

            while ((line = br.readLine()) != null) {
                String[] l = line.split(":");
                int id = Integer.parseInt(l[0]);
                String display = l[1];
                String clazz = l[2];
                String group = l[3];
                String projectile = l[4];
                String idName = l[5];
                ID.put(id, new IdToName(id, idName, display, clazz, projectile, group));
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
     * Parses the projectile string to the number of projectiles the entity can shoot.
     *
     * @param entity that should be projectile parsed
     * @return List of parsed projectiles
     */
    private static Projectile[] parseProjectile(IdToName entity) {
        String[] l = entity.projectile.split(",");
        Projectile[] p = new Projectile[l.length / 3];
        int index = 0;
        for (int i = 0; i < l.length; i += 3) {
            int min = Integer.parseInt(l[i]);
            int max = Integer.parseInt(l[1 + i]);
            boolean ap = l[2 + i].equals("1");
            p[index] = new Projectile(min, max, ap);
            index++;
        }

        return p;
    }

    /**
     * Minimum damage of weapon.
     *
     * @param id Id of the object.
     * @return Minimum damage
     */
    public static int getIdProjectileMinDmg(int id, int projectileId) {
        IdToName i = ID.get(id);
        if (i.projectiles == null) i.projectiles = parseProjectile(i);
        return i.projectiles[projectileId].min;
    }

    /**
     * Maximum damage of weapon.
     *
     * @param id Id of the object.
     * @return Maximum damage
     */
    public static int getIdProjectileMaxDmg(int id, int projectileId) {
        IdToName i = ID.get(id);
        if (i.projectiles == null) i.projectiles = parseProjectile(i);
        return i.projectiles[projectileId].max;
    }

    /**
     * Maximum damage of weapon.
     *
     * @param id Id of the object.
     * @return Maximum damage
     */
    public static boolean getIdProjectileArmorPierces(int id, int projectileId) {
        IdToName i = ID.get(id);
        if (i.projectiles == null) i.projectiles = parseProjectile(i);
        return i.projectiles[projectileId].ap;
    }

    /**
     * Simple class to store projectile info
     */
    private static class Projectile {
        int min; // min dmg
        int max; // max dmg
        boolean ap; // armor piercing

        public Projectile(int min, int max, boolean ap) {
            this.min = min;
            this.max = max;
            this.ap = ap;
        }
    }
}
