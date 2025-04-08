package tomato.gui.maingui;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.*;
import tomato.Tomato;
import tomato.gui.TomatoGUI;
import tomato.gui.chat.ChatGUI;
import tomato.gui.chat.ChatPingGUI;
import tomato.gui.dps.DpsDisplayOptions;
import tomato.gui.dps.DpsGUI;
import tomato.gui.stats.LootGUI;
import tomato.realmshark.Sound;
import tomato.realmshark.enums.LootBags;
import util.PropertiesManager;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Menu bar builder class
 */
public class TomatoMenuBar implements ActionListener {
    private JMenuItem about, borders, clearChat, bandwidth, javav, clearDpsLogs, theme, fontMenu, dpsOptions, chat, sound, chatPingMessage, entityIdPingMessage;
    private JRadioButtonMenuItem fontSize8, fontSize12, fontSize16, fontSize24, fontSize48, fontSizeCustom;
    private JRadioButtonMenuItem themeDarcula, themeighContrastDark, themeHighContrastLight, themeIntelliJ, themeSolarizedDark, themeSolarizedLight;
    private JRadioButtonMenuItem fontNameMonospaced, fontNameDialog, fontNameDialogInput, fontNameSerif, fontNameSansSerif, fontNameSegoe;
    private JRadioButtonMenuItem dpsEquipmentNone, dpsEquipmentSimple, dpsEquipmentFull, dpsIcon;
    private JRadioButtonMenuItem dpsSortLastHit, dpsSortFirstHit, dpsSortMaxHp, dpsSortFightTimer, dpsSortBossOnly;
    private JCheckBoxMenuItem fontStyleBold, fontStyleItalic, dpsShowMe, saveChat, chatPing, chatPingGuild, whiteBagSound, chatPingParty, orangeBagSound, redBagSound, goldBagSound, eggBagSound, tradePing, disableDataSending;
    private JCheckBoxMenuItem filterWhiteBag, filterOrangeBag, filterRedBag, filterGoldBag, filterEggBag, filterBlueBag, filterTealBag, filterPurpleBag;
    private JSlider soundSlider;
    private JMenu file, edit, info;
    private JMenuBar jMenuBar;
    private JFrame frame;
    private static JMenuItem sniffer;

    /**
     * Main builder for menus for the Tomato GUI.
     *
     * @return returns this jMenuBar object to be added to the main frame.
     */
    public JMenuBar make() {
        jMenuBar = new JMenuBar();

        file = new JMenu("File");
        jMenuBar.add(file);

        chat = new JMenu("Chat");
        sound = new JMenu("Sound");
        theme = new JMenu("Theme");
        fontMenu = new JMenu("Font");
        dpsOptions = new JMenu("DPS Options");
        JMenu filterBags = new JMenu("Filter Loot");

        edit = new JMenu("Edit");
        edit.add(chat);
        edit.add(sound);
        edit.add(theme);
        edit.add(fontMenu);
        edit.add(dpsOptions);
        edit.add(filterBags);
        jMenuBar.add(edit);

        sniffer = new JMenuItem("Start Sniffer");
        sniffer.addActionListener(this);
        file.add(sniffer);
        file.add(new JSeparator(SwingConstants.HORIZONTAL));
        disableDataSending = new JCheckBoxMenuItem("Opt-out Loot Sharing");
        disableDataSending.setToolTipText("Disables sending loot to server");
        disableDataSending.addActionListener(this);
        file.add(disableDataSending);
        setFileCheckbox();

        chatPingMessage = new JMenuItem("Chat Message Pings");
        chatPingMessage.addActionListener(this);
        saveChat = new JCheckBoxMenuItem("Save Chat");
        saveChat.addActionListener(this);
        clearChat = new JMenuItem("Clear Chat");
        clearChat.addActionListener(this);

        chat.add(chatPingMessage);
        chat.add(saveChat);
        chat.add(new JSeparator(SwingConstants.HORIZONTAL));
        chat.add(clearChat);
        setChatCheckbox();

        soundSlider = new JSlider(0, 100, 100);
        soundSlider.addChangeListener(this::sliderChange);
        chatPing = new JCheckBoxMenuItem("Ping PM Chat");
        chatPing.addActionListener(this);
        chatPingGuild = new JCheckBoxMenuItem("Ping Guild Chat");
        chatPingGuild.addActionListener(this);
        chatPingParty = new JCheckBoxMenuItem("Ping Party Chat");
        chatPingParty.addActionListener(this);

        whiteBagSound = new JCheckBoxMenuItem("Ping White Bag");
        whiteBagSound.addActionListener(this);
        orangeBagSound = new JCheckBoxMenuItem("Ping Orange Bag");
        orangeBagSound.addActionListener(this);
        redBagSound = new JCheckBoxMenuItem("Ping Red Bag");
        redBagSound.addActionListener(this);
        goldBagSound = new JCheckBoxMenuItem("Ping Gold Bag");
        goldBagSound.addActionListener(this);
        eggBagSound = new JCheckBoxMenuItem("Ping Egg Bag");
        eggBagSound.addActionListener(this);
        tradePing = new JCheckBoxMenuItem("Trade Ping");
        tradePing.addActionListener(this);

        entityIdPingMessage = new JMenuItem("Entity ID Pings");
        entityIdPingMessage.addActionListener(this);

        sound.add(new JLabel("Volume:"));
        sound.add(soundSlider);
        sound.add(new JSeparator(SwingConstants.HORIZONTAL));
        sound.add(entityIdPingMessage);
        sound.add(new JSeparator(SwingConstants.HORIZONTAL));
        sound.add(chatPing);
        sound.add(chatPingParty);
        sound.add(chatPingGuild);
        sound.add(whiteBagSound);
        sound.add(orangeBagSound);
        sound.add(redBagSound);
        sound.add(goldBagSound);
        sound.add(eggBagSound);
        sound.add(tradePing);
        setSoundCheckbox();

        filterWhiteBag = new JCheckBoxMenuItem("Show White Bags");
        filterWhiteBag.addActionListener(e -> {
            LootGUI.filterWhiteBag = filterWhiteBag.isSelected();
            PropertiesManager.setProperties("filterWhiteBag", Boolean.toString(filterWhiteBag.isSelected()));
            LootGUI.applyFilters();
        });
        filterOrangeBag = new JCheckBoxMenuItem("Show Orange Bags");
        filterOrangeBag.addActionListener(e -> {
            LootGUI.filterOrangeBag = filterOrangeBag.isSelected();
            PropertiesManager.setProperties("filterOrangeBag", Boolean.toString(filterOrangeBag.isSelected()));
            LootGUI.applyFilters();
        });
        filterRedBag = new JCheckBoxMenuItem("Show Red Bags");
        filterRedBag.addActionListener(e -> {
            LootGUI.filterRedBag = filterRedBag.isSelected();
            PropertiesManager.setProperties("filterRedBag", Boolean.toString(filterRedBag.isSelected()));
            LootGUI.applyFilters();
        });
        filterGoldBag = new JCheckBoxMenuItem("Show Gold Bags");
        filterGoldBag.addActionListener(e -> {
            LootGUI.filterGoldBag = filterGoldBag.isSelected();
            PropertiesManager.setProperties("filterGoldBag", Boolean.toString(filterGoldBag.isSelected()));
            LootGUI.applyFilters();
        });
        filterEggBag = new JCheckBoxMenuItem("Show Egg Bags");
        filterEggBag.addActionListener(e -> {
            LootGUI.filterEggBag = filterEggBag.isSelected();
            PropertiesManager.setProperties("filterEggBag", Boolean.toString(filterEggBag.isSelected()));
            LootGUI.applyFilters();
        });
        filterBlueBag = new JCheckBoxMenuItem("Show Blue Bags");
        filterBlueBag.addActionListener(e -> {
            LootGUI.filterBlueBag = filterBlueBag.isSelected();
            PropertiesManager.setProperties("filterBlueBag", Boolean.toString(filterBlueBag.isSelected()));
            LootGUI.applyFilters();
        });
        filterTealBag = new JCheckBoxMenuItem("Show Teal Bags");
        filterTealBag.addActionListener(e -> {
            LootGUI.filterTealBag = filterTealBag.isSelected();
            PropertiesManager.setProperties("filterTealBag", Boolean.toString(filterTealBag.isSelected()));
            LootGUI.applyFilters();
        });
        filterPurpleBag = new JCheckBoxMenuItem("Show Purple Bags");
        filterPurpleBag.addActionListener(e -> {
            LootGUI.filterPurpleBag = filterPurpleBag.isSelected();
            PropertiesManager.setProperties("filterPurpleBag", Boolean.toString(filterPurpleBag.isSelected()));
            LootGUI.applyFilters();
        });

        // Add filter options to the Filter Loot menu
        filterBags.add(filterWhiteBag);
        filterBags.add(filterOrangeBag);
        filterBags.add(filterRedBag);
        filterBags.add(filterGoldBag);
        filterBags.add(filterEggBag);
        filterBags.add(filterBlueBag);
        filterBags.add(filterTealBag);
        filterBags.add(filterPurpleBag);
        loadFilteredBags();

        borders = new JMenuItem("Borders");
        borders.addActionListener(this);

        theme.add(borders);
        theme.add(new JSeparator(SwingConstants.HORIZONTAL));

        ButtonGroup groupTheme = new ButtonGroup();
        themeDarcula = addRadioButtonMenuItem(groupTheme, theme, "Darcula Theme");
        themeighContrastDark = addRadioButtonMenuItem(groupTheme, theme, "High Contrast Dark Theme");
        themeHighContrastLight = addRadioButtonMenuItem(groupTheme, theme, "High Contrast Light Theme");
        themeIntelliJ = addRadioButtonMenuItem(groupTheme, theme, "IntelliJ Theme");
        themeSolarizedDark = addRadioButtonMenuItem(groupTheme, theme, "Solarized Dark Theme");
        themeSolarizedLight = addRadioButtonMenuItem(groupTheme, theme, "Solarized Light Theme");
        setThemeRadioButton();

        ButtonGroup groupFontSize = new ButtonGroup();
        fontSize8 = addRadioButtonMenuItem(groupFontSize, fontMenu, "Size 8");
        fontSize12 = addRadioButtonMenuItem(groupFontSize, fontMenu, "Size 12");
        fontSize16 = addRadioButtonMenuItem(groupFontSize, fontMenu, "Size 16");
        fontSize24 = addRadioButtonMenuItem(groupFontSize, fontMenu, "Size 24");
        fontSize48 = addRadioButtonMenuItem(groupFontSize, fontMenu, "Size 48");
        fontSizeCustom = addRadioButtonMenuItem(groupFontSize, fontMenu, "Custom Size");
        fontMenu.add(new JSeparator(SwingConstants.HORIZONTAL));
        setFontSizeRadioButton();

        ButtonGroup groupFontName = new ButtonGroup();
        fontNameMonospaced = addRadioButtonMenuItem(groupFontName, fontMenu, "Monospaced");
        fontNameSegoe = addRadioButtonMenuItem(groupFontName, fontMenu, "Segoe");
        fontNameDialog = addRadioButtonMenuItem(groupFontName, fontMenu, "Dialog");
        fontNameDialogInput = addRadioButtonMenuItem(groupFontName, fontMenu, "DialogInput");
        fontNameSerif = addRadioButtonMenuItem(groupFontName, fontMenu, "Serif");
        fontNameSansSerif = addRadioButtonMenuItem(groupFontName, fontMenu, "SansSerif");
        fontStyleBold = new JCheckBoxMenuItem("Bold");
        fontStyleBold.addActionListener(this);
        fontStyleItalic = new JCheckBoxMenuItem("Italic");
        fontStyleItalic.addActionListener(this);
        fontMenu.add(new JSeparator(SwingConstants.HORIZONTAL));
        fontMenu.add(fontStyleBold);
        fontMenu.add(fontStyleItalic);
        setFontNameRadioButton();

        dpsShowMe = new JCheckBoxMenuItem("Show me");
        dpsShowMe.addActionListener(this);
        dpsShowMe.setToolTipText("Shows an arrow -> next to your name in the dps logs");
        dpsOptions.add(dpsShowMe);
        setShowMeCheckbox();

        dpsOptions.add(new JSeparator(SwingConstants.HORIZONTAL));

        ButtonGroup groupDpsEquipment = new ButtonGroup();
        dpsEquipmentNone = addRadioButtonMenuItem(groupDpsEquipment, dpsOptions, "None");
        dpsEquipmentSimple = addRadioButtonMenuItem(groupDpsEquipment, dpsOptions, "Simple");
        dpsEquipmentFull = addRadioButtonMenuItem(groupDpsEquipment, dpsOptions, "Full");
        dpsIcon = addRadioButtonMenuItem(groupDpsEquipment, dpsOptions, "Icon");
        setEquipmentRadioButton();

        dpsOptions.add(new JSeparator(SwingConstants.HORIZONTAL));

        ButtonGroup groupDpsSort = new ButtonGroup();
        dpsSortLastHit = addRadioButtonMenuItem(groupDpsSort, dpsOptions, "Last Hit");
        dpsSortFirstHit = addRadioButtonMenuItem(groupDpsSort, dpsOptions, "First Hit");
        dpsSortMaxHp = addRadioButtonMenuItem(groupDpsSort, dpsOptions, "Health Points");
        dpsSortFightTimer = addRadioButtonMenuItem(groupDpsSort, dpsOptions, "Fight Time");
        dpsSortBossOnly = addRadioButtonMenuItem(groupDpsSort, dpsOptions, "Only Bosses");
        setDpsSortRadioButton();

        dpsOptions.add(new JSeparator(SwingConstants.HORIZONTAL));
        clearDpsLogs = new JMenuItem("Clear DPS Logs");
        clearDpsLogs.addActionListener(this);
        dpsOptions.add(clearDpsLogs);

        about = new JMenuItem("About");
        about.addActionListener(this);
        bandwidth = new JMenuItem("Net traffic");
        bandwidth.addActionListener(this);
        javav = new JMenuItem("Java version");
        javav.addActionListener(this);
        info = new JMenu("Info");
        info.add(about);
        info.add(javav);
        info.add(bandwidth);
        jMenuBar.add(info);

        autoStartSnifferPreset();

        return jMenuBar;
    }

    private void sliderChange(ChangeEvent changeEvent) {
        JSlider source = (JSlider) changeEvent.getSource();
        if (!source.getValueIsAdjusting()) {
            if (soundSlider == source) {
                int v = source.getValue();
                Sound.setVolume(v);
                PropertiesManager.setProperties("soundVolume", String.valueOf(v));
                Sound.pm.play();
            }
        }
    }

    /**
     * Auto-starts the sniffer if the app was closed when it was running.
     */
    private void autoStartSnifferPreset() {
        String snifAuto = PropertiesManager.getProperty("sniffer");
        if (snifAuto == null || !snifAuto.equals("T")) return;

        sniffer.setText("Stop Sniffer");
        Tomato.startPacketSniffer();
        TomatoGUI.setStateOfSniffer(true);
    }

    /**
     * Selects the theme radio button from the preset.
     */
    private void setThemeRadioButton() {
        String theme = PropertiesManager.getProperty("theme");

        if (theme == null) {
            themeDarcula.setSelected(true);
            return;
        }

        switch (theme) {
            case "contrastDark":
                themeighContrastDark.setSelected(true);
                break;
            case "contrastLight":
                themeHighContrastLight.setSelected(true);
                break;
            case "intelliJ":
                themeIntelliJ.setSelected(true);
                break;
            case "solarizedDark":
                themeSolarizedDark.setSelected(true);
                break;
            case "solarizedLight":
                themeSolarizedLight.setSelected(true);
                break;
            default:
            case "darcula":
                themeDarcula.setSelected(true);
                break;
        }
    }

    /**
     * Selects the font size radio button from the preset.
     */
    private void setFontSizeRadioButton() {
        String fontSize = PropertiesManager.getProperty("fontSize");

        if (fontSize == null) {
            fontSize12.setSelected(true);
            return;
        }

        int fs = 0;
        try {
            fs = Integer.parseInt(fontSize);
        } catch (Exception ignored) {
        }
        if (fs > 0 && fs <= 1000) {
            switch (fs) {
                case 8:
                    fontSize8.setSelected(true);
                    break;
                case 12:
                    fontSize12.setSelected(true);
                    break;
                case 16:
                    fontSize16.setSelected(true);
                    break;
                case 24:
                    fontSize24.setSelected(true);
                    break;
                case 48:
                    fontSize48.setSelected(true);
                    break;
                default:
                    fontSizeCustom.setSelected(true);
            }
        }
    }

    /**
     * Selects the font size radio button from the preset.
     */
    private void setFontNameRadioButton() {
        String fontText = PropertiesManager.getProperty("fontName");
        String fontStyle = PropertiesManager.getProperty("fontStyle");
        int fs = 0;

        if (fontText == null) {
            fontNameMonospaced.setSelected(true);
            return;
        }

        if (fontStyle != null) {
            try {
                fs = Integer.parseInt(fontStyle);
            } catch (Exception ignored) {
            }
        }

        if (fs == 1 || fs == 3) {
            fontStyleBold.setSelected(true);
        }
        if (fs == 2 || fs == 3) {
            fontStyleItalic.setSelected(true);
        }

        switch (fontText) {
            case "Dialog":
                fontNameDialog.setSelected(true);
                break;
            case "DialogInput":
                fontNameDialogInput.setSelected(true);
                break;
            case "Serif":
                fontNameSerif.setSelected(true);
                break;
            case "SansSerif":
                fontNameSansSerif.setSelected(true);
                break;
            case "Segoe":
                fontNameSegoe.setSelected(true);
                break;
            default:
            case "Monospaced":
                fontNameMonospaced.setSelected(true);
                break;
        }
    }

    private void setFileCheckbox() {
        String dataSending = PropertiesManager.getProperty("disableDataSending");
        if (dataSending != null) {
            boolean b = dataSending.equals("true");
            disableDataSending.setSelected(b);
            LootGUI.lootSharing(b);
        }
    }

    private void setChatCheckbox() {
        String save = PropertiesManager.getProperty("saveChat");
        if (save != null) {
            saveChat.setSelected(save.equals("true"));
            ChatGUI.save = save.equals("true");
        }
    }

    private void setSoundCheckbox() {
        String volume = PropertiesManager.getProperty("soundVolume");
        if (volume != null) {
            try {
                int v = Integer.parseInt(volume);
                soundSlider.setValue(v);
                Sound.setVolume(v);
            } catch (NumberFormatException ignore) {
            }
        }

        String pm = PropertiesManager.getProperty("chatPing");
        if (pm != null) {
            chatPing.setSelected(pm.equals("true"));
            Sound.playPmSound = pm.equals("true");
        } else {
            Sound.playPmSound = false;
        }

        String guild = PropertiesManager.getProperty("chatPingGuild");
        if (guild != null) {
            chatPingGuild.setSelected(guild.equals("true"));
            Sound.playGuildSound = guild.equals("true");
        } else {
            Sound.playGuildSound = false;
        }

        String party = PropertiesManager.getProperty("chatPingParty");
        if (party != null) {
            chatPingParty.setSelected(party.equals("true"));
            Sound.playPartySound = party.equals("true");
        } else {
            Sound.playPartySound = false;
        }

        String white = PropertiesManager.getProperty("whiteBagSound");
        if (white != null) {
            whiteBagSound.setSelected(white.equals("true"));
            Sound.playWhiteBagSound = white.equals("true");
        } else {
            whiteBagSound.setSelected(true);
            Sound.playWhiteBagSound = true;
            PropertiesManager.setProperties("whiteBagSound", "true");
        }

        String orange = PropertiesManager.getProperty("orangeBagSound");
        if (orange != null) {
            orangeBagSound.setSelected(orange.equals("true"));
            Sound.playOrangeBagSound = orange.equals("true");
        } else {
            Sound.playOrangeBagSound = false;
        }

        String red = PropertiesManager.getProperty("redBagSound");
        if (red != null) {
            redBagSound.setSelected(red.equals("true"));
            Sound.playRedBagSound = red.equals("true");
        } else {
            Sound.playRedBagSound = false;
        }

        String gold = PropertiesManager.getProperty("goldBagSound");
        if (gold != null) {
            goldBagSound.setSelected(gold.equals("true"));
            Sound.playGoldBagSound = gold.equals("true");
        } else {
            Sound.playGoldBagSound = false;
        }

        String egg = PropertiesManager.getProperty("eggBagSound");
        if (egg != null) {
            eggBagSound.setSelected(egg.equals("true"));
            Sound.playEggBagSound = egg.equals("true");
        } else {
            Sound.playEggBagSound = false;
        }

        String trade = PropertiesManager.getProperty("tradePing");
        if (trade != null) {
            tradePing.setSelected(trade.equals("true"));
            Sound.playTradeSound = trade.equals("true");
        } else {
            Sound.playTradeSound = false;
        }
    }

    private void setShowMeCheckbox() {
        String showMe = PropertiesManager.getProperty("showMe");
        if (showMe != null) {
            dpsShowMe.setSelected(showMe.equals("true"));
        }
    }

    private void setEquipmentRadioButton() {
        String equipment = PropertiesManager.getProperty("equipment");

        if (equipment == null) {
            dpsIcon.setSelected(true);
            return;
        }

        switch (equipment) {
            case "0":
                dpsEquipmentNone.setSelected(true);
                break;
            case "1":
                dpsEquipmentSimple.setSelected(true);
                break;
            case "2":
                dpsEquipmentFull.setSelected(true);
                break;
            default:
            case "3":
                dpsIcon.setSelected(true);
                break;
        }
    }

    private void setDpsSortRadioButton() {
        String equipment = PropertiesManager.getProperty("sortDps");

        if (equipment == null) {
            dpsSortLastHit.setSelected(true);
            return;
        }

        switch (equipment) {
            case "1":
                dpsSortFirstHit.setSelected(true);
                break;
            case "2":
                dpsSortMaxHp.setSelected(true);
                break;
            case "3":
                dpsSortFightTimer.setSelected(true);
                break;
            case "4":
                dpsSortBossOnly.setSelected(true);
                break;
            default:
            case "0":
                dpsSortLastHit.setSelected(true);
                break;
        }
    }

    private void loadFilteredBags() {
        String whiteBag = PropertiesManager.getProperty("filterWhiteBag");
        filterWhiteBag.setSelected(whiteBag == null || whiteBag.equals("true"));

        String orangeBag = PropertiesManager.getProperty("filterOrangeBag");
        filterOrangeBag.setSelected(orangeBag == null || orangeBag.equals("true"));

        String redBag = PropertiesManager.getProperty("filterRedBag");
        filterRedBag.setSelected(redBag == null || redBag.equals("true"));

        String goldBag = PropertiesManager.getProperty("filterGoldBag");
        filterGoldBag.setSelected(goldBag == null || goldBag.equals("true"));

        String eggBag = PropertiesManager.getProperty("filterEggBag");
        filterEggBag.setSelected(eggBag == null || eggBag.equals("true"));

        String blueBag = PropertiesManager.getProperty("filterBlueBag");
        filterBlueBag.setSelected(blueBag == null || blueBag.equals("true"));

        String tealBag = PropertiesManager.getProperty("filterTealBag");
        filterTealBag.setSelected(tealBag == null || tealBag.equals("true"));

        String purpleBag = PropertiesManager.getProperty("filterPurpleBag");
        filterPurpleBag.setSelected(purpleBag == null || purpleBag.equals("true"));
    }


    /**
     * Adds a radiobutton menu item for the user to select.
     *
     * @param groupTheme The group to add the radio button.
     * @param superMenu  The menu selection to add the radio button.
     * @param name       Name of the radio button.
     * @return Radio button menu item object returned.
     */
    private JRadioButtonMenuItem addRadioButtonMenuItem(ButtonGroup groupTheme, JMenuItem superMenu, String name) {
        JRadioButtonMenuItem jRadioMenuItem = new JRadioButtonMenuItem(name);
        groupTheme.add(jRadioMenuItem);
        superMenu.add(jRadioMenuItem);
        jRadioMenuItem.addActionListener(this);
        return jRadioMenuItem;
    }

    /**
     * Gets the font size from property.
     *
     * @return Value of font size.
     */
    private String getFontSize() {
        return PropertiesManager.getProperty("fontSize");
    }

    /**
     * Gets the font name from property.
     *
     * @return Value of font name.
     */
    private String getFontName() {
        return PropertiesManager.getProperty("fontName");
    }

    /**
     * Gets the font style from selection buttons.
     *
     * @return Value of font style.
     */
    private int getFontStyle() {
        int fs = 0;
        if (fontStyleBold.isSelected()) fs += 1;
        if (fontStyleItalic.isSelected()) fs += 2;

        return fs;
    }

    /**
     * Sets the frame object for access to the frame.
     *
     * @param f The frame object.
     */
    public void setFrame(JFrame f) {
        frame = f;
    }

    /**
     * Action listiner for using the menu options.
     *
     * @param e event listener.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == sniffer) { // Starts and stops the sniffer
            if (sniffer.getText().contains("Start")) {
                sniffer.setText("Stop Sniffer");
                Tomato.startPacketSniffer();
                TomatoGUI.setStateOfSniffer(true);
                PropertiesManager.setProperties("sniffer", "T");
            } else {
                stopPacketSniffer();
                PropertiesManager.setProperties("sniffer", "F");
            }
        } else if (e.getSource() == disableDataSending) { // disables data sharing
            boolean b = disableDataSending.isSelected();
            PropertiesManager.setProperties("disableDataSending", b ? "true" : "false");
            LootGUI.lootSharing(b);
        } else if (e.getSource() == chatPingMessage) { // chat ping message
            TomatoGUI.openChatPingMessage();
        } else if (e.getSource() == entityIdPingMessage) { // entity id ping message
            TomatoGUI.openEntityIdPing();
        } else if (e.getSource() == saveChat) { // chat save logs
            boolean b = saveChat.isSelected();
            PropertiesManager.setProperties("saveChat", b ? "true" : "false");
            ChatGUI.save = b;
        } else if (e.getSource() == chatPing) { // sound chat ping pm
            boolean b = chatPing.isSelected();
            PropertiesManager.setProperties("chatPing", b ? "true" : "false");
            Sound.playPmSound = b;
            if(b) Sound.pm.play();
        } else if (e.getSource() == chatPingGuild) { // sound chat ping guild
            boolean b = chatPingGuild.isSelected();
            PropertiesManager.setProperties("chatPingGuild", b ? "true" : "false");
            Sound.playGuildSound = b;
            if(b) Sound.guild.play();
        } else if (e.getSource() == whiteBagSound) { // white bag sound
            boolean b = whiteBagSound.isSelected();
            PropertiesManager.setProperties("whiteBagSound", b ? "true" : "false");
            Sound.playWhiteBagSound = b;
            if(b) Sound.whitebag.play();
        } else if (e.getSource() == chatPingParty) { // sound chat ping party
            boolean b = chatPingParty.isSelected();
            PropertiesManager.setProperties("chatPingParty", b ? "true" : "false");
            Sound.playPartySound = b;
            if(b) Sound.party.play();
        } else if (e.getSource() == orangeBagSound) { // orange bag sound
            boolean b = orangeBagSound.isSelected();
            PropertiesManager.setProperties("orangeBagSound", b ? "true" : "false");
            Sound.playOrangeBagSound = b;
            if(b) Sound.orangebag.play();
        } else if (e.getSource() == redBagSound) { // red bag sound
            boolean b = redBagSound.isSelected();
            PropertiesManager.setProperties("redBagSound", b ? "true" : "false");
            Sound.playRedBagSound = b;
            if(b) Sound.redbag.play();
        } else if (e.getSource() == goldBagSound) { // gold bag sound
            boolean b = goldBagSound.isSelected();
            PropertiesManager.setProperties("goldBagSound", b ? "true" : "false");
            Sound.playGoldBagSound = b;
            if(b) Sound.goldbag.play();
        } else if (e.getSource() == eggBagSound) { // egg bag sound
            boolean b = eggBagSound.isSelected();
            PropertiesManager.setProperties("eggBagSound", b ? "true" : "false");
            Sound.playEggBagSound = b;
            if(b) Sound.eggbag.play();
        } else if (e.getSource() == tradePing) { // trade sound
            boolean b = tradePing.isSelected();
            PropertiesManager.setProperties("tradePing", b ? "true" : "false");
            Sound.playTradeSound = b;
            if(b) Sound.trade.play();
        } else if (e.getSource() == clearChat) { // clears the text chat
            ChatGUI.clearTextAreaChat();
        } else if (e.getSource() == borders) { // Removes the boarder of the window
            frame.dispose();
            frame.setUndecorated(!frame.isUndecorated());
            frame.setVisible(true);
        } else if (e.getSource() == themeDarcula) { // theme
            LafManager.install(new DarculaTheme());
            PropertiesManager.setProperties("theme", "darcula");
        } else if (e.getSource() == themeighContrastDark) { // theme
            LafManager.install(new HighContrastDarkTheme());
            PropertiesManager.setProperties("theme", "contrastDark");
        } else if (e.getSource() == themeHighContrastLight) { // theme
            LafManager.install(new HighContrastLightTheme());
            PropertiesManager.setProperties("theme", "contrastLight");
        } else if (e.getSource() == themeIntelliJ) { // theme
            LafManager.install(new IntelliJTheme());
            PropertiesManager.setProperties("theme", "intelliJ");
        } else if (e.getSource() == themeSolarizedDark) { // theme
            LafManager.install(new SolarizedDarkTheme());
            PropertiesManager.setProperties("theme", "solarizedDark");
        } else if (e.getSource() == themeSolarizedLight) { // theme
            LafManager.install(new SolarizedLightTheme());
            PropertiesManager.setProperties("theme", "solarizedLight");
        } else if (e.getSource() == fontSize8) { // font size
            TomatoGUI.fontSizeTextAreas(8);
            PropertiesManager.setProperties("fontSize", Integer.toString(8));
        } else if (e.getSource() == fontSize12) { // font size
            TomatoGUI.fontSizeTextAreas(12);
            PropertiesManager.setProperties("fontSize", Integer.toString(12));
        } else if (e.getSource() == fontSize16) { // font size
            TomatoGUI.fontSizeTextAreas(16);
            PropertiesManager.setProperties("fontSize", Integer.toString(16));
        } else if (e.getSource() == fontSize24) { // font size
            TomatoGUI.fontSizeTextAreas(24);
            PropertiesManager.setProperties("fontSize", Integer.toString(24));
        } else if (e.getSource() == fontSize48) { // font size
            TomatoGUI.fontSizeTextAreas(48);
            PropertiesManager.setProperties("fontSize", Integer.toString(48));
        } else if (e.getSource() == fontSizeCustom) { // font size
            String sizeText = JOptionPane.showInputDialog("Enter custom font size (between 1 and 1000)", getFontSize());
            int size = 0;
            try {
                size = Integer.parseInt(sizeText);
                PropertiesManager.setProperties("fontSize", Integer.toString(size));
            } catch (Exception ignored) {
            }
            if (size > 0 && size <= 1000) {
                TomatoGUI.fontSizeTextAreas(size);
            }
        } else if (e.getSource() == fontNameMonospaced) { // font text
            TomatoGUI.fontNameTextAreas("Monospaced", getFontStyle());
            PropertiesManager.setProperties("fontName", "Monospaced");
        } else if (e.getSource() == fontNameSegoe) { // font text
            TomatoGUI.fontNameTextAreas("Segoe", getFontStyle());
            PropertiesManager.setProperties("fontName", "Segoe");
        } else if (e.getSource() == fontNameDialog) { // font text
            TomatoGUI.fontNameTextAreas("Dialog", getFontStyle());
            PropertiesManager.setProperties("fontName", "Dialog");
        } else if (e.getSource() == fontNameDialogInput) { // font text
            TomatoGUI.fontNameTextAreas("DialogInput", getFontStyle());
            PropertiesManager.setProperties("fontName", "DialogInput");
        } else if (e.getSource() == fontNameSerif) { // font text
            TomatoGUI.fontNameTextAreas("Serif", getFontStyle());
            PropertiesManager.setProperties("fontName", "Serif");
        } else if (e.getSource() == fontNameSansSerif) { // font text
            TomatoGUI.fontNameTextAreas("SansSerif", getFontStyle());
            PropertiesManager.setProperties("fontName", "SansSerif");
        } else if (e.getSource() == fontStyleBold) { // font style
            TomatoGUI.fontNameTextAreas(getFontName(), getFontStyle());
            PropertiesManager.setProperties("fontStyle", Integer.toString(getFontStyle()));
        } else if (e.getSource() == fontStyleItalic) { // font style
            TomatoGUI.fontNameTextAreas(getFontName(), getFontStyle());
            PropertiesManager.setProperties("fontStyle", Integer.toString(getFontStyle()));
        } else if (e.getSource() == dpsShowMe) { // dps show me
            boolean b = dpsShowMe.isSelected();
            PropertiesManager.setProperties("showMe", b ? "true" : "false");
            DpsDisplayOptions.showMe = b;
            DpsGUI.update();
        } else if (e.getSource() == dpsEquipmentNone) { // dps equipment
            PropertiesManager.setProperties("equipment", "0");
            DpsDisplayOptions.equipmentOption = 0;
            DpsGUI.update();
        } else if (e.getSource() == dpsEquipmentSimple) { // dps equipment
            PropertiesManager.setProperties("equipment", "1");
            DpsDisplayOptions.equipmentOption = 1;
            DpsGUI.update();
        } else if (e.getSource() == dpsEquipmentFull) { // dps equipment
            PropertiesManager.setProperties("equipment", "2");
            DpsDisplayOptions.equipmentOption = 2;
            DpsGUI.update();
        } else if (e.getSource() == dpsIcon) { // dps icon
            PropertiesManager.setProperties("equipment", "3");
            DpsDisplayOptions.equipmentOption = 3;
            DpsGUI.update();
        } else if (e.getSource() == dpsSortLastHit) { // dps sort getLastDamageTaken
            PropertiesManager.setProperties("sortDps", "0");
            DpsDisplayOptions.sortOption = 0;
            DpsGUI.update();
        } else if (e.getSource() == dpsSortFirstHit) { // dps sort getFirstDamageTaken
            PropertiesManager.setProperties("sortDps", "1");
            DpsDisplayOptions.sortOption = 1;
            DpsGUI.update();
        } else if (e.getSource() == dpsSortMaxHp) { // dps sort maxHp
            PropertiesManager.setProperties("sortDps", "2");
            DpsDisplayOptions.sortOption = 2;
            DpsGUI.update();
        } else if (e.getSource() == dpsSortFightTimer) { // dps sort getFightTimer
            PropertiesManager.setProperties("sortDps", "3");
            DpsDisplayOptions.sortOption = 3;
            DpsGUI.update();
        } else if (e.getSource() == dpsSortBossOnly) { // dps sort only bosses
            PropertiesManager.setProperties("sortDps", "4");
            DpsDisplayOptions.sortOption = 4;
            DpsGUI.update();
        } else if (e.getSource() == clearDpsLogs) { // clears the dps logs
            DpsGUI.clearDpsLogs();
        } else if (e.getSource() == about) { // Opens about window
            new TomatoPopupAbout().addPopup(frame);
        } else if (e.getSource() == bandwidth) { // Opens bandwidth window
            TomatoBandwidth.make(frame);
        } else if (e.getSource() == javav) { // Opens bandwidth window
            String version = System.getProperty("java.version");
            String bit = System.getProperty("sun.arch.data.model");
            JFrame frame = new JFrame("Java version");
            JOptionPane.showMessageDialog(frame, String.format("Java version: %s (%s-bit)", version, bit));
        }
    }

    /**
     * Stops sniffer and changes GUI settings. TODO: temporary till better stream constructor solution is found.
     */
    public static void stopPacketSniffer() {
        sniffer.setText("Start Sniffer");
        Tomato.stopPacketSniffer();
        TomatoGUI.setStateOfSniffer(false);
    }
}