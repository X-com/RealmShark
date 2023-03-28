package tomato.gui;

import assets.AssetMissingException;
import assets.ImageBuffer;
import packets.data.QuestData;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class QuestGUI extends JPanel {
    private final JPanel questPanel;
    private final JLabel infoLabel;

    public QuestGUI() {
        setLayout(new BorderLayout());

        questPanel = new JPanel();
        JScrollPane scrollPane = new JScrollPane(questPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(40);

        questPanel.setLayout(new BoxLayout(questPanel, BoxLayout.Y_AXIS));
        questPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        questPanel.add(Box.createVerticalGlue());

        infoLabel = new JLabel("Enter Daily Quest Room to see quests");
        add(infoLabel, BorderLayout.NORTH);

        add(scrollPane);
    }

    private JPanel addQuest(QuestData q) {
        JPanel panel = new JPanel();

        panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setMaximumSize(new Dimension(400, 200));

        panel.add(new JLabel(q.name + "  "));
        try {
            addItems(q.requirements, panel);
            panel.add(new JLabel(" -> "));
            addItems(q.rewards, panel);
        } catch (IOException | AssetMissingException e) {
            e.printStackTrace();
        }

        return panel;
    }

    private void addItems(int[] items, JPanel panel) throws IOException, AssetMissingException {
        for (int id : items) {
            BufferedImage img = ImageBuffer.getImage(id);
            ImageIcon icon = new ImageIcon(img.getScaledInstance(20, 20, Image.SCALE_DEFAULT));
            JLabel item = new JLabel(icon);
            panel.add(item);
        }
    }

    public void update(QuestData[] quests) {
        questPanel.removeAll();
        for (QuestData q : quests) {
            if (!q.repeatable && q.completed) continue;
            questPanel.add(addQuest(q));
        }
        questPanel.revalidate();
        remove(infoLabel);
        revalidate();
    }
}
