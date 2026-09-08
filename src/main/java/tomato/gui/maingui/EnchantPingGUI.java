package tomato.gui.maingui;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import tomato.realmshark.ParseEnchants;
import tomato.realmshark.Sound;
import util.PropertiesManager;

public class EnchantPingGUI extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JTextField searchField = new JTextField(20);
    private final JPanel listPanel = new JPanel();
    // Map enchant id -> checkbox
    private final Map<Short, JCheckBox> checkBoxMap = new LinkedHashMap<>();
    // persisted selection (ids)
    private final Set<Short> savedSelected = new HashSet<>();

    public EnchantPingGUI(List<String> items) {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Load saved selection (comma separated short ids)
        String saved = PropertiesManager.getProperty("enchantPing.selected");
        if (saved != null && !saved.trim().isEmpty()) {
            String[] parts = saved.split(",");
            for (String p : parts) {
                try {
                    short v = Short.parseShort(p.trim());
                    savedSelected.add(v);
                } catch (NumberFormatException ignored) {}
            }
        }

        // Top: search bar
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JLabel("Search: "), BorderLayout.WEST);
        topPanel.add(searchField, BorderLayout.CENTER);
        this.add(topPanel, BorderLayout.NORTH);

        // Middle: scroll pane with checkbox list (grouped)
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setVerticalScrollBarPolicy(
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS
        );
        // Without this the viewport scrolls 1px per wheel tick.
        scrollPane.getVerticalScrollBar().setUnitIncrement(40);
        this.add(scrollPane, BorderLayout.CENTER);

        // Bottom: save button + global controls
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton selectAllBtn = new JButton("Select All");
        JButton clearAllBtn = new JButton("Clear All");
        JButton saveButton = new JButton("Save");
        bottomPanel.add(selectAllBtn);
        bottomPanel.add(clearAllBtn);
        bottomPanel.add(saveButton);
        this.add(bottomPanel, BorderLayout.SOUTH);

        // Populate grouped checkboxes from ParseEnchants
        buildGroupedListFromParseEnchants();

        // Search filtering: simple filter that shows matching checkboxes (expands groups with matches)
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

        // Save action - persist IDs
        saveButton.addActionListener(e -> {
            List<String> ids = new ArrayList<>();
            for (Map.Entry<Short, JCheckBox> en : checkBoxMap.entrySet()) {
                if (en.getValue().isSelected()) ids.add(
                    Short.toString(en.getKey())
                );
            }
            PropertiesManager.setProperties(
                "enchantPing.selected",
                String.join(",", ids)
            );
            JOptionPane.showMessageDialog(
                EnchantPingGUI.this,
                "Saved " + ids.size() + " items.",
                "Save",
                JOptionPane.INFORMATION_MESSAGE
            );
        });

        // Global select/clear
        selectAllBtn.addActionListener(e ->
            checkBoxMap.values().forEach(cb -> cb.setSelected(true))
        );
        clearAllBtn.addActionListener(e ->
            checkBoxMap.values().forEach(cb -> cb.setSelected(false))
        );
    }

    // Build grouped UI using ParseEnchants.ENCHANTS
    private void buildGroupedListFromParseEnchants() {
        listPanel.removeAll();
        checkBoxMap.clear();

        // Group enchants by first character (A-Z or Unique for all caps)
        Map<String, List<Map.Entry<Short, String>>> groups = new TreeMap<>();

        ParseEnchants.ENCHANTS.entrySet()
            .stream()
            .sorted(Comparator.comparing(e -> e.getValue().toLowerCase()))
            .forEach(entry -> {
                String name = entry.getValue();
                // Display names are now proper-case, so "is it uppercase" no
                // longer identifies uniques. The INTERNAL id still does:
                // unique/ST enchants are ALL_CAPS there (e.g. LUCKY_STREAK)
                // while rollable ones are mixed (e.g. Attack_Bonus_1).
                String internalId = ParseEnchants.ENCHANT_INTERNAL_IDS.get(
                    entry.getKey()
                );
                if (
                    internalId != null &&
                    !internalId.isEmpty() &&
                    internalId.equals(internalId.toUpperCase())
                ) {
                    groups
                        .computeIfAbsent("Unique", k -> new ArrayList<>())
                        .add(entry);
                    return;
                }
                char c = name.isEmpty() ? '#' : name.charAt(0);
                String key = (Character.isLetter(c))
                    ? String.valueOf(Character.toUpperCase(c))
                    : "#";
                groups
                    .computeIfAbsent(key, k -> new java.util.ArrayList<>())
                    .add(entry);
            });

        // For each group, create a collapsible panel
        for (Map.Entry<
            String,
            List<Map.Entry<Short, String>>
        > g : groups.entrySet()) {
            String groupName = g.getKey();
            List<Map.Entry<Short, String>> entries = g.getValue();

            JPanel groupContainer = new JPanel();
            groupContainer.setLayout(new BorderLayout());
            groupContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Header with toggle and group controls
            JPanel header = new JPanel(new BorderLayout());
            //            header.setBackground(new Color(0,0,0,0));
            JButton toggle = new JButton("▶ " + groupName);
            toggle.setFocusPainted(false);
            toggle.setBorderPainted(false);
            toggle.setContentAreaFilled(false);
            JPanel headerRight = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 5, 0)
            );
            headerRight.setOpaque(false);
            JButton groupSelect = new JButton("All");
            JButton groupClear = new JButton("Clear");
            headerRight.add(groupSelect);
            headerRight.add(groupClear);
            header.add(toggle, BorderLayout.WEST);
            header.add(headerRight, BorderLayout.EAST);
            groupContainer.add(header, BorderLayout.NORTH);

            // Content panel (checkboxes)
            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBorder(BorderFactory.createEmptyBorder(4, 16, 4, 4));

            for (Map.Entry<Short, String> entry : entries) {
                short id = entry.getKey();
                String name = entry.getValue();
                String cleanedName = name.replaceAll("_", " ").trim();
                String label = String.format("%s", cleanedName);
                JCheckBox cb = new JCheckBox(label);
                cb.setAlignmentX(Component.LEFT_ALIGNMENT);
                // Numeric id matches what the console debug log prints, so the
                // two can be cross-referenced while hunting a specific enchant.
                String internal = ParseEnchants.ENCHANT_INTERNAL_IDS.get(id);
                cb.setToolTipText(
                    "id " + id + (internal == null ? "" : "  ·  " + internal)
                );
                // pre-select if stored
                if (savedSelected.contains(id)) cb.setSelected(true);
                checkBoxMap.put(id, cb);
                content.add(cb);
            }

            // If any of the group's items were saved as selected, expand this group by default
            boolean groupHasSaved = entries
                .stream()
                .anyMatch(en -> savedSelected.contains(en.getKey()));
            content.setVisible(groupHasSaved ? true : false);
            groupContainer.add(content, BorderLayout.CENTER);
            listPanel.add(groupContainer);

            // Toggle behavior
            toggle.addActionListener(e -> {
                boolean visible = !content.isVisible();
                content.setVisible(visible);
                toggle.setText((visible ? "▼ " : "▶ ") + groupName);
                // re-layout
                SwingUtilities.invokeLater(() -> {
                    listPanel.revalidate();
                    listPanel.repaint();
                });
            });

            // If expanded due to saved selection, set toggle text accordingly
            if (groupHasSaved) {
                toggle.setText("▼ " + groupName);
            }
            // Group select/clear actions
            groupSelect.addActionListener(e ->
                entries.forEach(en -> {
                    JCheckBox cb = checkBoxMap.get(en.getKey());
                    if (cb != null) cb.setSelected(true);
                })
            );
            groupClear.addActionListener(e ->
                entries.forEach(en -> {
                    JCheckBox cb = checkBoxMap.get(en.getKey());
                    if (cb != null) cb.setSelected(false);
                })
            );
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    private void filterList() {
        String query = searchField.getText();
        if (query == null) query = "";
        String q = query.toLowerCase();

        // Iterate groups in listPanel and toggle visibility based on matches
        Component[] groups = listPanel.getComponents();
        for (Component comp : groups) {
            if (!(comp instanceof JPanel)) continue;
            JPanel groupContainer = (JPanel) comp;
            JPanel content = null;
            for (Component c : groupContainer.getComponents()) {
                if (c instanceof JPanel) {
                    JPanel p = (JPanel) c;
                    if (p.getLayout() instanceof BoxLayout) {
                        content = p;
                        break;
                    }
                }
            }
            // Alternative: find content by scanning children
            if (content == null) {
                for (Component c : groupContainer.getComponents()) {
                    if (
                        c instanceof JPanel &&
                        ((JPanel) c).getComponentCount() > 0
                    ) {
                        JPanel p = (JPanel) c;
                        if (
                            p.getComponentCount() > 0 &&
                            p.getComponent(0) instanceof JCheckBox
                        ) {
                            content = p;
                            break;
                        }
                    }
                }
            }

            boolean groupHasMatch = false;
            if (content != null) {
                for (Component itemComp : content.getComponents()) {
                    if (!(itemComp instanceof JCheckBox)) continue;
                    JCheckBox cb = (JCheckBox) itemComp;
                    boolean matches =
                        q.isEmpty() || cb.getText().toLowerCase().contains(q);
                    cb.setVisible(matches);
                    if (matches) groupHasMatch = true;
                }
                // If any match, ensure group is expanded so user sees results
                if (groupHasMatch) {
                    content.setVisible(true);
                    // update header toggle text: find header button and set expanded marker
                    for (Component c : groupContainer.getComponents()) {
                        if (c instanceof JPanel) {
                            for (Component hc : ((JPanel) c).getComponents()) {
                                if (hc instanceof JButton) {
                                    JButton tb = (JButton) hc;
                                    String text = tb.getText();
                                    if (!text.startsWith("▼")) tb.setText(
                                        "▼ " + text.replaceAll("^[▶▼] ", "")
                                    );
                                }
                            }
                        }
                    }
                } else {
                    content.setVisible(false);
                }
            }
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    public List<String> getSelectedItems() {
        List<String> sel = new ArrayList<>();
        for (Map.Entry<Short, JCheckBox> e : checkBoxMap.entrySet()) {
            if (e.getValue().isSelected()) {
                String name = ParseEnchants.ENCHANTS.get(e.getKey());
                sel.add(String.format("%s(%d)", name, e.getKey()));
            }
        }
        return sel;
    }

    // keep setItems for API compatibility (not used currently)
    public void setItems(List<String> items) {
        // rebuild UI from ParseEnchants (items parameter ignored)
        buildGroupedListFromParseEnchants();
    }

    /**
     * Opens the EnchantPing dialog with an empty/default list.
     * Call EnchantPingGUI.open() from other code to show the dialog.
     */
    public static void open() {
        Sound.custom.play();
        open(java.util.Collections.emptyList());
    }

    /**
     * Opens the EnchantPing dialog with the provided items.
     *
     * @param items list of strings to populate checkboxes (ignored currently, ParseEnchants used)
     */
    public static void open(List<String> items) {
        Sound.custom.play();
        JFrame parent = tomato.gui.TomatoGUI.getFrame();
        EnchantPingGUI panel = new EnchantPingGUI(items);
        JDialog dialog = new JDialog(parent, "Enchant Pings", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setSize(500, 700);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    // Simple test harness
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Enchant Ping GUI");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            EnchantPingGUI panel = new EnchantPingGUI(null);
            frame.getContentPane().add(panel);
            frame.setSize(500, 700);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
