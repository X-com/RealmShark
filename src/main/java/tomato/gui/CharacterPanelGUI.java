package tomato.gui;

import assets.AssetMissingException;
import assets.ImageBuffer;
import tomato.logic.CharacterData;
import tomato.logic.backend.VaultData;
import tomato.logic.backend.action.statmaxing.*;
import tomato.logic.backend.data.RealmCharacter;
import tomato.logic.backend.redux.Store;
import tomato.logic.backend.state.RootState;
import tomato.logic.backend.state.StatMaxingGuiState;
import tomato.logic.enums.CharacterClass;
import tomato.logic.enums.StatPotion;
import util.Pair;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Character GUI class to display character data in the character tab.
 */
public class CharacterPanelGUI extends JPanel {
    private static final int CHAR_PANEL_SIZE = 120;
    private JPanel charPanel;
    private JPanel maxingPanel;
    private JLabel[] potStatLabels = new JLabel[8];
    private int[] regularTotalPots = new int[8];
    private int[] seasonalTotalPots = new int[8];
    private boolean seasonalStatMaxingDisplay;
    private ArrayList<Pair<RealmCharacter, JCheckBox>> charStatSelection = new ArrayList<>();
    private VaultData vaultData;
    private JCheckBox charInvs, mainVault, potStorage, giftChest;
    private JRadioButton regularRadio, seasonalRadio;
    private JPanel exaltPanel;

    private static ArrayList<RealmCharacter> chars;

    public CharacterPanelGUI() {
        setLayout(new BorderLayout());

        charPanel = new JPanel();
        JScrollPane scrollPaneChars = new JScrollPane(charPanel);
        scrollPaneChars.getVerticalScrollBar().setUnitIncrement(40);

        JPanel mainMaxingPanel = new JPanel(new BorderLayout());
        maxingPanel = new JPanel();
        JScrollPane scrollPaneMaxing = new JScrollPane(maxingPanel);
        scrollPaneMaxing.getVerticalScrollBar().setUnitIncrement(40);
        mainMaxingPanel.add(scrollPaneMaxing, BorderLayout.CENTER);
        mainMaxingPanel.add(missingPotsPanel(), BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        add(tabbedPane);
        tabbedPane.addTab("Characters", scrollPaneChars);
        tabbedPane.addTab("Stat Maxing", mainMaxingPanel);

//        JButton button = new JButton("Test");
//        button.addActionListener(e -> {
//            try {
//                mainMaxingPanel.removeAll();
//                mainMaxingPanel.add(scrollPaneMaxing, BorderLayout.CENTER);
//                mainMaxingPanel.add(missingPotsPanel(), BorderLayout.NORTH);
//                mainMaxingPanel.revalidate();
//                java.io.InputStream is = Util.resourceFilePath("char");
//                FileInputStream is = new FileInputStream("tiles/assets/char");
//                String result = new java.io.BufferedReader(new java.io.InputStreamReader(is)).lines().collect(java.util.stream.Collectors.joining("\n"));
//                ArrayList<Character> l = HttpCharListRequest.getCharList(result);
//                chars = l;
//                updateCharPanel(chars);
//                updateMaxingPanel(l);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        });
//        add(button, BorderLayout.SOUTH);

        charPanel.add(new Label("Enter Daily Quest Room to see chars"));
        maxingPanel.add(new Label("Enter Daily Quest Room to see chars"));

        Store.INSTANCE.subscribe(this::onUpdate);
        onUpdate(Store.INSTANCE.getState());
    }

    private void onUpdate(RootState state) {
        StatMaxingGuiState s = state.statMaxingGui;
        charInvs.setSelected(s.isCharInv);
        mainVault.setSelected(s.isMainVault);
        potStorage.setSelected(s.isPotStorage);
        giftChest.setSelected(s.isGiftChest);

        regularRadio.setSelected(!s.isSeasonal);
        seasonalRadio.setSelected(s.isSeasonal);


        updateStatMaxPots();
    }

    /**
     * Tooltip showing exalts when hovering over char.
     */
    private String exaltStats(RealmCharacter c) {
        int[] exalts = RealmCharacter.exalts.get(c.classNum);
        ToolTipManager.sharedInstance().setInitialDelay(200);
        ToolTipManager.sharedInstance().setDismissDelay(1000000000);
        return String.format("<html>%d :HP<br>%d :MP<br>%d :Atk<br>%d :Def<br>%d :Spd<br>%d :Dex<br>%5d :Vit<br>%d :Wis</html>", exalts[7], exalts[6], exalts[5], exalts[4], exalts[1], exalts[0], exalts[2], exalts[3]);
    }

    /**
     * Section made for adding Char skin, char type, char level, char fame and char stats.
     */
    private JPanel leftColumn(RealmCharacter c) {
        JPanel panel = createLeftBox();

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
            } catch (Exception e) {
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
     * Creates a large box to add smaller content boxes into.
     *
     * @return Returns a main box panel.
     */
    private JPanel createMainBox() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        panel.setPreferredSize(new Dimension(370, CHAR_PANEL_SIZE));
        panel.setMaximumSize(new Dimension(370, CHAR_PANEL_SIZE));
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        return panel;
    }

    /**
     * Left box to fill with components.
     *
     * @return Left box to fill with components
     */
    private JPanel createLeftBox() {
        JPanel panel = new JPanel();
        panel.setMaximumSize(new Dimension(120, CHAR_PANEL_SIZE));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
        return panel;
    }

    /**
     * Right mid larger box to fill with components.
     *
     * @return Right mid larger box to fill with components
     */
    private JPanel createMidRightBox(JPanel panelTop, JPanel panelMid, JPanel panelBot) {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(240, 120));
        panel.setLayout(new GridLayout(3, 1));
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));

        panelTop.setLayout(new BoxLayout(panelTop, BoxLayout.X_AXIS));
        panelMid.setLayout(new BoxLayout(panelMid, BoxLayout.X_AXIS));
        panelBot.setLayout(new BoxLayout(panelBot, BoxLayout.X_AXIS));

        panel.add(panelTop);
        panel.add(panelMid);
        panel.add(panelBot);
        return panel;
    }

    /**
     * Character tab update, clears all data in the tab and repopulates it.
     *
     * @param list Character info to be updated in the char tab.
     */
    private void updateCharPanel(ArrayList<RealmCharacter> list) {
        chars = list;
        charPanel.setLayout(new BoxLayout(charPanel, BoxLayout.Y_AXIS));
        charPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        charPanel.add(Box.createVerticalGlue());
        charPanel.removeAll();
        for (RealmCharacter c : list) {
            JPanel box = createMainBox();

            box.add(leftColumn(c));
            box.add(midColumn(c));
            box.add(invBackpack(c));

            charPanel.add(box);
        }
        charPanel.revalidate();
    }

    /**
     * Updates the classes with missing pots in the stat potion maxing tab.
     */
    private void updateMaxingPanel(ArrayList<RealmCharacter> list) {
        if (list == null) return;
        maxingPanel.setLayout(new BoxLayout(maxingPanel, BoxLayout.Y_AXIS));
        maxingPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        maxingPanel.add(Box.createVerticalGlue());
        maxingPanel.removeAll();

        charStatSelection.clear();
        for (RealmCharacter c : list) {
            if ((c.seasonal && seasonalStatMaxingDisplay) || (!c.seasonal && !seasonalStatMaxingDisplay)) {
                int maxedStats = CharacterData.statsMaxed(c);
                if (maxedStats != 8) {
                    JPanel boxChars = createPanelCharWithMissingStats(c, maxedStats);
                    maxingPanel.add(boxChars);
                }
            }
        }
        exaltPanel = createMainBox();
        exaltBox(exaltPanel);
        maxingPanel.add(exaltPanel);

        maxingPanel.revalidate();
    }

    /**
     * Box at the bottom of the players displaying missing exalts.
     */
    private void exaltBox(JPanel boxExalt) {
        boxExalt.removeAll();
        int[] exalts = new int[8];
        for (CharacterClass cc : CharacterClass.CHAR_CLASS_LIST) {
            int[] charExalt = RealmCharacter.exalts.get(cc.getId());
            if (charExalt == null) return;
            for (int i = 0; i < 8; i++) {
                exalts[i] += Math.max(75 - charExalt[i], 0);
            }
        }
        JPanel panelLeft = createLeftBox();
        boxExalt.add(panelLeft);
        panelLeft.add(Box.createVerticalGlue());
        panelLeft.add(new JLabel("Missing exalts"));
        panelLeft.add(Box.createVerticalGlue());
        JPanel panelTop = new JPanel();
        JPanel panelMid = new JPanel();
        JPanel panelBot = new JPanel();
        boxExalt.add(createMidRightBox(panelTop, panelMid, panelBot));
        panelTop.add(Box.createHorizontalGlue());
        panelTop.add(new JLabel("Life: " + exalts[7]));
        panelTop.add(Box.createHorizontalGlue());
        panelTop.add(new JLabel("Mana: " + exalts[6]));
        panelTop.add(Box.createHorizontalGlue());
        panelMid.add(Box.createHorizontalGlue());
        panelMid.add(new JLabel("Atk: " + exalts[5]));
        panelMid.add(Box.createHorizontalGlue());
        panelMid.add(new JLabel("Def: " + exalts[4]));
        panelMid.add(Box.createHorizontalGlue());
        panelMid.add(new JLabel("Spd: " + exalts[1]));
        panelMid.add(Box.createHorizontalGlue());
        panelBot.add(Box.createHorizontalGlue());
        panelBot.add(new JLabel("Dex: " + exalts[0]));
        panelBot.add(Box.createHorizontalGlue());
        panelBot.add(new JLabel("Vit: " + exalts[2]));
        panelBot.add(Box.createHorizontalGlue());
        panelBot.add(new JLabel("Wis: " + exalts[3]));
        panelBot.add(Box.createHorizontalGlue());
    }

    /**
     * Top pots missing display in the stat maxing tab.
     */
    private JPanel missingPotsPanel() {
        JPanel boxPots = createMainBox();
        boxPots.setPreferredSize(new Dimension(390, CHAR_PANEL_SIZE));
        boxPots.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        JPanel panelLeft = createLeftBox();
        panelLeft.add(Box.createVerticalGlue());
        charInvs = new JCheckBox("Char Invs");
        mainVault = new JCheckBox("Main Vault");
        potStorage = new JCheckBox("Pot Storage");
        giftChest = new JCheckBox("Gift Chest");
        charInvs.addActionListener(e -> {
            JCheckBox c = (JCheckBox) e.getSource();
            Store.INSTANCE.dispatch(new SetCharInvs(c.isSelected()));
        });
        mainVault.addActionListener(e -> {
            JCheckBox c = (JCheckBox) e.getSource();
            Store.INSTANCE.dispatch(new SetMainVault(c.isSelected()));
        });
        potStorage.addActionListener(e -> {
            JCheckBox c = (JCheckBox) e.getSource();
            Store.INSTANCE.dispatch(new SetPotStorage(c.isSelected()));
        });
        giftChest.addActionListener(e -> {
            JCheckBox c = (JCheckBox) e.getSource();
            Store.INSTANCE.dispatch(new SetGiftChest(c.isSelected()));
        });
        panelLeft.add(charInvs);
        panelLeft.add(mainVault);
        panelLeft.add(potStorage);
        panelLeft.add(giftChest);
        panelLeft.add(Box.createVerticalGlue());
        boxPots.add(panelLeft);

        JPanel panelTop = new JPanel();
        JPanel panelMid = new JPanel();
        JPanel panelBot = new JPanel();
        JPanel rightPotDisplay = createMidRightBox(panelTop, panelMid, panelBot);
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

        for (int i = 0; i < 8; i++) {
            potStatLabels[i].setForeground(Color.white);
        }

        rightPotDisplay.setLayout(new GridLayout(4, 1));
        JPanel panelVeryBot = new JPanel();
        regularRadio = new JRadioButton("Regular", true);
        seasonalRadio = new JRadioButton("Seasonal");
        seasonalRadio.setForeground(Color.cyan);
//        ActionListener l = e -> {
//            seasonalStatMaxingDisplay = seasonalRadio.isSelected();
//            updateMaxingPanel(chars);
//            updateStatMaxPots();
//        };
        regularRadio.addActionListener(e-> {
            JRadioButton c = (JRadioButton) e.getSource();
            Store.INSTANCE.dispatch(new SetSeasonal(!c.isSelected()));
        });
        seasonalRadio.addActionListener(e-> {
            JRadioButton c = (JRadioButton) e.getSource();
            Store.INSTANCE.dispatch(new SetSeasonal(c.isSelected()));
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
    private void updateStatMaxPots() {
        Arrays.fill(regularTotalPots, 0);
        Arrays.fill(seasonalTotalPots, 0);
        if (vaultData != null) {
            if (charInvs.isSelected()) {
                vaultData.getPlayerInvPots(regularTotalPots, seasonalTotalPots);
            }
            if (mainVault.isSelected()) {
                vaultData.getVaultChestPots(regularTotalPots, seasonalTotalPots);
            }
            if (potStorage.isSelected()) {
                vaultData.getPotStoragePots(regularTotalPots, seasonalTotalPots);
            }
            if (giftChest.isSelected()) {
                vaultData.getGiftChestPots(regularTotalPots, seasonalTotalPots);
            }
        }
        if (chars != null) {
            for (RealmCharacter c : chars) {
                if (calcChar(c)) {
                    int[] missing = new int[8];
                    CharacterData.statMissing(c, missing);
                    if (c.seasonal) Arrays.setAll(seasonalTotalPots, i -> seasonalTotalPots[i] - missing[i]);
                    else Arrays.setAll(regularTotalPots, i -> regularTotalPots[i] - missing[i]);
                }
            }
        }
        for (int i = 0; i < 8; i++) {
            int pots;
            if (seasonalStatMaxingDisplay) {
                pots = seasonalTotalPots[i];
            } else {
                pots = regularTotalPots[i];
            }
            potStatLabels[i].setText("" + pots);
            if (pots < 0) potStatLabels[i].setForeground(Color.pink);
            else if (pots > 0) potStatLabels[i].setForeground(Color.green);
            else potStatLabels[i].setForeground(Color.white);
        }
    }

    /**
     * Individual characters in the max stat character list with their stats and what is missing.
     */
    private JPanel createPanelCharWithMissingStats(RealmCharacter c, int maxedStats) {
        JPanel boxChars = createMainBox();
        JPanel panelLeft = createLeftBox();
        boxChars.add(statMaxingChar(panelLeft, c, maxedStats));

        JPanel panelTop = new JPanel();
        JPanel panelMid = new JPanel();
        JPanel panelBot = new JPanel();
        boxChars.add(createMidRightBox(panelTop, panelMid, panelBot));
        statMaxingStats(c, panelTop, panelMid, panelBot);
        return boxChars;
    }

    /**
     * Individual characters in the max stat character list
     * with icons and basic info with a selection checkbox.
     */
    private JPanel statMaxingChar(JPanel panel, RealmCharacter c, int maxedStats) {
        panel.add(Box.createVerticalGlue());
        JLabel seasonalLabel = new JLabel(c.seasonal ? "Seasonal" : "");
        seasonalLabel.setForeground(Color.cyan);
        seasonalLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        panel.add(seasonalLabel);
        int eq = c.skin;
        if (eq == 0) eq = c.classNum;
        try {
            BufferedImage img = ImageBuffer.getImage(eq);
            ImageIcon icon = new ImageIcon(img.getScaledInstance(30, 30, Image.SCALE_DEFAULT));
            JLabel characterLabel = new JLabel(icon, JLabel.CENTER);
            characterLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            panel.add(characterLabel);
        } catch (IOException | AssetMissingException e) {
            e.printStackTrace();
        }
        JCheckBox checkBox = checkBoxMissingStats(c.classString + " " + c.level);
        charStatSelection.add(new Pair<>(c, checkBox));
        JLabel fame = new JLabel("Fame: " + c.fame);
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
     * Individual vaults checkbox created to be added in the top pot stat maxing display.
     */
    private JCheckBox checkBoxMissingStats(String s) {
        JCheckBox checkBox = new JCheckBox(s);
        checkBox.addActionListener(e -> updateStatMaxPots());
        return checkBox;
    }

    /**
     * Checks if character should be computed for maxing if selected.
     */
    private boolean calcChar(RealmCharacter c) {
        for (Pair<RealmCharacter, JCheckBox> p : charStatSelection) {
            if (p.left() == c) return p.right().isSelected();
        }
        return false;
    }

    /**
     * Stat maxing character stats to be added on the mid right panel.
     */
    private void statMaxingStats(RealmCharacter c, JPanel panelTop, JPanel panelMid, JPanel panelBot) {
        int[] missing = new int[8];
        CharacterData.statMissing(c, missing);

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
            l.setForeground(Color.white);
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
        ImageIcon icon = new ImageIcon(StatPotion.getImage(pot).getScaledInstance(15, 15, Image.SCALE_DEFAULT));
        return icon;
    }

    /**
     * Method to update character tabs with new char data.
     *
     * @param list Character info to be updated.
     */
    public void updateCharacters(ArrayList<RealmCharacter> list) {
        if (list == null) return;
        chars = list;

        updateCharPanel(list);
        updateMaxingPanel(list);
    }

    /**
     * Vault update method called when receiving vault packets.
     *
     * @param vaultData Vault data to update the GUI with.
     */
    public void vaultDataUpdate(VaultData vaultData) {
        this.vaultData = vaultData;
        updateStatMaxPots();
    }

    public void updateExaltBox() {
        if (exaltPanel != null) {
            exaltBox(exaltPanel);
            exaltPanel.revalidate();
        }
    }
}
