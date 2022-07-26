package example.damagecalc;

import packets.Packet;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Temp class to log dmg packets.
 */
public class DamageLogger {
    private static int index = 0;
    private static int size = 100;
    //    private static String[] logList = new String[size];
    private static ArrayList<String> logList = new ArrayList<>();
    private static String folderName = "dmgLog";
    private static String dungeonName = "N/A";
    private static boolean log = true;
    private static PrintWriter printWriter;

    public static void logDmg(Packet tcp) {
        if (!log) return;

        logDmg(Arrays.toString(tcp.getPayload()));
    }

    public static void logDmg(String s) {
        if (!log) return;

        logList.add(s);
    }

    public static void enableLogger(boolean set) {
        log = set;
    }

    public static void dumpData() {
//        for (int i = index; i <= (index + size); i++) {
//            int j = i % size;
//            String s = logList[j];
//            if (s != null) {
////                print(Arrays.toString(packet.getPayload()) + " " + j);
//                print(s + " " + j);
//            }
//        }

        for (int i = index; i < logList.size(); i++) {
            String s = logList.get(i);
            print(s + " " + i);
        }

        print("--close--");
    }

    public static void print(String s) {
        if (s.contains("--close--")) {
            if (printWriter != null) printWriter.close();
            printWriter = null;
        } else {
            if (printWriter == null) {
                try {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss");
                    LocalDateTime now = LocalDateTime.now();
                    File f = new File(folderName + "/" + name() + "-" + dtf.format(now) + ".data");
                    if (!f.exists()) {
                        if (f.getParentFile().mkdirs()) System.out.println("folder " + folderName + " made");
                        if (f.createNewFile()) System.out.println("file created " + f);
                    }
                    FileWriter fileWriter = new FileWriter(f);
                    printWriter = new PrintWriter(fileWriter);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
            printWriter.print("\n" + s);
            printWriter.flush();
        }
    }

    public static void reopenLogger() {
        if (!log) return;

        if(!name().contains("N/A")) dumpData();
        logList.clear();
    }

    private static String name() {
        return dungeonName + "." + folderName;
    }

    public static void setName(String dungName) {
        switch (dungName) {
            case "{s.vault}":
            case "Daily Quest Room":
            case "Pet Yard":
//            case "{s.guildhall}":
            case "{s.nexus}":
            case "{s.rotmg}":
                System.out.println("disabled - " + dungName);
                enableLogger(false);
                return;
            default:
                System.out.println("enabled - " + dungName);
                enableLogger(true);
        }

        dungeonName = dungName.replaceAll(" ", "_");
    }
}
