package util;

import javax.swing.*;
import java.io.*;

/**
 * Extractor for dll that is stored in the jar.
 * The dll needs to be extracted to make the packet reading work.
 * The native dll file need to be in the root directory.
 */
public class LibExtractor {

    /**
     * Extracts library's from inside the jar, if missing in directory
     * of running jar, and closes program.
     */
    public static void libraryExtractor() {
        boolean extracted = false;
        if (!new File("Jpcap.dll").exists()) {
            extracted = true;
            extract("Jpcap.dll");
        }
        if (!new File("jpcap-x64.jar").exists()) {
            extracted = true;
            extract("jpcap-x64.jar");
        }
        if (extracted) {
            JOptionPane.showMessageDialog(new JFrame(""), "Library files was extracted, RESTART.");
            System.exit(0);
        }
    }

    /**
     * Library file extractor. Extracts files in the resource folder
     * inside the jar to the root directory of the running jar.
     *
     * @param name Name of the file wanting to be extracted.
     */
    private static void extract(String name) {
        try {
            InputStream ddlStream = LibExtractor.class.getClassLoader().getResourceAsStream("libs/" + name);

            FileOutputStream fos = new FileOutputStream(name);
            byte[] buf = new byte[2048];
            int r;
            while (-1 != (r = ddlStream.read(buf))) {
                fos.write(buf, 0, r);
            }
            ddlStream.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
