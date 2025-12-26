package tomato.gui.stats;

/**
 * Bridge connecting FameTablePanel and FameTrackerGUI.
 * Provides a centralized point for fame updates and session management.
 */
public final class FameTableBridge {

    private static FameTableBridge INSTANCE;

    private FameTablePanel fameTablePanel;
    private FameTrackerGUI fameTrackerGUI;

    private FameTableBridge() {}

    public static FameTableBridge getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FameTableBridge();
        }
        return INSTANCE;
    }

    public static void initialize() {
        getInstance();
    }

    // --- Registration ---

    public void setFameTablePanel(FameTablePanel panel) {
        this.fameTablePanel = panel;
    }

    public void setFameTrackerGUI(FameTrackerGUI gui) {
        this.fameTrackerGUI = gui;
    }

    // --- Fame Updates ---

    public static void updateFame(
        int charId,
        long fame,
        long time,
        String className
    ) {
        FameTableBridge bridge = INSTANCE;
        if (bridge != null && bridge.fameTablePanel != null) {
            bridge.fameTablePanel.updateFame(charId, fame, time, className);
        }
    }

    // --- Session Management ---

    public void triggerAutoSave() {
        if (fameTrackerGUI != null) {
            fameTrackerGUI.triggerAutoSave();
        }
    }

    public void triggerMapChangeAutoSave() {
        if (fameTrackerGUI != null) {
            fameTrackerGUI.triggerMapChangeAutoSave();
        }
    }

    public void clearCurrentSessionFile() {
        if (fameTrackerGUI != null) {
            fameTrackerGUI.clearCurrentSessionFile();
        }
    }

    public void startNewSessionFile() {
        if (fameTrackerGUI != null) {
            fameTrackerGUI.startNewSessionFile();
        }
    }

    public boolean hasFameGainedSinceLastSave() {
        return (
            fameTrackerGUI != null &&
            fameTrackerGUI.hasFameGainedSinceLastSave()
        );
    }
}
