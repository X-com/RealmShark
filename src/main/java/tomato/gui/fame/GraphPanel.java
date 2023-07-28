package tomato.gui.fame;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class GraphPanel extends JPanel implements MouseMotionListener {

    private int padding = 25;
    private int labelPadding = 25;
    private Color lineColor = new Color(44, 102, 230, 180);
    private Color pointColor = new Color(100, 100, 100, 180);
    private Color gridColor = new Color(200, 200, 200, 200);
    private static final Stroke GRAPH_STROKE = new BasicStroke(2f);
    private int pointWidth = 4;
    private int numberYDivisions = 10;
    private ArrayList<Fame> scores;
    private int screenX;
    private int screenXdragLeft;
    private int screenXdragRight;
    private Fame dragLeft;
    private Fame dragRight;
    private double hoverFame = -1;
    private boolean pressed;

    public GraphPanel(ArrayList<Fame> scores) {
        addMouseMotionListener(this);
        this.scores = scores;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        MinMax minMax = getMinMaxScore();
        double xScale = ((double) getWidth() - (2 * padding) - labelPadding) / (minMax.maxScoreX - minMax.minScoreX);
        double yScale = ((double) getHeight() - 2 * padding - labelPadding) / (minMax.maxScoreY - minMax.minScoreY);

        List<Point> graphPoints = new ArrayList<>();
        int size = scores.size();
        for (int i = 0; i < size; i++) {
            double v = scores.get(i).time - minMax.minScoreX;
            int x1 = (int) (v * xScale + padding + labelPadding);
            double v1 = minMax.maxScoreY - scores.get(i).fame;
            int y1 = (int) (v1 * yScale + padding);
            Point e = new Point(x1, y1);
            graphPoints.add(e);
        }

        // draw white background
        g2.setColor(Color.WHITE);
        g2.fillRect(padding + labelPadding, padding, getWidth() - (2 * padding) - labelPadding, getHeight() - 2 * padding - labelPadding);
        g2.setColor(Color.BLACK);

        // draw selection box
        int xWidth = getWidth() - labelPadding;
        int x3 = padding * 2 + labelPadding - 12;
        int y3 = padding + labelPadding - 5;
        if (pressed) {
            g2.setColor(new Color(160, 180, 240));
            int w = screenXdragRight - screenXdragLeft;
            int x;
            if (w > 0) {
                x = screenXdragLeft;
            } else {
                x = screenXdragRight;
            }
            String s = String.format("Fame / Min: %.3f", (Math.abs(dragLeft.fame - dragRight.fame) / (Math.abs(dragLeft.time - dragRight.time) / 60000f)));
            g2.fillRect(x, padding, Math.abs(w), getHeight() - 2 * padding - labelPadding);
            g2.setColor(Color.BLACK);

            g2.drawString(s, x3, y3);
        } else if (hoverFame >= 0) {
            g2.drawLine(screenX, padding, screenX, getHeight() - padding - labelPadding);
            String s = String.format("Fame: %.0f", hoverFame);
            g2.drawString(s, x3, y3);
        }

        // create hatch marks and grid lines for y axis.
        for (int i = 0; i < numberYDivisions + 1; i++) {
            int x0 = padding + labelPadding;
            int x1 = pointWidth + padding + labelPadding;
            int y0 = getHeight() - ((i * (getHeight() - padding * 2 - labelPadding)) / numberYDivisions + padding + labelPadding);
            int y1 = y0;
            if (size > 0) {
                g2.setColor(gridColor);
                g2.drawLine(padding + labelPadding + 1 + pointWidth, y0, getWidth() - padding, y1);
                g2.setColor(Color.BLACK);
                String yLabel = String.valueOf(((int) ((minMax.minScoreY + (minMax.maxScoreY - minMax.minScoreY) * ((i * 1.0) / numberYDivisions)) * 100)) / 100);
                FontMetrics metrics = g2.getFontMetrics();
                int labelWidth = metrics.stringWidth(yLabel);
                g2.drawString(yLabel, x0 - labelWidth - 5, y0 + (metrics.getHeight() / 2) - 3);
            }
            g2.drawLine(x0, y0, x1, y1);
        }

        int seconds = (int) (minMax.maxScoreX - minMax.minScoreX) / 1000;
        int minutes = seconds / 60;

        int span;
        int display;
        String timeString;
        if (minutes < 10) {
            span = 60;
            display = 1;
            timeString = "(min)";
        } else if (minutes < 60) {
            span = 600;
            display = 10;
            timeString = "(min)";
        } else if (minutes < 120) {
            span = 1200;
            display = 30;
            timeString = "(min)";
        } else {
            span = 3600;
            display = 1;
            timeString = "(hour)";
        }
        g2.drawString(timeString, xWidth - padding - 9, getHeight() - padding - labelPadding - 5);

        float fraction = (float) span / seconds;

        // and for x axis
        for (int i = 0; i < 100; i++) {
            if (size > 1) {
                int x0 = (int) (((i * (getWidth() - padding * 2 - labelPadding)) * fraction) + padding + labelPadding);
                if (x0 >= xWidth) break;
                int x1 = x0;
                int y0 = getHeight() - padding - labelPadding;
                int y1 = y0 - pointWidth;
//                if ((i % ((int) ((size / 5.0)) + 1)) == 0) {
                if (scores.size() > 0) {
                    g2.setColor(gridColor);
                    g2.drawLine(x0, getHeight() - padding - labelPadding - 1 - pointWidth, x1, padding);
                    g2.setColor(Color.BLACK);
                    int sec = display * i;
                    FontMetrics metrics = g2.getFontMetrics();
                    String xLabel = Integer.toString(sec);
                    int labelWidth = metrics.stringWidth(xLabel);
                    g2.drawString(xLabel, x0 - labelWidth / 2, y0 + metrics.getHeight() + 3);
                }
                g2.drawLine(x0, y0, x1, y1);
            }
        }

        // create x and y axes
        g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, padding + labelPadding, padding);
        g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, getWidth() - padding, getHeight() - padding - labelPadding);

        Stroke oldStroke = g2.getStroke();
        g2.setColor(lineColor);
        g2.setStroke(GRAPH_STROKE);
        for (int i = 0; i < graphPoints.size() - 1; i++) {
            int x1 = graphPoints.get(i).x;
            int y1 = graphPoints.get(i).y;
            int x2 = graphPoints.get(i + 1).x;
            int y2 = graphPoints.get(i + 1).y;
            g2.drawLine(x1, y1, x2, y2);
        }

        g2.setStroke(oldStroke);
        g2.setColor(pointColor);
        for (int i = 0; i < graphPoints.size(); i++) {
            int x = graphPoints.get(i).x - pointWidth / 2;
            int y = graphPoints.get(i).y - pointWidth / 2;
            int ovalW = pointWidth;
            int ovalH = pointWidth;
            g2.fillOval(x, y, ovalW, ovalH);
        }
    }

    private MinMax getMinMaxScore() {
        MinMax minMax = new MinMax();
        for (Fame score : scores) {
            minMax.minScoreX = Math.min(minMax.minScoreX, score.time);
            minMax.minScoreY = Math.min(minMax.minScoreY, score.fame);
            minMax.maxScoreX = Math.max(minMax.maxScoreX, score.time);
            minMax.maxScoreY = Math.max(minMax.maxScoreY, score.fame);
        }
        return minMax;
    }

    public void setScores(ArrayList<Fame> scores) {
        this.scores = scores;
        invalidate();
        this.repaint();
    }

    public ArrayList<Fame> getScores() {
        return scores;
    }

    private static void createAndShowGui() {
        ArrayList<Fame> scores = new ArrayList<>();
        Random random = new Random();
        int maxDataPoints = 400;
        int maxScore = 10000;
        int sumy = 0;
        int sumx = 0;
        for (int i = 0; i < maxDataPoints; i++) {
            sumy += random.nextDouble() * maxScore;
            sumx += random.nextDouble() * maxScore * 3;
            scores.add(new Fame(sumx, sumy));
        }
        GraphPanel mainPanel = new GraphPanel(scores);
        mainPanel.setPreferredSize(new Dimension(800, 600));
        JFrame frame = new JFrame("DrawGraph");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GraphPanel::createAndShowGui);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (!pressed) {
            screenXdragLeft = e.getX();
            dragLeft = getRange(screenXdragLeft);
        }
        if (dragLeft != null) pressed = true;
        screenXdragRight = e.getX();
        dragRight = getRange(screenXdragRight);
        if (dragRight == null) pressed = false;
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        pressed = false;
        screenX = e.getX();
        Fame f = getRange(screenX);
        hoverFame = -1;
        if (f != null) {
            hoverFame = f.fame;
        }
        repaint();
    }

    public Fame getRange(int x) {
        float dx = (float) (x - padding - labelPadding) / (getWidth() - labelPadding * 3);
        if (dx < 0 || dx > 1) {
            return null;
        }
        MinMax m = getMinMaxScore();
        float ddx = (float) (m.maxScoreX - m.minScoreX) * dx;

        Fame next = null;
        for (Fame f : scores) {
            if ((f.time - m.minScoreX) > ddx) {
                break;
            }
            next = f;
        }
        return next;
    }

    private static class MinMax {
        double minScoreX = Double.MAX_VALUE;
        double minScoreY = Double.MAX_VALUE;
        double maxScoreX = Double.MIN_VALUE;
        double maxScoreY = Double.MIN_VALUE;
    }
}