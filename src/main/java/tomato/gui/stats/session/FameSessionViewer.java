package tomato.gui.stats.session;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import tomato.gui.stats.Fame;
import tomato.gui.stats.FameTablePanel;
import tomato.gui.stats.GraphPanel;
import tomato.gui.stats.data.MapFameData;

/**
 * Viewer for saved fame session files.
 * Displays character fame data, map fame data, and session information in a tabbed interface.
 */
public class FameSessionViewer extends JFrame {

    private final FameSession session;
    private JTabbedPane tabbedPane;
    private JTable characterFameTable;
    private JTable mapFameTable;
    private JTextArea sessionInfoArea;
    private JComboBox<String> characterSelector;
    private JComboBox<String> dungeonFilter;
    private GraphPanel graphPanel;

    public FameSessionViewer(FameSession session) {
        this.session = session;
        initializeUI();
        populateData();
        setVisible(true);
    }

    private void initializeUI() {
        setTitle("Fame Session Viewer - " + session.getSessionName());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Character selector panel at the top
        JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectorPanel.add(new JLabel("Select Character (Class & ID): "));
        characterSelector = new JComboBox<>();
        characterSelector.addActionListener(e -> updateCharacterData());
        selectorPanel.add(characterSelector);
        add(selectorPanel, BorderLayout.NORTH);

        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Character Fame", createCharacterFamePanel());

        // Use shared GraphPanel (minimal mode - no time range dropdown)
        graphPanel = GraphPanel.createMinimal();
        graphPanel.setPreferredSize(new Dimension(800, 400));
        tabbedPane.addTab("Fame Graph", graphPanel);

        tabbedPane.addTab("Map Fame", createMapFamePanel());
        tabbedPane.addTab("Session Info", createSessionInfoPanel());
        add(tabbedPane, BorderLayout.CENTER);

        // Close button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createCharacterFamePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columnNames = {
            "Character ID",
            "Class Name",
            "Fame Entries",
            "Start Fame",
            "End Fame",
            "Fame Gained",
        };
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        characterFameTable = new JTable(model);
        characterFameTable.setAutoCreateRowSorter(true);

        panel.add(new JScrollPane(characterFameTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMapFamePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filter Dungeon: "));
        dungeonFilter = new JComboBox<>();
        dungeonFilter.addItem("All Dungeons");
        dungeonFilter.addActionListener(e -> updateMapFameData());
        filterPanel.add(dungeonFilter);
        panel.add(filterPanel, BorderLayout.NORTH);

        // Table
        String[] columnNames = {
            "Class Name",
            "Map Name",
            "Fame Gained",
            "Time Spent",
            "Fame/Minute",
        };
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        mapFameTable = new JTable(model);
        mapFameTable.setAutoCreateRowSorter(true);

        panel.add(new JScrollPane(mapFameTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSessionInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        sessionInfoArea = new JTextArea();
        sessionInfoArea.setEditable(false);
        sessionInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        panel.add(new JScrollPane(sessionInfoArea), BorderLayout.CENTER);
        return panel;
    }

    private void populateData() {
        populateCharacterFameData();
        populateDungeonFilter();
        updateMapFameData();
        populateSessionInfo();
    }

    private void populateCharacterFameData() {
        DefaultTableModel model =
            (DefaultTableModel) characterFameTable.getModel();
        model.setRowCount(0);
        characterSelector.removeAllItems();

        HashMap<Integer, List<Fame>> fameData = session.getCharacterFameData();

        for (Integer charId : fameData.keySet()) {
            List<Fame> entries = fameData.get(charId);
            if (entries != null && !entries.isEmpty()) {
                String className = getClassNameForCharacter(charId);
                double startFame = entries.get(0).getFame();
                double endFame = entries.get(entries.size() - 1).getFame();
                double fameGained = endFame - startFame;

                if (fameGained > 0) {
                    model.addRow(
                        new Object[] {
                            charId,
                            className,
                            entries.size(),
                            String.format("%.0f", startFame),
                            String.format("%.0f", endFame),
                            String.format("%.1f", fameGained),
                        }
                    );
                    characterSelector.addItem(
                        className + " (ID: " + charId + ")"
                    );
                }
            }
        }

        if (characterSelector.getItemCount() > 0) {
            characterSelector.setSelectedIndex(0);
        }
    }

    private void populateDungeonFilter() {
        dungeonFilter.removeAllItems();
        dungeonFilter.addItem("All Dungeons");

        Integer selectedCharId = getSelectedCharacterId();
        HashSet<String> uniqueMaps = new HashSet<>();
        HashMap<Integer, List<MapFameData>> mapData =
            session.getCharacterMapFameData();

        if (selectedCharId == null) {
            // Show all dungeons from all characters
            for (List<MapFameData> entries : mapData.values()) {
                if (entries != null) {
                    for (MapFameData mapFame : entries) {
                        uniqueMaps.add(mapFame.mapName);
                    }
                }
            }
        } else {
            // Show dungeons only for selected character
            List<MapFameData> entries = mapData.get(selectedCharId);
            if (entries != null) {
                for (MapFameData mapFame : entries) {
                    uniqueMaps.add(mapFame.mapName);
                }
            }
        }

        ArrayList<String> sortedMaps = new ArrayList<>(uniqueMaps);
        Collections.sort(sortedMaps);
        for (String mapName : sortedMaps) {
            dungeonFilter.addItem(mapName);
        }
    }

    private void updateMapFameData() {
        DefaultTableModel model = (DefaultTableModel) mapFameTable.getModel();
        model.setRowCount(0);

        Integer selectedCharId = getSelectedCharacterId();
        String selectedDungeon = (String) dungeonFilter.getSelectedItem();
        boolean showAllDungeons = "All Dungeons".equals(selectedDungeon);

        HashMap<Integer, List<MapFameData>> mapData =
            session.getCharacterMapFameData();

        for (Integer charId : mapData.keySet()) {
            if (selectedCharId != null && !charId.equals(selectedCharId)) {
                continue;
            }

            List<MapFameData> entries = mapData.get(charId);
            if (entries == null) continue;

            String className = getClassNameForCharacter(charId);
            for (MapFameData mapFame : entries) {
                if (
                    !showAllDungeons && !mapFame.mapName.equals(selectedDungeon)
                ) {
                    continue;
                }

                long timeSpent = mapFame.endTime - mapFame.startTime;
                double minutesSpent = timeSpent / 60000.0;
                double famePerMinute = minutesSpent > 0
                    ? mapFame.getFameGained() / minutesSpent
                    : 0;

                if (mapFame.getFameGained() > 0) {
                    model.addRow(
                        new Object[] {
                            className,
                            mapFame.mapName,
                            String.format("%.1f", mapFame.getFameGained()),
                            formatDuration(timeSpent),
                            String.format("%.1f", famePerMinute),
                        }
                    );
                }
            }
        }
    }

    private void populateSessionInfo() {
        StringBuilder info = new StringBuilder();
        info
            .append("Session Name: ")
            .append(session.getSessionName())
            .append("\n\n");
        info
            .append("Created: ")
            .append(formatTimestamp(session.getCreatedTimestamp()))
            .append("\n");
        info
            .append("Last Modified: ")
            .append(formatTimestamp(session.getLastModifiedTimestamp()))
            .append("\n\n");
        info
            .append("Characters Tracked: ")
            .append(session.getCharacterFameData().size())
            .append("\n");
        info
            .append("Total Fame Entries: ")
            .append(getTotalFameEntries())
            .append("\n");
        info
            .append("Total Map Fame Entries: ")
            .append(getTotalMapFameEntries())
            .append("\n\n");
        info
            .append("Description:\n")
            .append(session.getDescription())
            .append("\n\n");
        info.append("Read Only: ").append(session.isReadOnly());

        sessionInfoArea.setText(info.toString());
    }

    private void updateCharacterData() {
        Integer selectedCharId = getSelectedCharacterId();
        if (selectedCharId != null) {
            List<Fame> fameData = session
                .getCharacterFameData()
                .get(selectedCharId);
            if (fameData != null) {
                graphPanel.setScores(new ArrayList<>(fameData));
            }
        }
        populateDungeonFilter();
        updateMapFameData();
    }

    private Integer getSelectedCharacterId() {
        String selectedItem = (String) characterSelector.getSelectedItem();
        if (selectedItem == null) return null;

        try {
            int startIndex = selectedItem.lastIndexOf("(ID: ") + 5;
            int endIndex = selectedItem.lastIndexOf(")");
            if (startIndex > 4 && endIndex > startIndex) {
                return Integer.parseInt(
                    selectedItem.substring(startIndex, endIndex).trim()
                );
            }
        } catch (Exception e) {
            // Parsing failed
        }
        return null;
    }

    private String getClassNameForCharacter(int charId) {
        String storedClassName = session.getCharacterClassNames().get(charId);
        if (storedClassName != null) {
            return storedClassName;
        }
        FameTablePanel instance = FameTablePanel.getInstance();
        return instance != null
            ? instance.getClassNameForCharacterId(charId)
            : "Char " + charId;
    }

    private int getTotalFameEntries() {
        int total = 0;
        for (List<Fame> entries : session.getCharacterFameData().values()) {
            total += entries.size();
        }
        return total;
    }

    private int getTotalMapFameEntries() {
        int total = 0;
        for (List<MapFameData> entries : session
            .getCharacterMapFameData()
            .values()) {
            total += entries.size();
        }
        return total;
    }

    private String formatTimestamp(long timestamp) {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
            new java.util.Date(timestamp)
        );
    }

    private String formatDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * Opens a file chooser to select and view a saved session.
     */
    public static void openSessionViewer() {
        FameSession session = FameSessionManager.loadSession();
        if (session != null) {
            SwingUtilities.invokeLater(() -> {
                new FameSessionViewer(session);
            });
        }
    }
}
