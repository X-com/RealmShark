package tomato.gui.stats;

import assets.IdToAsset;
import assets.ImageBuffer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import tomato.backend.data.DungeonStatData;
import tomato.backend.data.DungeonStatData.DungeonInfo;
import tomato.backend.data.DungeonStatData.Loot;
import tomato.gui.SmartScroller;
import tomato.gui.dps.DpsGUI;

public class DungeonStats extends JPanel {

    private static DungeonStats INSTANCE;

    private static JPanel dungeonStatPanel;
    private static JPanel radioPanel;
    private static Font mainFont;
    private DungeonStatData dungeonStatData;
    private int dungeonSize;
    private String selectionName;

    public DungeonStats() {
        this.INSTANCE = this;
        setLayout(new BorderLayout());

        dungeonStatPanel = new JPanel();
        dungeonStatPanel.setLayout(
            new BoxLayout(dungeonStatPanel, BoxLayout.Y_AXIS)
        );
        radioPanel = new JPanel();
        radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));
        radioPanel.setBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, Color.GRAY)
        );

        validate();

        JScrollPane scrollMid = new JScrollPane(dungeonStatPanel);
        scrollMid.getVerticalScrollBar().setUnitIncrement(40);
        new SmartScroller(scrollMid, 0);
        add(scrollMid, BorderLayout.CENTER);

        JScrollPane scrollRight = new JScrollPane(radioPanel);
        scrollRight.getVerticalScrollBar().setUnitIncrement(40);
        new SmartScroller(scrollRight, 0);
        add(scrollRight, BorderLayout.EAST);
    }

    private void updateGUI() {
        if (
            dungeonStatData == null ||
            dungeonStatData.data == null ||
            selectionName == null
        ) return;

        dungeonStatPanel.removeAll();

        DungeonInfo info = dungeonStatData.data.get(selectionName);
        if (info != null) {
            displayDungeon(info);
        }
        dungeonStatPanel.add(Box.createVerticalGlue());

        revalidate();
    }

    private void radioAction(ActionEvent actionEvent) {
        JRadioButton button = (JRadioButton) actionEvent.getSource();
        selectionName = button.getText();
        updateGUI();
    }

    private void updateRadioButtons() {
        if (dungeonStatData == null || dungeonStatData.data == null) return;
        String[] list = dungeonStatData.data
            .keySet()
            .stream()
            .sorted()
            .toArray(String[]::new);
        if (list.length == dungeonSize) return;
        dungeonSize = list.length;
        radioPanel.removeAll();
        ButtonGroup radioSelections = new ButtonGroup();

        for (String s : list) {
            JRadioButton b = new JRadioButton(s, s.equals(selectionName));
            b.addActionListener(this::radioAction);
            radioSelections.add(b);
            radioPanel.add(b);
        }

        revalidate();
    }

    private void displayDungeon(DungeonInfo info) {
        JPanel titlePanel = new JPanel();
        titlePanel.setBorder(
            BorderFactory.createTitledBorder(
                null,
                info.getName() +
                " [" +
                info.getEnteredDungeon() +
                "] " +
                DpsGUI.systemTimeToString(info.getTotalTime()),
                TitledBorder.CENTER,
                TitledBorder.CENTER,
                mainFont
            )
        );
        dungeonStatPanel.add(titlePanel);

        TreeMap<String, JPanel> list = new TreeMap<>();
        for (Map.Entry<Integer, Integer> e : info
            .getEntityDamaged()
            .entrySet()) {
            Integer id = e.getKey();
            if (id == null || id == 0) continue;

            String str = IdToAsset.objectName(id);
            JPanel p = addMobTitle(id, e.getValue(), info);
            if (p == null) continue;
            list.put(str, p);
        }

        list.keySet().forEach(s -> dungeonStatPanel.add(list.get(s)));

        JPanel unknownItems = addMobTitle(0, 0, info);
        if (unknownItems != null) {
            dungeonStatPanel.add(unknownItems);
        }
    }

    private JPanel addMobTitle(int id, int num, DungeonInfo di) {
        StringBuilder sb = new StringBuilder();
        JPanel mobPanel = new JPanel();
        mobPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
            )
        );
        mobPanel.setLayout(new BoxLayout(mobPanel, BoxLayout.Y_AXIS));

        if (id == 0) {
            Loot loot = di.getLoot(id);
            if (loot == null) return null;
            sb.append("Unknown");
        } else {
            String str = IdToAsset.objectName(id);
            if (str == null) return null;
            sb.append(str).append(" : ").append(num);
        }
        try {
            String mobName = sb.toString();
            JLabel l;
            if (id != 0) {
                ImageIcon outlinedIcon = ImageBuffer.getOutlinedIcon(id, 40);
                l = new JLabel(mobName, outlinedIcon, JLabel.LEFT);
            } else {
                l = new JLabel(mobName, JLabel.LEFT);
            }

            l.setFont(mainFont);
            l.setToolTipText(
                "Total number of hits on mob type (not confirmed killed or soulbound)"
            );
            mobPanel.add(l);
        } catch (Exception e) {
            System.out.println(
                "Entity id: " + id + " " + IdToAsset.tileName(id)
            );
            e.printStackTrace();
        }

        Loot loot = di.getLoot(id);
        if (loot == null) return mobPanel;

        ArrayList<JLabel> list = new ArrayList<>();
        for (Map.Entry<Integer, Integer> e : loot.getItems().entrySet()) {
            StringBuilder sb2 = new StringBuilder();
            Integer idItem = e.getKey();
            sb2
                .append(IdToAsset.objectName(idItem))
                .append(" : ")
                .append(e.getValue())
                .append("\n");
            JLabel itemLabel = new JLabel(
                sb2.toString(),
                ImageBuffer.getOutlinedIcon(idItem, 16),
                JLabel.LEFT
            );
            itemLabel.setFont(mainFont);
            list.add(itemLabel);
        }

        list.sort(Comparator.comparing(JLabel::getText));
        for (JLabel i : list) {
            mobPanel.add(i);
        }

        return mobPanel;
    }

    public static void update(DungeonStatData data, String dungeon) {
        if (INSTANCE == null) return;
        INSTANCE.dungeonStatData = data;
        INSTANCE.updateRadioButtons();
        if (
            dungeon == null || dungeon.equals(INSTANCE.selectionName)
        ) INSTANCE.updateGUI();
    }

    public static void editFont(Font font) {
        mainFont = font;
        INSTANCE.updateGUI();
    }
}
