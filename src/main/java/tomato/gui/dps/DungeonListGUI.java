package tomato.gui.dps;

import tomato.backend.data.DpsData;
import tomato.backend.data.TomatoData;

import javax.swing.filechooser.FileFilter;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DungeonListGUI extends JPanel {

    private DpsGUI dps;
    private TomatoData data;
    private int index;
    private JPanel livePanel;
    private ArrayList<JPanel> pList = new ArrayList<>();
    private static ArrayList<DpsDungeon> selectionList = new ArrayList<>();
    private JPanel boxScroll;

    public DungeonListGUI(DpsGUI dps, TomatoData data) {
        this.dps = dps;
        this.data = data;
        setLayout(new BorderLayout());

        JPanel buttons = new JPanel();
        JButton load = new JButton("Load");
        JButton save = new JButton("Save");
        buttons.add(load);
        buttons.add(save);
        load.addActionListener(e -> loadButton());
        save.addActionListener(e -> saveButton());
        add(buttons, BorderLayout.NORTH);

        selectionList.clear();
        boxScroll = new JPanel();
        JScrollPane scrollPane = new JScrollPane(boxScroll);
        scrollPane.getVerticalScrollBar().setUnitIncrement(40);
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setPreferredSize(new Dimension(300, 200));
        contentPane.add(scrollPane);
        add(contentPane, BorderLayout.CENTER);

        boxScroll.setLayout(new BoxLayout(boxScroll, BoxLayout.Y_AXIS));

        index = dps.getIndex();
        createDungeonList();
    }

    private void reCreateDungeonList() {
        pList.clear();
        selectionList.clear();
        boxScroll.removeAll();
        createDungeonList();
        revalidate();
    }

    private void createDungeonList() {
        int dataSize = data.dpsData.size();
        livePanel = dungeonSelection(dps, null, -1, "", "Live");
        boxScroll.add(livePanel);
        for (int i = dataSize - 1; i >= 0; i--) {
            DpsData d = data.dpsData.get(i);
            addDungeonToList(d, i, dataSize);
        }
    }

    private void addDungeonToList(DpsData d, int i, int dataSize) {
        String name;
        if (d.map.name.equals("Realm of the Mad God")) {
            name = String.format("Realm - %s", d.map.realmName.substring(12));
        } else {
            name = d.map.name;
        }
        JPanel p1 = dungeonSelection(dps, d, i, String.format(" %d / %d", i + 1, dataSize), name);
        pList.add(p1);
        boxScroll.add(p1);
    }

    private JPanel dungeonSelection(DpsGUI dps, DpsData data, int i, String s, String name) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setMaximumSize(new Dimension(300, 20));

        DpsDungeon d = DpsDungeon.add(i, data, name);

        p.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                select(dps, i);
            }
        });

        JPanel p1 = new JPanel();
        JPanel p2 = new JPanel();
        JPanel p3 = new JPanel();

        p1.setLayout(new BorderLayout());
        p2.setLayout(new BorderLayout());
        p3.setLayout(new BorderLayout());

        if (i >= 0) {
            JCheckBox checkBox = new JCheckBox();
            p1.add(checkBox);
            d.checkBox = checkBox;
        }
        p1.setPreferredSize(new Dimension(0, 0));
        p2.setPreferredSize(new Dimension(20, 0));
        p2.add(new JLabel(s));
        p3.setPreferredSize(new Dimension(150, 20));
        p3.add(new JLabel(name));

        Color c = UIManager.getColor("selectionBackground");
        p1.setBackground(c);
        p2.setBackground(c);
        p3.setBackground(c);

        p.add(Box.createHorizontalGlue());
        p.add(p1);
        p.add(p2);
        p.add(p3);
        p.add(Box.createHorizontalGlue());

        d.setPanels(p1, p2, p3);
        DpsDungeon.select(index);

        return p;
    }

    private void loadButton() {
        JFileChooser fc = new JFileChooser();
        fc.setAcceptAllFileFilterUsed(false);
        try {
            fc.setCurrentDirectory(new File(new File(".").getCanonicalPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                return f.getName().endsWith(".dps");
            }

            @Override
            public String getDescription() {
                return ".dps";
            }
        };
        fc.setFileFilter(fileFilter);
        int returnVal = fc.showDialog(this, "Load Dps File");
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try {
                FileInputStream fi = new FileInputStream(f);
                ObjectInputStream o = new ObjectInputStream(fi);
                DpsData d = (DpsData) o.readObject();
                data.dpsData.add(d);
                fi.close();
                o.close();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            reCreateDungeonList();
        }
    }

    private void saveButton() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        try {
            fc.setCurrentDirectory(new File(new File(".").getCanonicalPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        fc.setAccessory(new CheckBoxAccessory());

        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            CheckBoxAccessory cba = (CheckBoxAccessory)fc.getAccessory();
            boolean saveDebugData = cba.isBoxSelected();

            File folder = fc.getSelectedFile();
            for (DpsDungeon d : selectionList) {
                if (d.checkBox != null && d.checkBox.isSelected()) {
                    d.save(folder, saveDebugData);
                }
            }
        }
    }

    private void select(DpsGUI dps, int next) {
        if (next == index) return;
        DpsDungeon.select(next);
        index = next;
        dps.setIndex(next);
    }

    public static void open(DpsGUI dps, TomatoData data) {
        DungeonListGUI dList = new DungeonListGUI(dps, data);
        JButton close = new JButton("Close");
        JOptionPane pane = new JOptionPane(dList, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, new JButton[]{close}, close);
        close.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(close);
            pane.setValue(-1);
            w.dispose();
        });
        JDialog dialog = pane.createDialog(dps, "Dungeon List");
        dialog.pack();
        dialog.setResizable(true);
        dialog.setVisible(true);
    }

    public static class DpsDungeon {
        private static DpsDungeon lastSelection = null;
        private final String name;
        private final DpsData data;
        private final int i;
        private JCheckBox checkBox;
        private JPanel p1;
        private JPanel p2;
        private JPanel p3;

        public DpsDungeon(int i, DpsData data, String name) {
            this.i = i;
            this.data = data;
            this.name = name;
        }

        public void save(File folder, boolean saveDebugData) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
            Date date = new Date(data.dungeonStartTime);
            String fileName = folder + "\\" + name + " " + simpleDateFormat.format(date) + ".dps ";

            try {
                FileOutputStream f = new FileOutputStream(fileName);
                ObjectOutputStream o = new ObjectOutputStream(f);
                DpsData saveData = data.getSaveFile(saveDebugData);
                o.writeObject(saveData);
                o.close();
                f.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void setPanels(JPanel p1, JPanel p2, JPanel p3) {
            this.p1 = p1;
            this.p2 = p2;
            this.p3 = p3;
        }

        public static DpsDungeon add(int i, DpsData data, String name) {
            DpsDungeon d = new DpsDungeon(i, data, name);
            selectionList.add(d);
            return d;
        }

        public static void select(int i) {
            if (lastSelection != null) lastSelection.selected(false);
            for (DpsDungeon d : selectionList) {
                if (d.i == i) {
                    lastSelection = d;
                    d.selected(true);
                    return;
                }
            }
        }

        public void selected(boolean isSelected) {
            if (isSelected) {
                p1.setBackground(Color.GRAY);
                p2.setBackground(Color.GRAY);
                p3.setBackground(Color.GRAY);
            } else {
                Color c = UIManager.getColor("selectionBackground");
                p1.setBackground(c);
                p2.setBackground(c);
                p3.setBackground(c);
            }
        }
    }

    public class CheckBoxAccessory extends JComponent {
        JCheckBox checkBox;
        boolean checkBoxInit = false;

        int preferredWidth = 150;
        int preferredHeight = 100;
        int checkBoxPosX = 5;
        int checkBoxPosY = 20;
        int checkBoxWidth = preferredWidth;
        int checkBoxHeight = 20;

        public CheckBoxAccessory() {
            setPreferredSize(new Dimension(preferredWidth, preferredHeight));
            checkBox = new JCheckBox("Save Debug Data", checkBoxInit);
            checkBox.setBounds(checkBoxPosX, checkBoxPosY, checkBoxWidth, checkBoxHeight);
            add(checkBox);
        }

        public boolean isBoxSelected() {
            return checkBox.isSelected();
        }
    }
}
