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
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import tomato.gui.stats.Fame;
import tomato.gui.stats.data.MapFameData;

/**
 * Manages saving and loading of fame tracking sessions
 */
public class FameSessionManager {

    private static final String SESSIONS_DIRECTORY = "FameSessions";
    private static final String FILE_EXTENSION = ".fame";
    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .create();

    static {
        // Create sessions directory if it doesn't exist
        File dir = new File(SESSIONS_DIRECTORY);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Save a fame session to a file
     */
    public static boolean saveSession(FameSession session) {
        try {
            String filename =
                session.getSessionName().replaceAll("[^a-zA-Z0-9_\\- ]", "_") +
                FILE_EXTENSION;
            File file = new File(SESSIONS_DIRECTORY, filename);

            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(session, writer);
            }

            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                null,
                "Error saving session: " + e.getMessage(),
                "Save Error",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
    }

    /**
     * Save a fame session with a custom filename
     */
    public static boolean saveSessionAs(FameSession session) {
        JFileChooser fileChooser = new JFileChooser(SESSIONS_DIRECTORY);
        fileChooser.setDialogTitle("Save Fame Session");
        fileChooser.setFileFilter(
            new FileNameExtensionFilter(
                "Fame Session Files (*" + FILE_EXTENSION + ")",
                FILE_EXTENSION.substring(1)
            )
        );

        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().endsWith(FILE_EXTENSION)) {
                file = new File(file.getAbsolutePath() + FILE_EXTENSION);
            }

            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(session, writer);
                return true;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(
                    null,
                    "Error saving session: " + e.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
        return false;
    }

    /**
     * Load a fame session from a file
     */
    public static FameSession loadSession() {
        JFileChooser fileChooser = new JFileChooser(SESSIONS_DIRECTORY);
        fileChooser.setDialogTitle("Load Fame Session (Read-Only)");
        fileChooser.setFileFilter(
            new FileNameExtensionFilter(
                "Fame Session Files (*" + FILE_EXTENSION + ")",
                FILE_EXTENSION.substring(1)
            )
        );

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (FileReader reader = new FileReader(file)) {
                FameSession session = GSON.fromJson(reader, FameSession.class);
                if (session != null) {
                    // Mark session as read-only
                    session.setReadOnly(true);
                }
                return session;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(
                    null,
                    "Error loading session: " + e.getMessage(),
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
        return null;
    }

    /**
     * Load a session from a specific file
     */
    public static FameSession loadSession(File file) {
        try (FileReader reader = new FileReader(file)) {
            return GSON.fromJson(reader, FameSession.class);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                null,
                "Error loading session: " + e.getMessage(),
                "Load Error",
                JOptionPane.ERROR_MESSAGE
            );
            return null;
        }
    }

    /**
     * Get list of all saved sessions
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
     * Delete a saved session
     */
    public static boolean deleteSession(File sessionFile) {
        if (sessionFile.exists()) {
            return sessionFile.delete();
        }
        return false;
    }

    /**
     * Create a session from raw fame data
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

    /**
     * Convert fame data from FameTrackerGUI format to session format
     */
    public static HashMap<Integer, List<Fame>> convertToSessionFormat(
        HashMap<Integer, ArrayList<Fame>> fameData
    ) {
        HashMap<Integer, List<Fame>> convertedData = new HashMap<>();
        for (Integer charId : fameData.keySet()) {
            convertedData.put(charId, new ArrayList<>(fameData.get(charId)));
        }
        return convertedData;
    }

    /**
     * Convert map fame data to session format
     */
    public static HashMap<
        Integer,
        List<MapFameData>
    > convertMapDataToSessionFormat(
        HashMap<Integer, ArrayList<MapFameData>> mapFameData
    ) {
        HashMap<Integer, List<MapFameData>> convertedData = new HashMap<>();
        for (Integer charId : mapFameData.keySet()) {
            convertedData.put(charId, new ArrayList<>(mapFameData.get(charId)));
        }
        return convertedData;
    }

    /**
     * Extract fame data from a session back to the format used by FameTrackerGUI
     */
    public static HashMap<Integer, ArrayList<Fame>> extractFameData(
        FameSession session
    ) {
        HashMap<Integer, ArrayList<Fame>> fameData = new HashMap<>();

        for (Integer charId : session.getCharacterFameData().keySet()) {
            fameData.put(
                charId,
                new ArrayList<>(session.getCharacterFameData().get(charId))
            );
        }

        return fameData;
    }

    /**
     * Extract map fame data from a session back to the format used by FameTablePanel
     */
    public static HashMap<Integer, ArrayList<MapFameData>> extractMapFameData(
        FameSession session
    ) {
        HashMap<Integer, ArrayList<MapFameData>> mapFameData = new HashMap<>();

        for (Integer charId : session.getCharacterMapFameData().keySet()) {
            mapFameData.put(
                charId,
                new ArrayList<>(session.getCharacterMapFameData().get(charId))
            );
        }

        return mapFameData;
    }

    /**
     * Export session data to a different format (CSV for example)
     */
    public static boolean exportSessionToCsv(
        FameSession session,
        File outputFile
    ) {
        try {
            StringBuilder csvContent = new StringBuilder();
            csvContent.append("CharacterID,Timestamp,Fame\n");

            for (Integer charId : session.getCharacterFameData().keySet()) {
                List<Fame> fameList = session
                    .getCharacterFameData()
                    .get(charId);
                for (Fame fame : fameList) {
                    csvContent
                        .append(charId)
                        .append(",")
                        .append(fame.getTime())
                        .append(",")
                        .append(fame.getFame())
                        .append("\n");
                }
            }

            Files.write(outputFile.toPath(), csvContent.toString().getBytes());
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                null,
                "Error exporting session: " + e.getMessage(),
                "Export Error",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
    }
}
