package tomato.gui.stats;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.*;
import tomato.gui.stats.data.MapFameData;
import tomato.gui.stats.session.FameSession;
import tomato.gui.stats.session.FameSessionManager;

public class FameTrackerGUI extends JPanel {

    private static FameTrackerGUI INSTANCE;

    private final ArrayList<Fame> scores;
    private final HashMap<Integer, ArrayList<Fame>> fameList;
    private final GraphPanel graphPanel;

    // Session management

    private FameSession currentLiveSession;

    private boolean fameGainedSinceLastSave = false;

    public FameTrackerGUI() {
        INSTANCE = this;
        setLayout(new BorderLayout());

        scores = new ArrayList<>();
        fameList = new HashMap<>();

        graphPanel = new GraphPanel(scores);

        // Create a new live session
        currentLiveSession = new FameSession(
            "Live_" + System.currentTimeMillis()
        );

        // Create navigation panel (similar to DPS GUI)
        JPanel navigationPanel = createNavigationPanel();

        // Add graph panel directly since dropdown is now integrated
        add(graphPanel, BorderLayout.CENTER);
    }

    private JPanel createNavigationPanel() {
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));

        // Navigation panel is now empty since dropdown is integrated in GraphPanel
        // Keep this method for potential future navigation elements

        return navPanel;
    }

    public static void updateFame(int charId, long fame, long time) {
        INSTANCE.update(charId, fame, time);
    }

    private void update(int charId, long fame, long time) {
        // Track that fame has been gained since last save

        fameGainedSinceLastSave = true;

        fameList
            .computeIfAbsent(charId, e -> new ArrayList<>())
            .add(new Fame(fame, time));
        graphPanel.setScores(fameList.get(charId));
        graphPanel.repaint();

        // Update live session data
        currentLiveSession.setCharacterFameData(
            FameSessionManager.convertToSessionFormat(fameList)
        );

        // Store class names for all characters in the session
        HashMap<Integer, String> classNames = new HashMap<>();
        FameTablePanel fameTablePanel = FameTablePanel.getInstance();
        if (fameTablePanel != null) {
            for (Integer characterId : fameList.keySet()) {
                String className = fameTablePanel.getClassNameForCharacterId(
                    characterId
                );
                classNames.put(characterId, className);
            }
            currentLiveSession.setCharacterClassNames(classNames);
        }

        // Include map fame data from FameTablePanel if available
        try {
            if (fameTablePanel != null) {
                HashMap<Integer, ArrayList<MapFameData>> mapFameData =
                    fameTablePanel.getMapFameData();
                currentLiveSession.setCharacterMapFameData(
                    FameSessionManager.convertMapDataToSessionFormat(
                        mapFameData
                    )
                );
            }
        } catch (Exception e) {
            System.err.println(
                "Error updating live session map data: " + e.getMessage()
            );
        }
    }

    /**
     * Auto-saves the live session to prevent data loss
     */
    private void autoSaveLiveSession() {
        try {
            // Only auto-save if we have meaningful data AND fame has been gained
            if (!fameList.isEmpty() && fameGainedSinceLastSave) {
                FameSessionManager.saveSession(currentLiveSession);
                fameGainedSinceLastSave = false; // Reset flag after successful save
                System.out.println("Auto-saved session with new fame data");
            } else if (!fameGainedSinceLastSave && !fameList.isEmpty()) {
                System.out.println(
                    "Skipped auto-save: No new fame gained since last save"
                );
            }
        } catch (Exception e) {
            // Silent fail for auto-save - don't interrupt user experience
            System.err.println("Auto-save failed: " + e.getMessage());
        }
    }

    /**
     * Public method to trigger auto-save from external components
     */
    public void triggerAutoSave() {
        autoSaveLiveSession();
    }

    /**
     * Public method to trigger auto-save on map changes (always saves for edge case protection)
     */
    public void triggerMapChangeAutoSave() {
        try {
            // Always save on map change regardless of fame gain flag (edge case protection)
            if (!fameList.isEmpty()) {
                FameSessionManager.saveSession(currentLiveSession);
            }
        } catch (Exception e) {
            // Silent fail for auto-save - don't interrupt user experience
            System.err.println(
                "Map change auto-save failed: " + e.getMessage()
            );
        }
    }

    /**
     * Check if there's been fame gained since the last save
     */
    public boolean hasFameGainedSinceLastSave() {
        return fameGainedSinceLastSave;
    }

    /**
     * Manually save the current live session with a custom name
     */
    public void saveCurrentSession(String sessionName) {
        if (sessionName != null && !sessionName.trim().isEmpty()) {
            currentLiveSession.setSessionName(sessionName.trim());
            if (FameSessionManager.saveSession(currentLiveSession)) {
                JOptionPane.showMessageDialog(
                    this,
                    "Session saved successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                );

                // Create new live session for continuing tracking
                currentLiveSession = new FameSession(
                    "Live_" + System.currentTimeMillis()
                );
                fameGainedSinceLastSave = false; // Reset flag for new session
            }
        }
    }

    /**
     * Gets the current fame data for external access
     */
    public HashMap<Integer, ArrayList<Fame>> getFameData() {
        return fameList;
    }

    /**
     * Clear the current session file being written and start fresh
     */
    public void clearCurrentSessionFile() {
        // Delete the current session file if it exists
        try {
            String currentFilename =
                currentLiveSession
                    .getSessionName()
                    .replaceAll("[^a-zA-Z0-9_\\- ]", "_") +
                ".fame";
            File currentFile = new File("FameSessions", currentFilename);
            if (currentFile.exists()) {
                currentFile.delete();
            }
        } catch (Exception e) {
            System.err.println(
                "Error deleting session file: " + e.getMessage()
            );
        }

        // Create a completely new empty session
        currentLiveSession = new FameSession(
            "Live_" + System.currentTimeMillis()
        );

        // Clear the fame gained flag since we're starting fresh
        fameGainedSinceLastSave = false;

        JOptionPane.showMessageDialog(
            this,
            "Session data cleared. Starting fresh tracking.",
            "Session Reset",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Start a brand new live session file without deleting the previous one.
     * - Clears in-memory fame data so new file contains only new stats
     * - Creates a fresh FameSession with a new timestamped name
     * - Immediately writes an (empty) .fame file to disk
     */
    public void startNewSessionFile() {
        try {
            // Reset in-memory fame tracking for a clean start
            scores.clear();
            fameList.clear();
            graphPanel.setScores(new java.util.ArrayList<>());
            graphPanel.repaint();

            // Rotate to a new live session
            currentLiveSession = new FameSession(
                "Live_" + System.currentTimeMillis()
            );

            // Reset the "gained since last save" flag as we're starting fresh
            fameGainedSinceLastSave = false;

            // Immediately create the new session file on disk
            FameSessionManager.saveSession(currentLiveSession);

            // Optional UX feedback
            JOptionPane.showMessageDialog(
                this,
                "Started a new session file.",
                "New Session",
                JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception e) {
            System.err.println(
                "Error starting new session file: " + e.getMessage()
            );
        }
    }
}
