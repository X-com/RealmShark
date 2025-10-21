package tomato.gui.stats;

/**
 * A simple bridge for wiring FameTablePanel and FameTrackerGUI together.
 * This class intentionally contains no extra state; it just forwards updates and
 * exposes a few helper actions to the other components.
 */
public class FameTableBridge {

    private static FameTableBridge INSTANCE;

    private FameTablePanel fameTablePanel;
    private FameTrackerGUI fameTrackerGUI;

    // Connect both fame views
    public FameTableBridge() {
        INSTANCE = this;
    }

    /**
     * Initialize the singleton instance if not already created.
     */
    public static void initialize() {
        if (INSTANCE == null) {
            INSTANCE = new FameTableBridge();
        }
    }

    /**
     * Get the bridge singleton.
     */
    public static FameTableBridge getInstance() {
        return INSTANCE;
    }

    /**
     * Register the table panel (receiver of fame updates).
     */
    public void setFameTablePanel(FameTablePanel panel) {
        this.fameTablePanel = panel;
    }

    /**
     * Register the tracker GUI (for auto-save, file operations, etc.).
     */
    public void setFameTrackerGUI(FameTrackerGUI gui) {
        this.fameTrackerGUI = gui;
    }

    /**
     * Forward a fame update to the FameTablePanel.
     */
    public static void updateFame(
        int charId,
        long fame,
        long time,
        String className
    ) {
        if (INSTANCE != null && INSTANCE.fameTablePanel != null) {
            INSTANCE.fameTablePanel.updateFame(charId, fame, time, className);
        }
    }

    /**
     * Trigger auto-save of the current live session on the tracker GUI.
     */
    public void triggerAutoSave() {
        if (fameTrackerGUI != null) {
            fameTrackerGUI.triggerAutoSave();
        }
    }

    /**

     * Clear the current session file being written and start fresh.

     */

    public void clearCurrentSessionFile() {
        if (fameTrackerGUI != null) {
            fameTrackerGUI.clearCurrentSessionFile();
        }
    }

    /**
     * Start a brand new session file without deleting the previous one.
     * Also resets in-memory tracking for a fresh start.
     */
    public void startNewSessionFile() {
        if (fameTrackerGUI != null) {
            fameTrackerGUI.startNewSessionFile();
        }
    }

    /**
     * Trigger auto-save specifically for map changes (always saves for edge case protection)
     */
    public void triggerMapChangeAutoSave() {
        if (fameTrackerGUI != null) {
            fameTrackerGUI.triggerMapChangeAutoSave();
        }
    }

    /**
     * Check if there's been fame gained since the last save
     */
    public boolean hasFameGainedSinceLastSave() {
        if (fameTrackerGUI != null) {
            return fameTrackerGUI.hasFameGainedSinceLastSave();
        }
        return false;
    }
}
