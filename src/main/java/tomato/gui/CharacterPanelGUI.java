package tomato.gui;

import assets.ImageBuffer;
import tomato.logic.Character;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Character GUI class to display character data in the character tab.
 */
public class CharacterPanelGUI extends JPanel {
    private static final int CHAR_PANEL_SIZE = 120;
    private JPanel charPanel;
    private static JScrollPane scrollPane;

    public CharacterPanelGUI() {
        setLayout(new BorderLayout());

        charPanel = new JPanel();
        scrollPane = new JScrollPane(charPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(40);

        charPanel.setLayout(new BoxLayout(charPanel, BoxLayout.Y_AXIS));
        charPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        charPanel.add(Box.createVerticalGlue());

//        JButton button = new JButton("Add");
//        button.addActionListener(e -> {
//            updateCharacters(CharList.getCharList(null));
//        });
//        add(button, BorderLayout.SOUTH);
        add(new Label("Enter Daily Quest Room to see chars"), BorderLayout.NORTH);

        add(scrollPane);
    }

    /**
     * Tooltip showing exalts when hovering over char.
     */
    private String exaltStats(Character c) {
        int[] exalts = Character.exalts.get(c.classNum);
        ToolTipManager.sharedInstance().setInitialDelay(200);
        ToolTipManager.sharedInstance().setDismissDelay(1000000000);
        return String.format("<html>%d :Shat<br>%d :LH<br>%d :Cult<br>%d :Nest<br>%5d :Kog<br>%d :Fung<br>%d :O3<br>%d :Void</html>", exalts[5], exalts[4], exalts[1], exalts[0], exalts[2], exalts[3], exalts[7], exalts[6]);
    }

    /**
     * Section made for adding Char skin, char type, char level, char fame and char stats.
     */
    private JPanel leftColumn(Character c) {
        JPanel panel = new JPanel();
        panel.setMaximumSize(new Dimension(120, CHAR_PANEL_SIZE));
        panel.setPreferredSize(new Dimension(120, CHAR_PANEL_SIZE));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(5, 10, 5, 5));
//        panel.setBorder(new CompoundBorder(BorderFactory.createLineBorder(Color.black), new EmptyBorder(1, 1, 1, 1)));

        try {
            int eq = c.skin;
            if (eq == 0) eq = c.classNum;
            BufferedImage img = ImageBuffer.getImage(eq);
            ImageIcon icon = new ImageIcon(img.getScaledInstance(15, 15, Image.SCALE_DEFAULT));
            JLabel characterLabel = new JLabel(c.classString + " " + c.level, icon, JLabel.CENTER);
            panel.add(characterLabel);
            characterLabel.setToolTipText(exaltStats(c));
        } catch (Exception e) {
            e.printStackTrace();
        }
        panel.add(new JLabel(String.format("Fame:%3d", c.fame)));
        panel.add(new JLabel(String.format("HP:%3d MP:%3d", c.hp, c.mp)));
        panel.add(new JLabel(String.format("Ak:%3d Df:%3d", c.atk, c.def)));
        panel.add(new JLabel(String.format("Sp:%3d Dx:%3d", c.spd, c.dex)));
        panel.add(new JLabel(String.format("Vi:%3d Wi:%3d", c.vit, c.wis)));

        return panel;
    }

    /**
     * Section made for adding equipment, quickslot and date of character made.
     */
    private JPanel midColumn(Character c) {
        JPanel panel = new JPanel();
        panel.setMaximumSize(new Dimension(120, CHAR_PANEL_SIZE));
        panel.setPreferredSize(new Dimension(120, CHAR_PANEL_SIZE));
//        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
//        panel.setBorder(new CompoundBorder(BorderFactory.createLineBorder(Color.black), new EmptyBorder(1, 1, 1, 1)));

        JPanel panelEquip = new JPanel();
        panelEquip.setBorder(BorderFactory.createLineBorder(Color.black));
        panelEquip.setPreferredSize(new Dimension(90, 30));
        for (int i = 0; i < 4; i++) {
            int eq = c.equipment[i];
            try {
                BufferedImage img = ImageBuffer.getImage(eq);
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
                BufferedImage img = ImageBuffer.getImage(Integer.parseInt(s[0]));
                ImageIcon icon = new ImageIcon(img.getScaledInstance(20, 20, Image.SCALE_DEFAULT));
                panelBelt.add(new JLabel(icon));
                panelBelt.add(new JLabel(s[1]));
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }
        panel.add(panelBelt);

        panel.add(new JLabel(c.date));

        return panel;
    }

    /**
     * Section made for adding backpack data.
     */
    private JPanel rightColumn(Character c, boolean backpack) {
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
                BufferedImage img = ImageBuffer.getImage(eq);
                ImageIcon icon = new ImageIcon(img.getScaledInstance(12, 12, Image.SCALE_DEFAULT));
                if (i < 8 + b) {
                    topRow.add(new JLabel(icon));
                } else {
                    botRow.add(new JLabel(icon));
                }
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }

        return panel;
    }

    /**
     * Individual backpacks with icons constructed.
     */
    private JPanel invBackpack(Character c) {
        JPanel panel = new JPanel();
        panel.setMaximumSize(new Dimension(120, CHAR_PANEL_SIZE));
        panel.setPreferredSize(new Dimension(120, CHAR_PANEL_SIZE));
//        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
//        panel.setBorder(new CompoundBorder(BorderFactory.createLineBorder(Color.black), new EmptyBorder(0, 10, 0, 10)));

        panel.add(rightColumn(c, false));

        if (c.backpack) {
            panel.add(rightColumn(c, true));
        }

        return panel;
    }

    /**
     * Adds a single character in the list of characters.
     *
     * @param c Character to be added to the list of chars.
     * @return The panel containing player character data to added in the list of char data.
     */
    private JPanel addChar(Character c) {
        JPanel panel = new JPanel();
//        panel.setBorder(new CompoundBorder(BorderFactory.createLineBorder(Color.black), new EmptyBorder(1, 1, 1, 1)));
        panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        panel.setPreferredSize(new Dimension(0, CHAR_PANEL_SIZE));
        panel.setMaximumSize(new Dimension(370, CHAR_PANEL_SIZE));
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        panel.add(leftColumn(c));
        panel.add(midColumn(c));
        panel.add(invBackpack(c));

        return panel;
    }

    /**
     * Clears all current tab data and adds the character data in the list.
     *
     * @param list Character info to be updated in the char tab.
     */
    public void updateCharacters(ArrayList<Character> list) {
        if (list == null) return;

        charPanel.removeAll();
        for (Character c : list) {
            charPanel.add(addChar(c));
        }
        charPanel.revalidate();
    }
}
