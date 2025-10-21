package tomato.gui.dps;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.swing.*;
import tomato.gui.dps.shared.FilterPresetSerializer;
import tomato.realmshark.enums.CharacterClass;

public class FilterGUI extends JPanel {

    private final ArrayList<JTextField> textFieldNames = new ArrayList<>();
    private final ArrayList<JTextField> textFieldGuild = new ArrayList<>();
    private final ArrayList<JCheckBox> classCheckBoxes = new ArrayList<>();
    private JPanel namePanelBody;
    private JPanel guildPanelBody;
    private final JTextField nameText;
    private final JRadioButton filter;
    private final JRadioButton highlight;
    private final JComboBox<String> filterComboBox;
    private final DpsGUI dpsGui;

    public FilterGUI(DpsGUI dpsGui) {
        this.dpsGui = dpsGui;
        setLayout(new BorderLayout());

        // Top controls
        JPanel top = new JPanel();
        top.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        filterComboBox = new JComboBox<>(dpsGui.getComboBoxStrings());
        nameText = addTextField(true, true);
        JButton loadButton = new JButton("Load");
        JButton saveButton = new JButton("Save");
        JButton newButton = new JButton("New");
        JButton deleteButton = new JButton("Delete");
        JLabel nameLabel = new JLabel("Name: ");

        JPanel radio = new JPanel();
        ButtonGroup group = new ButtonGroup();
        filter = new JRadioButton("Filter");
        highlight = new JRadioButton("Highlight");
        group.add(filter);
        group.add(highlight);
        radio.add(filter);
        radio.add(highlight);
        filter.setSelected(true);

        nameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        top.add(saveButton, gbc);

        gbc.gridx = 1;
        top.add(loadButton, gbc);

        gbc.gridx = 2;
        top.add(filterComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        top.add(newButton, gbc);

        gbc.gridx = 1;
        top.add(nameLabel, gbc);

        gbc.gridx = 2;
        gbc.weightx = 1000;
        gbc.fill = GridBagConstraints.BOTH;
        top.add(nameText, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        top.add(deleteButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        top.add(radio, gbc);

        add(top, BorderLayout.NORTH);

        // Main content scroll area (increased width/height)
        JPanel boxScroll = new JPanel();
        JScrollPane scrollPane = new JScrollPane(boxScroll);
        int w = 360; // increased width
        int h = 360; // increased height
        scrollPane.setBounds(0, 0, w + 20, h);
        scrollPane.getVerticalScrollBar().setUnitIncrement(40);

        JPanel contentPane = new JPanel(null);
        contentPane.setPreferredSize(new Dimension(w, h));
        contentPane.add(scrollPane);
        add(contentPane, BorderLayout.CENTER);

        largeMethod(boxScroll);

        // Actions
        loadButton.addActionListener(e -> loadButton());
        saveButton.addActionListener(e -> saveButton());
        newButton.addActionListener(e -> newButton());
        deleteButton.addActionListener(e -> deleteButton());
    }

    private void deleteButton() {
        Object n = filterComboBox.getSelectedItem();
        if (n == null) return;
        filterComboBox.removeItem(n);
        dpsGui.removeComboBox(String.valueOf(n));
    }

    private void loadButton() {
        Object sel = filterComboBox.getSelectedItem();
        if (sel == null) return;
        String presetName = String.valueOf(sel);
        String serialized = dpsGui.getFilterString(presetName);
        if (serialized == null || serialized.isEmpty()) return;

        newButton(); // reset fields
        if (namePanelBody != null) namePanelBody.removeAll();
        if (guildPanelBody != null) guildPanelBody.removeAll();

        FilterPresetSerializer.FilterPreset preset =
            FilterPresetSerializer.deserialize(serialized);

        nameText.setText(preset.name);
        filter.setSelected(preset.filterMode);
        highlight.setSelected(preset.highlightMode);

        for (String s : preset.names) {
            JTextField comp = addTextField(false, false);
            textFieldNames.add(comp);
            namePanelBody.add(comp);
            comp.setText(s);
        }

        for (String s : preset.guilds) {
            JTextField comp = addTextField(true, false);
            textFieldGuild.add(comp);
            guildPanelBody.add(comp);
            comp.setText(s);
        }

        int max = Math.min(
            classCheckBoxes.size(),
            preset.flagsInGuiOrder.size()
        );
        for (int i = 0; i < max; i++) {
            classCheckBoxes
                .get(i)
                .setSelected(
                    Boolean.TRUE.equals(preset.flagsInGuiOrder.get(i))
                );
        }

        revalidate();
        repaint();
    }

    private void saveButton() {
        String nameField = nameText.getText() != null
            ? nameText.getText().trim()
            : "";
        if (nameField.replaceAll(" ", "").length() == 0) {
            return;
        }

        ArrayList<String> names = new ArrayList<>();
        for (JTextField field : textFieldNames) {
            String text = field.getText();
            if (text != null) {
                text = text.trim();
                if (!text.isEmpty()) names.add(text);
            }
        }

        ArrayList<String> guilds = new ArrayList<>();
        for (JTextField field : textFieldGuild) {
            String text = field.getText();
            if (text != null) {
                text = text.trim();
                if (!text.isEmpty()) guilds.add(text);
            }
        }

        ArrayList<Boolean> flags = new ArrayList<>();
        for (JCheckBox field : classCheckBoxes) {
            flags.add(field.isSelected());
        }

        String serialized = FilterPresetSerializer.serialize(
            nameField,
            filter.isSelected(),
            highlight.isSelected(),
            names,
            guilds,
            flags
        );

        boolean add = dpsGui.addComboBox(nameField, serialized);
        if (add) {
            filterComboBox.addItem(nameField);
        }
    }

    private void newButton() {
        textFieldNames.clear();
        textFieldGuild.clear();
        for (JCheckBox c : classCheckBoxes) {
            c.setSelected(false);
        }
        nameText.setText("");

        if (namePanelBody != null) namePanelBody.removeAll();
        if (guildPanelBody != null) guildPanelBody.removeAll();

        filter.setSelected(true);
        highlight.setSelected(false);

        // Create starter fields
        JTextField comp1 = addTextField(false, false);
        textFieldNames.add(comp1);
        if (namePanelBody != null) namePanelBody.add(comp1);

        // Re-add "My Guild" checkbox into the guild body if present in registry as index 0
        if (!classCheckBoxes.isEmpty() && guildPanelBody != null) {
            guildPanelBody.add(classCheckBoxes.get(0));
        }

        JTextField comp2 = addTextField(true, false);
        textFieldGuild.add(comp2);
        if (guildPanelBody != null) guildPanelBody.add(comp2);

        revalidate();
        repaint();
    }

    private void largeMethod(JPanel mainPanel) {
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        textFieldOptions(mainPanel, "By Name", textFieldNames, false);
        mainPanel.add(new JSeparator(SwingConstants.HORIZONTAL));

        textFieldOptions(mainPanel, "By Guild", textFieldGuild, true);
        mainPanel.add(new JSeparator(SwingConstants.HORIZONTAL));

        checkBoxOptions(mainPanel);
    }

    private void checkBoxOptions(JPanel mainPanel) {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());

        // Header
        JPanel top = new JPanel();
        JLabel n = new JLabel("By Class");
        top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
        top.add(n);
        top.add(Box.createHorizontalGlue());
        topPanel.add(top, BorderLayout.NORTH);

        // Body: My Class + grid of class checkboxes in two rows
        JPanel body = new JPanel(new BorderLayout());
        topPanel.add(body, BorderLayout.CENTER);

        JCheckBox myClass = new JCheckBox("My Class");
        classCheckBoxes.add(myClass);
        body.add(myClass, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 2, 10, 5)); // 2 columns, dynamic rows

        // Build checkboxes in CHAR_CLASS_LIST order for consistent serialization,
        // but display them alphabetically by class name.
        java.util.ArrayList<JCheckBox> display = new java.util.ArrayList<>();
        java.util.ArrayList<String> displayNames = new java.util.ArrayList<>();

        for (CharacterClass s : CharacterClass.CHAR_CLASS_LIST) {
            String className = CharacterClass.getName(s.getId());
            JCheckBox comp = new JCheckBox(className);

            // Keep internal order (My Guild, My Class, then CHAR_CLASS_LIST) for flags
            classCheckBoxes.add(comp);

            // Collect for alphabetical display
            display.add(comp);

            displayNames.add(className);
        }

        // Sort by name for display while preserving internal order for serialization
        java.util.ArrayList<Integer> order = new java.util.ArrayList<>();
        for (int i = 0; i < display.size(); i++) order.add(i);
        order.sort((a, b) ->
            displayNames.get(a).compareToIgnoreCase(displayNames.get(b))
        );
        for (Integer i : order) {
            grid.add(display.get(i));
        }

        body.add(grid, BorderLayout.CENTER);

        mainPanel.add(topPanel);
    }

    private void textFieldOptions(
        JPanel mainPanel,
        String labelName,
        ArrayList<JTextField> fields,
        boolean isGuild
    ) {
        JPanel body = new JPanel();
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());

        // Header
        JPanel top = new JPanel();
        JLabel n = new JLabel(labelName);
        top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
        top.add(n);
        top.add(Box.createHorizontalGlue());
        topPanel.add(top, BorderLayout.NORTH);
        topPanel.add(body, BorderLayout.CENTER);

        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        // My Guild checkbox goes here (and registered first in classCheckBoxes)
        if (isGuild) {
            JCheckBox myGuildCheckBox = new JCheckBox("My Guild");
            body.add(myGuildCheckBox);
            classCheckBoxes.add(myGuildCheckBox);
            guildPanelBody = body;
        } else {
            namePanelBody = body;
        }

        // Bottom tools
        JPanel bot = new JPanel(new GridBagLayout());
        JButton addButton = new JButton("+");
        bot.add(addButton);

        // One initial field
        JTextField comp1 = addTextField(isGuild, false);
        fields.add(comp1);
        body.add(comp1);

        addButton.addActionListener(e -> {
            JTextField comp2 = addTextField(isGuild, false);
            fields.add(comp2);
            body.add(comp2);
            revalidate();
            repaint();
        });
        topPanel.add(bot, BorderLayout.SOUTH);

        mainPanel.add(topPanel);
    }

    private JTextField addTextField(boolean withSpace, boolean withNumbers) {
        JTextField comp = new JTextField();
        comp.addKeyListener(
            new KeyAdapter() {
                public void keyTyped(KeyEvent e) {
                    char caracter = e.getKeyChar();
                    if (
                        ((caracter < 'a') || (caracter > 'z')) &&
                        ((caracter < 'A') || (caracter > 'Z')) &&
                        (!withNumbers ||
                            (caracter < '0') ||
                            (caracter > '9')) &&
                        (caracter != '\b') &&
                        (!withSpace || (caracter != ' '))
                    ) {
                        e.consume();
                    }
                }
            }
        );
        return comp;
    }

    public static void open(DpsGUI dpsGui) {
        FilterGUI filter = new FilterGUI(dpsGui);

        JButton close = new JButton("Close");
        JOptionPane pane = new JOptionPane(
            filter,
            JOptionPane.PLAIN_MESSAGE,
            JOptionPane.OK_CANCEL_OPTION,
            null,
            new JButton[] { close },
            close
        );
        close.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(close);
            pane.setValue(-1);
            w.dispose();
        });
        JDialog dialog = pane.createDialog(dpsGui, "Filter Options");
        dialog.setVisible(true);
    }
}
