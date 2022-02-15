package util;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Generic utility class to insert methods used randomly.
 */
public class Util {

    public static String version = "1.0";
    public static boolean showLogs = false;
    private static PrintWriter printWriter;

    /**
     * Fast method to get an integer out of the first 4 bytes of an array.
     *
     * @param bytes Byte array needing an integer extracted from.
     * @return Returns the integer from the first bytes of an array.
     */
    public static int decodeInt(byte[] bytes) {
        return (Byte.toUnsignedInt(bytes[0]) << 24) | (Byte.toUnsignedInt(bytes[1]) << 16) | (Byte.toUnsignedInt(bytes[2]) << 8) | Byte.toUnsignedInt(bytes[3]);
    }

    /**
     * Enable / disable log printouts.
     *
     * @param logs boolean to enable logs.
     */
    public static void setShowLogs(boolean logs) {
        showLogs = logs;
    }

    /**
     * Print error logs to console or to file.
     *
     * @param s String of the log.
     */
    public static void print(String s) {
        if (showLogs) {
            System.out.println(s);
        } else {
            System.out.println(s);
            if (printWriter == null) {
                try {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss");
                    LocalDateTime now = LocalDateTime.now();
                    File f = new File("error/" + dtf.format(now) + ".data");
                    if (!f.exists()) {
                        f.getParentFile().mkdirs();
                        f.createNewFile();
                    }
                    FileWriter fileWriter = new FileWriter(f);
                    printWriter = new PrintWriter(fileWriter);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            printWriter.print("\n" + s);
            printWriter.flush();
        }
    }
}
