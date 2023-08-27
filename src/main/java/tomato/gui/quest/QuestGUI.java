package tomato.gui.quest;

import assets.AssetMissingException;
import assets.IdToAsset;
import assets.ImageBuffer;
import packets.data.QuestData;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

/**
 * Quest tab GUI
 */
public class QuestGUI extends JPanel {
    private final JPanel questPanel;

    /**
     * Main constructor for the quest GUI panel.
     */
    public QuestGUI() {
        setLayout(new BorderLayout());

        questPanel = new JPanel();
        JScrollPane scrollPane = new JScrollPane(questPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(40);

        questPanel.add(new JLabel("Enter Daily Quest Room to see quests"));

        add(scrollPane);
    }

    /**
     * Creates a row panel to add a single quest to the list of quests.
     *
     * @param q Data of the specific quest to be added.
     * @return Quest panel object to be added to the list of quests.
     */
    private JPanel addQuest(QuestData q) {
        JPanel panel = new JPanel();

        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setMaximumSize(new Dimension(420, 200));

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

    /**
     * Adds item images to the quest row.
     *
     * @param items List of items to be added to the row.
     * @param panel The panel to be added to.
     */
    private void addItems(int[] items, JPanel panel) throws IOException, AssetMissingException {
        for (int id : items) {
            BufferedImage img = ImageBuffer.getImage(id);
            ImageIcon icon = new ImageIcon(img.getScaledInstance(20, 20, Image.SCALE_DEFAULT));
            JLabel item = new JLabel(icon);
            item.setToolTipText(IdToAsset.objectName(id));
            panel.add(item);
        }
    }

    /**
     * Used to update the GUI with new quest data.
     * Clears all old inputs to add the new list.
     *
     * @param quests List of quests to be added in the quest tab.
     */
    public void update(QuestData[] quests) {
        questPanel.setLayout(new BoxLayout(questPanel, BoxLayout.Y_AXIS));
        questPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        questPanel.add(Box.createVerticalGlue());

        questPanel.removeAll();
        for (QuestData q : quests) {
            if (!q.repeatable && q.completed) continue;
            questPanel.add(addQuest(q));
        }
        questPanel.revalidate();
    }
}
