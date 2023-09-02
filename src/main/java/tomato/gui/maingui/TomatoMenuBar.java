package tomato.gui.maingui;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.*;
import tomato.Tomato;
import tomato.gui.TomatoGUI;
import tomato.gui.dps.DpsDisplayOptions;
import tomato.gui.dps.DpsGUI;
import util.PropertiesManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Menu bar builder class
 */
public class TomatoMenuBar implements ActionListener {
    private JMenuItem about, borders, clearChat, bandwidth, javav, clearDpsLogs, theme, fontMenu, dpsOptions;
    private JRadioButtonMenuItem fontSize8, fontSize12, fontSize16, fontSize24, fontSize48, fontSizeCustom;
    private JRadioButtonMenuItem themeDarcula, themeighContrastDark, themeHighContrastLight, themeIntelliJ, themeSolarizedDark, themeSolarizedLight;
    private JRadioButtonMenuItem fontNameMonospaced, fontNameDialog, fontNameDialogInput, fontNameSerif, fontNameSansSerif, fontNameSegoe;
    private JRadioButtonMenuItem dpsEquipmentNone, dpsEquipmentSimple, dpsEquipmentFull, dpsIcon;
    private JRadioButtonMenuItem dpsSortLastHit, dpsSortFirstHit, dpsSortMaxHp, dpsSortFightTimer;
    private JCheckBoxMenuItem fontStyleBold, fontStyleItalic;
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

        sniffer = new JMenuItem("Start Sniffer");
        sniffer.addActionListener(this);
        file = new JMenu("File");
        file.add(sniffer);
        jMenuBar.add(file);

        borders = new JMenuItem("Borders");
        borders.addActionListener(this);
        clearChat = new JMenuItem("Clear Chat");
        clearChat.addActionListener(this);
        clearDpsLogs = new JMenuItem("Clear DPS Logs");
        clearDpsLogs.addActionListener(this);
        theme = new JMenu("Theme");
        theme.addActionListener(this);
        fontMenu = new JMenu("Font");
        fontMenu.addActionListener(this);
        dpsOptions = new JMenu("DPS Options");
        dpsOptions.addActionListener(this);

        edit = new JMenu("Edit");
        edit.add(borders);
        edit.add(clearChat);
        edit.add(clearDpsLogs);
        edit.add(theme);
        edit.add(fontMenu);
        edit.add(dpsOptions);
        jMenuBar.add(edit);

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
        setDpsSortRadioButton();

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

    private void setEquipmentRadioButton() {
        String equipment = PropertiesManager.getProperty("equipment");

        if (equipment == null) {
            dpsEquipmentSimple.setSelected(true);
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
            default:
            case "0":
                dpsSortLastHit.setSelected(true);
                break;
        }
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
        } else if (e.getSource() == borders) { // Removes the boarder of the window
            frame.dispose();
            frame.setUndecorated(!frame.isUndecorated());
            frame.setVisible(true);
        } else if (e.getSource() == clearChat) { // clears the text chat
            TomatoGUI.clearTextAreaChat();
        } else if (e.getSource() == clearDpsLogs) { // clears the dps logs
            DpsGUI.clearDpsLogs();
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