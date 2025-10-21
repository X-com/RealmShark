package tomato.gui.stats;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import tomato.backend.data.TomatoData;
import tomato.gui.stats.data.MapFameData;
import tomato.gui.stats.session.FameSessionManager;
import tomato.gui.stats.session.FameSessionViewer;
import tomato.realmshark.RealmCharacter;

public class FameTablePanel extends JPanel {

    private final JTable fameTable;
    private final HashMap<Integer, ArrayList<MapFameData>> mapFameData;
    private final HashMap<Integer, MapFameData> currentMapData;
    private String currentMapName = "";
    private final DefaultTableModel tableModel;
    private final HashMap<Integer, ArrayList<Fame>> fameData;
    private final HashMap<Integer, Fame> lastFameEntries;
    private final HashMap<Integer, Double> sessionStartFame;
    private final HashMap<Integer, Long> sessionStartTime;
    private final HashMap<Integer, String> characterClassNames;
    private static FameTablePanel INSTANCE;
    private final JLabel infoLabel;
    private final TomatoData tomatoData;
    private int currentCharacterId = -1;
    private int previousCharacterId = -1;
    private final HashMap<String, Boolean> dungeonFilterState;

    public FameTablePanel(TomatoData tomatoData) {
        INSTANCE = this;
        this.tomatoData = tomatoData;
        setLayout(new BorderLayout());
        fameData = new HashMap<>();
        lastFameEntries = new HashMap<>();
        sessionStartFame = new HashMap<>();
        sessionStartTime = new HashMap<>();
        characterClassNames = new HashMap<>();
        mapFameData = new HashMap<>();
        currentMapData = new HashMap<>();
        dungeonFilterState = new HashMap<>();

        // Create table with column headers
        String[] columnNames = {
            "Character",
            "Initial Fame",
            "Current Fame",
            "Fame/Hour",
            "Session Gain",
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };

        fameTable = new JTable(tableModel);

        fameTable.setAutoCreateRowSorter(false);
        fameTable.getTableHeader().setReorderingAllowed(false);
        fameTable
            .getTableHeader()
            .addMouseListener(
                new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        if (e.getClickCount() == 2) {
                            int viewCol = fameTable.columnAtPoint(e.getPoint());
                            int modelCol = fameTable.convertColumnIndexToModel(
                                viewCol
                            );

                            // Determine sort order toggle
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
                            if (lastCol == modelCol) {
                                sortAsc = !sortAsc;
                            } else {
                                sortAsc = true;
                            }
                            fameTable
                                .getTableHeader()
                                .putClientProperty(
                                    "lastSortedColumn",
                                    modelCol
                                );
                            fameTable
                                .getTableHeader()
                                .putClientProperty("sortAscending", sortAsc);

                            // Identify pinned (current) row
                            String pinnedName = getClassNameForCharacterId(
                                currentCharacterId
                            );
                            int pinnedIndex = -1;
                            for (int i = 0; i < tableModel.getRowCount(); i++) {
                                Object v = tableModel.getValueAt(i, 0);
                                if (pinnedName.equals(v)) {
                                    pinnedIndex = i;
                                    break;
                                }
                            }

                            java.util.List<Object[]> rows =
                                new java.util.ArrayList<>();
                            Object[] pinnedRow = null;
                            for (int i = 0; i < tableModel.getRowCount(); i++) {
                                Object[] row =
                                    new Object[tableModel.getColumnCount()];
                                for (
                                    int c = 0;
                                    c < tableModel.getColumnCount();
                                    c++
                                ) {
                                    row[c] = tableModel.getValueAt(i, c);
                                }
                                if (i == pinnedIndex) {
                                    pinnedRow = row;
                                } else {
                                    rows.add(row);
                                }
                            }

                            final boolean sortAscFinal = sortAsc;
                            java.util.Comparator<Object[]> cmp = (a, b) -> {
                                Object va = a[modelCol];
                                Object vb = b[modelCol];
                                Double da = null;
                                Double db = null;
                                try {
                                    da = va != null
                                        ? Double.parseDouble(va.toString())
                                        : null;
                                } catch (NumberFormatException ignored) {}
                                try {
                                    db = vb != null
                                        ? Double.parseDouble(vb.toString())
                                        : null;
                                } catch (NumberFormatException ignored) {}
                                int result;

                                if (da != null && db != null) {
                                    result = Double.compare(da, db);
                                } else {
                                    String sa = va == null ? "" : va.toString();
                                    String sb = vb == null ? "" : vb.toString();
                                    result = sa.compareToIgnoreCase(sb);
                                }
                                return sortAscFinal ? result : -result;
                            };

                            rows.sort(cmp);

                            // Rebuild model with pinned row first
                            tableModel.setRowCount(0);
                            if (pinnedRow != null) {
                                tableModel.addRow(pinnedRow);
                            }
                            for (Object[] r : rows) {
                                tableModel.addRow(r);
                            }
                        }
                    }
                }
            );

        // Set column widths
        fameTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        fameTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        fameTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        fameTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        fameTable.getColumnModel().getColumn(4).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(fameTable);
        add(scrollPane, BorderLayout.CENTER);

        // Set custom cell renderer for Fame/Hour column to show tooltip
        fameTable
            .getColumnModel()
            .getColumn(3)
            .setCellRenderer(
                new DefaultTableCellRenderer() {
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

                        if (value != null) {
                            try {
                                double famePerHour = Double.parseDouble(
                                    value.toString()
                                );
                                double famePerMinute = famePerHour / 60.0;

                                // Get character ID from row data and find session start time
                                int charId = -1;
                                try {
                                    String charName =
                                        (String) tableModel.getValueAt(row, 0);
                                    if (charName != null) {
                                        if (charName.startsWith("Char ")) {
                                            try {
                                                charId = Integer.parseInt(
                                                    charName.substring(5)
                                                );
                                            } catch (NumberFormatException e) {
                                                // Ignore malformed IDs
                                            }
                                        } else {
                                            // Look up charId by class name
                                            for (Integer id : characterClassNames.keySet()) {
                                                if (
                                                    characterClassNames
                                                        .get(id)
                                                        .equals(charName)
                                                ) {
                                                    charId = id;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    // Handle potential index out of bounds or other exceptions
                                    charId = -1;
                                }

                                String tooltipText;
                                try {
                                    if (charId != -1) {
                                        Long sessionStart =
                                            sessionStartTime.get(charId);
                                        if (sessionStart != null) {
                                            java.util.Date startDate =
                                                new java.util.Date(
                                                    sessionStart
                                                );
                                            java.text.SimpleDateFormat sdf =
                                                new java.text.SimpleDateFormat(
                                                    "yyyy-MM-dd HH:mm:ss"
                                                );
                                            tooltipText = String.format(
                                                "Fame per minute: %.2f | Session started: %s",
                                                famePerMinute,
                                                sdf.format(startDate)
                                            );
                                        } else {
                                            tooltipText = String.format(
                                                "Fame per minute: %.2f | Session start: N/A",
                                                famePerMinute
                                            );
                                        }
                                    } else {
                                        tooltipText = String.format(
                                            "Fame per minute: %.2f | Session start: Unknown character",
                                            famePerMinute
                                        );
                                    }
                                } catch (Exception e) {
                                    tooltipText = String.format(
                                        "Fame per minute: %.2f | Session start: Error",
                                        famePerMinute
                                    );
                                }

                                setToolTipText(tooltipText);
                            } catch (NumberFormatException e) {
                                setToolTipText(
                                    "Fame per minute: 0.00 | Session start: N/A"
                                );
                            }
                        } else {
                            setToolTipText(
                                "Fame per minute: 0.00 | Session start: N/A"
                            );
                        }

                        return c;
                    }
                }
            );

        // Add some instructions
        // Add reset button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JButton newSessionButton = new JButton("New Session");
        newSessionButton.setToolTipText(
            "New Session: starts a fresh .fame file and resets stats. Shift+Click to delete current file and reset."
        );

        newSessionButton.addMouseListener(
            new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.isShiftDown()) {
                        // Shift + click: preserve current behavior (reset + delete current file)
                        resetAllSessionsAndClearFile();
                    } else {
                        // Click: start a new .fame file and reset stats for all characters
                        resetAllSessions();
                        try {
                            FameTableBridge bridge =
                                FameTableBridge.getInstance();
                            if (bridge != null) {
                                bridge.startNewSessionFile();
                            }
                        } catch (Exception ex) {
                            System.err.println(
                                "Error starting new session: " + ex.getMessage()
                            );
                        }
                    }
                }
            }
        );
        buttonPanel.add(newSessionButton);

        JButton mapFameButton = new JButton("Show Map Fame");
        mapFameButton.addActionListener(e -> showMapFameTable());
        buttonPanel.add(mapFameButton);

        JButton viewSessionsButton = new JButton("View Saved Sessions");
        viewSessionsButton.addActionListener(e -> viewSavedSessions());
        buttonPanel.add(viewSessionsButton);

        infoLabel = new JLabel(
            "Enter Daily Quest Room to load char data | Fame tracking - updates automatically when fame changes"
        );
        infoLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(buttonPanel, BorderLayout.NORTH);
        southPanel.add(infoLabel, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);
    }

    /**
     * Gets the singleton instance of FameTablePanel for session integration
     */
    public static FameTablePanel getInstance() {
        return INSTANCE;
    }

    /**
     * Method for receiving realm character list info to pre-populate the fame table.
     */
    public static void updateRealmChars() {
        if (INSTANCE != null) {
            INSTANCE.populateFromCharacterData();
        }
    }

    /**
     * Open the saved sessions viewer
     */
    private void viewSavedSessions() {
        FameSessionViewer.openSessionViewer();
    }

    /**
     * Populates the fame table with character data when available.
     */
    private void populateFromCharacterData() {
        if (
            tomatoData == null ||
            tomatoData.chars == null ||
            tomatoData.chars.isEmpty()
        ) {
            // Character data not loaded yet - show informative message
            SwingUtilities.invokeLater(() -> {
                if (tableModel.getRowCount() == 0) {
                    infoLabel.setText(
                        "Enter Daily Quest Room to load character data | Fame tracking - updates automatically when fame changes"
                    );
                }
            });
            return;
        }

        SwingUtilities.invokeLater(() -> {
            // Clear the initial message
            infoLabel.setText(
                "Fame tracking - updates automatically when fame changes"
            );

            // Pre-populate table with character data
            for (RealmCharacter character : tomatoData.chars) {
                // Skip characters with invalid data
                if (
                    character.charId == 0 ||
                    character.classString == null ||
                    character.classString.isEmpty()
                ) {
                    continue;
                }

                // Check if this character already exists in the table
                boolean characterExists = false;
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String rowCharName = (String) tableModel.getValueAt(i, 0);
                    if (rowCharName.equals(character.classString)) {
                        characterExists = true;
                        break;
                    }
                }

                if (!characterExists) {
                    Object[] rowData = {
                        character.classString,
                        Formatters.formatNumberExact(character.fame),
                        Formatters.formatNumberExact(character.fame),
                        Formatters.formatFamePerHour(0),
                        "0.00",
                    };

                    tableModel.addRow(rowData);

                    // Store character class name for future reference
                    characterClassNames.put(
                        character.charId,
                        character.classString
                    );

                    // Initialize session data
                    sessionStartFame.put(
                        character.charId,
                        (double) character.fame
                    );
                    sessionStartTime.put(
                        character.charId,
                        System.currentTimeMillis()
                    );
                    lastFameEntries.put(
                        character.charId,
                        new Fame(character.fame, System.currentTimeMillis())
                    );
                }
            }

            // Set current character ID if available and check for character changes

            if (tomatoData.getCharId() != -1) {
                if (
                    tomatoData.getCharId() != currentCharacterId &&
                    currentCharacterId != -1
                ) {
                    // Character changed - reset fame/hour for previous character

                    resetFamePerHourForInactiveCharacters();

                    previousCharacterId = currentCharacterId;
                }

                currentCharacterId = tomatoData.getCharId();

                // Ensure current character is at the top after initial population
                String displayNameTop = getClassNameForCharacterId(
                    currentCharacterId
                );
                int rows = tableModel.getRowCount();
                int idx = -1;
                for (int i = 0; i < rows; i++) {
                    Object v = tableModel.getValueAt(i, 0);
                    if (displayNameTop.equals(v)) {
                        idx = i;
                        break;
                    }
                }
                if (idx > 0) {
                    Object[] rowDataTop =
                        new Object[tableModel.getColumnCount()];
                    for (int c = 0; c < tableModel.getColumnCount(); c++) {
                        rowDataTop[c] = tableModel.getValueAt(idx, c);
                    }
                    tableModel.removeRow(idx);
                    tableModel.insertRow(0, rowDataTop);
                }
            }

            // If no characters were added but data exists, show appropriate message
            if (tableModel.getRowCount() == 0 && !tomatoData.chars.isEmpty()) {
                infoLabel.setText(
                    "Character data loaded but no valid characters found | Enter Daily Quest Room to refresh"
                );
            }
        });
    }

    public void updateFame(int charId, long fame, long time, String className) {
        SwingUtilities.invokeLater(() -> {
            // Check if character has changed

            if (charId != currentCharacterId && currentCharacterId != -1) {
                // Character changed - reset fame/hour for previous character

                resetFamePerHourForInactiveCharacters();

                previousCharacterId = currentCharacterId;

                currentCharacterId = charId;
            } else if (currentCharacterId == -1) {
                // First character update

                currentCharacterId = charId;
            }
            // Ensure current character is always at the top row
            SwingUtilities.invokeLater(() -> {
                String displayNameTop = getClassNameForCharacterId(
                    currentCharacterId
                );
                int rows = tableModel.getRowCount();
                int idx = -1;
                for (int i = 0; i < rows; i++) {
                    Object v = tableModel.getValueAt(i, 0);
                    if (displayNameTop.equals(v)) {
                        idx = i;
                        break;
                    }
                }
                if (idx > 0) {
                    Object[] rowDataTop =
                        new Object[tableModel.getColumnCount()];
                    for (int c = 0; c < tableModel.getColumnCount(); c++) {
                        rowDataTop[c] = tableModel.getValueAt(idx, c);
                    }
                    tableModel.removeRow(idx);
                    tableModel.insertRow(0, rowDataTop);
                }
            });

            // Store the class name for this character
            if (!className.isEmpty()) {
                characterClassNames.put(charId, className);
            }

            // Store the fame data
            fameData
                .computeIfAbsent(charId, id -> new ArrayList<>())
                .add(new Fame(fame, time));

            // Update or add row for this character
            Fame lastEntry = lastFameEntries.get(charId);
            if (lastEntry == null) {
                // New character
                addCharacterRow(charId, fame, time, className);
            } else {
                // Update existing character
                updateCharacterRow(charId, fame, time, lastEntry, className);
            }

            lastFameEntries.put(charId, new Fame(fame, time));
        });
    }

    private void addCharacterRow(
        int charId,
        long fame,
        long time,
        String className
    ) {
        String displayName = className.isEmpty() ? "Char " + charId : className;
        double initialFame = getSessionStartFame(charId);

        Object[] rowData = {
            displayName,
            Formatters.formatNumberExact(initialFame),
            Formatters.formatNumberExact(fame),
            Formatters.formatFamePerHour(0),
            "0.00",
        };

        tableModel.addRow(rowData);

        // Set session start fame and time to current values
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
        String displayName = className.isEmpty() ? "Char " + charId : className;
        // Find the row for this character
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String rowCharId = (String) tableModel.getValueAt(i, 0);
            if (rowCharId.equals(displayName)) {
                // Calculate fame gain and rate
                double fameGain = fame - lastEntry.getFame();
                long timeDiff = time - lastEntry.getTime();
                double hoursDiff = timeDiff / 3600000.0; // ms to hours
                double famePerHour = hoursDiff > 0 ? fameGain / hoursDiff : 0;

                // Calculate session-based fame per hour
                Long sessionStart = sessionStartTime.get(charId);
                double sessionFamePerHour = 0;
                if (sessionStart != null && time > sessionStart) {
                    double sessionHoursDiff = (time - sessionStart) / 3600000.0;
                    double sessionFameGain = fame - getSessionStartFame(charId);
                    sessionFamePerHour = sessionHoursDiff > 0
                        ? sessionFameGain / sessionHoursDiff
                        : 0;
                }

                // Update the row - use session-based fame per hour for accuracy
                tableModel.setValueAt(Formatters.formatNumberExact(fame), i, 2);
                tableModel.setValueAt(
                    Formatters.formatFamePerHour(sessionFamePerHour),
                    i,
                    3
                );

                // Update session gain (total gain for this character)
                double sessionGain = fame - getSessionStartFame(charId);
                tableModel.setValueAt(
                    Formatters.formatFame(sessionGain, 2),
                    i,
                    4
                );

                break;
            }
        }
    }

    private double getSessionStartFame(int charId) {
        return sessionStartFame.getOrDefault(charId, getInitialFame(charId));
    }

    private double getInitialFame(int charId) {
        ArrayList<Fame> entries = fameData.get(charId);
        return entries != null && !entries.isEmpty()
            ? entries.get(0).getFame()
            : 0;
    }

    private String formatNumber(double number) {
        // Display exact values for accuracy instead of truncated values
        if (number == (long) number) {
            // Integer value - display without decimals
            return String.format("%d", (long) number);
        } else {
            // Decimal value - display with full precision
            return String.valueOf(number);
        }
    }

    private String formatFamePerHour(double famePerHour) {
        // Format fame/hour values with 2 decimal places for readability
        return String.format("%.2f", famePerHour);
    }

    private void resetAllSessions() {
        for (Integer charId : lastFameEntries.keySet()) {
            Fame lastEntry = lastFameEntries.get(charId);
            if (lastEntry != null) {
                sessionStartFame.put(charId, lastEntry.getFame());
                sessionStartTime.put(charId, lastEntry.getTime());
            }
        }

        // Clear map fame data when resetting sessions
        mapFameData.clear();

        updateAllTables();
    }

    private void resetAllSessionsAndClearFile() {
        resetAllSessions();

        // Clear the current .fame file being written
        try {
            FameTableBridge bridge = FameTableBridge.getInstance();
            if (bridge != null) {
                bridge.clearCurrentSessionFile();
            }
        } catch (Exception e) {
            System.err.println(
                "Error clearing session file: " + e.getMessage()
            );
        }
    }

    private void updateAllTables() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String rowCharId = (String) tableModel.getValueAt(i, 0);
            if (rowCharId.startsWith("Char ")) {
                try {
                    int charId = Integer.parseInt(rowCharId.substring(5));
                    Fame lastEntry = lastFameEntries.get(charId);
                    if (lastEntry != null) {
                        double sessionGain =
                            lastEntry.getFame() - getSessionStartFame(charId);
                        tableModel.setValueAt(
                            Formatters.formatFame(sessionGain, 2),
                            i,
                            4
                        );
                        // Reset fame/hour to 0 when session is reset
                        tableModel.setValueAt(
                            Formatters.formatFamePerHour(0),
                            i,
                            3
                        );
                    }
                } catch (NumberFormatException e) {
                    // Ignore malformed character IDs
                }
            }
        }
    }

    private void resetFamePerHourForInactiveCharacters() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String rowCharName = (String) tableModel.getValueAt(i, 0);
            int rowCharId = getCharacterIdFromRowName(rowCharName);

            // Reset fame/hour to 0 for all inactive characters
            if (rowCharId != currentCharacterId) {
                tableModel.setValueAt(Formatters.formatFamePerHour(0), i, 3);
            }
        }
    }

    // Method to detect character changes from TomatoData updates
    public void checkForCharacterChange() {
        SwingUtilities.invokeLater(() -> {
            if (tomatoData != null && tomatoData.getCharId() != -1) {
                if (
                    tomatoData.getCharId() != currentCharacterId &&
                    currentCharacterId != -1
                ) {
                    // Character changed - reset fame/hour for previous character

                    resetFamePerHourForInactiveCharacters();

                    previousCharacterId = currentCharacterId;

                    currentCharacterId = tomatoData.getCharId();
                } else if (currentCharacterId == -1) {
                    // First character detection

                    currentCharacterId = tomatoData.getCharId();
                }
                // Ensure current character is always at the top row
                String displayNameTop = getClassNameForCharacterId(
                    currentCharacterId
                );
                int rows = tableModel.getRowCount();
                int idx = -1;
                for (int i = 0; i < rows; i++) {
                    Object v = tableModel.getValueAt(i, 0);
                    if (displayNameTop.equals(v)) {
                        idx = i;
                        break;
                    }
                }
                if (idx > 0) {
                    Object[] rowDataTop =
                        new Object[tableModel.getColumnCount()];
                    for (int c = 0; c < tableModel.getColumnCount(); c++) {
                        rowDataTop[c] = tableModel.getValueAt(idx, c);
                    }
                    tableModel.removeRow(idx);
                    tableModel.insertRow(0, rowDataTop);
                }
            }
        });
    }

    private int getCharacterIdFromRowName(String rowCharName) {
        // Check if row name matches "Char X" format
        if (rowCharName.startsWith("Char ")) {
            try {
                return Integer.parseInt(rowCharName.substring(5));
            } catch (NumberFormatException e) {
                // Ignore malformed character IDs
                return -1;
            }
        } else {
            // Look up the character ID from the class names map
            for (Integer charId : characterClassNames.keySet()) {
                String className = characterClassNames.get(charId);
                if (className.equals(rowCharName)) {
                    return charId;
                }
            }
        }
        return -1; // Character ID not found
    }

    // Helper method to get all fame data for a character (useful for potential export)
    public ArrayList<Fame> getFameData(int charId) {
        return fameData.get(charId);
    }

    // Helper method to get current fame for a character
    public Double getCurrentFame(int charId) {
        Fame lastEntry = lastFameEntries.get(charId);
        return lastEntry != null ? lastEntry.getFame() : null;
    }

    /**
     * Updates map fame tracking when changing maps
     */
    private void updateMapFameTracking(
        int charId,
        String mapName,
        double currentFame,
        long currentTime
    ) {
        // Get or create map fame data list for this character
        ArrayList<MapFameData> charMapData = mapFameData.computeIfAbsent(
            charId,
            k -> new ArrayList<>()
        );

        // Check if we're already tracking a map for this character
        MapFameData currentData = currentMapData.get(charId);
        if (currentData != null) {
            // Update the end time and fame for the current map
            currentData.endTime = currentTime;
            currentData.endFame = currentFame;

            // Only add the completed map data to the list if fame was gained
            // This ensures we capture maps where bosses are killed instantly
            if (currentData.getFameGained() > 0) {
                charMapData.add(currentData);
            }
        }

        // Start tracking new map (but don't add to the list yet - wait for completion)
        if (mapName != null && !mapName.isEmpty()) {
            MapFameData newMapData = new MapFameData(
                mapName,
                currentTime,
                (double) currentFame
            );
            currentMapData.put(charId, newMapData);
        }
    }

    /**
     * Public method to handle map changes and update fame tracking
     */
    public void onMapChange(String newMapName) {
        if (newMapName != null && !newMapName.equals(currentMapName)) {
            // First, complete tracking for the current map
            String oldMapName = currentMapName;
            currentMapName = newMapName;

            // Update map fame tracking for all characters with current fame data
            // This will complete the current map and start tracking the new one
            for (Integer charId : lastFameEntries.keySet()) {
                Fame lastFame = lastFameEntries.get(charId);
                if (lastFame != null) {
                    updateMapFameTracking(
                        charId,
                        newMapName,
                        lastFame.getFame(),
                        System.currentTimeMillis()
                    );
                }
            }

            // Auto-save session when leaving a map to prevent data loss from crashes
            autoSaveSessionOnMapChange();
        }
    }

    /**
     * Static method to handle map changes from external classes
     */
    public static void handleMapChange(String newMapName) {
        if (INSTANCE != null) {
            INSTANCE.onMapChange(newMapName);
        }
    }

    /**
     * Auto-saves the current session when leaving a map to prevent data loss
     * Always saves on map change due to rare edge case bugs
     */
    private void autoSaveSessionOnMapChange() {
        try {
            // Get the FameTrackerGUI instance through the bridge
            FameTableBridge bridge = FameTableBridge.getInstance();
            if (bridge != null) {
                // Always auto-save on map change to prevent rare edge case data loss
                bridge.triggerMapChangeAutoSave();
            }
        } catch (Exception e) {
            // Silent fail for auto-save - don't interrupt user experience
            System.err.println(
                "Map change auto-save failed: " + e.getMessage()
            );
        }
    }

    /**
     * Displays a table showing fame gained per map for the selected character
     */
    private void showMapFameTable() {
        // Check if we have map fame data for any characters
        if (mapFameData.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "No map fame data available for any characters.",
                "Map Fame Data",
                JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        // Get all characters with map fame data
        java.util.List<Integer> charactersWithMapData =
            new java.util.ArrayList<>();
        for (Integer charId : mapFameData.keySet()) {
            ArrayList<MapFameData> charMapData = mapFameData.get(charId);
            if (charMapData != null && !charMapData.isEmpty()) {
                charactersWithMapData.add(charId);
            }
        }

        if (charactersWithMapData.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "No map fame data available for any characters.",
                "Map Fame Data",
                JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        // Let user select which character to view
        String[] characterOptions = new String[charactersWithMapData.size()];
        for (int i = 0; i < charactersWithMapData.size(); i++) {
            int charId = charactersWithMapData.get(i);
            String className = getClassNameForCharacterId(charId);
            characterOptions[i] = className + " (ID: " + charId + ")";
        }

        String selectedCharacter = (String) JOptionPane.showInputDialog(
            this,
            "Select the character to display map fame information:",
            "Character Selection",
            JOptionPane.QUESTION_MESSAGE,
            null,
            characterOptions,
            characterOptions[0]
        );

        if (selectedCharacter == null) {
            return; // User cancelled
        }

        // Extract character ID from selection
        int selectedCharId = extractCharIdFromSelection(selectedCharacter);
        if (selectedCharId == -1) {
            return;
        }

        // Show map fame dialog for selected character
        ArrayList<MapFameData> mapData = mapFameData.get(selectedCharId);
        showMapFameDialog(selectedCharId, mapData);
    }

    /**
     * Shows dialog with map fame data for a specific character
     */
    private void showMapFameDialog(int charId, ArrayList<MapFameData> mapData) {
        JDialog dialog = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            "Map Fame Data - " + getClassNameForCharacterId(charId),
            true
        );
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(this);

        // Create main panel with table
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Create table model
        String[] columnNames = {
            "Map Name",
            "Time Spent",
            "Fame Gained",
            "Fame/Minute",
        };
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Populate table with data
        model.setRowCount(0);
        if (mapData != null) {
            for (MapFameData data : mapData) {
                double fameGained = data.getFameGained();
                long timeSpentMs = data.getTimeSpent();
                double minutesSpent = timeSpentMs / 60000.0;
                double famePerMinute = minutesSpent > 0
                    ? fameGained / minutesSpent
                    : 0;

                model.addRow(
                    new Object[] {
                        data.mapName,
                        data.getTimeSpentFormatted(),
                        String.format("%.1f", fameGained),
                        String.format("%.1f", famePerMinute),
                    }
                );
            }
        }

        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        dialog.add(mainPanel, BorderLayout.CENTER);

        // Create button panel with filter and close buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Add dungeon filter button
        JButton filterButton = new JButton("Dungeon Filter");
        filterButton.addActionListener(e -> {
            showDungeonFilterDialog(dialog, model, mapData);
        });
        buttonPanel.add(filterButton);

        // Add close button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeButton);

        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Set size after adding all components
        dialog.setSize(800, 500);
        dialog.setVisible(true);
    }

    /**
     * Shows dialog for filtering dungeons in the map fame table
     */
    private void showDungeonFilterDialog(
        JDialog parentDialog,
        DefaultTableModel tableModel,
        ArrayList<MapFameData> originalData
    ) {
        JDialog filterDialog = new JDialog(
            parentDialog,
            "Dungeon Filter",
            true
        );
        filterDialog.setLayout(new BorderLayout());
        filterDialog.setSize(300, 400);
        filterDialog.setLocationRelativeTo(parentDialog);

        // Get unique dungeon names
        java.util.Set<String> dungeonNames = new java.util.HashSet<>();
        for (MapFameData data : originalData) {
            dungeonNames.add(data.mapName);
        }

        // Create checkboxes for each dungeon with persisted state
        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
        java.util.Map<String, JCheckBox> checkBoxMap =
            new java.util.HashMap<>();

        for (String dungeonName : dungeonNames) {
            // Use stored filter state or default to true if not set
            boolean isSelected = dungeonFilterState.getOrDefault(
                dungeonName,
                true
            );
            JCheckBox checkBox = new JCheckBox(dungeonName, isSelected);
            checkBoxMap.put(dungeonName, checkBox);
            checkBoxPanel.add(checkBox);
            checkBoxPanel.add(Box.createVerticalStrut(2)); // Small 2px spacing
        }

        JScrollPane scrollPane = new JScrollPane(checkBoxPanel);
        filterDialog.add(scrollPane, BorderLayout.CENTER);

        // Add apply and close buttons
        JPanel buttonPanel = new JPanel();
        JButton applyButton = new JButton("Apply");
        applyButton.addActionListener(e -> {
            applyDungeonFilter(tableModel, originalData, checkBoxMap);
            // Save filter state for persistence
            for (String dungeonName : checkBoxMap.keySet()) {
                dungeonFilterState.put(
                    dungeonName,
                    checkBoxMap.get(dungeonName).isSelected()
                );
            }
            filterDialog.dispose();
        });
        buttonPanel.add(applyButton);

        JButton resetButton = new JButton("Reset Filters");
        resetButton.addActionListener(e -> {
            // Reset all checkboxes to selected
            for (JCheckBox checkBox : checkBoxMap.values()) {
                checkBox.setSelected(true);
            }
            // Clear filter state
            dungeonFilterState.clear();
        });
        buttonPanel.add(resetButton);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> filterDialog.dispose());
        buttonPanel.add(closeButton);

        filterDialog.add(buttonPanel, BorderLayout.SOUTH);
        filterDialog.setVisible(true);
    }

    /**
     * Applies the dungeon filter to the table model
     */
    private void applyDungeonFilter(
        DefaultTableModel tableModel,
        ArrayList<MapFameData> originalData,
        java.util.Map<String, JCheckBox> checkBoxMap
    ) {
        // Clear current table data
        tableModel.setRowCount(0);

        // Add filtered data
        for (MapFameData data : originalData) {
            JCheckBox checkBox = checkBoxMap.get(data.mapName);
            if (checkBox != null && checkBox.isSelected()) {
                double fameGained = data.getFameGained();
                long timeSpentMs = data.getTimeSpent();
                double minutesSpent = timeSpentMs / 60000.0;
                double famePerMinute = minutesSpent > 0
                    ? fameGained / minutesSpent
                    : 0;

                tableModel.addRow(
                    new Object[] {
                        data.mapName,
                        data.getTimeSpentFormatted(),
                        String.format("%.1f", fameGained),
                        String.format("%.1f", famePerMinute),
                    }
                );
            }
        }
    }

    /**
     * Resets all dungeon filters to their default state (all enabled).
     */
    public void resetDungeonFilters() {
        dungeonFilterState.clear();
    }

    /**
     * Gets the map fame data for session persistence
     */
    public HashMap<Integer, ArrayList<MapFameData>> getMapFameData() {
        return mapFameData;
    }

    /**
     * Sets the map fame data from a loaded session
     */
    public void setMapFameData(
        HashMap<Integer, ArrayList<MapFameData>> newMapFameData
    ) {
        mapFameData.clear();
        mapFameData.putAll(newMapFameData);
    }

    /**
     * Gets the current map data for session persistence
     */
    public HashMap<Integer, MapFameData> getCurrentMapData() {
        return currentMapData;
    }

    /**
     * Sets the current map data from a loaded session
     */
    public void setCurrentMapData(
        HashMap<Integer, MapFameData> newCurrentMapData
    ) {
        currentMapData.clear();
        currentMapData.putAll(newCurrentMapData);
    }

    /**
     * Get class name for a character ID
     */
    public String getClassNameForCharacterId(int charId) {
        return characterClassNames.getOrDefault(charId, "Char " + charId);
    }

    /**
     * Extract character ID from selection string (format: "ClassName (ID: 123)")
     */
    private int extractCharIdFromSelection(String selection) {
        try {
            int startIndex = selection.lastIndexOf("(ID: ") + 5;
            int endIndex = selection.lastIndexOf(")");
            if (startIndex > 0 && endIndex > startIndex) {
                String idStr = selection.substring(startIndex, endIndex).trim();
                return Integer.parseInt(idStr);
            }
        } catch (Exception e) {
            // Fallback: try to parse the entire string as integer
            try {
                return Integer.parseInt(selection.trim());
            } catch (NumberFormatException ex) {
                // If all fails, return -1
            }
        }
        return -1;
    }
}
