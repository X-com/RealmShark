package tomato.gui.stats;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.*;
import tomato.gui.stats.data.MapFameData;
import tomato.gui.stats.session.FameSession;
import tomato.gui.stats.session.FameSessionManager;

/**
 * GUI panel for displaying fame graph and managing live session tracking.
 */
public class FameTrackerGUI extends JPanel {

    private static FameTrackerGUI INSTANCE;

    private final HashMap<Integer, ArrayList<Fame>> fameList = new HashMap<>();
    private final GraphPanel graphPanel;
    private FameSession currentLiveSession;
    private boolean fameGainedSinceLastSave = false;

    public FameTrackerGUI() {
        INSTANCE = this;
        setLayout(new BorderLayout());

        graphPanel = new GraphPanel(new ArrayList<>());
        add(graphPanel, BorderLayout.CENTER);

        resetSession();
    }

    // --- Public API ---

    public static void updateFame(int charId, long fame, long time) {
        if (INSTANCE != null) {
            INSTANCE.update(charId, fame, time);
        }
    }

    public void triggerAutoSave() {
        if (!fameList.isEmpty() && fameGainedSinceLastSave) {
            if (saveCurrentLiveSession()) {
                fameGainedSinceLastSave = false;
            }
        }
    }

    public void triggerMapChangeAutoSave() {
        if (!fameList.isEmpty()) {
            saveCurrentLiveSession();
        }
    }

    public boolean hasFameGainedSinceLastSave() {
        return fameGainedSinceLastSave;
    }

    public HashMap<Integer, ArrayList<Fame>> getFameData() {
        return fameList;
    }

    public void saveCurrentSession(String sessionName) {
        if (sessionName == null || sessionName.trim().isEmpty()) return;

        currentLiveSession.setSessionName(sessionName.trim());
        if (saveCurrentLiveSession()) {
            showMessage("Session saved successfully!", "Success");
            resetSession();
        }
    }

    public void clearCurrentSessionFile() {
        deleteSessionFile(currentLiveSession.getSessionName());
        resetSession();
        showMessage(
            "Session data cleared. Starting fresh tracking.",
            "Session Reset"
        );
    }

    public void startNewSessionFile() {
        fameList.clear();
        graphPanel.clearData();
        resetSession();
        saveCurrentLiveSession();
        showMessage("Started a new session file.", "New Session");
    }

    // --- Private Methods ---

    private void update(int charId, long fame, long time) {
        fameGainedSinceLastSave = true;

        fameList
            .computeIfAbsent(charId, k -> new ArrayList<>())
            .add(new Fame(fame, time));

        graphPanel.setScores(fameList.get(charId));

        updateLiveSessionData();
    }

    private void updateLiveSessionData() {
        currentLiveSession.setCharacterFameData(
            FameSessionManager.convertToSessionFormat(fameList)
        );

        FameTablePanel tablePanel = FameTablePanel.getInstance();
        if (tablePanel == null) return;

        // Update class names
        HashMap<Integer, String> classNames = new HashMap<>();
        for (Integer charId : fameList.keySet()) {
            classNames.put(
                charId,
                tablePanel.getClassNameForCharacterId(charId)
            );
        }
        currentLiveSession.setCharacterClassNames(classNames);

        // Update map fame data
        try {
            HashMap<Integer, ArrayList<MapFameData>> mapData =
                tablePanel.getMapFameData();
            currentLiveSession.setCharacterMapFameData(
                FameSessionManager.convertMapDataToSessionFormat(mapData)
            );
        } catch (Exception e) {
            System.err.println(
                "Error updating live session map data: " + e.getMessage()
            );
        }
    }

    private boolean saveCurrentLiveSession() {
        try {
            return FameSessionManager.saveSession(currentLiveSession);
        } catch (Exception e) {
            System.err.println("Auto-save failed: " + e.getMessage());
            return false;
        }
    }

    private void resetSession() {
        currentLiveSession = new FameSession(
            "Live_" + System.currentTimeMillis()
        );
        fameGainedSinceLastSave = false;
    }

    private void deleteSessionFile(String sessionName) {
        try {
            String filename =
                sessionName.replaceAll("[^a-zA-Z0-9_\\- ]", "_") + ".fame";
            File file = new File("FameSessions", filename);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            System.err.println(
                "Error deleting session file: " + e.getMessage()
            );
        }
    }

    private void showMessage(String message, String title) {
        JOptionPane.showMessageDialog(
            this,
            message,
            title,
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}
