package tomato.gui.maingui;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import tomato.realmshark.ParseDungeon;
import util.PropertiesManager;

/**
 * Selection window for "ping me when this dungeon drops".
 *
 * Mirrors EnchantPingGUI, but as a flat searchable list - there are only ~150
 * dungeons and they are already recognisable by name, so the A-Z grouping the
 * 1000-entry enchant list needs would just add clicks here.
 *
 * Selections are persisted as dungeon NAMES (property "dungeonPing.selected"),
 * not portal object ids, so a dungeon with several portal variants is covered
 * by a single tick. TomatoData.isDungeonPing() reads the same property.
 */
public class DungeonPingGUI extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final String PROP = "dungeonPing.selected";

    private final JTextField searchField = new JTextField(20);
    private final JPanel listPanel = new JPanel();
    private final Map<String, JCheckBox> checkBoxMap = new LinkedHashMap<>();
    private final JLabel countLabel = new JLabel();

    public DungeonPingGUI() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        List<String> saved = loadSelected();

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JLabel("Search: "), BorderLayout.WEST);
        topPanel.add(searchField, BorderLayout.CENTER);
        this.add(topPanel, BorderLayout.NORTH);

        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setVerticalScrollBarPolicy(
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS
        );
        // JPanel is not Scrollable: without this the wheel moves 1px per tick.
        scrollPane.getVerticalScrollBar().setUnitIncrement(40);
        this.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton selectAllBtn = new JButton("Select All");
        JButton clearAllBtn = new JButton("Clear All");
        JButton saveButton = new JButton("Save");
        buttons.add(selectAllBtn);
        buttons.add(clearAllBtn);
        buttons.add(saveButton);
        countLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        bottomPanel.add(countLabel, BorderLayout.WEST);
        bottomPanel.add(buttons, BorderLayout.EAST);
        this.add(bottomPanel, BorderLayout.SOUTH);

        buildList(saved);

        searchField
            .getDocument()
            .addDocumentListener(
                new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        filterList();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        filterList();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        filterList();
                    }
                }
            );

        saveButton.addActionListener(e -> {
            List<String> selected = new ArrayList<>();
            for (Map.Entry<String, JCheckBox> en : checkBoxMap.entrySet()) {
                if (en.getValue().isSelected()) selected.add(en.getKey());
            }
            PropertiesManager.setProperties(PROP, String.join(",", selected));
            updateCount();
            JOptionPane.showMessageDialog(
                DungeonPingGUI.this,
                "Saved " + selected.size() + " dungeon(s).",
                "Save",
                JOptionPane.INFORMATION_MESSAGE
            );
        });

        // Only touch what the search currently shows, so "Select All" while
        // filtered does the obvious thing instead of silently ticking all 150.
        selectAllBtn.addActionListener(e -> setVisibleSelected(true));
        clearAllBtn.addActionListener(e -> setVisibleSelected(false));
    }

    private void setVisibleSelected(boolean selected) {
        for (JCheckBox cb : checkBoxMap.values()) {
            if (cb.isVisible()) cb.setSelected(selected);
        }
        updateCount();
    }

    private static List<String> loadSelected() {
        List<String> out = new ArrayList<>();
        String saved = PropertiesManager.getProperty(PROP);
        if (saved == null || saved.trim().isEmpty()) return out;
        for (String s : saved.split(",")) {
            String v = s.trim();
            if (!v.isEmpty()) out.add(v);
        }
        return out;
    }

    private void buildList(List<String> saved) {
        listPanel.removeAll();
        checkBoxMap.clear();

        List<String> names = ParseDungeon.allDungeonNames();
        if (names.isEmpty()) {
            listPanel.add(
                new JLabel(
                    "<html>No dungeons found.<br/>" +
                    "Game assets have not been extracted yet.</html>"
                )
            );
        }

        for (String name : names) {
            JCheckBox cb = new JCheckBox(name);
            cb.setAlignmentX(Component.LEFT_ALIGNMENT);
            for (String s : saved) {
                if (s.equalsIgnoreCase(name)) {
                    cb.setSelected(true);
                    break;
                }
            }
            cb.addActionListener(e -> updateCount());
            checkBoxMap.put(name, cb);
            listPanel.add(cb);
        }

        updateCount();
        listPanel.revalidate();
        listPanel.repaint();
    }

    private void updateCount() {
        int n = 0;
        for (JCheckBox cb : checkBoxMap.values()) {
            if (cb.isSelected()) n++;
        }
        countLabel.setText(n + " of " + checkBoxMap.size() + " selected");
    }

    private void filterList() {
        String q = searchField.getText();
        q = (q == null) ? "" : q.toLowerCase().trim();

        for (Map.Entry<String, JCheckBox> en : checkBoxMap.entrySet()) {
            boolean matches =
                q.isEmpty() || en.getKey().toLowerCase().contains(q);
            en.getValue().setVisible(matches);
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    /**
     * Opens the dungeon ping selection dialog.
     */
    public static void open() {
        JFrame parent = tomato.gui.TomatoGUI.getFrame();
        DungeonPingGUI panel = new DungeonPingGUI();
        JDialog dialog = new JDialog(parent, "Dungeon Pings", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setSize(420, 700);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }
}
