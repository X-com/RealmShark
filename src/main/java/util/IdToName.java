package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class IdToName {
    private final int iD;
    private final String idName;
    private final String display;
    private final String clazz;
    private final String group;
    private static final HashMap<Integer, IdToName> ID = new HashMap<>();

    public IdToName(int i, String n, String d, String c, String g) {
        iD = i;
        idName = n;
        display = d;
        clazz = c;
        group = g;
    }

    static {
        readList();
    }

    private static void readList() {
        String fileName = "src/main/resources/ID.list";

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String name(int id) {
        IdToName i = ID.get(id);
        if (i.display.equals("")) return i.idName;
        return i.display;
    }

    public static String getIdName(int id) {
        IdToName i = ID.get(id);
        return i.idName;
    }

    public static String getDisplayName(int id) {
        IdToName i = ID.get(id);
        return i.display;
    }

    public static String getClazz(int id) {
        IdToName i = ID.get(id);
        return i.clazz;
    }

    public static String getIdGroup(int id) {
        IdToName i = ID.get(id);
        return i.group;
    }
}
