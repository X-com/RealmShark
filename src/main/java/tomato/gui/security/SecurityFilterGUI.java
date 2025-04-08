package tomato.gui.security;

import assets.ImageBuffer;
import com.google.gson.Gson;
import tomato.realmshark.ParseEquipment;
import tomato.realmshark.enums.CharacterClass;
import tomato.realmshark.enums.StatPotion;
import util.PropertiesManager;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.IntStream;

public class SecurityFilterGUI extends JPanel {

    private final ParsePanelGUI parentPanel;

    private final ArrayList<Integer> OMITTED_SLOT_TYPES = new ArrayList<Integer>() {
        {
            add(10); //tokens
        }
    };

    private final ArrayList<FilterEntity> classPoints = new ArrayList<>();
    private final ArrayList<FilterEntity> items = new ArrayList<>();
    private final ArrayList<FilterEntity> minTiers = new ArrayList<>();
    private final ArrayList<JCheckBox> checkBoxStats = new ArrayList<>();

    private JPanel itemsPanel;
    private JToggleButton toggleWhiteList;
    private JToggleButton toggleBlackList;
    private boolean itemSelectMode;
    private JTextField searchField;
    private String currentSearchValue = null;

    private final JComboBox<String> filterComboBox;
    private final JTextField jsonField;
    private JTextField nameField;
    private JTextField exaltSkinPointsField;
    private final FilterEntity exaltSkin = new FilterEntity();

    public SecurityFilterGUI(ParsePanelGUI parentPanel) {
        this.parentPanel = parentPanel;
        setLayout(new BorderLayout());

        filterComboBox = new JComboBox<>();
        for (SecurityFilter sf : parentPanel.getFilters().values()) {
            filterComboBox.addItem(sf.name);
        }

        JPanel topPanel = new JPanel(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        top(topPanel);

        JPanel bot = new JPanel(new BorderLayout());
        JLabel json = new JLabel("Json:");
        jsonField = new JTextField();
        jsonField.setEditable(false);
        JButton copy = new JButton("Copy Json");
        copy.addActionListener(this::copy);
        bot.add(json, BorderLayout.WEST);
        bot.add(jsonField, BorderLayout.CENTER);
        bot.add(copy, BorderLayout.EAST);
        add(bot, BorderLayout.SOUTH);


        JPanel leftBox = getScrollPanel(this, BorderLayout.WEST, 160, 400);
        leftColumn(leftBox);

        JPanel itemsBox = getScrollPanel(this, BorderLayout.CENTER, 320, 400);
        rightArea(itemsBox);
    }

    private void rightArea(JPanel panel) {
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        textFieldOptions(panel);
    }

    private static JPanel getScrollPanel(JPanel panel, String placement, int w, int h) {
        JPanel boxScroll = new JPanel();
        JScrollPane scrollPane = new JScrollPane(boxScroll);
        scrollPane.getVerticalScrollBar().setUnitIncrement(40);
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setPreferredSize(new Dimension(w, h));
        contentPane.add(scrollPane);
        panel.add(contentPane, placement);
        scrollPane.setBounds(0, 0, w + 15, h);
        return boxScroll;
    }

    private void top(JPanel topPanel) {
        filterComboBox.setPreferredSize(new Dimension(230, 0));

        JPanel panel1 = new JPanel();
        topPanel.add(panel1, BorderLayout.NORTH);

        panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));
        JLabel name = new JLabel("Name:");
        nameField = new JTextField(15);
        panel1.add(Box.createHorizontalGlue());
        panel1.add(filterComboBox);

        panel1.add(name);
        panel1.add(nameField);
        panel1.add(Box.createHorizontalGlue());

        JPanel panel2 = new JPanel();
        topPanel.add(panel2, BorderLayout.SOUTH);
        panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS));
        JButton save = new JButton("Save");
        save.addActionListener(this::save);
        JButton load = new JButton("Load");
        load.addActionListener(this::load);
        JButton delete = new JButton("Delete");
        delete.addActionListener(this::deleteButton);
        JButton clear = new JButton("Clear");
        clear.addActionListener(this::clear);
        JButton paste = new JButton("Paste Json");
        paste.addActionListener(this::paste);
        panel2.add(Box.createHorizontalGlue());
        panel2.add(save);
        panel2.add(load);
        panel2.add(delete);
        panel2.add(clear);
        panel2.add(paste);
        panel2.add(Box.createRigidArea(new Dimension(10, 0)));
        panel2.add(Box.createHorizontalGlue());
    }

    private void search(ActionEvent actionEvent) {
        // only search if a new value is provided
        String searchValue = searchField.getText();
        if (searchValue.equals(currentSearchValue)) return;

        currentSearchValue = searchValue;
        updateItemsPanel(searchField.getText());
    }

    private void copy(ActionEvent actionEvent) {
        String text = jsonField.getText();
        if (text.isEmpty()) return;
        StringSelection stringSelection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    private void save(ActionEvent actionEvent) {
        String name = nameField.getText();
        if (!name.isEmpty()) {
            SecurityFilter sf = new SecurityFilter();
            sf.name = name;
            sf.isWhitelistFilter = this.toggleWhiteList.isSelected();
            for (FilterEntity item : items) {
                if (item.checkBox.isSelected()) {
                    sf.itemPoint.put(item.id, item.point);
                }
            }
            sf.exaltSkinPoints = exaltSkin.point;
            for (FilterEntity classPoint : classPoints) {
                sf.classPoint.put(classPoint.id, classPoint.point);
            }
            for (FilterEntity minTier : minTiers) {
                if (minTier.checkBox.isSelected()) sf.minTier.put(minTier.id, minTier.point);
            }
            for (int i = 0; i < checkBoxStats.size(); i++) {
                JCheckBox c = checkBoxStats.get(i);
                sf.statMaxed[i] = c.isSelected();
            }

            Gson gson = new Gson();
            sf.json = gson.toJson(sf);
            jsonField.setText(sf.json);

            saveSF(sf);
        }
    }

    private void saveSF(SecurityFilter sf) {
        if (parentPanel.getFilters().containsKey(sf.name)) {
            if (ask("Are you sure you want to overwrite: " + sf.name)) {
                parentPanel.getFilters().put(sf.name, sf);
                saveToProfile();
            }
        } else {
            parentPanel.getFilters().put(sf.name, sf);
            filterComboBox.addItem(sf.name);
            saveToProfile();
        }
    }

    private void saveToProfile() {
        StringBuilder str = new StringBuilder();
        int i = parentPanel.getFilters().size();
        for (SecurityFilter sf : parentPanel.getFilters().values()) {
            if (!sf.json.isEmpty()) {
                str.append(sf.json);

                // if there's at least one iteration left, add the delimiter
                if (i > 1) str.append("§");

                --i;
            }
        }
        if (str.length() > 0) {
            PropertiesManager.setProperties("securityFilters", str.toString());
        } else {
            PropertiesManager.setProperties("securityFilters", "");
        }
    }

    private void load(ActionEvent actionEvent) {
        String name = (String) filterComboBox.getSelectedItem();
        if (name == null) return;
        SecurityFilter sf = parentPanel.getFilters().get(name);
        if (sf == null) return;
        loadSF(sf);
    }

    private void loadSF(SecurityFilter sf) {
        nameField.setText(sf.name);
        filterComboBox.setSelectedItem(sf.name);

        for (int i = 0; i < checkBoxStats.size(); i++) {
            JCheckBox c = checkBoxStats.get(i);
            c.setSelected(sf.statMaxed[i]);
        }
        exaltSkinPointsField.setText(String.valueOf(sf.exaltSkinPoints));
        exaltSkin.point = sf.exaltSkinPoints;
        if (sf.minTier != null) {
            for (FilterEntity minTier : minTiers) {
                Integer v = sf.minTier.get(minTier.id);

                // default value in the event that it's missing
                minTier.field.setEnabled(v != null);
                minTier.checkBox.setSelected(v != null);
                if (v == null) v = 0;

                minTier.field.setText(String.valueOf(v));
                minTier.point = v;
            }
        }
        for (FilterEntity classPoint : classPoints) {
            Integer c = sf.classPoint.get(classPoint.id);

            // default value in the event that it's missing
            if (c == null) c = 0;

            classPoint.field.setText(String.valueOf(c));
            classPoint.point = c;
        }
        setItemSelectMode(sf.isWhitelistFilter);
        for (FilterEntity item : items) {
            Integer i = sf.itemPoint.get(item.id);
            if (i == null) {
                item.field.setText("");
                item.field.setEnabled(false);
                item.checkBox.setSelected(false);
                item.point = 0;
            } else {
                if (i != 0) {
                    item.field.setText(String.valueOf(i));
                } else {
                    item.field.setText("");
                }
                item.field.setEnabled(sf.isWhitelistFilter);
                item.checkBox.setSelected(true);
                item.point = i;
            }
        }
        jsonField.setText(sf.json);
    }

    private void deleteButton(ActionEvent actionEvent) {
        String n = (String) filterComboBox.getSelectedItem();
        if (!ask("Are you sure you want to delete: " + n)) return;
        filterComboBox.removeItem(n);
        parentPanel.getFilters().remove(n);
        saveToProfile();
    }

    private void clear(ActionEvent actionEvent) {
        clearAll();
    }

    private void paste(ActionEvent actionEvent) {
        Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable t = c.getContents(this);
        if (t == null)
            return;
        String json;
        try {
            json = (String) t.getTransferData(DataFlavor.stringFlavor);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        SecurityFilter sf = SecurityFilter.loadJson(json);
        if (sf == null) return;
        saveSF(sf);
        loadSF(sf);
    }

    private void clearAll() {
        nameField.setText("");
        for (JCheckBox c : checkBoxStats) {
            c.setSelected(false);
        }
        for (FilterEntity minTier : minTiers) {
            minTier.field.setText("");
            minTier.field.setEnabled(false);
            minTier.point = 0;
            minTier.checkBox.setSelected(false);
        }
        exaltSkinPointsField.setText("");
        exaltSkin.point = 0;
        for (FilterEntity classPoint : classPoints) {
            classPoint.field.setText("");
            classPoint.point = 0;
        }
        for (FilterEntity item : items) {
            item.field.setText("");
            item.field.setEnabled(false);
            item.checkBox.setSelected(false);
            item.point = 0;
        }
        jsonField.setText("");
    }

    private void toggleItem(FilterEntity item) {
        item.field.setEnabled(item.checkBox.isSelected() && getItemSelectMode());
    }

    private void onClickWhitelist(ActionEvent event) {
        setItemSelectMode(true);
    }

    private void onClickBlacklist(ActionEvent event) {
        setItemSelectMode(false);
    }

    private boolean getItemSelectMode() {
        return this.itemSelectMode;
    }

    private void setItemSelectMode(boolean isWhitelistMode) {
        boolean previousMode = getItemSelectMode();

        toggleWhiteList.setSelected(isWhitelistMode);
        toggleBlackList.setSelected(!isWhitelistMode);

        this.itemSelectMode = isWhitelistMode;

        if (previousMode != isWhitelistMode) updateItemsPanel(); // update the items panel if it's a mode change
    }

    private static class MinTierFocusListener implements FocusListener {

        @Override
        public void focusGained(FocusEvent e) {
            JTextComponent textField;
            if (e.getComponent() instanceof JTextComponent) textField = (JTextField) e.getComponent();
            else return;

            textField.setSelectionStart(0);
            textField.setSelectionEnd(textField.getText().length());
        }

        @Override
        public void focusLost(FocusEvent e) {
            // nothing
        }
    }

    private void leftColumn(JPanel panel) {
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        stats(panel);
        panel.add(new JSeparator(SwingConstants.HORIZONTAL));
        minTiers(panel);
        panel.add(new JSeparator(SwingConstants.HORIZONTAL));
        skinPoints(panel);
        panel.add(new JSeparator(SwingConstants.HORIZONTAL));
        classes(panel);
    }

    private void stats(JPanel mainPanel) {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel n = new JLabel("Stats Maxed");
        panel.add(n, BorderLayout.NORTH);

        JPanel stats = new JPanel(new GridLayout(8, 1));

        for (StatPotion o : StatPotion.values()) {
            JCheckBox box = new JCheckBox(o.name());
            checkBoxStats.add(box);
            stats.add(box);
        }

        panel.add(stats, BorderLayout.CENTER);

        mainPanel.add(panel);
    }

    private void minTiers(JPanel mainPanel) {
        JPanel panel = new JPanel(new BorderLayout());

        ArrayList<String> equipment = new ArrayList<String>() {{
            add("Weapon");
            add("Ability");
            add("Armor");
            add("Ring");
        }};

        JLabel n = new JLabel("Minimum Equipment Tiers");
        panel.add(n, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new GridLayout(4, 2));
        panel.add(body, BorderLayout.CENTER);

        IntStream.range(0, equipment.size()).forEachOrdered( i -> {
            FilterEntity minTier = new FilterEntity();
            minTiers.add(minTier);

            minTier.id = i;
            minTier.checkBox = new JCheckBox(equipment.get(i));
            minTier.field = addTextField(0, minTier);
            body.add(minTier.checkBox);
            body.add(minTier.field);

            minTier.field.setText("0");
            minTier.field.addFocusListener(new MinTierFocusListener());
            minTier.field.setEnabled(false);

            minTier.checkBox.addActionListener(e -> minTier.field.setEnabled(minTier.checkBox.isSelected()));
        });

        mainPanel.add(panel);
    }

    private void skinPoints(JPanel mainPanel) {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel n = new JLabel("Skin points");
        panel.add(n, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new GridLayout(1, 2));
        panel.add(body, BorderLayout.CENTER);

        exaltSkinPointsField = addTextField(1, exaltSkin);
        body.add(new JLabel("Exalted"));
        body.add(exaltSkinPointsField);

        mainPanel.add(panel);
    }

    private void classes(JPanel mainPanel) {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel n = new JLabel("Class Points");
        panel.add(n, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new GridLayout(CharacterClass.CHAR_CLASS_LIST.length, 2));
        panel.add(body, BorderLayout.CENTER);

        for (CharacterClass s : CharacterClass.CHAR_CLASS_LIST) {
            FilterEntity classPoint = new FilterEntity();
            classPoints.add(classPoint);

            JLabel label = new JLabel(CharacterClass.getName(s.getId()));
            classPoint.id = s.getId();
            classPoint.field = addTextField(1, classPoint);
            body.add(label);
            body.add(classPoint.field);
//    classPoint.field.setText("0");
        }


        mainPanel.add(panel);
    }

    private void textFieldOptions(JPanel mainPanel) {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel searchSelectPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        // add toggles
        JPanel togglePanel = new JPanel();
        toggleWhiteList = new JToggleButton("Whitelist Mode");
        toggleWhiteList.addActionListener(this::onClickWhitelist);
        toggleBlackList = new JToggleButton("Blacklist Mode");
        toggleBlackList.addActionListener(this::onClickBlacklist);
        togglePanel.add(toggleWhiteList);
        togglePanel.add(toggleBlackList);

        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridwidth = 5;
        c.weightx = 0.0;
        c.gridy = 0;
        searchSelectPanel.add(togglePanel, c);

        // add search bar
        searchField = new JTextField();
        searchField.addActionListener(this::search);
        c.anchor = GridBagConstraints.LINE_END;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridwidth = 4;
        c.weightx = 1.0;
        c.gridy = 1;
        searchSelectPanel.add(searchField, c);

        JButton searchButton = new JButton("->");
        searchButton.addActionListener(this::search);
        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.VERTICAL;
        c.gridx = 4;
        c.gridwidth = 1;
        c.weightx = 0.0;
        c.gridy = 1;
        searchSelectPanel.add(searchButton, c);

        panel.add(searchSelectPanel, BorderLayout.PAGE_START);

        JLabel n = new JLabel("Item Points");
        panel.add(n, BorderLayout.NORTH);

        itemsPanel = new JPanel();
        itemsPanel.setLayout(new GridBagLayout());
        panel.add(itemsPanel, BorderLayout.CENTER);

        createItemFilterEntities();
        updateItemsPanel();

        mainPanel.add(panel);
    }

    private void createItemFilterEntities() {
        // should only be run once
        if (!items.isEmpty()) System.err.println("Tried to generate item filter entities more than once?");

        ArrayList<ParseEquipment.Equipment> list = ParseEquipment.getParseItems();
        list.sort(Comparator.comparing(ParseEquipment.Equipment::name));

        for (ParseEquipment.Equipment e : list) {
            // omit some items
            if (OMITTED_SLOT_TYPES.contains(e.slotType)) continue;

            // TODO: omit gear covered by minimum equipment tiers

            FilterEntity item = new FilterEntity();
            items.add(item);
            item.id = e.id;
            item.field = addTextField(3, item);
            item.checkBox = new JCheckBox();

            item.checkBox.addActionListener(event -> toggleItem(item));
        }

    }

    private void updateItemsPanel() { updateItemsPanel(searchField.getText().isEmpty() ? null : searchField.getText()); }

    private void updateItemsPanel(String withSearch) {
        // Clear current item list
        itemsPanel.removeAll();

        ArrayList<ParseEquipment.Equipment> list = ParseEquipment.getParseItems();
        list.sort(Comparator.comparing(ParseEquipment.Equipment::name));


        int count = 0;
        for (FilterEntity itemFilterEntity : items) {
            GridBagConstraints c = new GridBagConstraints();
            ParseEquipment.Equipment e = ParseEquipment.getEquipmentById(itemFilterEntity.id);

            // omit some items
            if (OMITTED_SLOT_TYPES.contains(e.slotType)) continue;

            // TODO: omit gear covered by minimum equipment tiers

            // basic search
            String entitySearchName = e.name().toLowerCase();
            String[] entitySearchLabels = e.labels.toLowerCase().split(",");
            String searchName = withSearch != null ? withSearch.toLowerCase() : "";

            boolean searchByName = entitySearchName.contains(searchName);
            boolean searchByLabel = Arrays.asList(entitySearchLabels).contains(searchName);
            if (withSearch != null && !searchByName && !searchByLabel) continue;

            c.gridy = count;
            c.anchor = GridBagConstraints.LINE_START;

            // add checkbox + field value
            c.gridx = 0;
            itemsPanel.add(itemFilterEntity.field, c);
            c.gridx = 1;
            itemsPanel.add(itemFilterEntity.checkBox, c);
            itemFilterEntity.field.setEnabled(itemFilterEntity.checkBox.isSelected() && getItemSelectMode());

            // add icons + name
            JLabel icon = new JLabel(ImageBuffer.getOutlinedIcon(e.id, 24));
            icon.setText(e.name());
            icon.setHorizontalAlignment(JLabel.LEFT);
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 2;
            c.weightx = 1.0;
            c.insets = new Insets(0,10,0,0);
            itemsPanel.add(icon, c);

            count++;
        }

        this.updateUI();
    }

    private static JTextField addTextField(int withNumbers, FilterEntity entity) {
        JTextField comp = new JTextField(withNumbers);
        comp.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char character = e.getKeyChar();
                if (!((character >= '0') && (character <= '9')) && (character != '-')) {
                    e.consume();
                }
            }

            public void keyReleased(KeyEvent e) {
                String s = comp.getText();
                try {
                    entity.point = Integer.parseInt(s);
                } catch (NumberFormatException exception) {
                    if (!s.isEmpty()) {
                        comp.setText(String.valueOf(entity.point));
                    }
                }
            }
        });
        return comp;
    }

    private static boolean ask(String message) {
        int dialogResult = JOptionPane.showConfirmDialog(null, message, "Warning", JOptionPane.YES_NO_OPTION);
        return dialogResult == 0;
    }

    public static void open(ParsePanelGUI parsePanelGUI) {
        SecurityFilterGUI filter = new SecurityFilterGUI(parsePanelGUI);

        JButton close = new JButton("Close");
        JOptionPane pane = new JOptionPane(filter, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, new JButton[]{close}, close);
        close.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(close);
            pane.setValue(-1);
            w.dispose();
            parsePanelGUI.filterUpdate();
        });
        JDialog dialog = pane.createDialog(null, "Security Filter");
//        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // Load the currently-active filter (if there is one)
        if (ParsePanelGUI.currentFilter != null) {
            filter.loadSF(ParsePanelGUI.currentFilter);
        }

        dialog.setVisible(true);
    }

    private static class FilterEntity {
        int id;
        int point;
        JCheckBox checkBox;
        JTextField field;
    }
}
