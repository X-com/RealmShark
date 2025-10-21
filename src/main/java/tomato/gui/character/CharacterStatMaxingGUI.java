package tomato.gui.character;

import assets.ImageBuffer;
import tomato.backend.data.VaultData;
import tomato.realmshark.RealmCharacter;
import tomato.backend.data.TomatoData;
import tomato.realmshark.enums.CharacterClass;
import tomato.realmshark.enums.StatPotion;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

public class CharacterStatMaxingGUI extends JPanel {
    private static CharacterStatMaxingGUI INSTANCE;

    private final JLabel[] potStatLabels = new JLabel[8];
    private final JCheckBox charInvs, mainVault, potStorage, giftChest;
    private final JRadioButton regularRadio, seasonalRadio;
    private final JPanel maxingPanel;
    private final HashMap<Integer, Boolean> characters;
    private final TomatoData data;

    public CharacterStatMaxingGUI(TomatoData data) {
        INSTANCE = this;
        this.data = data;
        characters = new HashMap<>();

        setLayout(new BorderLayout());
        maxingPanel = new JPanel();
        maxingPanel.setLayout(new BoxLayout(maxingPanel, BoxLayout.Y_AXIS));
        maxingPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JScrollPane scrollPaneMaxing = new JScrollPane(maxingPanel);
        scrollPaneMaxing.getVerticalScrollBar().setUnitIncrement(40);
        add(scrollPaneMaxing, BorderLayout.CENTER);

        charInvs = new JCheckBox("Char Invs");
        mainVault = new JCheckBox("Main Vault");
        potStorage = new JCheckBox("Pot Storage");
        giftChest = new JCheckBox("Gift Chest");

        regularRadio = new JRadioButton("Regular", true);
        seasonalRadio = new JRadioButton("Seasonal");

        JPanel missingPots = missingPotsPanel();

        add(missingPots, BorderLayout.NORTH);
    }

    /**
     * Method for receiving realm character list info.
     */
    public static void updateRealmChars() {
        INSTANCE.updateSelection();
        INSTANCE.updateMaxingPanel();
    }

    /**
     * Updates selection boxes as vault and char inventories.
     */
    private void updateSelection() {
        charInvs.setEnabled(data.characterDataRecieved);
        if (regularRadio.isSelected()) {
            mainVault.setEnabled(data.vaultDataRecievedRegular);
            potStorage.setEnabled(data.vaultDataRecievedRegular);
            giftChest.setEnabled(data.vaultDataRecievedRegular);
        } else if (seasonalRadio.isSelected()) {
            mainVault.setEnabled(data.vaultDataRecievedSeasonal);
            potStorage.setEnabled(data.vaultDataRecievedSeasonal);
            giftChest.setEnabled(data.vaultDataRecievedSeasonal);
        }
    }

    /**
     * Vault update method called when receiving vault packets.
     */
    public static void vaultDataUpdate() {
        INSTANCE.updateMissingPotsPanel();
        INSTANCE.updateSelection();
    }

    /**
     * Updates the classes with missing pots in the stat potion maxing tab.
     */
    public void updateMaxingPanel() {
        if (data.chars == null) return;

        maxingPanel.removeAll();

        maxingPanel.add(Box.createVerticalGlue());
        for (RealmCharacter c : data.chars) {
            if ((c.seasonal && seasonalRadio.isSelected()) || (!c.seasonal && regularRadio.isSelected())) {
                int maxedStats = statsMaxed(c);
                if (maxedStats != 8) {
                    JPanel boxChars = createPanelCharWithMissingStats(c, maxedStats);
                    maxingPanel.add(boxChars);
                }
            }
        }

        maxingPanel.revalidate();
    }

    /**
     * Computes the missing pots needed to max the character.
     */
    public static void statMissing(RealmCharacter c, int[] missing) {
        missing[0] += (int) Math.ceil((CharacterClass.getLife(c.classNum) - c.hp) / 5.0);
        missing[1] += (int) Math.ceil((CharacterClass.getMana(c.classNum) - c.mp) / 5.0);
        missing[2] += CharacterClass.getAtk(c.classNum) - c.atk;
        missing[3] += CharacterClass.getDef(c.classNum) - c.def;
        missing[4] += CharacterClass.getSpd(c.classNum) - c.spd;
        missing[5] += CharacterClass.getDex(c.classNum) - c.dex;
        missing[6] += CharacterClass.getVit(c.classNum) - c.vit;
        missing[7] += CharacterClass.getWis(c.classNum) - c.wis;
    }

    /**
     * Gets the characters maxed stat count.
     */
    public static int statsMaxed(RealmCharacter c) {
        int outof8 = 0;
        if (CharacterClass.getLife(c.classNum) == c.hp) outof8++;
        if (CharacterClass.getMana(c.classNum) == c.mp) outof8++;
        if (CharacterClass.getAtk(c.classNum) == c.atk) outof8++;
        if (CharacterClass.getDef(c.classNum) == c.def) outof8++;
        if (CharacterClass.getSpd(c.classNum) == c.spd) outof8++;
        if (CharacterClass.getDex(c.classNum) == c.dex) outof8++;
        if (CharacterClass.getVit(c.classNum) == c.vit) outof8++;
        if (CharacterClass.getWis(c.classNum) == c.wis) outof8++;

        return outof8;
    }

    /**
     * Top pots missing display in the stat maxing tab.
     */
    private JPanel missingPotsPanel() {
        JPanel boxPots = CharacterPanelGUI.createMainBox();
        boxPots.setPreferredSize(new Dimension(390, CharacterPanelGUI.CHAR_PANEL_SIZE));
        boxPots.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        JPanel panelLeft = CharacterPanelGUI.createLeftBox();
        panelLeft.add(Box.createVerticalGlue());
        charInvs.setEnabled(false);
        mainVault.setEnabled(false);
        potStorage.setEnabled(false);
        giftChest.setEnabled(false);
        charInvs.addActionListener(e -> updateMissingPotsPanel());
        mainVault.addActionListener(e -> updateMissingPotsPanel());
        potStorage.addActionListener(e -> updateMissingPotsPanel());
        giftChest.addActionListener(e -> updateMissingPotsPanel());
        panelLeft.add(charInvs);
        panelLeft.add(mainVault);
        panelLeft.add(potStorage);
        panelLeft.add(giftChest);
        panelLeft.add(Box.createVerticalGlue());
        boxPots.add(panelLeft);

        JPanel panelTop = new JPanel();
        JPanel panelMid = new JPanel();
        JPanel panelBot = new JPanel();
        JPanel rightPotDisplay = CharacterPanelGUI.createMidRightBox(panelTop, panelMid, panelBot);
        potStatLabels[0] = new JLabel("0", getImageIcon(StatPotion.Life), JLabel.CENTER);
        potStatLabels[1] = new JLabel("0", getImageIcon(StatPotion.Mana), JLabel.CENTER);
        potStatLabels[2] = new JLabel("0", getImageIcon(StatPotion.Attack), JLabel.CENTER);
        potStatLabels[3] = new JLabel("0", getImageIcon(StatPotion.Defense), JLabel.CENTER);
        potStatLabels[4] = new JLabel("0", getImageIcon(StatPotion.Speed), JLabel.CENTER);
        potStatLabels[5] = new JLabel("0", getImageIcon(StatPotion.Dexterity), JLabel.CENTER);
        potStatLabels[6] = new JLabel("0", getImageIcon(StatPotion.Vitality), JLabel.CENTER);
        potStatLabels[7] = new JLabel("0", getImageIcon(StatPotion.Wisdom), JLabel.CENTER);
        panelTop.add(Box.createHorizontalGlue());
        panelTop.add(potStatLabels[0]);
        panelTop.add(Box.createHorizontalGlue());
        panelTop.add(potStatLabels[1]);
        panelTop.add(Box.createHorizontalGlue());
        panelMid.add(Box.createHorizontalGlue());
        panelMid.add(potStatLabels[2]);
        panelMid.add(Box.createHorizontalGlue());
        panelMid.add(potStatLabels[3]);
        panelMid.add(Box.createHorizontalGlue());
        panelMid.add(potStatLabels[4]);
        panelMid.add(Box.createHorizontalGlue());
        panelBot.add(Box.createHorizontalGlue());
        panelBot.add(potStatLabels[5]);
        panelBot.add(Box.createHorizontalGlue());
        panelBot.add(potStatLabels[6]);
        panelBot.add(Box.createHorizontalGlue());
        panelBot.add(potStatLabels[7]);
        panelBot.add(Box.createHorizontalGlue());

        rightPotDisplay.setLayout(new GridLayout(4, 1));
        JPanel panelVeryBot = new JPanel();

        seasonalRadio.setForeground(Color.cyan);

        regularRadio.addActionListener(e -> {
            updateMissingPotsPanel();
            updateRealmChars();
            updateSelection();
        });
        seasonalRadio.addActionListener(e -> {
            updateMissingPotsPanel();
            updateRealmChars();
            updateSelection();
        });

        ButtonGroup g = new ButtonGroup();
        g.add(regularRadio);
        g.add(seasonalRadio);
        panelVeryBot.add(Box.createHorizontalGlue());
        panelVeryBot.add(regularRadio);
        panelVeryBot.add(Box.createHorizontalGlue());
        panelVeryBot.add(seasonalRadio);
        panelVeryBot.add(Box.createHorizontalGlue());
        rightPotDisplay.add(panelVeryBot);
        boxPots.add(rightPotDisplay);

        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.X_AXIS));
        outer.add(Box.createHorizontalGlue());
        outer.add(boxPots);
        outer.add(Box.createHorizontalGlue());
        return outer;
    }

    /**
     * Updates the stat maxing tab top display with missing pots.
     */
    private void updateMissingPotsPanel() {
        int[] totalPots = new int[8];
        boolean seasonalSelected = seasonalRadio.isSelected();
        VaultData vaultData = seasonalSelected ? data.seasonalVault : data.regularVault;
        if (vaultData != null) {
            if (charInvs.isSelected()) {
                vaultData.getPlayerInvPots(totalPots);
            }
            if (mainVault.isSelected()) {
                vaultData.getVaultChestPots(totalPots);
            }
            if (potStorage.isSelected()) {
                vaultData.getPotStoragePots(totalPots);
            }
            if (giftChest.isSelected()) {
                vaultData.getGiftChestPots(totalPots);
            }
        }
        if (data.chars != null) {
            for (RealmCharacter c : data.chars) {
                if (c.seasonal == seasonalSelected && charSelected(c)) {
                    int[] missing = new int[8];
                    statMissing(c, missing);
                    Arrays.setAll(totalPots, i -> totalPots[i] - missing[i]);
                }
            }
        }
        for (int i = 0; i < 8; i++) {
            int pots = totalPots[i];
            potStatLabels[i].setText("" + pots);
        }
    }

    /**
     * Individual characters in the max stat character list with their stats and what is missing.
     */
    private JPanel createPanelCharWithMissingStats(RealmCharacter c, int maxedStats) {
        JPanel boxChars = CharacterPanelGUI.createMainBox();
        JPanel panelLeft = CharacterPanelGUI.createLeftBox();
        boxChars.add(statMaxingChar(panelLeft, c, maxedStats));

        JPanel panelTop = new JPanel();
        JPanel panelMid = new JPanel();
        JPanel panelBot = new JPanel();
        boxChars.add(CharacterPanelGUI.createMidRightBox(panelTop, panelMid, panelBot));
        statMaxingStats(c, panelTop, panelMid, panelBot);
        return boxChars;
    }

    /**
     * Individual characters in the max stat character list
     * with icons and basic info with a selection checkbox.
     */
    private JPanel statMaxingChar(JPanel panel, RealmCharacter character, int maxedStats) {
        panel.add(Box.createVerticalGlue());
        JLabel seasonalLabel = new JLabel(character.seasonal ? "Seasonal" : "");
        seasonalLabel.setForeground(Color.cyan);
        seasonalLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        panel.add(seasonalLabel);
        int eq = character.skin;
        if (eq == 0) eq = character.classNum;
        JLabel characterLabel = new JLabel(ImageBuffer.getOutlinedIcon(eq, 30), JLabel.CENTER);
        characterLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        panel.add(characterLabel);
//        JCheckBox checkBox = checkBoxMissingStats(c.classString + " " + c.level);
        JCheckBox checkBox = new JCheckBox(character.classString + " " + character.level);
        checkBox.addActionListener(e -> {
            JCheckBox j = (JCheckBox) e.getSource();
//            Store.INSTANCE.dispatch(new SetCharacter(character.charId, j.isSelected()));
            characters.put(character.charId, j.isSelected());
            updateMissingPotsPanel();
        });
        if (charSelected(character)) {
            checkBox.setSelected(true);
        }

        JLabel fame = new JLabel("Fame: " + character.fame);
        JLabel stat = new JLabel(maxedStats + " / " + 8);
        checkBox.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        fame.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        stat.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        panel.add(checkBox);
        panel.add(fame);
        panel.add(stat);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    /**
     * Checks if character should be computed for maxing if selected.
     */
    private boolean charSelected(RealmCharacter character) {
        return characters != null && characters.get(character.charId) != null && characters.get(character.charId);
    }

    /**
     * Stat maxing character stats to be added on the mid right panel.
     */
    private void statMaxingStats(RealmCharacter c, JPanel panelTop, JPanel panelMid, JPanel panelBot) {
        int[] missing = new int[8];
        statMissing(c, missing);

        panelTop.add(Box.createHorizontalGlue());
        statLabel(panelTop, "HP", c.hp, missing[0]);
        panelTop.add(Box.createHorizontalGlue());
        statLabel(panelTop, "MP", c.mp, missing[1]);
        panelTop.add(Box.createHorizontalGlue());

        panelMid.add(Box.createHorizontalGlue());
        statLabel(panelMid, "Atk", c.atk, missing[2]);
        panelMid.add(Box.createHorizontalGlue());
        statLabel(panelMid, "Def", c.def, missing[3]);
        panelMid.add(Box.createHorizontalGlue());
        statLabel(panelMid, "Spd", c.spd, missing[4]);
        panelMid.add(Box.createHorizontalGlue());

        panelBot.add(Box.createHorizontalGlue());
        statLabel(panelBot, "Dex", c.dex, missing[5]);
        panelBot.add(Box.createHorizontalGlue());
        statLabel(panelBot, "Vit", c.vit, missing[6]);
        panelBot.add(Box.createHorizontalGlue());
        statLabel(panelBot, "Wis", c.wis, missing[7]);
        panelBot.add(Box.createHorizontalGlue());
    }

    /**
     * Individual label to be added for stat maxing stats.
     */
    private void statLabel(JPanel panel, String name, int stat, int missing) {
        JLabel l = new JLabel();
//        new JLabel("<html>Text color: <font color='red'>red</font></html>");
        String s;
        if (missing != 0) {
            s = String.format(name + ": %d (%d)", stat, missing);
        } else {
            l.setForeground(Color.yellow);
            s = String.format(name + ": %d", stat);
        }
        l.setText(s);
        panel.add(l);
    }

    /**
     * Creates a potion image icon to display.
     *
     * @param pot Potion type
     */
    private ImageIcon getImageIcon(StatPotion pot) {
        return ImageBuffer.getOutlinedIcon(pot.smallId(), 15);
    }
}
