package util;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 * Generic utility class to insert methods used randomly.
 */
public class Util {

    public static boolean saveLogs = true;
    private static HashMap<String, PrintWriter> printWriter = new HashMap<>();

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
    public static void setSaveLogs(boolean logs) {
        saveLogs = logs;
    }

    /**
     * Error logger.
     *
     * @param s String of the error log.
     */
    public static void print(String s) {
        print("error", s);
    }

    /**
     * Print logs to console or to files in a folder.
     *
     * @param folder The folder to write the logs into.
     * @param s      String of the log.
     */
    public static void print(String folder, String s) {
        if (!saveLogs) {
            System.out.println(s);
        } else {
            System.out.println(s);
            PrintWriter printWriterObject = printWriter.get(folder);
            if (printWriterObject == null) {
                try {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss");
                    LocalDateTime now = LocalDateTime.now();
                    File f = new File(folder + "/" + folder + "-" + dtf.format(now) + ".data");
                    if (!f.exists()) {
                        f.getParentFile().mkdirs();
                        f.createNewFile();
                    }
                    FileWriter fileWriter = new FileWriter(f);
                    printWriterObject = new PrintWriter(fileWriter);
                    printWriter.put(folder, printWriterObject);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
            printWriterObject.print("\n" + s);
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
     * Method to turn byte array from a hex string.
     *
     * @param hex String of hex data with a pair of numbers represents a byte.
     * @return Returns a byte array translated from the hex string.
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
            sb.append("\n" + i);
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
            sb.append("\n" + i);
        }
        return sb.toString();
    }

    /**
     * Gets string format of the current time.
     *
     * @return String of the current time in hour:min:sec
     */
    public static String getHourTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    /**
     * Returns the resource file as stream in the resource's folder.
     *
     * @param fileName Name of resource file.
     * @return The resource file as stream.
     */
    public static InputStream resourceFilePath(String fileName) throws URISyntaxException {
        return IdToName.class.getClassLoader().getResourceAsStream(fileName);
    }

    public static void playSound() {
        File lol = new File("beep.wav");

        try {
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(lol));
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
