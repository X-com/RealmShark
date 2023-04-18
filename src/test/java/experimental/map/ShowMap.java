package experimental.map;

import util.ImageBuffer;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class ShowMap {
    private static final String FOLDER = "./";
    private ArrayList<String> list;
    private Canvas canvas;
    private JLabel mapName;
    private int mapIndex;
    private MapData map;
    private JList jList;

    public static void main(String[] args) {
        new ShowMap();
    }

    public ShowMap() {
        JFrame frame = new JFrame();
        JPanel panel = new JPanel();

        canvas = new Canvas() {
            @Override
            public void paint(Graphics g) {
                render((Graphics2D) g);
            }
        };
        canvas.setBackground(new Color(0, 0, 0));
        canvas.setSize(72 * 10, 72 * 10);
        panel.add(canvas);

        frame.setLayout(new BorderLayout());

        JButton prev = new JButton("Prev");
        JButton next = new JButton("Next");
        JButton save = new JButton("Save");
        mapName = new JLabel("Map name");

        save.addActionListener(e -> saveMap());
        prev.addActionListener(e -> prevMap());
        next.addActionListener(e -> nextMap());

        Panel buttons = new Panel();
        buttons.add(prev);
        buttons.add(save);
        buttons.add(next);
        Panel top = new Panel();
        top.add(mapName);
        list = LoadMapData.getMapNames(FOLDER, "mapdata");

        jList = new JList();
        jList.setModel(new AbstractListModel() {

            String[] strings = list.toArray(new String[0]);

            @Override
            public int getSize() {
                return strings.length;
            }

            @Override
            public Object getElementAt(int i) {
                return strings[i];
            }
        });
        jList.addListSelectionListener(this::click);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(jList);
        jList.setLayoutOrientation(JList.VERTICAL);

        frame.add(scrollPane, BorderLayout.WEST);
        frame.add(top, BorderLayout.NORTH);
        frame.add(buttons, BorderLayout.SOUTH);
        frame.add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void click(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        Object o = e.getSource();
        if (o instanceof JList) {
            JList list = (JList) e.getSource();
            mapIndex = list.getSelectedIndex();
            selectMap();
        }
    }

    public void render(Graphics2D g) {
        renderMap(g);
        g.dispose();
    }

    public void renderMap(Graphics2D g) {
        if (map == null) return;
        BufferedImage bi = new BufferedImage(map.width, map.height, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < bi.getWidth(); x++) {
            for (int y = 0; y < bi.getHeight(); y++) {
                int id = map.mapArray[x][y];
                bi.setRGB(x, y, ImageBuffer.getColor(id));
            }
        }

        g.drawImage(bi, 0, 0, canvas.getWidth(), canvas.getHeight(), null);
    }

    private void prevMap() {
        if (list.size() == 0) return;

        mapIndex--;
        if (mapIndex < 0) mapIndex = 0;

        jList.setSelectedIndex(mapIndex);
        selectMap();
    }

    private void nextMap() {
        if (list.size() == 0) return;

        mapIndex++;
        if (mapIndex >= list.size()) mapIndex = list.size() - 1;

        jList.setSelectedIndex(mapIndex);
        selectMap();
    }

    private void selectMap() {
        map = LoadMapData.loadMap(FOLDER + list.get(mapIndex) + ".mapdata");
        mapName.setText(list.get(mapIndex).replaceFirst(".mapdata", ""));
        canvas.repaint();
    }

    private void saveMap() {
        MapData.printTiles(map.mapArray, FOLDER + mapName.getText());
    }
}
