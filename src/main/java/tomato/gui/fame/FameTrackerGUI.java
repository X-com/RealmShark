package tomato.gui.fame;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class FameTrackerGUI extends JPanel {
    private static FameTrackerGUI INSTANCE;

    private final ArrayList<Fame> scores;
    private final HashMap<Integer, ArrayList<Fame>> fameList;
    private final GraphPanel graphPanel;

    public FameTrackerGUI() {
        INSTANCE = this;
        setLayout(new BorderLayout());

        scores = new ArrayList<>();
        fameList = new HashMap<>();

        graphPanel = new GraphPanel(scores);

//        JButton button = new JButton("Test");
//        button.addActionListener(e -> {
//            buttonLol();
//        });
//        add(button, BorderLayout.SOUTH);

        add(graphPanel);
    }

    private void buttonLol() {
        graphPanel.repaint();
    }

    public static void updateFame(int charId, long fame, long time) {
        INSTANCE.update(charId, fame, time);
    }

    private void update(int charId, long fame, long time) {
//        scores.add((double) exp);
        fameList.computeIfAbsent(charId, e -> new ArrayList<>()).add(new Fame(fame, time));
        graphPanel.setScores(fameList.get(charId));
        graphPanel.repaint();
    }
}
