package tomato.gui.character;

import assets.ImageBuffer;
import tomato.realmshark.RealmCharacter;
import tomato.backend.data.TomatoData;
import tomato.realmshark.enums.CharacterClass;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class CharacterExaltGUI extends JPanel {

    private static CharacterExaltGUI INSTANCE;

    private final JLabel[][] grid;
    private final TomatoData data;
    private final int charListSize;

    public CharacterExaltGUI(TomatoData data) {
        INSTANCE = this;
        this.data = data;

        JPanel top = new JPanel();
        JPanel left = new JPanel();
        JPanel right = new JPanel();
        JPanel topLeft = new JPanel();

        JScrollPane spLeft = new JScrollPane(left);
        JScrollPane spRight = new JScrollPane(right);

        spRight.getVerticalScrollBar().setModel(spLeft.getVerticalScrollBar().getModel());
        spRight.getVerticalScrollBar().setUnitIncrement(9);

        spLeft.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        spRight.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        spRight.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        setLayout(new BorderLayout());
        JPanel leftBar = new JPanel();
        JPanel rightBar = new JPanel();
        leftBar.setLayout(new BorderLayout());
        rightBar.setLayout(new BorderLayout());
        add(leftBar, BorderLayout.WEST);
        add(rightBar, BorderLayout.CENTER);
        topLeft.setPreferredSize(new Dimension(0, 27));

        leftBar.add(topLeft, BorderLayout.NORTH);
        leftBar.add(spLeft, BorderLayout.CENTER);
        rightBar.add(top, BorderLayout.NORTH);
        rightBar.add(spRight, BorderLayout.CENTER);

        charListSize = CharacterClass.CHAR_CLASS_LIST.length;
        grid = new JLabel[charListSize + 2][8];

        fixTop(top);
        fixRealmClasses(left);
        fixGrid(right);
    }

    private void fixTop(JPanel top) {
        String[] exaltList = {"Life", "Mana", "Atk", "Def", "Spd", "Dex", "Vit", "Wis"};
        top.setLayout(new GridLayout(1, exaltList.length));

        for (String ex : exaltList) {
            JLabel exalts = new JLabel(ex);

            JPanel p = new JPanel();
            p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.gray));
            p.add(exalts);
            p.setPreferredSize(new Dimension(35, 27));
            top.add(p);
        }
    }

    private void fixRealmClasses(JPanel left) {
        left.setLayout(new GridLayout(charListSize + 2, 1));
        for (int i = 0; i < charListSize + 2; i++) {
            JLabel classes;
            if (i < charListSize) {
                CharacterClass c = CharacterClass.CHAR_CLASS_LIST[i];
                classes = classIcon(c.getId(), c.toString());
            } else if (i == charListSize) {
                classes = new JLabel("Total");
            } else {
                classes = new JLabel("Missing");
            }
            JPanel p = new JPanel(new GridBagLayout());
            p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.gray));
            p.setPreferredSize(new Dimension(110, 27));
            p.add(classes);

            left.add(p);
        }
    }

    private void fixGrid(JPanel right) {
        right.setLayout(new GridLayout(charListSize + 2, 8));

        for (int i = 0; i < charListSize + 2; i++) {
            for (int j = 0; j < 8; j++) {
                JPanel p = new JPanel();
                p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.gray));
                grid[i][j] = new JLabel("-");
                p.add(grid[i][j]);
                right.add(p);
            }
        }
    }

    /**
     * Method for receiving realm character list info.
     */
    public static void updateRealmChars() {
        INSTANCE.update();
    }

    /**
     * Update exalt tab with exalt stats.
     */
    public static void updateExalts() {
        INSTANCE.update();
    }

    /**
     * Update exalt stats
     */
    private void update() {
        if (data.chars == null) return;

        int[][] exalts = RealmCharacter.exalts.values().toArray(new int[0][]);
        int[] sum = new int[8];
        int[] missing = new int[8];

        for (int i = 0; i < exalts.length; i++) {
            int[] e = exalts[i];
            for (int j = 0; j < 8; j++) {
                sum[j] += e[j];
                missing[j] += Math.max(75 - e[j], 0);
                grid[i][j].setText("" + e[j]);
            }
        }

        for (int j = 0; j < 8; j++) {
            grid[charListSize][j].setText("" + sum[j]);
        }

        for (int j = 0; j < 8; j++) {
            grid[charListSize + 1][j].setText("" + missing[j]);
        }
    }

    private JLabel classIcon(int skin, String classString) {
        try {
            BufferedImage img = ImageBuffer.getImage(skin);
            ImageIcon icon = new ImageIcon(img.getScaledInstance(15, 15, Image.SCALE_DEFAULT));
            return new JLabel(classString, icon, JLabel.CENTER);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
