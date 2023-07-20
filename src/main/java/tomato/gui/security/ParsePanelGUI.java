package tomato.gui.security;

import assets.IdToAsset;
import assets.ImageBuffer;
import packets.data.StatData;
import tomato.backend.data.Entity;
import tomato.gui.SmartScroller;
import tomato.realmshark.enums.CharacterClass;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;

public class ParsePanelGUI extends JPanel {

    private static ParsePanelGUI INSTANCE;

    private static int HEIGHT = 24;

    private static JPanel charPanel;
    private static HashMap<Integer, Player> playerDisplay;

    public ParsePanelGUI() {
        INSTANCE = this;
        setLayout(new BorderLayout());

        playerDisplay = new HashMap<>();
        charPanel = new JPanel();
        charPanel.setLayout(new GridBagLayout());

        charPanel.setLayout(new BoxLayout(charPanel, BoxLayout.Y_AXIS));

        validate();

        JScrollPane scroll = new JScrollPane(charPanel);
        scroll.getVerticalScrollBar().setUnitIncrement(40);
        new SmartScroller(scroll);
        add(scroll, BorderLayout.CENTER);

//        JButton button = new JButton("Clear");
//        button.addActionListener(e -> clicked());
//        add(button, BorderLayout.SOUTH);
    }

    private void clicked() {
        charPanel.removeAll();
        Player p = playerDisplay.get(552);
        JPanel panel = createMainBox(p, p.playerEntity);
        charPanel.add(panel);
        INSTANCE.updateUI();
    }

    private void guiUpdate() {
        validate();
        repaint();
    }

    private static JPanel createMainBox(Player p, Entity player) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED), BorderFactory.createEmptyBorder(0, 20, 0, 20)));
        panel.setPreferredSize(new Dimension(370, HEIGHT));
        panel.setMaximumSize(new Dimension(370, HEIGHT));
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        JPanel left = leftPanel(player);
        panel.add(left);

        JPanel inv = equipment(p, player);
        panel.add(inv);

        JPanel stat = rightPanel(player);
        panel.add(stat);

        return panel;
    }

    private static JPanel leftPanel(Entity player) {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(110, HEIGHT));
        panel.setLayout(new BorderLayout());

        try {
            int eq = player.stat.SKIN_ID.statValue;
            if (eq == 0) eq = player.objectType;
            BufferedImage img = ImageBuffer.getImage(eq);
            ImageIcon icon = new ImageIcon(img.getScaledInstance(20, 20, Image.SCALE_DEFAULT));
            int level = player.stat.LEVEL_STAT.statValue;
            StatData nameStat = player.stat.NAME_STAT;
            String name;
            if (nameStat != null) {
                name = nameStat.stringStatValue;
            } else {
                name = "N/A";
            }
            JLabel characterLabel = new JLabel(name.split(",")[0] + " [" + level + "]", icon, JLabel.CENTER);
            characterLabel.setAlignmentX(JLabel.LEFT);
            panel.setAlignmentX(JLabel.LEFT);
            panel.setAlignmentX(LEFT_ALIGNMENT);
            characterLabel.setHorizontalAlignment(SwingConstants.LEFT);
            panel.add(characterLabel);
//            characterLabel.setToolTipText(exaltStats(c));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return panel;
    }

    private static JPanel rightPanel(Entity player) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        int stat = statsMaxed(player);
        JLabel stats = new JLabel(stat + " / 8");
        stats.setToolTipText(statMissing(player));
        stats.setHorizontalAlignment(SwingConstants.RIGHT);

        panel.add(stats);

        return panel;
    }

    private static JPanel equipment(Player p, Entity player) {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(100, HEIGHT));
        panel.setLayout(new GridLayout(1, 4));
//        panelEquip.setBorder(BorderFactory.createLineBorder(Color.black));
//        panelEquip.setPreferredSize(new Dimension(110, 33));
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
                p.icon[i] = new JLabel(new ImageIcon(img.getScaledInstance(20, 20, Image.SCALE_DEFAULT)));
                p.icon[i].setToolTipText(IdToAsset.objectName(eq));
                panel.add(p.icon[i]);
            } catch (Exception e) {
            }
        }

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
        INSTANCE.guiUpdate();
    }

    public static class Player {
        int[] inv = new int[4];
        JLabel[] icon = new JLabel[4];
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
                icon[i].setIcon(new ImageIcon(img.getScaledInstance(20, 20, Image.SCALE_DEFAULT)));
                icon[i].setToolTipText(IdToAsset.objectName(eq));
            } catch (Exception e) {
            }
            INSTANCE.updateUI();
        }
    }
}
