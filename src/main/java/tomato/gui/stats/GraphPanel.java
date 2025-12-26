package tomato.gui.stats;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 * Reusable graph panel for displaying fame data over time.
 * Can be used for both live tracking and session viewing.
 */
public class GraphPanel
    extends JPanel
    implements MouseMotionListener, MouseListener {

    // Layout constants
    private static final int POINT_SIZE = 4;
    private static final int NUMBER_Y_DIVISIONS = 10;
    private static final int PADDING = 25;
    private static final int LABEL_PADDING = 25;

    // Selection info display constants
    private static final int INFO_X_OFFSET = 12;
    private static final int INFO_Y_OFFSET = 5;
    private static final int INFO_LINE_HEIGHT = 14;

    // Time constants
    private static final int SECONDS_PER_MINUTE = 60;
    private static final int MILLISECONDS_PER_MINUTE = 60000;
    private static final int MILLISECONDS_PER_HOUR = 3600000;
    private static final int MINUTES_PER_HOUR = 60;

    // Colors
    private static final Color LINE_COLOR = new Color(44, 102, 230, 180);
    private static final Color POINT_COLOR = new Color(100, 100, 100, 180);
    private static final Color GRID_COLOR = new Color(200, 200, 200, 200);
    private static final Color SELECTION_COLOR = new Color(160, 180, 240);
    private static final Color SELECTION_FILL_COLOR = new Color(
        160,
        180,
        240,
        100
    );
    private static final Stroke GRAPH_STROKE = new BasicStroke(2f);

    // Data
    private ArrayList<Fame> scores;
    private ArrayList<Fame> originalScores;

    // Mouse interaction state
    private int screenX;
    private Fame dragLeft;
    private Fame dragRight;
    private double hoverFame = -1;
    private boolean pressed;
    private int rightSelectionValue;
    private int leftSelectionValue;

    // Button-based selection (for time range filtering)
    private Fame buttonDragLeft;
    private Fame buttonDragRight;
    private boolean buttonSelectionActive;

    // Time filtering
    private long currentTimeFilter = -1;
    private boolean showTimeRangeDropdown;
    private JComboBox<String> timeRangeDropdown;

    /**
     * Creates a graph panel with time range dropdown for live tracking.
     */
    public GraphPanel(ArrayList<Fame> scores) {
        this(scores, true);
    }

    /**
     * Creates a graph panel with optional time range dropdown.
     *
     * @param scores               Initial fame data
     * @param showTimeRangeDropdown Whether to show the time range filter dropdown
     */
    public GraphPanel(ArrayList<Fame> scores, boolean showTimeRangeDropdown) {
        this.scores = scores != null ? scores : new ArrayList<>();
        this.originalScores = new ArrayList<>(this.scores);
        this.showTimeRangeDropdown = showTimeRangeDropdown;

        addMouseMotionListener(this);
        addMouseListener(this);
        setLayout(new BorderLayout());

        if (showTimeRangeDropdown) {
            createTimeRangeDropdown();
        }
    }

    /**
     * Creates a minimal graph panel for embedding (e.g., in session viewer).
     */
    public static GraphPanel createMinimal() {
        return new GraphPanel(new ArrayList<>(), false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );

        if (scores == null || scores.isEmpty()) {
            drawEmptyState(g2);
            return;
        }

        MinMax minMax = getMinMaxScore();
        if (minMax.maxScoreX == minMax.minScoreX) {
            drawEmptyState(g2);
            return;
        }

        List<Point> graphPoints = calculateGraphPoints(minMax);
        drawGrid(g2, minMax);
        drawAxes(g2, minMax);
        drawSelectionAndInfo(g2, minMax, graphPoints);
        drawDataLines(g2, graphPoints);
        drawDataPoints(g2, graphPoints);
    }

    private void drawEmptyState(Graphics2D g2) {
        g2.setColor(Color.GRAY);
        String msg = "No data available";
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(msg)) / 2;
        int y = getHeight() / 2;
        g2.drawString(msg, x, y);
    }

    private List<Point> calculateGraphPoints(MinMax minMax) {
        double xScale =
            ((double) getWidth() - (2 * PADDING) - LABEL_PADDING) /
            (minMax.maxScoreX - minMax.minScoreX);
        double yScale =
            ((double) getHeight() - 2 * PADDING - LABEL_PADDING) /
            (minMax.maxScoreY - minMax.minScoreY);

        List<Point> graphPoints = new ArrayList<>();
        for (Fame score : scores) {
            int x = (int) ((score.getTime() - minMax.minScoreX) * xScale +
                PADDING +
                LABEL_PADDING);
            int y = (int) ((minMax.maxScoreY - score.getFame()) * yScale +
                PADDING);
            graphPoints.add(new Point(x, y));
        }
        return graphPoints;
    }

    private void drawGrid(Graphics2D g2, MinMax minMax) {
        g2.setColor(GRID_COLOR);

        // Y-axis grid lines
        for (int i = 0; i <= NUMBER_Y_DIVISIONS; i++) {
            int y =
                getHeight() -
                ((i * (getHeight() - PADDING * 2 - LABEL_PADDING)) /
                        NUMBER_Y_DIVISIONS +
                    PADDING +
                    LABEL_PADDING);
            if (!scores.isEmpty()) {
                g2.drawLine(
                    PADDING + LABEL_PADDING + 1 + POINT_SIZE,
                    y,
                    getWidth() - PADDING,
                    y
                );
            }
        }
    }

    private void drawAxes(Graphics2D g2, MinMax minMax) {
        int xWidth = getWidth() - LABEL_PADDING;

        // Y-axis labels and tick marks
        g2.setColor(Color.GRAY);
        for (int i = 0; i <= NUMBER_Y_DIVISIONS; i++) {
            int x0 = PADDING + LABEL_PADDING;
            int x1 = POINT_SIZE + PADDING + LABEL_PADDING;
            int y0 =
                getHeight() -
                ((i * (getHeight() - PADDING * 2 - LABEL_PADDING)) /
                        NUMBER_Y_DIVISIONS +
                    PADDING +
                    LABEL_PADDING);

            if (!scores.isEmpty()) {
                double value =
                    minMax.minScoreY +
                    (minMax.maxScoreY - minMax.minScoreY) *
                    ((i * 1.0) / NUMBER_Y_DIVISIONS);
                String yLabel = String.valueOf((int) (value * 100) / 100);
                FontMetrics metrics = g2.getFontMetrics();
                int labelWidth = metrics.stringWidth(yLabel);
                g2.drawString(
                    yLabel,
                    x0 - labelWidth - 5,
                    y0 + (metrics.getHeight() / 2) - 3
                );
            }
            g2.drawLine(x0, y0, x1, y0);
        }

        // X-axis labels
        drawXAxisLabels(g2, minMax, xWidth);

        // Draw axes lines
        g2.drawLine(
            PADDING + LABEL_PADDING,
            getHeight() - PADDING - LABEL_PADDING,
            PADDING + LABEL_PADDING,
            PADDING
        );
        g2.drawLine(
            PADDING + LABEL_PADDING,
            getHeight() - PADDING - LABEL_PADDING,
            getWidth() - PADDING,
            getHeight() - PADDING - LABEL_PADDING
        );
    }

    private void drawXAxisLabels(Graphics2D g2, MinMax minMax, int xWidth) {
        int seconds = (int) (minMax.maxScoreX - minMax.minScoreX) / 1000;
        int minutes = seconds / SECONDS_PER_MINUTE;
        int hours = minutes / SECONDS_PER_MINUTE;

        int span;
        int display;
        String timeString;

        if (minutes < 10) {
            span = SECONDS_PER_MINUTE;
            display = 1;
            timeString = "(min)";
        } else if (minutes < SECONDS_PER_MINUTE) {
            span = SECONDS_PER_MINUTE * 10;
            display = 10;
            timeString = "(min)";
        } else if (minutes < 120) {
            span = SECONDS_PER_MINUTE * 20;
            display = 30;
            timeString = "(min)";
        } else if (hours < 20) {
            span = MILLISECONDS_PER_HOUR / 1000;
            display = 1;
            timeString = "(hour)";
        } else if (hours < SECONDS_PER_MINUTE) {
            display = 3;
            span = (MILLISECONDS_PER_HOUR / 1000) * display;
            timeString = "(hour)";
        } else {
            display = 9;
            span = (MILLISECONDS_PER_HOUR / 1000) * display;
            timeString = "(hour)";
        }

        g2.drawString(
            timeString,
            xWidth - PADDING - 9,
            getHeight() - PADDING - LABEL_PADDING - 5
        );

        if (seconds == 0) return;
        float fraction = (float) span / seconds;

        for (int i = 0; i < 100; i++) {
            if (scores.size() > 1) {
                int x0 = (int) (((i *
                            (getWidth() - PADDING * 2 - LABEL_PADDING)) *
                        fraction) +
                    PADDING +
                    LABEL_PADDING);
                if (x0 >= xWidth) break;
                int y0 = getHeight() - PADDING - LABEL_PADDING;
                int y1 = y0 - POINT_SIZE;

                if (!scores.isEmpty()) {
                    g2.setColor(GRID_COLOR);
                    g2.drawLine(
                        x0,
                        getHeight() - PADDING - LABEL_PADDING - 1 - POINT_SIZE,
                        x0,
                        PADDING
                    );
                    g2.setColor(Color.GRAY);
                    int sec = display * i;
                    FontMetrics metrics = g2.getFontMetrics();
                    String xLabel = Integer.toString(sec);
                    int labelWidth = metrics.stringWidth(xLabel);
                    g2.drawString(
                        xLabel,
                        x0 - labelWidth / 2,
                        y0 + metrics.getHeight() + 3
                    );
                }
                g2.drawLine(x0, y0, x0, y1);
            }
        }
    }

    private void drawSelectionAndInfo(
        Graphics2D g2,
        MinMax minMax,
        List<Point> graphPoints
    ) {
        int x3 = PADDING * 2 + LABEL_PADDING - INFO_X_OFFSET;
        int y3 = PADDING + LABEL_PADDING + INFO_Y_OFFSET;

        if (pressed && dragLeft != null && dragRight != null) {
            // Mouse drag selection
            drawSelection(
                g2,
                graphPoints,
                dragLeft,
                dragRight,
                leftSelectionValue,
                rightSelectionValue,
                x3,
                y3
            );
        } else if (
            buttonSelectionActive &&
            buttonDragLeft != null &&
            buttonDragRight != null
        ) {
            // Button-based selection (time filter)
            drawButtonSelection(
                g2,
                minMax,
                buttonDragLeft,
                buttonDragRight,
                x3,
                y3
            );
        } else if (hoverFame >= 0) {
            // Hover info
            drawHoverInfo(g2, x3, y3);
        } else {
            // Default: show overall fame/min
            drawOverallInfo(g2, x3, y3);
        }
    }

    private void drawSelection(
        Graphics2D g2,
        List<Point> graphPoints,
        Fame left,
        Fame right,
        int leftIdx,
        int rightIdx,
        int x3,
        int y3
    ) {
        g2.setColor(SELECTION_COLOR);
        double dfame = Math.abs(left.getFame() - right.getFame());
        long dtime = Math.abs(left.getTime() - right.getTime());
        double famePerMinute = dtime > 0
            ? dfame / (dtime / (double) MILLISECONDS_PER_MINUTE)
            : 0;

        String s1 = String.format("Selected fame: %.0f", dfame);
        String s2 = String.format(
            "Selected time: %.2f min ( %.1f h )",
            dtime / (double) MILLISECONDS_PER_MINUTE,
            dtime / (double) MILLISECONDS_PER_HOUR
        );
        String s3 = String.format(
            "Fame / Min: %.3f ( %.1f f/h )",
            famePerMinute,
            famePerMinute * MINUTES_PER_HOUR
        );

        int leftX = graphPoints.get(leftIdx).x;
        int rightX = graphPoints.get(rightIdx).x;
        int selectionWidth = Math.abs(leftX - rightX);

        g2.fillRect(
            Math.min(leftX, rightX),
            PADDING,
            selectionWidth,
            getHeight() - 2 * PADDING - LABEL_PADDING
        );
        g2.setColor(Color.GRAY);

        g2.drawString(s1, x3, y3);
        g2.drawString(s2, x3, y3 + INFO_LINE_HEIGHT);
        g2.drawString(s3, x3, y3 + (INFO_LINE_HEIGHT * 2));
        g2.drawLine(
            screenX,
            PADDING,
            screenX,
            getHeight() - PADDING - LABEL_PADDING
        );
    }

    private void drawButtonSelection(
        Graphics2D g2,
        MinMax minMax,
        Fame left,
        Fame right,
        int x3,
        int y3
    ) {
        double xScale =
            ((double) getWidth() - (2 * PADDING) - LABEL_PADDING) /
            (minMax.maxScoreX - minMax.minScoreX);

        g2.setColor(SELECTION_FILL_COLOR);
        double dfame = Math.abs(left.getFame() - right.getFame());
        long dtime = Math.abs(left.getTime() - right.getTime());
        double famePerMinute = dtime > 0
            ? dfame / (dtime / (double) MILLISECONDS_PER_MINUTE)
            : 0;

        String s1 = String.format("Selected fame: %.0f", dfame);
        String s2 = String.format(
            "Selected time: %.2f min ( %.1f h )",
            dtime / (double) MILLISECONDS_PER_MINUTE,
            dtime / (double) MILLISECONDS_PER_HOUR
        );
        String s3 = String.format(
            "Fame / Min: %.3f ( %.1f f/h )",
            famePerMinute,
            famePerMinute * MINUTES_PER_HOUR
        );

        int leftX = (int) ((left.getTime() - minMax.minScoreX) * xScale +
            PADDING +
            LABEL_PADDING);
        int rightX = (int) ((right.getTime() - minMax.minScoreX) * xScale +
            PADDING +
            LABEL_PADDING);
        int selectionWidth = Math.abs(leftX - rightX);

        g2.fillRect(
            Math.min(leftX, rightX),
            PADDING,
            selectionWidth,
            getHeight() - 2 * PADDING - LABEL_PADDING
        );
        g2.setColor(Color.GRAY);

        g2.drawString(s1, x3, y3);
        g2.drawString(s2, x3, y3 + INFO_LINE_HEIGHT);
        g2.drawString(s3, x3, y3 + (INFO_LINE_HEIGHT * 2));
    }

    private void drawHoverInfo(Graphics2D g2, int x3, int y3) {
        g2.setColor(Color.GRAY);
        g2.drawLine(
            screenX,
            PADDING,
            screenX,
            getHeight() - PADDING - LABEL_PADDING
        );
        g2.drawString(String.format("Fame: %.0f", hoverFame), x3, y3);

        double overallFpm = calculateOverallFamePerMin();
        g2.drawString(
            String.format(
                "Fame / Min: %.3f ( %.1f f/h )",
                overallFpm,
                overallFpm * MINUTES_PER_HOUR
            ),
            x3,
            y3 + INFO_LINE_HEIGHT
        );
    }

    private void drawOverallInfo(Graphics2D g2, int x3, int y3) {
        g2.setColor(Color.GRAY);
        double overallFpm = calculateOverallFamePerMin();
        g2.drawString(
            String.format(
                "Fame / Min: %.3f ( %.1f f/h )",
                overallFpm,
                overallFpm * MINUTES_PER_HOUR
            ),
            x3,
            y3 + INFO_LINE_HEIGHT
        );
    }

    private void drawDataLines(Graphics2D g2, List<Point> graphPoints) {
        if (graphPoints.size() < 2) return;

        Stroke oldStroke = g2.getStroke();
        g2.setColor(LINE_COLOR);
        g2.setStroke(GRAPH_STROKE);

        for (int i = 0; i < graphPoints.size() - 1; i++) {
            Point p1 = graphPoints.get(i);
            Point p2 = graphPoints.get(i + 1);
            // Step-style line (horizontal then vertical)
            g2.drawLine(p1.x, p1.y, p2.x, p1.y);
            g2.drawLine(p2.x, p1.y, p2.x, p2.y);
        }

        g2.setStroke(oldStroke);
    }

    private void drawDataPoints(Graphics2D g2, List<Point> graphPoints) {
        g2.setColor(POINT_COLOR);
        for (Point p : graphPoints) {
            int x = p.x - POINT_SIZE / 2;
            int y = p.y - POINT_SIZE / 2;
            g2.fillOval(x, y, POINT_SIZE, POINT_SIZE);
        }
    }

    private MinMax getMinMaxScore() {
        MinMax minMax = new MinMax();
        if (scores == null || scores.isEmpty()) return minMax;

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

    private double calculateOverallFamePerMin() {
        if (scores == null || scores.size() < 2) return 0.0;

        double totalFame =
            scores.get(scores.size() - 1).getFame() - scores.get(0).getFame();
        long totalTime =
            scores.get(scores.size() - 1).getTime() - scores.get(0).getTime();

        return totalTime > 0
            ? totalFame / (totalTime / (double) MILLISECONDS_PER_MINUTE)
            : 0.0;
    }

    // --- Public API ---

    /**
     * Sets the fame data to display.
     */
    public void setScores(ArrayList<Fame> scores) {
        this.scores = scores != null ? scores : new ArrayList<>();
        this.originalScores = new ArrayList<>(this.scores);

        if (currentTimeFilter > 0) {
            filterData(currentTimeFilter);
        } else {
            repaint();
        }
    }

    /**
     * Gets the current scores being displayed.
     */
    public ArrayList<Fame> getScores() {
        return scores;
    }

    /**
     * Clears all data from the graph.
     */
    public void clearData() {
        this.scores = new ArrayList<>();
        this.originalScores = new ArrayList<>();
        this.buttonSelectionActive = false;
        this.currentTimeFilter = -1;
        repaint();
    }

    // --- Time Range Filtering ---

    private void createTimeRangeDropdown() {
        String[] timeRangeOptions = {
            "All",
            "1 min",
            "5 min",
            "10 min",
            "20 min",
            "30 min",
        };
        timeRangeDropdown = new JComboBox<>(timeRangeOptions);
        timeRangeDropdown.setFont(new Font("Arial", Font.PLAIN, 11));
        timeRangeDropdown.setPreferredSize(new Dimension(80, 25));

        timeRangeDropdown.addActionListener(e -> {
            String selected = (String) timeRangeDropdown.getSelectedItem();
            if (selected == null) return;

            switch (selected) {
                case "1 min":
                    filterData(MILLISECONDS_PER_MINUTE);
                    break;
                case "5 min":
                    filterData(5L * MILLISECONDS_PER_MINUTE);
                    break;
                case "10 min":
                    filterData(10L * MILLISECONDS_PER_MINUTE);
                    break;
                case "20 min":
                    filterData(20L * MILLISECONDS_PER_MINUTE);
                    break;
                case "30 min":
                    filterData(30L * MILLISECONDS_PER_MINUTE);
                    break;
                default:
                    showAllData();
                    break;
            }
        });

        JPanel dropdownPanel = new JPanel(
            new FlowLayout(FlowLayout.RIGHT, 10, 5)
        );
        dropdownPanel.setOpaque(false);
        dropdownPanel.add(new JLabel("Time Range:"));
        dropdownPanel.add(timeRangeDropdown);
        add(dropdownPanel, BorderLayout.SOUTH);
    }

    private void filterData(long timeRangeMs) {
        if (originalScores == null || originalScores.isEmpty()) return;

        long currentTime = System.currentTimeMillis();
        long cutoffTime = currentTime - timeRangeMs;

        ArrayList<Fame> filteredScores = new ArrayList<>();
        for (Fame fame : originalScores) {
            if (fame.getTime() >= cutoffTime) {
                filteredScores.add(fame);
            }
        }

        this.scores = filteredScores;
        this.currentTimeFilter = timeRangeMs;

        if (!filteredScores.isEmpty()) {
            buttonDragLeft = filteredScores.get(0);
            buttonDragRight = filteredScores.get(filteredScores.size() - 1);
            buttonSelectionActive = true;
        } else {
            buttonSelectionActive = false;
        }

        repaint();
    }

    private void showAllData() {
        if (originalScores != null) {
            this.scores = new ArrayList<>(originalScores);
            this.currentTimeFilter = -1;
            buttonSelectionActive = false;
            repaint();
        }
    }

    /**
     * Creates panel for time range buttons (kept for API compatibility).
     */
    public JPanel createTimeRangeButtons() {
        JPanel emptyPanel = new JPanel();
        emptyPanel.setOpaque(false);
        return emptyPanel;
    }

    // --- Mouse Interaction ---

    @Override
    public void mouseDragged(MouseEvent e) {
        if (!pressed) {
            int screenXdragLeft = e.getX();
            dragLeft = getRange(screenXdragLeft, false);
        }
        if (dragLeft != null) pressed = true;

        int screenXdragRight = e.getX();
        if (e.getX() > screenX) {
            dragRight = getRange(screenXdragRight, true);
        } else {
            Fame temp = dragLeft;
            dragLeft = getRange(screenXdragRight, false);
            dragRight = temp;
        }

        screenX = e.getX();
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        pressed = false;
        screenX = e.getX();
        Fame f = getRange(screenX, false);
        hoverFame = (f != null) ? f.getFame() : -1;
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        pressed = true;
        if (!scores.isEmpty()) {
            leftSelectionValue = findClosestPoint(e.getX());
            if (leftSelectionValue >= 0 && leftSelectionValue < scores.size()) {
                dragLeft = scores.get(leftSelectionValue);
                rightSelectionValue = leftSelectionValue;
                dragRight = dragLeft;
            }
        }
        repaint();
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
    public void mouseExited(MouseEvent e) {
        hoverFame = -1;
        repaint();
    }

    private Fame getRange(int x, boolean isRight) {
        if (scores == null || scores.isEmpty()) return null;

        float dx =
            (float) (x - PADDING - LABEL_PADDING) /
            (getWidth() - LABEL_PADDING * 3);
        if (dx < 0) {
            if (isRight) rightSelectionValue = 0;
            return scores.get(0);
        } else if (dx > 1) {
            if (isRight) rightSelectionValue = scores.size() - 1;
            return scores.get(scores.size() - 1);
        }

        MinMax m = getMinMaxScore();
        float ddx = (float) (m.maxScoreX - m.minScoreX) * dx;

        int f = 0;
        for (int i = 0; i < scores.size(); i++) {
            Fame fame = scores.get(i);
            if ((fame.getTime() - m.minScoreX) > ddx) break;
            f = i;
        }

        if (isRight) {
            f++;
            rightSelectionValue = Math.min(f, scores.size() - 1);
        } else {
            leftSelectionValue = f;
        }

        return f < scores.size()
            ? scores.get(f)
            : scores.get(scores.size() - 1);
    }

    private int findClosestPoint(int x) {
        if (scores == null || scores.isEmpty()) return -1;

        MinMax minMax = getMinMaxScore();
        if (minMax.maxScoreX == minMax.minScoreX) return 0;

        double xScale =
            ((double) getWidth() - (2 * PADDING) - LABEL_PADDING) /
            (minMax.maxScoreX - minMax.minScoreX);

        int closestIndex = 0;
        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < scores.size(); i++) {
            Fame score = scores.get(i);
            double pointX =
                PADDING +
                LABEL_PADDING +
                (score.getTime() - minMax.minScoreX) * xScale;
            double distance = Math.abs(pointX - x);
            if (distance < minDistance) {
                minDistance = distance;
                closestIndex = i;
            }
        }

        return closestIndex;
    }

    // --- Helper Classes ---

    private static class MinMax {

        double minScoreX = Double.MAX_VALUE;
        double minScoreY = Double.MAX_VALUE;
        double maxScoreX = Double.MIN_VALUE;
        double maxScoreY = Double.MIN_VALUE;
    }
}
