package util;

/**
 * Generic utility class to insert methods used randomly.
 */
public class Util {

    public static boolean showLogs = true;

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
     * Print logs.
     *
     * @param s String of the log.
     */
    public static void print(String s) {
        if (showLogs) {
            System.out.println(s);
        }
    }
}
