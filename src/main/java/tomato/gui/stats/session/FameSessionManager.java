package tomato.gui.stats.session;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import tomato.gui.stats.Fame;
import tomato.gui.stats.data.MapFameData;

/**
 * Manages saving and loading of fame tracking sessions.
 * Handles file I/O and data format conversion for session persistence.
 */
public class FameSessionManager {

    private static final String SESSIONS_DIRECTORY = "FameSessions";
    private static final String FILE_EXTENSION = ".fame";
    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .create();

    static {
        ensureDirectoryExists();
    }

    private static void ensureDirectoryExists() {
        File dir = new File(SESSIONS_DIRECTORY);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    // --- Save Operations ---

    /**
     * Save a fame session to its default file location.
     */
    public static boolean saveSession(FameSession session) {
        String filename =
            sanitizeFilename(session.getSessionName()) + FILE_EXTENSION;
        File file = new File(SESSIONS_DIRECTORY, filename);
        return writeSession(session, file);
    }

    /**
     * Save a fame session with a file chooser dialog.
     */
    public static boolean saveSessionAs(FameSession session) {
        JFileChooser fileChooser = createFileChooser("Save Fame Session");

        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().endsWith(FILE_EXTENSION)) {
                file = new File(file.getAbsolutePath() + FILE_EXTENSION);
            }
            return writeSession(session, file);
        }
        return false;
    }

    private static boolean writeSession(FameSession session, File file) {
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(session, writer);
            return true;
        } catch (IOException e) {
            showError("Error saving session: " + e.getMessage(), "Save Error");
            return false;
        }
    }

    // --- Load Operations ---

    /**
     * Load a fame session with a file chooser dialog.
     */
    public static FameSession loadSession() {
        JFileChooser fileChooser = createFileChooser(
            "Load Fame Session (Read-Only)"
        );

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            FameSession session = loadSession(fileChooser.getSelectedFile());
            if (session != null) {
                session.setReadOnly(true);
            }
            return session;
        }
        return null;
    }

    /**
     * Load a session from a specific file.
     */
    public static FameSession loadSession(File file) {
        try (FileReader reader = new FileReader(file)) {
            return GSON.fromJson(reader, FameSession.class);
        } catch (IOException e) {
            showError("Error loading session: " + e.getMessage(), "Load Error");
            return null;
        }
    }

    /**
     * Get list of all saved session files.
     */
    public static List<File> getSavedSessions() {
        List<File> sessions = new ArrayList<>();
        File dir = new File(SESSIONS_DIRECTORY);

        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) ->
                name.endsWith(FILE_EXTENSION)
            );
            if (files != null) {
                for (File file : files) {
                    sessions.add(file);
                }
            }
        }
        return sessions;
    }

    /**
     * Delete a saved session file.
     */
    public static boolean deleteSession(File sessionFile) {
        return sessionFile.exists() && sessionFile.delete();
    }

    // --- Session Creation ---

    /**
     * Create a session from raw fame and map data.
     */
    public static FameSession createSessionFromData(
        HashMap<Integer, ArrayList<Fame>> fameData,
        HashMap<Integer, ArrayList<MapFameData>> mapFameData,
        String sessionName
    ) {
        FameSession session = new FameSession(sessionName);
        session.setCharacterFameData(convertToSessionFormat(fameData));
        session.setCharacterMapFameData(
            convertMapDataToSessionFormat(mapFameData)
        );
        return session;
    }

    // --- Data Conversion (Generic) ---

    /**
     * Generic conversion from HashMap<Integer, ArrayList<T>> to HashMap<Integer, List<T>>.
     * Used for converting between runtime and session storage formats.
     */
    private static <T> HashMap<Integer, List<T>> convertMapToList(
        HashMap<Integer, ArrayList<T>> source
    ) {
        HashMap<Integer, List<T>> result = new HashMap<>();
        if (source != null) {
            for (Map.Entry<Integer, ArrayList<T>> entry : source.entrySet()) {
                result.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
        }
        return result;
    }

    /**
     * Generic conversion from HashMap<Integer, List<T>> to HashMap<Integer, ArrayList<T>>.
     * Used for extracting data from sessions back to runtime format.
     */
    private static <T> HashMap<Integer, ArrayList<T>> convertListToMap(
        HashMap<Integer, List<T>> source
    ) {
        HashMap<Integer, ArrayList<T>> result = new HashMap<>();
        if (source != null) {
            for (Map.Entry<Integer, List<T>> entry : source.entrySet()) {
                result.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
        }
        return result;
    }

    /**
     * Convert fame data from runtime format to session format.
     */
    public static HashMap<Integer, List<Fame>> convertToSessionFormat(
        HashMap<Integer, ArrayList<Fame>> fameData
    ) {
        return convertMapToList(fameData);
    }

    /**
     * Convert map fame data from runtime format to session format.
     */
    public static HashMap<
        Integer,
        List<MapFameData>
    > convertMapDataToSessionFormat(
        HashMap<Integer, ArrayList<MapFameData>> mapFameData
    ) {
        return convertMapToList(mapFameData);
    }

    /**
     * Extract fame data from a session to runtime format.
     */
    public static HashMap<Integer, ArrayList<Fame>> extractFameData(
        FameSession session
    ) {
        return convertListToMap(session.getCharacterFameData());
    }

    /**
     * Extract map fame data from a session to runtime format.
     */
    public static HashMap<Integer, ArrayList<MapFameData>> extractMapFameData(
        FameSession session
    ) {
        return convertListToMap(session.getCharacterMapFameData());
    }

    // --- Export ---

    /**
     * Export session data to CSV format.
     */
    public static boolean exportSessionToCsv(
        FameSession session,
        File outputFile
    ) {
        try {
            StringBuilder csv = new StringBuilder();
            csv.append("CharacterID,Timestamp,Fame\n");

            for (Map.Entry<Integer, List<Fame>> entry : session
                .getCharacterFameData()
                .entrySet()) {
                int charId = entry.getKey();
                for (Fame fame : entry.getValue()) {
                    csv
                        .append(charId)
                        .append(",")
                        .append(fame.getTime())
                        .append(",")
                        .append(fame.getFame())
                        .append("\n");
                }
            }

            Files.write(outputFile.toPath(), csv.toString().getBytes());
            return true;
        } catch (IOException e) {
            showError(
                "Error exporting session: " + e.getMessage(),
                "Export Error"
            );
            return false;
        }
    }

    // --- Utilities ---

    private static JFileChooser createFileChooser(String title) {
        JFileChooser fileChooser = new JFileChooser(SESSIONS_DIRECTORY);
        fileChooser.setDialogTitle(title);
        fileChooser.setFileFilter(
            new FileNameExtensionFilter(
                "Fame Session Files (*" + FILE_EXTENSION + ")",
                FILE_EXTENSION.substring(1)
            )
        );
        return fileChooser;
    }

    private static String sanitizeFilename(String name) {
        return name.replaceAll("[^a-zA-Z0-9_\\- ]", "_");
    }

    private static void showError(String message, String title) {
        JOptionPane.showMessageDialog(
            null,
            message,
            title,
            JOptionPane.ERROR_MESSAGE
        );
    }
}
