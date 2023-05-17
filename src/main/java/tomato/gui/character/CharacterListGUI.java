package tomato.gui.character;

import assets.ImageBuffer;
import tomato.realmshark.RealmCharacter;
import tomato.backend.data.TomatoData;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class CharacterListGUI extends JPanel {

    private static CharacterListGUI INSTANCE;

    private final TomatoData data;
    private final JPanel charPanel;

    public CharacterListGUI(TomatoData data) {
        INSTANCE = this;
        this.data = data;

        setLayout(new BorderLayout());
        charPanel = new JPanel();
        charPanel.add(new Label("Enter Daily Quest Room to see chars"));
        JScrollPane scrollPaneChars = new JScrollPane(charPanel);
        scrollPaneChars.getVerticalScrollBar().setUnitIncrement(40);
        add(scrollPaneChars);
    }

//    /**
//     * Tooltip showing exalts when hovering over char.
//     */
//    private String exaltStats(RealmCharacter c) {
//        int[] exalts = RealmCharacter.exalts.get(c.classNum);
//        ToolTipManager.sharedInstance().setInitialDelay(200);
//        ToolTipManager.sharedInstance().setDismissDelay(1000000000);
//        return String.format("<html>%d :HP<br>%d :MP<br>%d :Atk<br>%d :Def<br>%d :Spd<br>%d :Dex<br>%5d :Vit<br>%d :Wis</html>", exalts[7], exalts[6], exalts[5], exalts[4], exalts[1], exalts[0], exalts[2], exalts[3]);
//    }

    /**
     * Section made for adding Char skin, char type, char level, char fame and char stats.
     */
    private JPanel leftColumn(RealmCharacter c) {
        JPanel panel = CharacterPanelGUI.createLeftBox();

        try {
            int eq = c.skin;
            if (eq == 0) eq = c.classNum;
            BufferedImage img = ImageBuffer.getImage(eq);
            ImageIcon icon = new ImageIcon(img.getScaledInstance(15, 15, Image.SCALE_DEFAULT));
            JLabel characterLabel = new JLabel(c.classString + " " + c.level, icon, JLabel.CENTER);
            panel.add(characterLabel);
//            characterLabel.setToolTipText(exaltStats(c));
        } catch (Exception e) {
            e.printStackTrace();
        }
        panel.add(new JLabel(String.format("Fame:%d", c.fame)));
        panel.add(new JLabel(String.format("HP:%3d MP:%3d", c.hp, c.mp)));
        panel.add(new JLabel(String.format("Ak:%3d Df:%3d", c.atk, c.def)));
        panel.add(new JLabel(String.format("Sp:%3d Dx:%3d", c.spd, c.dex)));
        panel.add(new JLabel(String.format("Vi:%3d Wi:%3d", c.vit, c.wis)));

        return panel;
    }

    /**
     * Section made for adding equipment, quickslot and date of character made.
     */
    private JPanel midColumn(RealmCharacter c) {
        JPanel panel = new JPanel();
        panel.setMaximumSize(new Dimension(120, CharacterPanelGUI.CHAR_PANEL_SIZE));
        panel.setPreferredSize(new Dimension(120, CharacterPanelGUI.CHAR_PANEL_SIZE));

        JPanel panelEquip = new JPanel();
        panelEquip.setBorder(BorderFactory.createLineBorder(Color.black));
        panelEquip.setPreferredSize(new Dimension(90, 30));
        for (int i = 0; i < 4; i++) {
            int eq = c.equipment[i];
            try {
                BufferedImage img;
                if (eq == -1) {
                    img = ImageBuffer.getEmptyImg();
                } else {
                    img = ImageBuffer.getImage(eq);
                }
                ImageIcon icon = new ImageIcon(img.getScaledInstance(15, 15, Image.SCALE_DEFAULT));
                panelEquip.add(new JLabel(icon));
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }
        panel.add(panelEquip);

        JPanel panelBelt = new JPanel();
        panelBelt.setBorder(BorderFactory.createLineBorder(Color.black));
        int add = c.qs3 ? 40 : 0;
        panelBelt.setPreferredSize(new Dimension(80 + add, 30));
        for (String eqs : c.equipQS) {
            try {
                String[] s = eqs.split("\\|");
                int eq = Integer.parseInt(s[0]);
                BufferedImage img;
                if (eq == -1) {
                    img = ImageBuffer.getEmptyImg();
                } else {
                    img = ImageBuffer.getImage(eq);
                }
                ImageIcon icon = new ImageIcon(img.getScaledInstance(20, 20, Image.SCALE_DEFAULT));
                panelBelt.add(new JLabel(icon));
                panelBelt.add(new JLabel(s[1]));
            } catch (Exception ignored) {
            }
        }
        panel.add(panelBelt);

        panel.add(new JLabel(c.date));

        return panel;
    }

    /**
     * Section made for adding backpack data.
     */
    private JPanel rightColumn(RealmCharacter c, boolean backpack) {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(90, 50));
        panel.setMaximumSize(new Dimension(120, 50));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createLineBorder(Color.black));

        int b = backpack ? 8 : 0;

        JPanel topRow = new JPanel();
        JPanel botRow = new JPanel();
        panel.add(topRow);
        panel.add(botRow);

        for (int i = 4 + b; i < 12 + b; i++) {
            int eq = c.equipment[i];
            try {
                BufferedImage img;
                if (eq == -1) {
                    img = ImageBuffer.getEmptyImg();
                } else {
                    img = ImageBuffer.getImage(eq);
                }
                ImageIcon icon = new ImageIcon(img.getScaledInstance(12, 12, Image.SCALE_DEFAULT));
                if (i < 8 + b) {
                    topRow.add(new JLabel(icon));
                } else {
                    botRow.add(new JLabel(icon));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return panel;
    }

    /**
     * Individual backpacks with icons constructed.
     */
    private JPanel invBackpack(RealmCharacter c) {
        JPanel panel = new JPanel();
        panel.setMaximumSize(new Dimension(120, CharacterPanelGUI.CHAR_PANEL_SIZE));
        panel.setPreferredSize(new Dimension(120, CharacterPanelGUI.CHAR_PANEL_SIZE));

        panel.add(rightColumn(c, false));

        if (c.backpack) {
            panel.add(rightColumn(c, true));
        }

        return panel;
    }

    /**
     * Method for receiving realm character list info.
     */
    public static void updateRealmChars() {
        INSTANCE.updateCharPanel();
    }

    /**
     * Character tab update, clears all data in the tab and repopulates it.
     */
    private void updateCharPanel() {
        charPanel.setLayout(new BoxLayout(charPanel, BoxLayout.Y_AXIS));
        charPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        charPanel.add(Box.createVerticalGlue());
        charPanel.removeAll();
        for (RealmCharacter c : data.chars) {
            JPanel box = CharacterPanelGUI.createMainBox();

            box.add(leftColumn(c));
            box.add(midColumn(c));
            box.add(invBackpack(c));

            charPanel.add(box);
        }

        validate();
    }
}
