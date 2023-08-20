package tomato.gui.dps;

import tomato.realmshark.enums.CharacterClass;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

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
    private DpsGUI dpsGui;

    public FilterGUI(DpsGUI dpsGui) {
        this.dpsGui = dpsGui;
        setLayout(new BorderLayout());

        JPanel top = new JPanel();
        top.setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
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
        radio.add(filter);
        group.add(highlight);
        radio.add(highlight);
        filter.setSelected(true);

        nameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        top.add(saveButton, gridBagConstraints);
        gridBagConstraints.gridx = 1;
        top.add(loadButton, gridBagConstraints);
        gridBagConstraints.gridx = 2;
        top.add(filterComboBox, gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        top.add(newButton, gridBagConstraints);
        gridBagConstraints.gridx = 1;
        top.add(nameLabel, gridBagConstraints);
        gridBagConstraints.gridx = 2;
        gridBagConstraints.weightx = 1000;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        top.add(nameText, gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.weightx = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        top.add(deleteButton, gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        top.add(radio, gridBagConstraints);

        add(top, BorderLayout.NORTH);

        JPanel right = new JPanel();
        JScrollPane scrollPane = new JScrollPane(right);
        int w = 260;
        int h = 200;
        scrollPane.setBounds(0, 0, w + 15, h);
        scrollPane.getVerticalScrollBar().setUnitIncrement(40);
        JPanel contentPane = new JPanel(null);
        contentPane.setPreferredSize(new Dimension(w, h));
        contentPane.add(scrollPane);
        add(contentPane, BorderLayout.CENTER);

        largeMethod(right);

        loadButton.addActionListener(e -> {
            loadButton();
        });
        saveButton.addActionListener(e -> {
            saveButton();
        });
        newButton.addActionListener(e -> {
            newButton();
        });
        deleteButton.addActionListener(e -> {
            deleteButton();
        });
    }

    private void deleteButton() {
        Object n = filterComboBox.getSelectedItem();
        filterComboBox.removeItem(n);
        dpsGui.removeComboBox((String) n);
    }

    private void loadButton() {
        String n = String.valueOf(filterComboBox.getSelectedItem());
        if (n.equals("null")) return;
        newButton();
        namePanelBody.removeAll();
        guildPanelBody.removeAll();
        String ss = dpsGui.getFilterString(n);
        int part = 0;
        int fieldIndex = 0;
        for (String s : ss.split(",")) {
            if (s.equals("-")) {
                part++;
            } else if (part == 0) {
                nameText.setText(s);
            } else if (part == 1) {
                filter.setSelected(s.equals("F"));
                highlight.setSelected(s.equals("H"));
            } else if (part == 2) {
                JTextField comp = addTextField(false, false);
                textFieldNames.add(comp);
                namePanelBody.add(comp);
                comp.setText(s);
            } else if (part == 3) {
                JTextField comp = addTextField(true, false);
                textFieldGuild.add(comp);
                guildPanelBody.add(comp);
                comp.setText(s);
            } else if (part == 4) {
                JCheckBox field = classCheckBoxes.get(fieldIndex);
                boolean equals = s.equals("1");
                field.setSelected(equals);
                fieldIndex++;
            }
        }
    }

    private void saveButton() {
        StringBuilder sb = new StringBuilder();
        String nameField = nameText.getText();
        if (nameField.replaceAll(" ", "").length() == 0) {
            return;
        }
        sb.append(nameField).append(",");
        sb.append("-").append(",");
        if (highlight.isSelected()) sb.append("H").append(",");
        if (filter.isSelected()) sb.append("F").append(",");
        sb.append("-").append(",");
        for (JTextField field : textFieldNames) {
            String text = field.getText();
            if (text.length() > 0) {
                sb.append(text).append(",");
            }
        }
        sb.append("-").append(",");
        for (JTextField field : textFieldGuild) {
            String text = field.getText();
            if (text.length() > 0) {
                sb.append(text).append(",");
            }
        }
        sb.append("-").append(",");
        for (JCheckBox field : classCheckBoxes) {
            sb.append(field.isSelected() ? "1" : "0").append(",");
        }
        boolean add = dpsGui.addComboBox(nameField, sb.toString());
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

        namePanelBody.removeAll();
        guildPanelBody.removeAll();

        filter.setSelected(true);
        highlight.setSelected(false);

        JTextField comp1 = addTextField(false, false);
        textFieldNames.add(comp1);
        namePanelBody.add(comp1);

        guildPanelBody.add(classCheckBoxes.get(0));

        JTextField comp2 = addTextField(true, false);
        textFieldGuild.add(comp2);
        guildPanelBody.add(comp2);

        revalidate();
    }

    private void largeMethod(JPanel mainPanel) {
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        namePanelBody = new JPanel();
        textFieldOptions(mainPanel, "By Name", namePanelBody, textFieldNames, false);

        mainPanel.add(new JSeparator(SwingConstants.HORIZONTAL));

        guildPanelBody = new JPanel();
        textFieldOptions(mainPanel, "By Guild", guildPanelBody, textFieldGuild, true);

        mainPanel.add(new JSeparator(SwingConstants.HORIZONTAL));

        checkBoxOptions(mainPanel);
    }

    private void checkBoxOptions(JPanel mainPanel) {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());

        JPanel top = new JPanel();
        JLabel n = new JLabel("By Class");
        top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
        top.add(n);
        top.add(Box.createHorizontalGlue());
        topPanel.add(top, BorderLayout.NORTH);
        JPanel body = new JPanel();
        topPanel.add(body, BorderLayout.CENTER);

        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        JCheckBox myClass = new JCheckBox("My Class");
        classCheckBoxes.add(myClass);
        body.add(myClass);
        for (CharacterClass s : CharacterClass.CHAR_CLASS_LIST) {
            JCheckBox comp = new JCheckBox(s.name());
            classCheckBoxes.add(comp);
            body.add(comp);
        }

        mainPanel.add(topPanel);
    }

    private void textFieldOptions(JPanel mainPanel, String labelName, JPanel body, ArrayList<JTextField> fields, boolean isGuild) {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());

        JPanel top = new JPanel();
        JLabel n = new JLabel(labelName);
        top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
        top.add(n);
        top.add(Box.createHorizontalGlue());
        topPanel.add(top, BorderLayout.NORTH);
        topPanel.add(body, BorderLayout.CENTER);

        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        if (isGuild) {
            JCheckBox myGuildCheckBox = new JCheckBox("My Guild");
            body.add(myGuildCheckBox);
            classCheckBoxes.add(myGuildCheckBox);
        }

        JPanel bot = new JPanel(new GridBagLayout());
        JButton addButton = new JButton("+");
        bot.add(addButton);
        JTextField comp1 = addTextField(isGuild, false);
        fields.add(comp1);
        body.add(comp1);
        addButton.addActionListener(e -> {
            JTextField comp2 = addTextField(isGuild, false);
            fields.add(comp2);
            body.add(comp2);
            revalidate();
        });
        topPanel.add(bot, BorderLayout.SOUTH);

        mainPanel.add(topPanel);
    }

    private JTextField addTextField(boolean withSpace, boolean withNumbers) {
        JTextField comp = new JTextField();
        comp.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char caracter = e.getKeyChar();
                if (((caracter < 'a') || (caracter > 'z')) && ((caracter < 'A') || (caracter > 'Z')) && (!withNumbers || (caracter < '0') || (caracter > '9')) && (caracter != '\b') && (!withSpace || (caracter != ' '))) {
                    e.consume();
                }
            }
        });
        return comp;
    }

    public static void open(DpsGUI dpsGui, JPanel INSTANCE) {
        FilterGUI filter = new FilterGUI(dpsGui);
        JPanel p = new JPanel();
        p.add(new TextArea());

        JButton close = new JButton("Close");
        JOptionPane pane = new JOptionPane(filter, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, new JButton[]{close}, close);
        close.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(close);
            pane.setValue(-1);
            w.dispose();
        });
        JDialog dialog = pane.createDialog(INSTANCE, "Filter Options");
//        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dialog.setVisible(true);
    }
}
