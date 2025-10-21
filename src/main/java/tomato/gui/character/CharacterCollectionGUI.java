package tomato.gui.character;

import assets.ImageBuffer;
import tomato.backend.data.TomatoData;
import tomato.realmshark.DungeonCollection;
import tomato.realmshark.RealmCharacter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Dungeon completion display GUI class showing all dungeon completions of the users account.
 */
public class CharacterCollectionGUI extends JPanel {
    private static CharacterCollectionGUI INSTANCE;
    private final JPanel left, right;
    private final int collectionCount;
    private boolean sortOrder;
    private final TomatoData data;
    private int charCount;
    private JLabel[][] labels;
    private JLabel topLeftLabel;

    public CharacterCollectionGUI(TomatoData data) {
        INSTANCE = this;
        this.data = data;

        JPanel top = new JPanel();
        left = new JPanel();
        right = new JPanel();
        JPanel topLeft = new JPanel();

        HorizontalJScrollPane spTop = new HorizontalJScrollPane(top);
        JScrollPane spLeft = new JScrollPane(left);
        JScrollPane spRight = new JScrollPane(right);

        spRight.getHorizontalScrollBar().setModel(spTop.getHorizontalScrollBar().getModel());
        spRight.getVerticalScrollBar().setModel(spLeft.getVerticalScrollBar().getModel());
        spTop.getHorizontalScrollBar().setUnitIncrement(3);
        spRight.getVerticalScrollBar().setUnitIncrement(9);

        spLeft.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        spLeft.getVerticalScrollBar().setUnitIncrement(9);

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
        topLeft.setPreferredSize(new Dimension(37, 37));
        topLeftLabel = new JLabel(getImageIcon(810), JLabel.CENTER);
        topLeft.add(topLeftLabel);
        topLeftLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (data.chars == null) return;
                sortOrder = !sortOrder;
                if (sortOrder) {
                    data.chars.sort(Comparator.comparingLong(o -> o.fame));
                } else {
                    data.chars.sort(Comparator.comparingLong(o -> -o.fame));
                }
                update();
            }
        });

        leftBar.add(topLeft, BorderLayout.NORTH);
        leftBar.add(spLeft, BorderLayout.CENTER);
        rightBar.add(spTop, BorderLayout.NORTH);
        rightBar.add(spRight, BorderLayout.CENTER);

        ToolTipManager.sharedInstance().setInitialDelay(200);
        ToolTipManager.sharedInstance().setDismissDelay(1000000000);

        collectionCount = 12;

        topDungeonCollectionList(top);
    }

    /**
     * Creates top panel dungeon collection list
     *
     * @param top Panel to add dungeon icons to.
     */
    private void topDungeonCollectionList(JPanel top) {
        top.setLayout(new GridLayout(1, collectionCount));

        for (int j = 0; j < collectionCount; j++) {
            JLabel dungeonIcon = new JLabel(DungeonCollection.collectionShort[j]);
            String t1 = DungeonCollection.collection[j];
            String t2 = DungeonCollection.collectionBonus[j];

            dungeonIcon.setToolTipText("<html>" + t1 + "<br>" + t2 + "</html>");

//            int finalJ = j;
//            dungeonIcon.addMouseListener(new MouseAdapter() {
//                public void mouseClicked(MouseEvent e) {
//                    if (data.chars == null) return;
//                    sortOrder = !sortOrder;
//                    if (sortOrder) {
//                        data.chars.sort(Comparator.comparingLong(o -> -o.charStats.dungeonStats[finalJ]));
//                    } else {
//                        data.chars.sort(Comparator.comparingLong(o -> o.charStats.dungeonStats[finalJ]));
//                    }
//                    update();
//                }
//            });

            JPanel p = new JPanel();
            p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.gray));
            p.add(dungeonIcon);
            p.setPreferredSize(new Dimension(35, 37));
            top.add(p);
        }
    }

    /**
     * Method for receiving realm character list info.
     */
    public static void updateRealmChars() {
        INSTANCE.update();
    }

    /**
     * Update method clearing all the display and re-display it with the updated info.
     */
    private void update() {
        if (data.chars == null) return;

        int charCount = data.chars.size();

        if (charCount != this.charCount) {
            updatePanelWithPlayerListChanged();
            validate();
        }
        updateDungeonLabels();
        updatePlayerList();
        revalidate();
    }

    /**
     * Updates dungeon labels with dungeon completes.
     */
    private void updateDungeonLabels() {
        for (int i = 0; i < charCount; i++) {
            RealmCharacter c = data.chars.get(i);

            for (int j = 0; j < collectionCount; j++) {
                ArrayList<String> l = DungeonCollection.collection(DungeonCollection.collection[j], c);
                JLabel jLabel = labels[i][j];
                if (jLabel != null && l != null) {
                    if (l.isEmpty()) {
                        jLabel.setText("1");
                        jLabel.setToolTipText("");
                    } else {
                        jLabel.setText("0");
                        StringBuilder s = new StringBuilder("<html>Missing:<br>");
                        for (String d : l) {
                            s.append(d).append("<br>");
                        }
                        s.append("</html>");
                        jLabel.setToolTipText(s.toString());
                    }
                }
            }
        }
    }

    /**
     * Updates player list on the left panel.
     */
    private void updatePlayerList() {
        left.removeAll();

        int totalFame = 0;
        for (int i = 0; i < charCount; i++) {
            RealmCharacter c = data.chars.get(i);
            JLabel player = playerIcon(c);
            JPanel p = new JPanel(new GridBagLayout());
            p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.gray));
            p.setPreferredSize(new Dimension(150, 27));
            p.add(player);

            left.add(p);
            totalFame += c.fame;
        }
        topLeftLabel.setText("Total Fame: " + totalFame);
    }

    /**
     * Flushes player dungeon complete labels and rebuilds labels with corrected player list.
     */
    private void updatePanelWithPlayerListChanged() {
        charCount = data.chars.size();

        left.setLayout(new GridLayout(charCount, 1));
        right.removeAll();
        right.setLayout(new GridLayout(charCount, collectionCount));
        labels = new JLabel[charCount][collectionCount];

        for (int i = 0; i < charCount; i++) {
            for (int j = 0; j < collectionCount; j++) {
                JPanel p2 = new JPanel();
                p2.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.gray));
                labels[i][j] = new JLabel();
                p2.add(labels[i][j]);
                p2.setPreferredSize(new Dimension(35, 27));
                right.add(p2);
            }
        }
    }

    /**
     * Creates account character label to be displayed to the left bar showing icon fame and class name.
     *
     * @param c Character to be made into label showing icon fame and class name.
     * @return Label displaying the character.
     */
    private JLabel playerIcon(RealmCharacter c) {
        try {
            int eq = c.skin;
            if (eq == 0) eq = c.classNum;
            ImageIcon icon = getImageIcon(eq);
            JLabel characterLabel = new JLabel(c.classString + " " + c.fame, icon, JLabel.CENTER);
            characterLabel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (data.chars == null) return;
                    boolean b = data.chars.remove(c);
                    if (b) {
                        data.chars.add(0, c);
                    }
//                    sortOrder = !sortOrder;
//                    if (sortOrder) {
//                        data.chars.sort(Comparator.comparingLong(o -> -o.fame));
//                    } else {
//                        data.chars.sort(Comparator.comparingLong(o -> o.fame));
//                    }
                    update();
                }
            });
            return characterLabel;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets image from image ID.
     *
     * @param imageId ID of image to grab
     * @return Icon to be added to the label
     */
    private static ImageIcon getImageIcon(int imageId) {
        return ImageBuffer.getOutlinedIcon(imageId, 20);
    }

    /**
     * Horizontal scroll class
     */
    class HorizontalJScrollPane extends JScrollPane {
        public HorizontalJScrollPane(Component component) {
            super(component);
            final JScrollBar horizontalScrollBar = getHorizontalScrollBar();
            setWheelScrollingEnabled(false);
            addMouseWheelListener(new MouseAdapter() {
                public void mouseWheelMoved(MouseWheelEvent evt) {

                    int iScrollAmount = horizontalScrollBar.getUnitIncrement();
                    if (evt.getWheelRotation() >= 1)//mouse wheel was rotated down/ towards the user
                    {
                        int iNewValue = horizontalScrollBar.getValue() + horizontalScrollBar.getBlockIncrement() * iScrollAmount * Math.abs(evt.getWheelRotation());
                        if (iNewValue <= horizontalScrollBar.getMaximum()) {
                            horizontalScrollBar.setValue(iNewValue);
                        }
                    } else if (evt.getWheelRotation() <= -1)//mouse wheel was rotated up/away from the user
                    {
                        int iNewValue = horizontalScrollBar.getValue() - horizontalScrollBar.getBlockIncrement() * iScrollAmount * Math.abs(evt.getWheelRotation());
                        if (iNewValue >= 0) {
                            horizontalScrollBar.setValue(iNewValue);
                        }
                    }
                }
            });
        }
    }
}
