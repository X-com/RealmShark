package tomato.realmshark;

import util.Util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Crash logger, storing crash data to file.
 */
public class CrashLogger {

    /**
     * Logger for crashes captured by the exception handler.
     *
     * @param error Error to be logged.
     */
    public static void printCrash(Exception error) {
        Util.printLogs("Main crash:");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        error.printStackTrace(pw);
        Util.printLogs(sw.toString());
    }

    /**
     * Class loader method in java. Loading class here in case of stackoverflow errors.
     */
    public static void loadThisClass() {
    }
}
