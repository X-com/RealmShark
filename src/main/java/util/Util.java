package util;

import assets.IdToAsset;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 * Generic utility class for utility methods.
 */
public class Util {

    public static boolean saveLogs = true;
    private static HashMap<String, PrintWriter> printWriter = new HashMap<>();

    /**
     * Fast method to return an integer from the first 4 bytes of a byte array.
     *
     * @param bytes The byte array to extract the integer from.
     * @return The integer converted from the first 4 bytes of an array.
     */
    public static int decodeInt(byte[] bytes) {
        return (Byte.toUnsignedInt(bytes[0]) << 24) | (Byte.toUnsignedInt(bytes[1]) << 16) | (Byte.toUnsignedInt(bytes[2]) << 8) | Byte.toUnsignedInt(bytes[3]);
    }

    /**
     * Enable / disable log print-outs.
     *
     * @param logs Set the saving of logs to true/false.
     */
    public static void setSaveLogs(boolean logs) {
        saveLogs = logs;
    }

    /**
     * Error logger.
     *
     * @param message The error message.
     */
    public static void print(String message) {
        print("error/error", message);
    }

    /**
     * Print logs to console or to files in a folderAndName.
     *
     * @param folderAndName The folder and the name to write the logs into.
     * @param s             String of the log.
     */
    public static void print(String folderAndName, String s) {
        if (!saveLogs) {
            System.out.println(s);
        } else {
            // System.out.println(s);
            PrintWriter printWriterObject = printWriter.get(folderAndName);
            if (printWriterObject == null) {
                try {
                    DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss");
                    LocalDateTime dateTime = LocalDateTime.now();
                    String fileName;
                    if(folderAndName.endsWith("-")){
                        fileName = folderAndName.substring(0, folderAndName.length() - 1);
                    }else{
                        fileName = folderAndName + "-" + dateTimeFormat.format(dateTime) + ".data";
                    }
                    File file = new File(fileName);
                    if (!file.getParentFile().exists()) {
                        if (!file.getParentFile().mkdirs()) {
                            System.out.println("[X] Failed to create path for logfile '" + fileName + "'.");
                        }
                        if (!file.createNewFile()) {
                            System.out.println("[X] Failed to create logfile '" + fileName + "'.");
                        }
                    }
                    FileWriter fileWriter = new FileWriter(file);
                    printWriterObject = new PrintWriter(fileWriter);
                    printWriter.put(folderAndName, printWriterObject);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
            printWriterObject.print(s + "\n");
            printWriterObject.flush();
        }
    }

    /**
     * Hex with lines printer.
     *
     * @param bytes Byte array to be printed with pare of hex numbers separated with a line
     * @return Printed hex values with separated lines.
     */
    public static String byteArrayPrint(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (byte b : bytes) {
            if (!first) sb.append("|");
            first = false;
            sb.append(String.format("%01x", b));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Receives a hex string and returns it in byte array format.
     *
     * @param hex String of hex data where a pair of numbers represents a byte.
     * @return Returns a byte array converted from the passed hex string.
     */
    public static byte[] hexStringToByteArray(String hex) {
        int l = hex.length();
        byte[] data = new byte[l / 2];
        for (int i = 0; i < l; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * String output of all objects in the list.
     *
     * @param list List all objects to be printed.
     * @return String output of the list.
     */
    public static String showAll(Object[] list) {
        StringBuilder sb = new StringBuilder();
        for (Object o : list) {
            sb.append(o);
        }
        return sb.toString();
    }

    /**
     * String output of all integers in the list.
     *
     * @param list List of integers to be printed.
     * @return String output of the list.
     */
    public static Object showAll(int[] list) {
        StringBuilder sb = new StringBuilder();
        for (int i : list) {
            sb.append("\n").append(i);
        }
        return sb.toString();
    }

    /**
     * String output of all bytes in the list.
     *
     * @param list List of bytes to be printed.
     * @return String output of the list.
     */
    public static Object showAll(byte[] list) {
        StringBuilder sb = new StringBuilder();
        for (int i : list) {
            sb.append("\n").append(i);
        }
        return sb.toString();
    }

    /**
     * Returns the current time in string format e.g. "03:34:10".
     *
     * @return The current time as a formatted string.
     */
    public static String getHourTime() {
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.now();
        return dateTimeFormat.format(dateTime);
    }

    /**
     * Returns the resource file as stream in the resource's folder.
     *
     * @param fileName Name of resource file.
     * @return The resource file as stream.
     */
    public static InputStream resourceFilePath(String fileName) {
        return IdToAsset.class.getClassLoader().getResourceAsStream(fileName);
    }

    /**
     * Returns the OS version as 'win' or 'mac' as a string. Returns empty if the OS is unsupported.
     *
     * @return The name of the current operating system.
     */
    public static String getOperatingSystem() {
        String os = System.getProperty("os.name");
        if (os == null) {
            System.out.println("[X] Failed to detect operating system using 'os.name'.");
            return "";
        } else if (!os.equals("win") && !os.equals("mac")) {
            // Unsupported operating system such as most Linux distributions
            return "";
        } else {
            return os;
        }
    }
}
