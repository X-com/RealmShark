package tomato.gui;

import assets.AssetMissingException;
import assets.IdToAsset;
import assets.ImageBuffer;
import tomato.logic.backend.data.RealmCharacter;
import tomato.logic.enums.CharacterStatistics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Dungeon completion display GUI class showing all dungeon completions of the users account.
 */
public class CharacterStatsGUI extends JPanel {
    private static CharacterStatsGUI INSTANCE;
    private JPanel top;
    private JPanel left;
    private JPanel right;
    private ArrayList<RealmCharacter> realmChars;
    private boolean sortOrder;

    public CharacterStatsGUI() {
        INSTANCE = this;

        top = new JPanel();
        left = new JPanel();
        right = new JPanel();
        JPanel topLeft = new JPanel();

        JScrollPane spTop = new JScrollPane(top);
        JScrollPane spLeft = new JScrollPane(left);
        JScrollPane spRight = new JScrollPane(right);

        spRight.getHorizontalScrollBar().setModel(spTop.getHorizontalScrollBar().getModel());
        spRight.getVerticalScrollBar().setModel(spLeft.getVerticalScrollBar().getModel());

        spLeft.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        spRight.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        spRight.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        spTop.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        setLayout(new BorderLayout());
        JPanel leftBar = new JPanel();
        JPanel rightBar = new JPanel();
        leftBar.setLayout(new BorderLayout());
        rightBar.setLayout(new BorderLayout());
        add(leftBar, BorderLayout.WEST);
        add(rightBar, BorderLayout.CENTER);
        topLeft.setPreferredSize(new Dimension(0, 37));

        leftBar.add(topLeft, BorderLayout.NORTH);
        leftBar.add(spLeft, BorderLayout.CENTER);
        rightBar.add(spTop, BorderLayout.NORTH);
        rightBar.add(spRight, BorderLayout.CENTER);

        ToolTipManager.sharedInstance().setInitialDelay(200);
        ToolTipManager.sharedInstance().setDismissDelay(1000000000);
    }

    /**
     * Method for receiving realm character list info.
     *
     * @param r Realm character list.
     */
    public static void updateRealmChars(ArrayList<RealmCharacter> r) {
        INSTANCE.realmChars = r;
        INSTANCE.update();
    }

    /**
     * Update method clearing all the display and re-display it with the updated info.
     */
    private void update() {
        if (realmChars == null) return;

        top.removeAll();
        left.removeAll();
        right.removeAll();

        int dungeonCount = realmChars.get(0).charStats.dungeonStats.length;
        int charCount = realmChars.size();
        top.setLayout(new GridLayout(1, dungeonCount));
        left.setLayout(new GridLayout(charCount, 1));
        right.setLayout(new GridLayout(charCount, dungeonCount));

        for (int i = 0; i < charCount; i++) {
            RealmCharacter c = realmChars.get(i);
            JLabel player = playerIcon(c);
            JPanel p = new JPanel(new GridBagLayout());
            p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));
            p.setPreferredSize(new Dimension(150, 27));
            p.add(player);

            left.add(p);

            for (int j = 0; j < dungeonCount; j++) {
                int v = c.charStats.dungeonStats[j];
                JPanel p2 = new JPanel();
                p2.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));
                p2.add(new JLabel("" + v));
                p2.setPreferredSize(new Dimension(35, 27));
                right.add(p2);
            }
        }

        for (int j = 0; j < dungeonCount; j++) {
            BufferedImage img = null;
            String name;
            try {
                int id = CharacterStatistics.DUNGEONS.get(j);
                name = IdToAsset.getDisplayName(id);
                img = ImageBuffer.getImage(id);
            } catch (IOException | AssetMissingException e) {
                e.printStackTrace();
                return;
            }
            ImageIcon icon = new ImageIcon(img.getScaledInstance(15, 15, Image.SCALE_DEFAULT));
            JLabel dungeonIcon = new JLabel(icon, JLabel.CENTER);
            dungeonIcon.setToolTipText(name);

            int finalJ = j;
            dungeonIcon.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (realmChars == null) return;
                    sortOrder = !sortOrder;
                    if (sortOrder) {
                        realmChars.sort(Comparator.comparingLong(o -> -o.charStats.dungeonStats[finalJ]));
                    } else {
                        realmChars.sort(Comparator.comparingLong(o -> o.charStats.dungeonStats[finalJ]));
                    }
                    update();
                }
            });

            JPanel p = new JPanel();
            p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));
            p.add(dungeonIcon);
            p.setPreferredSize(new Dimension(35, 37));
            top.add(p);
        }
        validate();
    }

    /**
     * Creates account character label to be displayed to the left bar showing icon fame and class name.
     *
     * @param c Character to be made into label showing icon fame and class name.
     * @return Label displaying the character.
     */
    JLabel playerIcon(RealmCharacter c) {
        try {
            int eq = c.skin;
            if (eq == 0) eq = c.classNum;
            BufferedImage img = ImageBuffer.getImage(eq);
            ImageIcon icon = new ImageIcon(img.getScaledInstance(15, 15, Image.SCALE_DEFAULT));
            JLabel characterLabel = new JLabel(c.classString + " " + c.fame, icon, JLabel.CENTER);
            characterLabel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (realmChars == null) return;
                    sortOrder = !sortOrder;
                    if (sortOrder) {
                        realmChars.sort(Comparator.comparingLong(o -> -o.fame));
                    } else {
                        realmChars.sort(Comparator.comparingLong(o -> o.fame));
                    }
                    update();
                }
            });
            return characterLabel;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
