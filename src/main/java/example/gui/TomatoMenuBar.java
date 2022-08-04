package example.gui;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.*;
import example.ExampleModTomato;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

/**
 * Menu bar builder class
 */
public class TomatoMenuBar implements ActionListener {
    private JMenuItem about, borders, clearChat, bandwidth, javav, clearDpsLogs, theme, fontSize;
    private JRadioButtonMenuItem fontSize8, fontSize10, fontSize12, fontSize14, fontSize16, fontSize24, fontSize32, fontSize48, fontSizeCustom;
    private JRadioButtonMenuItem themeDarcula, themeighContrastDark, themeHighContrastLight, themeIntelliJ, themeSolarizedDark, themeSolarizedLight;
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
        theme = new JMenu("Theme");
        theme.addActionListener(this);
        fontSize = new JMenu("Font size");
        fontSize.addActionListener(this);
        file = new JMenu("File");
        file.add(sniffer);
        file.add(theme);
        file.add(fontSize);
        jMenuBar.add(file);

        borders = new JMenuItem("Borders");
        borders.addActionListener(this);
        clearChat = new JMenuItem("Clear Chat");
        clearChat.addActionListener(this);
        clearDpsLogs = new JMenuItem("Clear DPS Logs");
        clearDpsLogs.addActionListener(this);
        edit = new JMenu("Edit");
        edit.add(borders);
        edit.add(clearChat);
        edit.add(clearDpsLogs);
        jMenuBar.add(edit);

        ButtonGroup groupTheme = new ButtonGroup();
        themeDarcula = addRadioButtonMenuItem(groupTheme, theme, "Darcula Theme");
        themeighContrastDark = addRadioButtonMenuItem(groupTheme, theme, "High Contrast Dark Theme");
        themeHighContrastLight = addRadioButtonMenuItem(groupTheme, theme, "High Contrast Light Theme");
        themeIntelliJ = addRadioButtonMenuItem(groupTheme, theme, "IntelliJ Theme");
        themeSolarizedDark = addRadioButtonMenuItem(groupTheme, theme, "Solarized Dark Theme");
        themeSolarizedLight = addRadioButtonMenuItem(groupTheme, theme, "Solarized Light Theme");
        setThemeRadioButton();

        ButtonGroup groupFont = new ButtonGroup();
        fontSize8 = addRadioButtonMenuItem(groupFont, fontSize, "Size 8");
        fontSize10 = addRadioButtonMenuItem(groupFont, fontSize, "Size 10");
        fontSize12 = addRadioButtonMenuItem(groupFont, fontSize, "Size 12");
        fontSize14 = addRadioButtonMenuItem(groupFont, fontSize, "Size 14");
        fontSize16 = addRadioButtonMenuItem(groupFont, fontSize, "Size 16");
        fontSize24 = addRadioButtonMenuItem(groupFont, fontSize, "Size 24");
        fontSize32 = addRadioButtonMenuItem(groupFont, fontSize, "Size 32");
        fontSize48 = addRadioButtonMenuItem(groupFont, fontSize, "Size 48");
        fontSizeCustom = addRadioButtonMenuItem(groupFont, fontSize, "Custom Size");
        setFontSizeRadioButton();

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

        return jMenuBar;
    }

    /**
     * Selects the theme radio button from the preset.
     */
    private void setThemeRadioButton() {
        String theme = TomatoGUI.getPropertie("theme");

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
        String fontSize = TomatoGUI.getPropertie("fontSize");

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
                case 10:
                    fontSize10.setSelected(true);
                    break;
                case 12:
                    fontSize12.setSelected(true);
                    break;
                case 14:
                    fontSize14.setSelected(true);
                    break;
                case 16:
                    fontSize16.setSelected(true);
                    break;
                case 24:
                    fontSize24.setSelected(true);
                    break;
                case 32:
                    fontSize32.setSelected(true);
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
        if (e.getSource() == about) { // Opens about window
            new TomatoPopupAbout().addPopup(frame);
        } else if (e.getSource() == borders) { // Removes the boarder of the window
            frame.dispose();
            frame.setUndecorated(!frame.isUndecorated());
            frame.setVisible(true);
        } else if (e.getSource() == clearChat) { // clears the text chat
            TomatoGUI.clearTextAreaChat();
        } else if (e.getSource() == clearDpsLogs) { // clears the dps logs
            ExampleModTomato.clearDpsLogs();
            TomatoGUI.setTextAreaAndLabelDPS("", "0/0");
        } else if (e.getSource() == themeDarcula) { // theme
            LafManager.install(new DarculaTheme());
            TomatoGUI.setProperties("theme", "darcula");
        } else if (e.getSource() == themeighContrastDark) { // theme
            LafManager.install(new HighContrastDarkTheme());
            TomatoGUI.setProperties("theme", "contrastDark");
        } else if (e.getSource() == themeHighContrastLight) { // theme
            LafManager.install(new HighContrastLightTheme());
            TomatoGUI.setProperties("theme", "contrastLight");
        } else if (e.getSource() == themeIntelliJ) { // theme
            LafManager.install(new IntelliJTheme());
            TomatoGUI.setProperties("theme", "intelliJ");
        } else if (e.getSource() == themeSolarizedDark) { // theme
            LafManager.install(new SolarizedDarkTheme());
            TomatoGUI.setProperties("theme", "solarizedDark");
        } else if (e.getSource() == themeSolarizedLight) { // theme
            LafManager.install(new SolarizedLightTheme());
            TomatoGUI.setProperties("theme", "solarizedLight");
        } else if (e.getSource() == fontSize8) { // font size
            TomatoGUI.fontSizeTextAreas(8);
            TomatoGUI.setProperties("fontSize", Integer.toString(8));
        } else if (e.getSource() == fontSize10) { // font size
            TomatoGUI.fontSizeTextAreas(10);
            TomatoGUI.setProperties("fontSize", Integer.toString(10));
        } else if (e.getSource() == fontSize12) { // font size
            TomatoGUI.fontSizeTextAreas(12);
            TomatoGUI.setProperties("fontSize", Integer.toString(12));
        } else if (e.getSource() == fontSize14) { // font size
            TomatoGUI.fontSizeTextAreas(14);
            TomatoGUI.setProperties("fontSize", Integer.toString(14));
        } else if (e.getSource() == fontSize16) { // font size
            TomatoGUI.fontSizeTextAreas(16);
            TomatoGUI.setProperties("fontSize", Integer.toString(16));
        } else if (e.getSource() == fontSize24) { // font size
            TomatoGUI.fontSizeTextAreas(24);
            TomatoGUI.setProperties("fontSize", Integer.toString(24));
        } else if (e.getSource() == fontSize32) { // font size
            TomatoGUI.fontSizeTextAreas(32);
            TomatoGUI.setProperties("fontSize", Integer.toString(32));
        } else if (e.getSource() == fontSize48) { // font size
            TomatoGUI.fontSizeTextAreas(48);
            TomatoGUI.setProperties("fontSize", Integer.toString(48));
        } else if (e.getSource() == fontSizeCustom) { // font size
            String sizeText = JOptionPane.showInputDialog("Enter custom font size (between 1 and 1000)");
            int size = 0;
            try {
                size = Integer.parseInt(sizeText);
                TomatoGUI.setProperties("fontSize", Integer.toString(size));
            } catch (Exception ignored) {
            }
            if (size > 0 && size <= 1000) {
                TomatoGUI.fontSizeTextAreas(size);
            }
        } else if (e.getSource() == sniffer) { // Starts and stops the sniffer
            if (sniffer.getText().contains("Start")) {
                sniffer.setText("Stop Sniffer");
                ExampleModTomato.startPacketSniffer();
                TomatoGUI.setStateOfSniffer(true);
            } else {
                stopPacketSniffer();
            }
        } else if (e.getSource() == bandwidth) { // Opens bandwidth window
            TomatoBandwidth.make(frame);
        } else if (e.getSource() == javav) { // Opens bandwidth window
            String version = System.getProperty("java.version");
            System.out.println("Java version: " + version);
            JFrame frame = new JFrame("Java version");
            JOptionPane.showMessageDialog(frame, "Java version: " + version);
        }
    }

    /**
     * Stops sniffer and changes GUI settings. TODO: temporary till better stream constructor solution is found.
     */
    public static void stopPacketSniffer() {
        sniffer.setText("Start Sniffer");
        ExampleModTomato.stopPacketSniffer();
        TomatoGUI.setStateOfSniffer(false);
    }
}