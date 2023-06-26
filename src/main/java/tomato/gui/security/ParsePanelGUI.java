package tomato.gui.security;

import assets.ImageBuffer;
import tomato.backend.data.Entity;
import tomato.gui.SmartScroller;
import tomato.realmshark.enums.CharacterClass;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class ParsePanelGUI extends JPanel {

    private static ParsePanelGUI INSTANCE;

    private static int HEIGHT = 40;

    private JScrollPane scroll;
    private static JPanel charPanel;
    private static HashMap<Integer, Player> playerDisplay;

    public ParsePanelGUI() {
        INSTANCE = this;
        setLayout(new BorderLayout());

        playerDisplay = new HashMap<>();
        charPanel = new JPanel();
        charPanel.setLayout(new GridBagLayout());

        charPanel.setLayout(new BoxLayout(charPanel, BoxLayout.Y_AXIS));
//        charPanel.setBorder(BorderFactory.createEmptyBorder(2, 20, 2, 20));
        charPanel.add(Box.createVerticalGlue());
//        charPanel.removeAll();
//        for (RealmCharacter c : data.chars) {
//            JPanel box = CharacterPanelGUI.createMainBox();
//
//            charPanel.add(box);
//        }

        validate();

        scroll = new JScrollPane(charPanel);
        scroll.getVerticalScrollBar().setUnitIncrement(40);
        new SmartScroller(scroll);
        add(scroll, BorderLayout.CENTER);

        JButton button = new JButton("Add");
        button.addActionListener(e -> {
            add();
        });
        add(button, BorderLayout.SOUTH);
    }

    private void guiUpdate() {
        validate();
        repaint();
    }

    private void add() {
//        JPanel panel = createMainBox();
//        charPanel.add(panel);
//        validate();
    }

    private static JPanel createMainBox(Player p, Entity player) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        panel.setPreferredSize(new Dimension(370, HEIGHT));
        panel.setMaximumSize(new Dimension(370, HEIGHT));
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        JPanel left = leftPanel(player);
        left.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(left);

        JPanel inv = equipment(p, player);
        panel.add(inv);

        JPanel stat = rightPanel(player);
        panel.add(stat);

        return panel;
    }

    private static JPanel leftPanel(Entity player) {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(110, 33));

        try {
            int eq = player.stat.SKIN_ID.statValue;
            if (eq == 0) eq = player.objectType;
            BufferedImage img = ImageBuffer.getImage(eq);
            ImageIcon icon = new ImageIcon(img.getScaledInstance(20, 20, Image.SCALE_DEFAULT));
            int level = player.stat.LEVEL_STAT.statValue;
            JLabel characterLabel = new JLabel(player.stat.NAME_STAT.stringStatValue + " " + level, icon, JLabel.CENTER);
            panel.add(characterLabel);
//            characterLabel.setToolTipText(exaltStats(c));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return panel;
    }

    private static JPanel rightPanel(Entity player) {
        JPanel panel = new JPanel();

        int stat = statsMaxed(player);
        JLabel stats = new JLabel(stat + " / 8");
        stats.setToolTipText(statMissing(player));

        panel.add(stats);

        return panel;
    }

    private static JPanel equipment(Player p, Entity player) {
        JPanel panel = new JPanel();
        panel.setMaximumSize(new Dimension(120, HEIGHT));
        panel.setPreferredSize(new Dimension(120, HEIGHT));

        JPanel panelEquip = new JPanel();
        panelEquip.setBorder(BorderFactory.createLineBorder(Color.black));
        panelEquip.setPreferredSize(new Dimension(110, 33));
        p.inv[0] = player.stat.INVENTORY_0_STAT.statValue;
        p.inv[1] = player.stat.INVENTORY_1_STAT.statValue;
        p.inv[2] = player.stat.INVENTORY_2_STAT.statValue;
        p.inv[3] = player.stat.INVENTORY_3_STAT.statValue;
        for (int i = 0; i < 4; i++) {
            int eq = p.inv[i];
            try {
                BufferedImage img;
                if (eq == -1) {
                    img = ImageBuffer.getEmptyImg();
                } else {
                    img = ImageBuffer.getImage(eq);
                }
                p.icon[i] = new ImageIcon(img.getScaledInstance(20, 20, Image.SCALE_DEFAULT));
                panelEquip.add(new JLabel(p.icon[i]));
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }
        panel.add(panelEquip);

        return panel;
    }

    /**
     * Computes the missing pots needed to max the character.
     */
    public static String statMissing(Entity player) {
        int life = (int) Math.ceil((CharacterClass.getLife(player.objectType) - player.baseStats[0]) / 5.0);
        int mana = (int) Math.ceil((CharacterClass.getMana(player.objectType) - player.baseStats[1]) / 5.0);
        int atk = CharacterClass.getAtk(player.objectType) - player.baseStats[2];
        int def = CharacterClass.getDef(player.objectType) - player.baseStats[3];
        int spd = CharacterClass.getSpd(player.objectType) - player.baseStats[4];
        int dex = CharacterClass.getDex(player.objectType) - player.baseStats[5];
        int vit = CharacterClass.getVit(player.objectType) - player.baseStats[6];
        int wis = CharacterClass.getWis(player.objectType) - player.baseStats[7];

        return String.format("<html>Missing<br>%d :Life<br>%d :Mana<br>%d :Atk<br>%d :Def<br>%d :Dex<br>%d :Spd<br>%d :Vit<br>%d :Wis</html>", life, mana, atk, def, spd, dex, vit, wis);
    }

    /**
     * Gets the characters maxed stat count.
     */
    public static int statsMaxed(Entity player) {
        int outof8 = 0;
        if (CharacterClass.getLife(player.objectType) == player.baseStats[0]) outof8++;
        if (CharacterClass.getMana(player.objectType) == player.baseStats[1]) outof8++;
        if (CharacterClass.getAtk(player.objectType) == player.baseStats[2]) outof8++;
        if (CharacterClass.getDef(player.objectType) == player.baseStats[3]) outof8++;
        if (CharacterClass.getSpd(player.objectType) == player.baseStats[4]) outof8++;
        if (CharacterClass.getDex(player.objectType) == player.baseStats[5]) outof8++;
        if (CharacterClass.getVit(player.objectType) == player.baseStats[6]) outof8++;
        if (CharacterClass.getWis(player.objectType) == player.baseStats[7]) outof8++;

        return outof8;
    }

    public static void addPlayer(int id, Entity entity) {
        Player p = new Player();
        p.id = id;
        p.playerEntity = entity;
        p.panel = createMainBox(p, entity);
        playerDisplay.put(id, p);
        charPanel.add(p.panel);

        INSTANCE.guiUpdate();
    }

    public static void removePlayer(int dropId) {
        Player p = playerDisplay.remove(dropId);
        if (p != null) {
            charPanel.remove(p.panel);
            INSTANCE.guiUpdate();
        }
    }

    public static void update(int id, Entity entity) {
        Player player = playerDisplay.get(id);
        if (player != null) {
            player.update(entity);
        }
    }

    public static void clear() {
        playerDisplay.clear();
        charPanel.removeAll();
        charPanel.add(Box.createVerticalGlue());
        INSTANCE.guiUpdate();
    }

    public static class Player {
        int[] inv = new int[4];
        ImageIcon[] icon = new ImageIcon[4];
        int id;
        Entity playerEntity;
        JPanel panel;

        public void update(Entity player) {
            setIcon(0, player.stat.INVENTORY_0_STAT.statValue);
            setIcon(1, player.stat.INVENTORY_1_STAT.statValue);
            setIcon(2, player.stat.INVENTORY_2_STAT.statValue);
            setIcon(3, player.stat.INVENTORY_3_STAT.statValue);
        }

        private void setIcon(int i, int eq) {
            if (inv[i] == eq) return;
            inv[i] = eq;
            try {
                BufferedImage img;
                if (eq == -1) {
                    img = ImageBuffer.getEmptyImg();
                } else {
                    img = ImageBuffer.getImage(eq);
                }
                icon[i].setImage(img.getScaledInstance(20, 20, Image.SCALE_DEFAULT));
            } catch (Exception e) {
            }
            INSTANCE.updateUI();
        }
    }
}
