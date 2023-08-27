package tomato.gui.dps;

import packets.incoming.MapInfoPacket;
import packets.incoming.NotificationPacket;
import tomato.backend.data.DpsData;
import tomato.backend.data.Entity;
import tomato.backend.data.TomatoData;
import util.PropertiesManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class DpsGUI extends JPanel {

    private static final String DISABLE_FILTER = "Default";
    private static DpsGUI INSTANCE;

    private TomatoData data;
    private JButton next, prev, live, dList;
    private StringDpsGUI displayString;
    private IconDpsGUI displayIcon;
    private DisplayDpsGUI centerDisplay;
    private JPanel dpsTopPanel;
    private JPanel center;
    private boolean liveUpdates = true;
    private int index = 0;
    private JComboBox<String> filterComboBox;
    private HashMap<String, String> filterList = new HashMap<>();

    public DpsGUI(TomatoData data) {
        INSTANCE = this;

        this.data = data;

        next = new JButton(">");
        prev = new JButton("<");
        live = new JButton(">>>");
        dList = new JButton("Live");

        next.addActionListener(event -> nextDpsLogDungeon());
        prev.addActionListener(event -> previousDpsLogDungeon());
        live.addActionListener(event -> setLive());
        dList.addActionListener(event -> dListButton(dList));

//        textFilter = new JTextField();
//        textFilter.addKeyListener(new KeyAdapter() {
//            public void keyReleased(KeyEvent e) {
//                String text = textFilter.getText();
//                PropertiesManager.setProperties("nameFilter", text);
//                DpsDisplayOptions.filteredStrings = text.split(" ");
//                updateGui();
//            }
//        });
//        textFilterToggle = new JCheckBox();
//        textFilterToggle.setSelected(true);
//        textFilterToggle.addActionListener(event -> {
//            boolean selected = textFilterToggle.isSelected();
//            textFilter.setEnabled(selected);
//            PropertiesManager.setProperties("toggleFilter", selected ? "T" : "F");
//            DpsDisplayOptions.nameFilter = selected;
//            updateGui();
//        });
        JButton addFilter = new JButton("+");
        addFilter.addActionListener(e -> openFilter());
        filterComboBox = new JComboBox<>(new String[]{DISABLE_FILTER});
        filterComboBox.setPreferredSize(new Dimension(10000, 0));
        filterComboBox.addActionListener(this::comboAction);

        dpsTopPanel = new JPanel();
        dpsTopPanel.setLayout(new BoxLayout(dpsTopPanel, BoxLayout.X_AXIS));
        dpsTopPanel.add(Box.createHorizontalGlue());
        dpsTopPanel.add(addFilter);
        dpsTopPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        dpsTopPanel.add(filterComboBox);
        dpsTopPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        dpsTopPanel.add(prev);
        dpsTopPanel.add(dList);
        dpsTopPanel.add(next);
        dpsTopPanel.add(live);
        dpsTopPanel.add(Box.createHorizontalGlue());

        setLayout(new BorderLayout());
        add(dpsTopPanel, BorderLayout.NORTH);

        center = new JPanel();
        center.setLayout(new BorderLayout());
        add(center, BorderLayout.CENTER);

        displayString = new StringDpsGUI(data);
        displayIcon = new IconDpsGUI(data);
        centerDisplay = displayIcon;
        setCenterDisplay();
    }

    private void dListButton(JButton dpsLabel) {
        DungeonListGUI.open(this, data);
    }

    private void comboAction(ActionEvent actionEvent) {
        JComboBox<String> combo = (JComboBox<String>) actionEvent.getSource();
        String selectedItem = String.valueOf(combo.getSelectedItem());
        setupFilter(selectedItem);
        PropertiesManager.setProperties("filterName", selectedItem);
        update();
    }

    private static void setupFilter(String selectedItem) {
        if (selectedItem.equals(DISABLE_FILTER)) {
            Filter.disable();
            return;
        }
        String s = INSTANCE.filterList.get(selectedItem);
        Filter.selectFilter(s);
    }

    public static String systemTimeToString(long time) {
        if (time == 0) return " [-]";
        long ms = time % 1000;
        if (time < 1000) return String.format(" [%dms]", ms);
        long s = time / 1000 % 60;
        if (time < 60000) return String.format(" [%ds %dms]", s, ms);
        long m = time / 60000 % 60;
        if (time < 3600000) return String.format(" [%dm %ds %dms]", m, s, ms);
        long h = time / 3600000;
        return String.format(" [%dh %dm %ds %dms]", h, m, s, ms);
    }

    private void openFilter() {
        FilterGUI.open(this);
    }

    private void setCenterDisplay() {
        DisplayDpsGUI display;
        if (liveUpdates || DpsDisplayOptions.equipmentOption < 3) {
            display = displayString;
        } else if (DpsDisplayOptions.equipmentOption == 3) {
            display = displayIcon;
        } else {
            return;
        }

        if (display.getClass() == centerDisplay.getClass()) return;

        centerDisplay = display;
        center.removeAll();
        center.add(display);
        repaint();
    }

    public static void updateNewTickPacket(TomatoData data) {
        if (!INSTANCE.liveUpdates) return;
        INSTANCE.renderData(data.map, data.getEntityHitList(), data.getDeathNotifications(), data.dungeonTime(), true);
    }

    private void renderData(MapInfoPacket map, Entity[] entityHitList, ArrayList<NotificationPacket> notifications, long totalDungeonPcTime, boolean b) {
        setCenterDisplay();
        List<Entity> sortedEntityHitList = getSortedEntityList(entityHitList);
        centerDisplay.renderData(map, sortedEntityHitList, notifications, totalDungeonPcTime, b);
    }

    private List<Entity> getSortedEntityList(Entity[] entityHitList) {
        if (DpsDisplayOptions.sortOption == 1) {
            return Arrays.stream(entityHitList).sorted(Comparator.comparingLong(Entity::getFirstDamageTaken).reversed()).collect(Collectors.toList());
        } else if (DpsDisplayOptions.sortOption == 2) {
            return Arrays.stream(entityHitList).sorted(Comparator.comparingLong(Entity::maxHp).reversed()).collect(Collectors.toList());
        } else if (DpsDisplayOptions.sortOption == 3) {
            return Arrays.stream(entityHitList).sorted(Comparator.comparingLong(Entity::getFightTimer).reversed()).collect(Collectors.toList());
        } else {
            return Arrays.stream(entityHitList).sorted(Comparator.comparingLong(Entity::getLastDamageTaken).reversed()).collect(Collectors.toList());
        }
    }

    public static void editFont(Font font) {
        INSTANCE.displayString.editFont(font);
        INSTANCE.displayIcon.editFont(font);
        update();
    }

    public String getFilterString(String a) {
        return filterList.get(a);
    }

    public String[] getComboBoxStrings() {
        return filterList.keySet().toArray(new String[0]);
    }

    public boolean addComboBox(String a, String b) {
        boolean add = false;
        if (!filterList.containsKey(a)) {
            filterComboBox.addItem(a);
            add = true;
        }
        filterList.put(a, b);
        saveFilterProperty();
        return add;
    }

    public void removeComboBox(String o) {
        filterComboBox.removeItem(o);
        filterList.remove(o);
        saveFilterProperty();
    }

    /**
     * Clear the DPS logs.
     */
    public static void clearDpsLogs() {
        INSTANCE.data.dpsData.clear();
        INSTANCE.liveUpdates = true;
        INSTANCE.dList.setText("Live");
        update();
    }

    /**
     * Next dungeon displayed by dps calculator.
     */
    public static void nextDpsLogDungeon() {
        INSTANCE.scrollData(1);
    }

    /**
     * Previous dungeon displayed by dps calculator.
     */
    public static void previousDpsLogDungeon() {
        INSTANCE.scrollData(-1);
    }

    private void setLive() {
        INSTANCE.scrollData(1000000000);
    }

    /**
     * Updates the display label tracking dungeon index.
     */
    public static void updateLabel() {
        if (INSTANCE.liveUpdates) return;
        int size = INSTANCE.data.dpsData.size();
        INSTANCE.dList.setText((INSTANCE.index + 1) + "/" + size);
    }

    /**
     * Saves the preset chosen by the user.
     */
    private void saveFilterProperty() {
        StringBuilder sb = new StringBuilder();
        for (String v : filterList.values()) {
            sb.append(v).append("\n");
        }
        String substring;
        if (sb.length() > 0) {
            substring = sb.substring(0, sb.length() - 1);
        } else {
            substring = "";
        }
        PropertiesManager.setProperties("filters", substring);
    }

    /**
     * Loads the filter preset chosen by the user.
     */
    public static void loadFilterPreset() {
        String f = PropertiesManager.getProperty("filters");
        if (f != null) {
            String[] lines = f.split("\n");
            for (String l : lines) {
                String name = l.split(",")[0];
                INSTANCE.filterList.put(name, l);
                INSTANCE.filterComboBox.addItem(name);
            }
        }

        String nameFilter = PropertiesManager.getProperty("filterName");
        if (nameFilter != null) {
            setupFilter(nameFilter);
            INSTANCE.filterComboBox.setSelectedItem(nameFilter);
        } else {
            Filter.disable();
            INSTANCE.filterComboBox.setSelectedItem(DISABLE_FILTER);
        }
    }

    public static void update() {
        INSTANCE.updateGui();
    }

    private void updateGui() {
        if (liveUpdates) {
            renderData(data.map, data.getEntityHitList(), data.getDeathNotifications(), data.dungeonTime(), true);
        } else {
            DpsData dpsData = data.dpsData.get(index);
            Entity[] entityHitList = dpsData.hitList.values().toArray(new Entity[0]);
            renderData(dpsData.map, entityHitList, dpsData.deathNotifications, dpsData.totalDungeonPcTime, false);
        }
    }

    public int getIndex() {
        if (liveUpdates) return -1;
        return index;
    }

    public void setIndex(int index) {
        this.index = index;

        if (index == -1) {
            liveUpdates = true;
            dList.setText("Live");
            return;
        } else {
            liveUpdates = false;
            int size = data.dpsData.size();
            dList.setText((index + 1) + "/" + size);
        }

        setCenterDisplay();
        DpsData dpsData = data.dpsData.get(index);
        Entity[] entityHitList = dpsData.hitList.values().toArray(new Entity[0]);
        renderData(dpsData.map, entityHitList, dpsData.deathNotifications, dpsData.totalDungeonPcTime, false);
    }

    private void scrollData(int a) {
        int size = data.dpsData.size();
        index += a;
        if (liveUpdates) {
            if (a > 0 || size == 0) {
                return;
            }
            index = size - 1;
            liveUpdates = false;
            setCenterDisplay();
        } else if (index >= size) {
            liveUpdates = true;
            dList.setText("Live");
            setCenterDisplay();
            return;
        } else if (index < 0) {
            index = 0;
            return;
        }
        dList.setText((index + 1) + "/" + size);

        DpsData dpsData = data.dpsData.get(index);
        Entity[] entityHitList = dpsData.hitList.values().toArray(new Entity[0]);
        renderData(dpsData.map, entityHitList, dpsData.deathNotifications, dpsData.totalDungeonPcTime, false);
    }
}