package tomato.gui.stats;

import java.awt.*;
import javax.swing.*;
import tomato.backend.data.TomatoData;

public class StatisticsGUI extends JPanel {

    public StatisticsGUI(TomatoData data) {
        setLayout(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();
        add(tabbedPane);

        FameTrackerGUI fameTracker = new FameTrackerGUI();
        tabbedPane.addTab("Fame Graph", fameTracker);

        FameTablePanel fameTable = new FameTablePanel(data);
        tabbedPane.addTab("Fame Table", fameTable);

        // Initialize and connect the fame table bridge
        FameTableBridge.initialize();
        FameTableBridge bridge = FameTableBridge.getInstance();
        bridge.setFameTablePanel(fameTable);
        bridge.setFameTrackerGUI(fameTracker);

        LootGUI loot = new LootGUI(data);
        tabbedPane.addTab("Loot", loot);
        DungeonStats dungeonStats = new DungeonStats();
        tabbedPane.addTab("Dungeon Stats", dungeonStats);
    }
}
