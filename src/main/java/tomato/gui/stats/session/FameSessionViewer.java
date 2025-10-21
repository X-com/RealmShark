package tomato.gui.stats.session;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import tomato.gui.stats.Fame;
import tomato.gui.stats.FameTablePanel;
import tomato.gui.stats.data.MapFameData;

public class FameSessionViewer extends JFrame {

    private final FameSession session;
    private JTabbedPane tabbedPane;
    private JTable characterFameTable;
    private JTable mapFameTable;
    private JTextArea sessionInfoArea;
    private JComboBox<String> characterSelector;
    private JComboBox<String> dungeonFilter;
    private GraphTabPanel graphTabPanel;

    // Graph styling to match application theme
    private static final Color LINE_COLOR = new Color(44, 102, 230, 180);
    private static final Color POINT_COLOR = new Color(100, 100, 100, 180);
    private static final Color GRID_COLOR = new Color(200, 200, 200, 200);
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Color AXIS_COLOR = Color.GRAY;
    private static final Stroke GRAPH_STROKE = new BasicStroke(2f);

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

        // Create character selector panel at the top (visible across all tabs)
        JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectorPanel.add(new JLabel("Select Character (Class & ID): "));
        characterSelector = new JComboBox<>();
        characterSelector.addActionListener(e -> updateCharacterData());
        selectorPanel.add(characterSelector);
        add(selectorPanel, BorderLayout.NORTH);

        // Create tabbed pane
        tabbedPane = new JTabbedPane();

        // Character fame tab
        JPanel characterFamePanel = createCharacterFamePanel();
        tabbedPane.addTab("Character Fame", characterFamePanel);

        // Graph tab with full GraphPanel functionality
        graphTabPanel = new GraphTabPanel();
        tabbedPane.addTab("Fame Graph", graphTabPanel);

        // Map fame tab
        JPanel mapFamePanel = createMapFamePanel();
        tabbedPane.addTab("Map Fame", mapFamePanel);

        // Session info tab
        JPanel infoPanel = createSessionInfoPanel();
        tabbedPane.addTab("Session Info", infoPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // Add close button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createCharacterFamePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create table model (removed Total Fame column)
        String[] columnNames = {
            "Character ID",
            "Class Name",
            "Fame Entries",
            "Start Fame",
            "End Fame",
            "Fame Gained",
        };
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        characterFameTable = new JTable(model);

        JScrollPane scrollPane = new JScrollPane(characterFameTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createMapFamePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create filter panel for dungeon filtering
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filter Dungeon: "));
        dungeonFilter = new JComboBox<>();
        dungeonFilter.addItem("All Dungeons");
        dungeonFilter.addActionListener(e -> updateMapFameData());
        filterPanel.add(dungeonFilter);
        panel.add(filterPanel, BorderLayout.NORTH);

        // Create table model for map fame (changed to Class Name and Fame/Minute)
        String[] columnNames = {
            "Class Name",
            "Map Name",
            "Fame Gained",
            "Time Spent",
            "Fame/Minute",
        };
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        mapFameTable = new JTable(model);

        JScrollPane scrollPane = new JScrollPane(mapFameTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSessionInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        sessionInfoArea = new JTextArea();
        sessionInfoArea.setEditable(false);
        sessionInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(sessionInfoArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void populateData() {
        populateCharacterFameData();
        populateMapFameData();
        populateSessionInfo();
    }

    private void populateCharacterFameData() {
        DefaultTableModel model =
            (DefaultTableModel) characterFameTable.getModel();
        model.setRowCount(0);

        HashMap<Integer, List<Fame>> fameData = session.getCharacterFameData();
        characterSelector.removeAllItems();

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
                            startFame,
                            endFame,
                            fameGained,
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

    private void populateMapFameData() {
        DefaultTableModel model = (DefaultTableModel) mapFameTable.getModel();
        model.setRowCount(0);
        updateMapFameData();
    }

    private void populateDungeonFilter() {
        dungeonFilter.removeAllItems();
        dungeonFilter.addItem("All Dungeons");

        Integer selectedCharId = getSelectedCharacterId();
        if (selectedCharId == null) {
            // If no character selected, show all dungeons from all characters
            HashSet<String> uniqueMaps = new HashSet<>();
            HashMap<Integer, List<MapFameData>> mapData =
                session.getCharacterMapFameData();
            for (List<MapFameData> entries : mapData.values()) {
                if (entries != null) {
                    for (MapFameData mapFame : entries) {
                        uniqueMaps.add(mapFame.mapName);
                    }
                }
            }

            // Add sorted unique map names to filter
            ArrayList<String> sortedMaps = new ArrayList<>(uniqueMaps);
            Collections.sort(sortedMaps);
            for (String mapName : sortedMaps) {
                dungeonFilter.addItem(mapName);
            }
        } else {
            // Get dungeons only for the selected character
            HashSet<String> uniqueMaps = new HashSet<>();
            HashMap<Integer, List<MapFameData>> mapData =
                session.getCharacterMapFameData();
            List<MapFameData> entries = mapData.get(selectedCharId);

            if (entries != null) {
                for (MapFameData mapFame : entries) {
                    uniqueMaps.add(mapFame.mapName);
                }
            }

            // Add sorted unique map names to filter
            ArrayList<String> sortedMaps = new ArrayList<>(uniqueMaps);
            Collections.sort(sortedMaps);
            for (String mapName : sortedMaps) {
                dungeonFilter.addItem(mapName);
            }
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
            // Skip if character filter is active and this isn't the selected character
            if (selectedCharId != null && !charId.equals(selectedCharId)) {
                continue;
            }

            List<MapFameData> entries = mapData.get(charId);
            if (entries != null) {
                String className = getClassNameForCharacter(charId);
                for (MapFameData mapFame : entries) {
                    // Skip if dungeon filter is active and this isn't the selected dungeon
                    if (
                        !showAllDungeons &&
                        !mapFame.mapName.equals(selectedDungeon)
                    ) {
                        continue;
                    }

                    long timeSpent = mapFame.endTime - mapFame.startTime;
                    double minutesSpent = timeSpent / 60000.0; // ms to minutes
                    double famePerMinute = minutesSpent > 0
                        ? mapFame.getFameGained() / minutesSpent
                        : 0;

                    if (mapFame.getFameGained() > 0) {
                        model.addRow(
                            new Object[] {
                                className,
                                mapFame.mapName,
                                mapFame.getFameGained(),
                                formatTime(timeSpent),
                                String.format("%.1f", famePerMinute),
                            }
                        );
                    }
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

    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private String getClassNameForCharacter(int charId) {
        // Use stored class name from session data, fallback to live data if not available
        String storedClassName = session.getCharacterClassNames().get(charId);
        if (storedClassName != null) {
            return storedClassName;
        }
        // Fallback to live character data if class name wasn't stored in session
        return FameTablePanel.getInstance().getClassNameForCharacterId(charId);
    }

    private void updateCharacterData() {
        Integer selectedCharId = getSelectedCharacterId();
        if (selectedCharId != null) {
            List<Fame> fameData = session
                .getCharacterFameData()
                .get(selectedCharId);
            if (fameData != null) {
                graphTabPanel.setScores(new ArrayList<>(fameData));
            }
        }
        // Update dungeon filter and map data when character selection changes
        populateDungeonFilter();
        updateMapFameData();
    }

    /**
     * Extract character ID from the selected dropdown item
     */
    private Integer getSelectedCharacterId() {
        String selectedItem = (String) characterSelector.getSelectedItem();
        if (selectedItem == null) {
            return null;
        }

        // Extract character ID from format "ClassName (ID: 123)"
        try {
            int startIndex = selectedItem.lastIndexOf("(ID: ") + 5;
            int endIndex = selectedItem.lastIndexOf(")");
            if (startIndex > 0 && endIndex > startIndex) {
                String idStr = selectedItem
                    .substring(startIndex, endIndex)
                    .trim();
                return Integer.parseInt(idStr);
            }
        } catch (Exception e) {
            // If parsing fails, return null
        }
        return null;
    }

    // Inner class for the graph tab panel that mimics GraphPanel functionality
    private class GraphTabPanel
        extends JPanel
        implements MouseMotionListener, MouseListener {

        private static final int POINT_SIZE = 4;
        private static final int NUMBER_Y_DIVISIONS = 10;
        private final int padding = 25;
        private final int labelPadding = 25;
        private ArrayList<Fame> scores;
        private int screenX;
        private Fame dragLeft;
        private Fame dragRight;
        private double hoverFame = -1;
        private boolean pressed;
        private int rightSelectionValue;
        private int leftSelectionValue;

        public GraphTabPanel() {
            addMouseMotionListener(this);
            addMouseListener(this);
            scores = new ArrayList<>();
            setPreferredSize(new Dimension(800, 400));
        }

        public void setScores(ArrayList<Fame> scores) {
            this.scores = scores;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );

            if (scores.isEmpty()) {
                g2.setColor(Color.GRAY);
                g2.drawString(
                    "No data available",
                    getWidth() / 2 - 40,
                    getHeight() / 2
                );
                return;
            }

            MinMax minMax = getMinMaxScore();

            double xScale =
                ((double) getWidth() - (2 * padding) - labelPadding) /
                (minMax.maxScoreX - minMax.minScoreX);
            double yScale =
                ((double) getHeight() - 2 * padding - labelPadding) /
                (minMax.maxScoreY - minMax.minScoreY);

            List<Point> graphPoints = new ArrayList<>();
            int size = scores.size();
            for (Fame score : scores) {
                double v = score.getTime() - minMax.minScoreX;
                int x1 = (int) (v * xScale + padding + labelPadding);
                double v1 = minMax.maxScoreY - score.getFame();
                int y1 = (int) (v1 * yScale + padding);
                Point e = new Point(x1, y1);
                graphPoints.add(e);
            }

            g2.setColor(GRID_COLOR);

            // Draw grid lines for y axis
            for (int i = 0; i < NUMBER_Y_DIVISIONS + 1; i++) {
                int y0 =
                    getHeight() -
                    ((i * (getHeight() - padding * 2 - labelPadding)) /
                            NUMBER_Y_DIVISIONS +
                        padding +
                        labelPadding);
                if (size > 0) {
                    g2.drawLine(
                        padding + labelPadding + 1 + POINT_SIZE,
                        y0,
                        getWidth() - padding,
                        y0
                    );
                }
            }

            // Draw axes
            g2.setColor(AXIS_COLOR);
            g2.drawLine(
                padding + labelPadding,
                getHeight() - padding - labelPadding,
                padding + labelPadding,
                padding
            );
            g2.drawLine(
                padding + labelPadding,
                getHeight() - padding - labelPadding,
                getWidth() - padding,
                getHeight() - padding - labelPadding
            );

            // Draw data lines
            Stroke oldStroke = g2.getStroke();
            g2.setColor(LINE_COLOR);
            g2.setStroke(GRAPH_STROKE);
            for (int i = 0; i < graphPoints.size() - 1; i++) {
                int x1 = graphPoints.get(i).x;
                int y1 = graphPoints.get(i).y;
                int x2 = graphPoints.get(i + 1).x;
                int y2 = graphPoints.get(i + 1).y;
                g2.drawLine(x1, y1, x2, y1);
                g2.drawLine(x2, y1, x2, y2);
            }

            g2.setStroke(oldStroke);
            g2.setColor(POINT_COLOR);
            for (Point graphPoint : graphPoints) {
                int x = graphPoint.x - POINT_SIZE / 2;
                int y = graphPoint.y - POINT_SIZE / 2;
                g2.fillOval(x, y, POINT_SIZE, POINT_SIZE);
            }

            // Draw selection and hover info
            if (pressed && dragLeft != null && dragRight != null) {
                g2.setColor(new Color(160, 180, 240, 100));
                int leftX = graphPoints.get(leftSelectionValue).x;
                int rightX = graphPoints.get(rightSelectionValue).x;
                g2.fillRect(
                    leftX,
                    padding,
                    Math.abs(leftX - rightX),
                    getHeight() - 2 * padding - labelPadding
                );

                g2.setColor(AXIS_COLOR);
                double dfame = Math.abs(
                    dragLeft.getFame() - dragRight.getFame()
                );
                long dtime = Math.abs(dragLeft.getTime() - dragRight.getTime());
                double fpm = (dfame / (dtime / 60000f));
                g2.drawString(
                    String.format("Selected fame: %.0f", dfame),
                    padding + 5,
                    padding + 15
                );
                g2.drawString(
                    String.format("Time: %.1f min", dtime / 60000f),
                    padding + 5,
                    padding + 30
                );
                g2.drawString(
                    String.format("Fame/Min: %.1f", fpm),
                    padding + 5,
                    padding + 45
                );
            }
        }

        private MinMax getMinMaxScore() {
            MinMax minMax = new MinMax();
            if (scores.isEmpty()) return minMax;

            minMax.minScoreX = scores.get(0).getTime();
            minMax.maxScoreX = scores.get(0).getTime();
            minMax.minScoreY = scores.get(0).getFame();
            minMax.maxScoreY = scores.get(0).getFame();

            for (Fame score : scores) {
                minMax.minScoreX = Math.min(minMax.minScoreX, score.getTime());
                minMax.maxScoreX = Math.max(minMax.maxScoreX, score.getTime());
                minMax.minScoreY = Math.min(minMax.minScoreY, score.getFame());
                minMax.maxScoreY = Math.max(minMax.maxScoreY, score.getFame());
            }

            return minMax;
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            screenX = e.getX();
            if (pressed && !scores.isEmpty()) {
                rightSelectionValue = findClosestPoint(screenX);
                dragRight = scores.get(rightSelectionValue);
                repaint();
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            screenX = e.getX();
            if (!scores.isEmpty()) {
                int index = findClosestPoint(screenX);
                if (index >= 0 && index < scores.size()) {
                    hoverFame = scores.get(index).getFame();
                    repaint();
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            pressed = true;
            if (!scores.isEmpty()) {
                leftSelectionValue = findClosestPoint(e.getX());
                dragLeft = scores.get(leftSelectionValue);
                rightSelectionValue = leftSelectionValue;
                dragRight = dragLeft;
                repaint();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            pressed = false;
        }

        @Override
        public void mouseClicked(MouseEvent e) {}

        @Override
        public void mouseEntered(MouseEvent e) {}

        @Override
        public void mouseExited(MouseEvent e) {}

        private int findClosestPoint(int x) {
            if (scores.isEmpty()) return -1;

            MinMax minMax = getMinMaxScore();
            double xScale =
                ((double) getWidth() - (2 * padding) - labelPadding) /
                (minMax.maxScoreX - minMax.minScoreX);

            int closestIndex = 0;
            double minDistance = Double.MAX_VALUE;

            for (int i = 0; i < scores.size(); i++) {
                Fame score = scores.get(i);
                double pointX =
                    padding +
                    labelPadding +
                    (score.getTime() - minMax.minScoreX) * xScale;
                double distance = Math.abs(pointX - x);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestIndex = i;
                }
            }

            return closestIndex;
        }

        private class MinMax {

            long minScoreX;
            long maxScoreX;
            double minScoreY;
            double maxScoreY;
        }

        private class Point {

            int x, y;

            Point(int x, int y) {
                this.x = x;
                this.y = y;
            }
        }
    }

    public static void openSessionViewer() {
        FameSession session = FameSessionManager.loadSession();
        if (session != null) {
            SwingUtilities.invokeLater(() -> {
                FameSessionViewer viewer = new FameSessionViewer(session);
                viewer.setVisible(true);
            });
        }
    }
}
