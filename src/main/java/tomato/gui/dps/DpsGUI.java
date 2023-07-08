package tomato.gui.dps;

import tomato.backend.data.Entity;
import tomato.backend.data.TomatoData;
import util.PropertiesManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class DpsGUI extends JPanel {

    private static DpsGUI INSTANCE;

    private TomatoData data;
    private JButton next, prev;
    private DisplayDpsGUI display;
    private static JTextField textFilter;
    private static JCheckBox textFilterToggle;
    private static JLabel dpsLabel;
    private JPanel dpsTopPanel;
    private JPanel center;
    private boolean liveUpdates = true;
    private int index = 0;

    public DpsGUI(TomatoData data) {
        INSTANCE = this;

        this.data = data;

        next = new JButton("  Next  ");
        prev = new JButton("Previous");

        next.addActionListener(event -> nextDpsLogDungeon());
        prev.addActionListener(event -> previousDpsLogDungeon());

        dpsLabel = new JLabel("Live");
        textFilter = new JTextField();
        textFilter.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String text = textFilter.getText();
                PropertiesManager.setProperties("nameFilter", text);
                DpsDisplayOptions.filteredStrings = text.split(" ");
                updateGui();
            }
        });
        textFilterToggle = new JCheckBox();
        textFilterToggle.setSelected(true);
        textFilterToggle.addActionListener(event -> {
            boolean selected = textFilterToggle.isSelected();
            textFilter.setEnabled(selected);
            PropertiesManager.setProperties("toggleFilter", selected ? "T" : "F");
            DpsDisplayOptions.nameFilter = selected;
            updateGui();
        });

        dpsTopPanel = new JPanel();
        dpsTopPanel.setLayout(new BoxLayout(dpsTopPanel, BoxLayout.X_AXIS));
        dpsTopPanel.add(Box.createHorizontalGlue());
        dpsTopPanel.add(textFilterToggle);
        dpsTopPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        dpsTopPanel.add(textFilter);
        dpsTopPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        dpsTopPanel.add(prev);
        dpsTopPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        dpsTopPanel.add(dpsLabel);
        dpsTopPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        dpsTopPanel.add(next);
        dpsTopPanel.add(Box.createHorizontalGlue());

        setLayout(new BorderLayout());
        add(dpsTopPanel, BorderLayout.NORTH);

        center = new JPanel();
        center.setLayout(new BorderLayout());
        add(center, BorderLayout.CENTER);

        setCenterDisplay();
    }

    private void setCenterDisplay() {
        display = new StringDpsGUI();

        center.removeAll();
        center.add(display);
    }

    public static void updateNewTickPacket(TomatoData data) {
        if (!INSTANCE.liveUpdates) return;
        INSTANCE.display.renderData(data.getEntityHitList(), true);
    }

    public static void editFont(Font font) {
        INSTANCE.display.editFont(font);
    }

    /**
     * Clear the DPS logs.
     */
    public static void clearDpsLogs() {
        INSTANCE.data.dpsData.clear();
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

    /**
     * Updates the display label tracking dungeon index.
     */
    public static void updateLabel() {
        if (INSTANCE.liveUpdates) return;
        int size = INSTANCE.data.dpsData.size();
        dpsLabel.setText((INSTANCE.index + 1) + "/" + size);
    }

    /**
     * Loads the filter preset chosen by the user.
     */
    public static void loadFilterPreset() {
        String nameFilter = PropertiesManager.getProperty("nameFilter");
        String toggleFilter = PropertiesManager.getProperty("toggleFilter");

        if (nameFilter != null) {
            textFilter.setText(nameFilter);
        }
        if (toggleFilter != null) {
            boolean toggled = toggleFilter.equals("T");
            textFilterToggle.setSelected(toggled);
            textFilter.setEnabled(toggled);
        }
    }

    public static void update() {
        INSTANCE.updateGui();
    }

    private void updateGui() {
        if (liveUpdates) {
            display.renderData(data.getEntityHitList(), true);
        } else {
            Entity[] entityHitList = data.dpsData.get(index).hitList.values().toArray(new Entity[0]);
            display.renderData(entityHitList, false);
        }
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
        } else if (index >= size) {
            liveUpdates = true;
            dpsLabel.setText("Live");
            return;
        } else if (index < 0) {
            index = 0;
            return;
        }
        dpsLabel.setText((index + 1) + "/" + size);

        Entity[] entityHitList = data.dpsData.get(index).hitList.values().toArray(new Entity[0]);
        display.renderData(entityHitList, false);
    }
}
