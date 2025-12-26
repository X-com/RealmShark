package tomato.gui.stats;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import tomato.backend.data.TomatoData;
import tomato.gui.stats.data.MapFameData;
import tomato.gui.stats.session.FameSessionViewer;
import tomato.realmshark.RealmCharacter;

/**
 * Panel displaying fame tracking data for all characters in a table format.
 * Tracks fame per character, session gains, and map-specific fame data.
 */
public class FameTablePanel extends JPanel {

    private static FameTablePanel INSTANCE;

    // Table components
    private final JTable fameTable;
    private final DefaultTableModel tableModel;
    private final JLabel infoLabel;

    // Data storage
    private final HashMap<Integer, ArrayList<Fame>> fameData = new HashMap<>();
    private final HashMap<Integer, Fame> lastFameEntries = new HashMap<>();
    private final HashMap<Integer, Double> sessionStartFame = new HashMap<>();
    private final HashMap<Integer, Long> sessionStartTime = new HashMap<>();
    private final HashMap<Integer, String> characterClassNames =
        new HashMap<>();

    // Map fame tracking
    private final HashMap<Integer, ArrayList<MapFameData>> mapFameData =
        new HashMap<>();
    private final HashMap<Integer, MapFameData> currentMapData =
        new HashMap<>();
    private final HashMap<String, Boolean> dungeonFilterState = new HashMap<>();
    private String currentMapName = "";

    // Active map fame dialog for real-time updates
    private JDialog activeMapFameDialog = null;
    private DefaultTableModel activeMapFameModel = null;
    private int activeMapFameCharId = -1;
    private Timer mapFameRefreshTimer = null;

    // Character tracking
    private final TomatoData tomatoData;
    private int currentCharacterId = -1;

    private static final String[] COLUMN_NAMES = {
        "Character",
        "Initial Fame",
        "Current Fame",
        "Fame/Hour",
        "Session Gain",
    };

    public FameTablePanel(TomatoData tomatoData) {
        INSTANCE = this;
        this.tomatoData = tomatoData;
        setLayout(new BorderLayout());

        tableModel = createTableModel();
        fameTable = createTable();
        infoLabel = new JLabel(
            "Enter Daily Quest Room to load char data | Fame tracking active"
        );
        infoLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        add(new JScrollPane(fameTable), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    // --- Factory Methods ---

    private DefaultTableModel createTableModel() {
        return new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JTable createTable() {
        JTable table = new JTable(tableModel);
        table.setAutoCreateRowSorter(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().addMouseListener(new TableSortHandler());

        // Column widths
        int[] widths = { 80, 100, 100, 80, 100 };
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Fame/Hour tooltip renderer
        table
            .getColumnModel()
            .getColumn(3)
            .setCellRenderer(new FamePerHourRenderer());
        return table;
    }

    private JPanel createBottomPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JButton newSessionBtn = new JButton("New Session");
        newSessionBtn.setToolTipText(
            "Start fresh session. Shift+Click to delete current file."
        );
        newSessionBtn.addMouseListener(
            new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    handleNewSession(e.isShiftDown());
                }
            }
        );

        JButton mapFameBtn = new JButton("Show Map Fame");
        mapFameBtn.addActionListener(e -> showMapFameTable());

        JButton viewSessionsBtn = new JButton("View Saved Sessions");
        viewSessionsBtn.addActionListener(e ->
            FameSessionViewer.openSessionViewer()
        );

        buttonPanel.add(newSessionBtn);
        buttonPanel.add(mapFameBtn);
        buttonPanel.add(viewSessionsBtn);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(buttonPanel, BorderLayout.NORTH);
        southPanel.add(infoLabel, BorderLayout.SOUTH);
        return southPanel;
    }

    // --- Public API ---

    public static FameTablePanel getInstance() {
        return INSTANCE;
    }

    public static void updateRealmChars() {
        if (INSTANCE != null) {
            INSTANCE.populateFromCharacterData();
        }
    }

    public static void handleMapChange(String newMapName) {
        if (INSTANCE != null) {
            INSTANCE.onMapChange(newMapName);
        }
    }

    public void updateFame(int charId, long fame, long time, String className) {
        SwingUtilities.invokeLater(() -> {
            handleCharacterChange(charId);

            if (!className.isEmpty()) {
                characterClassNames.put(charId, className);
            }

            fameData
                .computeIfAbsent(charId, id -> new ArrayList<>())
                .add(new Fame(fame, time));

            Fame lastEntry = lastFameEntries.get(charId);
            if (lastEntry == null) {
                addCharacterRow(charId, fame, time, className);
            } else {
                updateCharacterRow(charId, fame, time, lastEntry, className);
            }
            lastFameEntries.put(charId, new Fame(fame, time));

            ensureCurrentCharacterAtTop();
        });
    }

    public void onMapChange(String newMapName) {
        if (newMapName == null || newMapName.equals(currentMapName)) return;

        currentMapName = newMapName;
        long now = System.currentTimeMillis();

        for (Integer charId : lastFameEntries.keySet()) {
            Fame lastFame = lastFameEntries.get(charId);
            if (lastFame != null) {
                updateMapFameTracking(
                    charId,
                    newMapName,
                    lastFame.getFame(),
                    now
                );
            }
        }
        triggerMapChangeAutoSave();
    }

    public void checkForCharacterChange() {
        SwingUtilities.invokeLater(() -> {
            if (tomatoData != null && tomatoData.getCharId() != -1) {
                handleCharacterChange(tomatoData.getCharId());
                ensureCurrentCharacterAtTop();
            }
        });
    }

    // --- Getters ---

    public ArrayList<Fame> getFameData(int charId) {
        return fameData.get(charId);
    }

    public Double getCurrentFame(int charId) {
        Fame lastEntry = lastFameEntries.get(charId);
        return lastEntry != null ? lastEntry.getFame() : null;
    }

    public String getClassNameForCharacterId(int charId) {
        return characterClassNames.getOrDefault(charId, "Char " + charId);
    }

    public HashMap<Integer, ArrayList<MapFameData>> getMapFameData() {
        return mapFameData;
    }

    public void setMapFameData(HashMap<Integer, ArrayList<MapFameData>> data) {
        mapFameData.clear();
        mapFameData.putAll(data);
    }

    public HashMap<Integer, MapFameData> getCurrentMapData() {
        return currentMapData;
    }

    public void setCurrentMapData(HashMap<Integer, MapFameData> data) {
        currentMapData.clear();
        currentMapData.putAll(data);
    }

    public void resetDungeonFilters() {
        dungeonFilterState.clear();
    }

    // --- Character Management ---

    private void handleCharacterChange(int newCharId) {
        if (newCharId != currentCharacterId && currentCharacterId != -1) {
            resetFamePerHourForInactiveCharacters();
        }
        currentCharacterId = newCharId;
    }

    private void populateFromCharacterData() {
        if (
            tomatoData == null ||
            tomatoData.chars == null ||
            tomatoData.chars.isEmpty()
        ) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            infoLabel.setText(
                "Fame tracking - updates automatically when fame changes"
            );

            for (RealmCharacter character : tomatoData.chars) {
                if (
                    character.charId == 0 || character.classString == null
                ) continue;
                // Check if this specific charId already has a row (not just class name)
                if (findRowByCharId(character.charId) >= 0) continue;

                // Store class name first so getDisplayName can use it
                characterClassNames.put(
                    character.charId,
                    character.classString
                );

                String displayName = getDisplayName(character.charId);
                Object[] rowData = {
                    displayName,
                    Formatters.formatNumberExact(character.fame),
                    Formatters.formatNumberExact(character.fame),
                    Formatters.formatFamePerHour(0),
                    "0.00",
                };
                tableModel.addRow(rowData);

                sessionStartFame.put(character.charId, (double) character.fame);
                sessionStartTime.put(
                    character.charId,
                    System.currentTimeMillis()
                );
                lastFameEntries.put(
                    character.charId,
                    new Fame(character.fame, System.currentTimeMillis())
                );
            }

            if (tomatoData.getCharId() != -1) {
                handleCharacterChange(tomatoData.getCharId());
                ensureCurrentCharacterAtTop();
            }
        });
    }

    private void addCharacterRow(
        int charId,
        long fame,
        long time,
        String className
    ) {
        // Store class name first so getDisplayName can use it
        if (!className.isEmpty()) {
            characterClassNames.put(charId, className);
        }
        String displayName = getDisplayName(charId);

        tableModel.addRow(
            new Object[] {
                displayName,
                Formatters.formatNumberExact(fame),
                Formatters.formatNumberExact(fame),
                Formatters.formatFamePerHour(0),
                "0.00",
            }
        );

        sessionStartFame.put(charId, (double) fame);
        sessionStartTime.put(charId, time);
    }

    private void updateCharacterRow(
        int charId,
        long fame,
        long time,
        Fame lastEntry,
        String className
    ) {
        int row = findRowByCharId(charId);
        if (row < 0) return;

        // Calculate session-based fame per hour
        Long sessionStart = sessionStartTime.get(charId);
        double sessionFamePerHour = 0;
        if (sessionStart != null && time > sessionStart) {
            double hours = (time - sessionStart) / 3600000.0;
            double gain = fame - getSessionStartFame(charId);
            sessionFamePerHour = hours > 0 ? gain / hours : 0;
        }

        double sessionGain = fame - getSessionStartFame(charId);

        tableModel.setValueAt(Formatters.formatNumberExact(fame), row, 2);
        tableModel.setValueAt(
            Formatters.formatFamePerHour(sessionFamePerHour),
            row,
            3
        );
        tableModel.setValueAt(Formatters.formatFame(sessionGain, 2), row, 4);
    }

    private int findRowByName(String name) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (name.equals(tableModel.getValueAt(i, 0))) {
                return i;
            }
        }
        return -1;
    }

    private int findRowByCharId(int charId) {
        String displayName = getDisplayName(charId);
        return findRowByName(displayName);
    }

    /**
     * Creates a unique display name for a character using class name and charId.
     * Format: "ClassName (cID: charId)" (e.g., "Wizard (cID: 12345)")
     */
    private String getDisplayName(int charId) {
        String className = characterClassNames.get(charId);
        if (className == null || className.isEmpty()) {
            return "Char (cID: " + charId + ")";
        }
        return className + " (cID: " + charId + ")";
    }

    private int getCharacterIdFromRowName(String rowName) {
        // Parse charId from display name format "ClassName (cID: charId)" or "Char (cID: charId)"
        int cidIndex = rowName.lastIndexOf("(cID: ");
        if (cidIndex >= 0) {
            int endIndex = rowName.indexOf(")", cidIndex);
            if (endIndex > cidIndex) {
                try {
                    return Integer.parseInt(
                        rowName.substring(cidIndex + 6, endIndex)
                    );
                } catch (NumberFormatException e) {
                    // Fall through to legacy parsing
                }
            }
        }
        // Legacy format: "ClassName #charId"
        int hashIndex = rowName.lastIndexOf(" #");
        if (hashIndex >= 0) {
            try {
                return Integer.parseInt(rowName.substring(hashIndex + 2));
            } catch (NumberFormatException e) {
                // Fall through to legacy parsing
            }
        }
        // Legacy format: "Char 12345" (without #)
        if (rowName.startsWith("Char ")) {
            try {
                return Integer.parseInt(rowName.substring(5));
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        // Fallback: search by class name (for backwards compatibility)
        for (Integer charId : characterClassNames.keySet()) {
            if (characterClassNames.get(charId).equals(rowName)) {
                return charId;
            }
        }
        return -1;
    }

    private double getSessionStartFame(int charId) {
        return sessionStartFame.getOrDefault(charId, getInitialFame(charId));
    }

    private double getInitialFame(int charId) {
        ArrayList<Fame> entries = fameData.get(charId);
        return (entries != null && !entries.isEmpty())
            ? entries.get(0).getFame()
            : 0;
    }

    private void ensureCurrentCharacterAtTop() {
        int idx = findRowByCharId(currentCharacterId);
        if (idx > 0) {
            Object[] rowData = new Object[tableModel.getColumnCount()];
            for (int c = 0; c < tableModel.getColumnCount(); c++) {
                rowData[c] = tableModel.getValueAt(idx, c);
            }
            tableModel.removeRow(idx);
            tableModel.insertRow(0, rowData);
        }
    }

    private void resetFamePerHourForInactiveCharacters() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String rowName = (String) tableModel.getValueAt(i, 0);
            int rowCharId = getCharacterIdFromRowName(rowName);
            if (rowCharId != currentCharacterId) {
                tableModel.setValueAt(Formatters.formatFamePerHour(0), i, 3);
            }
        }
    }

    // --- Session Management ---

    private void handleNewSession(boolean deleteFile) {
        resetAllSessions();
        FameTableBridge bridge = FameTableBridge.getInstance();
        if (bridge != null) {
            if (deleteFile) {
                bridge.clearCurrentSessionFile();
            } else {
                bridge.startNewSessionFile();
            }
        }
    }

    private void resetAllSessions() {
        for (Integer charId : lastFameEntries.keySet()) {
            Fame lastEntry = lastFameEntries.get(charId);
            if (lastEntry != null) {
                sessionStartFame.put(charId, lastEntry.getFame());
                sessionStartTime.put(charId, lastEntry.getTime());
            }
        }
        mapFameData.clear();
        updateAllTableRows();
    }

    private void updateAllTableRows() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String rowName = (String) tableModel.getValueAt(i, 0);
            int charId = getCharacterIdFromRowName(rowName);
            if (charId != -1) {
                Fame lastEntry = lastFameEntries.get(charId);
                if (lastEntry != null) {
                    double newSessionStartFame = getSessionStartFame(charId);
                    double sessionGain =
                        lastEntry.getFame() - newSessionStartFame;
                    // Update Initial Fame column (column 1) to reflect new session start
                    tableModel.setValueAt(
                        Formatters.formatNumberExact(newSessionStartFame),
                        i,
                        1
                    );
                    tableModel.setValueAt(
                        Formatters.formatFame(sessionGain, 2),
                        i,
                        4
                    );
                }
                tableModel.setValueAt(Formatters.formatFamePerHour(0), i, 3);
            }
        }
    }

    private void triggerMapChangeAutoSave() {
        FameTableBridge bridge = FameTableBridge.getInstance();
        if (bridge != null) {
            bridge.triggerMapChangeAutoSave();
        }
    }

    // --- Map Fame Tracking ---

    private void updateMapFameTracking(
        int charId,
        String mapName,
        double currentFame,
        long currentTime
    ) {
        ArrayList<MapFameData> charMapData = mapFameData.computeIfAbsent(
            charId,
            k -> new ArrayList<>()
        );

        MapFameData current = currentMapData.get(charId);
        if (current != null) {
            current.endTime = currentTime;
            current.endFame = currentFame;
            if (current.getFameGained() > 0) {
                charMapData.add(current);
            }
        }

        if (mapName != null && !mapName.isEmpty()) {
            currentMapData.put(
                charId,
                new MapFameData(mapName, currentTime, currentFame)
            );
        }
    }

    private void showMapFameTable() {
        // Close existing dialog if open
        closeMapFameDialog();

        if (mapFameData.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "No map fame data available.",
                "Map Fame",
                JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        List<Integer> charsWithData = new ArrayList<>();
        for (Integer charId : mapFameData.keySet()) {
            ArrayList<MapFameData> data = mapFameData.get(charId);
            if (data != null && !data.isEmpty()) {
                charsWithData.add(charId);
            }
        }

        if (charsWithData.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "No map fame data available.",
                "Map Fame",
                JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        String[] options = charsWithData
            .stream()
            .map(id -> getClassNameForCharacterId(id) + " (ID: " + id + ")")
            .toArray(String[]::new);

        String selected = (String) JOptionPane.showInputDialog(
            this,
            "Select character:",
            "Character Selection",
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );

        if (selected == null) return;

        int charId = extractCharIdFromSelection(selected);
        if (charId != -1) {
            showMapFameDialog(charId);
        }
    }

    private void showMapFameDialog(int charId) {
        // Non-modal dialog for real-time updates
        activeMapFameDialog = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            "Map Fame - " + getClassNameForCharacterId(charId) + " (Live)",
            false // Non-modal
        );
        activeMapFameCharId = charId;
        activeMapFameDialog.setLayout(new BorderLayout());
        activeMapFameDialog.setSize(800, 500);
        activeMapFameDialog.setLocationRelativeTo(this);

        String[] cols = {
            "Map Name",
            "Time Spent",
            "Fame Gained",
            "Fame/Minute",
        };
        activeMapFameModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        refreshMapFameTableData();

        JTable table = new JTable(activeMapFameModel);
        table.setAutoCreateRowSorter(true);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshMapFameTableData());

        JButton filterBtn = new JButton("Dungeon Filter");
        filterBtn.addActionListener(e -> {
            ArrayList<MapFameData> data = mapFameData.get(activeMapFameCharId);
            if (data != null) {
                showDungeonFilterDialog(
                    activeMapFameDialog,
                    activeMapFameModel,
                    data
                );
            }
        });

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> closeMapFameDialog());

        buttonPanel.add(refreshBtn);
        buttonPanel.add(filterBtn);
        buttonPanel.add(closeBtn);

        activeMapFameDialog.add(new JScrollPane(table), BorderLayout.CENTER);
        activeMapFameDialog.add(buttonPanel, BorderLayout.SOUTH);

        // Clean up when dialog is closed
        activeMapFameDialog.addWindowListener(
            new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    closeMapFameDialog();
                }
            }
        );

        // Start auto-refresh timer (updates every 2 seconds)
        mapFameRefreshTimer = new Timer(2000, e -> {
            if (
                activeMapFameDialog != null && activeMapFameDialog.isVisible()
            ) {
                refreshMapFameTableData();
            }
        });
        mapFameRefreshTimer.start();

        activeMapFameDialog.setVisible(true);
    }

    private void refreshMapFameTableData() {
        if (activeMapFameModel == null || activeMapFameCharId == -1) return;

        ArrayList<MapFameData> data = mapFameData.get(activeMapFameCharId);
        if (data == null) data = new ArrayList<>();

        activeMapFameModel.setRowCount(0);
        for (MapFameData d : data) {
            // Apply dungeon filter if set
            Boolean filterState = dungeonFilterState.get(d.mapName);
            if (filterState != null && !filterState) continue;

            double fameGained = d.getFameGained();
            double minutes = d.getTimeSpent() / 60000.0;
            double fpm = minutes > 0 ? fameGained / minutes : 0;
            activeMapFameModel.addRow(
                new Object[] {
                    d.mapName,
                    d.getTimeSpentFormatted(),
                    String.format("%.1f", fameGained),
                    String.format("%.1f", fpm),
                }
            );
        }
    }

    private void closeMapFameDialog() {
        if (mapFameRefreshTimer != null) {
            mapFameRefreshTimer.stop();
            mapFameRefreshTimer = null;
        }
        if (activeMapFameDialog != null) {
            activeMapFameDialog.dispose();
            activeMapFameDialog = null;
        }
        activeMapFameModel = null;
        activeMapFameCharId = -1;
    }

    private void showDungeonFilterDialog(
        JDialog parent,
        DefaultTableModel model,
        ArrayList<MapFameData> originalData
    ) {
        JDialog filterDialog = new JDialog(parent, "Dungeon Filter", true);
        filterDialog.setLayout(new BorderLayout());
        filterDialog.setSize(300, 400);
        filterDialog.setLocationRelativeTo(parent);

        java.util.Set<String> dungeonNames = new java.util.HashSet<>();
        for (MapFameData d : originalData) {
            dungeonNames.add(d.mapName);
        }

        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
        java.util.Map<String, JCheckBox> checkBoxMap =
            new java.util.HashMap<>();

        for (String name : dungeonNames) {
            boolean selected = dungeonFilterState.getOrDefault(name, true);
            JCheckBox cb = new JCheckBox(name, selected);
            checkBoxMap.put(name, cb);
            checkBoxPanel.add(cb);
        }

        JPanel buttonPanel = new JPanel();
        JButton applyBtn = new JButton("Apply");
        applyBtn.addActionListener(e -> {
            for (String name : checkBoxMap.keySet()) {
                dungeonFilterState.put(
                    name,
                    checkBoxMap.get(name).isSelected()
                );
            }
            // Refresh the live table with new filter
            refreshMapFameTableData();
            filterDialog.dispose();
        });
        JButton resetBtn = new JButton("Reset");
        resetBtn.addActionListener(e -> {
            for (JCheckBox cb : checkBoxMap.values()) cb.setSelected(true);
            dungeonFilterState.clear();
        });
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> filterDialog.dispose());

        buttonPanel.add(applyBtn);
        buttonPanel.add(resetBtn);
        buttonPanel.add(closeBtn);

        filterDialog.add(new JScrollPane(checkBoxPanel), BorderLayout.CENTER);
        filterDialog.add(buttonPanel, BorderLayout.SOUTH);
        filterDialog.setVisible(true);
    }

    private int extractCharIdFromSelection(String selection) {
        try {
            int start = selection.lastIndexOf("(ID: ") + 5;
            int end = selection.lastIndexOf(")");
            if (start > 4 && end > start) {
                return Integer.parseInt(selection.substring(start, end).trim());
            }
        } catch (Exception ignored) {}
        return -1;
    }

    // --- Inner Classes ---

    private class TableSortHandler extends java.awt.event.MouseAdapter {

        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            if (e.getClickCount() != 2) return;

            int col = fameTable.convertColumnIndexToModel(
                fameTable.columnAtPoint(e.getPoint())
            );

            Object lastColObj = fameTable
                .getTableHeader()
                .getClientProperty("lastSortedColumn");
            Object sortAscObj = fameTable
                .getTableHeader()
                .getClientProperty("sortAscending");
            int lastCol = lastColObj instanceof Integer
                ? (Integer) lastColObj
                : -1;
            boolean sortAsc = sortAscObj instanceof Boolean
                ? (Boolean) sortAscObj
                : true;
            sortAsc = (lastCol == col) ? !sortAsc : true;

            fameTable
                .getTableHeader()
                .putClientProperty("lastSortedColumn", col);
            fameTable
                .getTableHeader()
                .putClientProperty("sortAscending", sortAsc);

            String pinnedName = getClassNameForCharacterId(currentCharacterId);
            List<Object[]> rows = new ArrayList<>();
            Object[] pinnedRow = null;

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                Object[] row = new Object[tableModel.getColumnCount()];
                for (int c = 0; c < tableModel.getColumnCount(); c++) {
                    row[c] = tableModel.getValueAt(i, c);
                }
                if (pinnedName.equals(row[0])) {
                    pinnedRow = row;
                } else {
                    rows.add(row);
                }
            }

            final boolean ascending = sortAsc;
            final int sortCol = col;
            rows.sort((a, b) -> {
                Object va = a[sortCol],
                    vb = b[sortCol];
                Double da = parseDouble(va),
                    db = parseDouble(vb);
                int result;
                if (da != null && db != null) {
                    result = Double.compare(da, db);
                } else {
                    result = String.valueOf(va).compareToIgnoreCase(
                        String.valueOf(vb)
                    );
                }
                return ascending ? result : -result;
            });

            tableModel.setRowCount(0);
            if (pinnedRow != null) tableModel.addRow(pinnedRow);
            for (Object[] r : rows) tableModel.addRow(r);
        }

        private Double parseDouble(Object val) {
            if (val == null) return null;
            try {
                return Double.parseDouble(val.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    private class FamePerHourRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column
        ) {
            Component c = super.getTableCellRendererComponent(
                table,
                value,
                isSelected,
                hasFocus,
                row,
                column
            );

            String tooltip = "Fame per minute: 0.00 | Session start: N/A";
            if (value != null) {
                try {
                    double fph = Double.parseDouble(value.toString());
                    double fpm = fph / 60.0;

                    String charName = (String) tableModel.getValueAt(row, 0);
                    int charId = getCharacterIdFromRowName(charName);

                    if (charId != -1) {
                        Long sessionStart = sessionStartTime.get(charId);
                        if (sessionStart != null) {
                            String timeStr = Formatters.formatTimestamp(
                                sessionStart
                            );
                            tooltip = String.format(
                                "Fame per minute: %.2f | Session started: %s",
                                fpm,
                                timeStr
                            );
                        } else {
                            tooltip = String.format(
                                "Fame per minute: %.2f | Session start: N/A",
                                fpm
                            );
                        }
                    } else {
                        tooltip = String.format(
                            "Fame per minute: %.2f | Session start: Unknown",
                            fpm
                        );
                    }
                } catch (NumberFormatException ignored) {}
            }
            setToolTipText(tooltip);
            return c;
        }
    }
}
