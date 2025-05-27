package tomato.gui.keypop;

import assets.IdToAsset;
import packets.data.enums.NotificationEffectType;
import packets.incoming.NotificationPacket;
import tomato.backend.data.TomatoData;
import tomato.gui.TomatoGUI;
import tomato.realmshark.Sound;
import tomato.realmshark.RealmCharacterStats;
import tomato.realmshark.enums.CharacterStatistics;
import util.PropertiesManager;
import util.Util;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GUI class for popping dungeons.
 */
public class KeypopGUI extends JPanel {

    private static JTextArea textAreaKeypop;
    private static boolean logToFile = false;
    private static final String LOG_FILE = "keypops.log";
    private static final SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final Pattern keypopParse = Pattern.compile("\"player\":\"([^\"]+)\"");
    private static final Pattern nonkeypopParse = Pattern.compile("[^ ]*\"name\":\"([A-Za-z ]*)\",\"player\":\"([A-Za-z]*)[^ ]*");
    private static final Pattern calloutParsePlayer = Pattern.compile("([^;]+);");
    private static final Pattern calloutParseDungeon = Pattern.compile("\"name\":\"([^\"]+)\"");

    private static HashSet<String> selectedDungeons = new HashSet<>();

    public KeypopGUI() {
        loadDungeonChoices();
        loadLoggingPreference();

        setLayout(new BorderLayout());
        textAreaKeypop = new JTextArea();
        add(TomatoGUI.createTextArea(textAreaKeypop, false));

        JPanel south = new JPanel(new GridLayout(1, 3));
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> textAreaKeypop.setText(""));
        south.add(clearButton);

        JButton notificationButton = new JButton("Notifications");
        notificationButton.addActionListener(e -> showConfigureDialog());
        south.add(notificationButton);

        JCheckBox logCheckbox = new JCheckBox("Log to file");
        logCheckbox.setSelected(logToFile);
        logCheckbox.addActionListener(e -> {
            logToFile = logCheckbox.isSelected();
            saveLoggingPreference();
        });
        south.add(logCheckbox);

        add(south, BorderLayout.SOUTH);
    }

    /**
     * Packet parser for notification packets that will be used to add key, vial, rune or inc pops.
     *
     * @param data
     * @param packet Notification packet containing info about who pops keys, vial, runes or inc pops.
     */
    public static void packet(TomatoData data, NotificationPacket packet) {
        if (packet.effect == NotificationEffectType.PortalOpened) {
            String msg = packet.message;
            Matcher m = keypopParse.matcher(msg);

            if (m.find()) {
                String playerName = m.group(1).split(",")[0];
                String dungeonName = IdToAsset.objectName(packet.pictureType);

                appendTextAreaKeypop(String.format("%s [%s]: %s\n", Util.getHourTime(), playerName, dungeonName));

                playDungeonSound(data, dungeonName);
            }
        } else if (packet.effect == NotificationEffectType.PlayerCallout) {
            String msg = packet.message;
            Matcher playerMatcher = calloutParsePlayer.matcher(msg);
            String playerName = "";
            if (playerMatcher.find()) {
                playerName = playerMatcher.group(1);
            }
            Matcher dungeonMatcher = calloutParseDungeon.matcher(msg);
            String dungeonName = "";
            if (dungeonMatcher.find()) {
                dungeonName = dungeonMatcher.group(1);
            }

            if (!playerName.isEmpty() && !dungeonName.isEmpty()) {
                playDungeonSound(data, dungeonName);
            }
        } else if (packet.effect == NotificationEffectType.ServerMessage && packet.message != null) {
            String msg = packet.message;
            Matcher m = nonkeypopParse.matcher(msg);
            if (m.matches()) {
                String type = m.group(1);
                String playerName = m.group(2);
                String pop = null;
                if (type.contains("Monument has been activated")) {
                    pop = type.split(" ")[1] + " Rune";
                } else if (type.equals("The Void")) {
                    pop = "Vial";
                } else if (type.equals("Wine Cellar")) {
                    pop = "Inc";
                }
                if (pop != null) {
                    appendTextAreaKeypop(String.format("%s [%s]: %s\n", Util.getHourTime(), playerName, pop));
                }
            }
        }
    }

    public static void playDungeonSound(TomatoData data, String dungeonName) {
        if (selectedDungeons.contains(dungeonName)) {
            Sound.keypop.play();
        } else if (isMissingDungeonsSelected() && isMissingDungeon(data.getCurrentDungeonStats(), dungeonName)) {
            Sound.keypop.play();
        }
    }

    /**
     * Dungeon the currently playing character missing a complete on.
     *
     * @param data        Character data containing dungeon completes.
     * @param dungeonName Name of the dungeon the player is missing
     * @return True if missing the dungeon.
     */
    private static boolean isMissingDungeon(RealmCharacterStats data, String dungeonName) {
        if (data == null) return false;
        int completes = data.getDungeonInfoByName(dungeonName);

        return completes == 0;
    }

    /**
     * Add text to the key pop text area and optionally log to file.
     *
     * @param s The text to be added at the end of text area.
     */
    public static void appendTextAreaKeypop(String s) {
        if (textAreaKeypop != null) {
            textAreaKeypop.append(s);
        }

        if (logToFile) {
            logToFile(s);
        }
    }

    /**
     * Logs the key pop message to a file with timestamp.
     *
     * @param message The message to log
     */
    private static void logToFile(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            String timestamp = logDateFormat.format(new Date());
            writer.write(timestamp + " " + message);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error writing to keypop log file: " + e.getMessage());
        }
    }

    /**
     * Sets the font of the text area.
     *
     * @param font Font to be set.
     */
    public static void editFont(Font font) {
        textAreaKeypop.setFont(font);
    }

    /**
     * Dialog window used to display notification dungeons.
     */
    private static void showConfigureDialog() {
        JDialog configureDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(new JPanel()), "Configure Dungeons", true);
        configureDialog.setLayout(new BorderLayout());
        configureDialog.setResizable(false);

        JCheckBox[] checkboxes = createDungeonCheckboxes();

        JButton selectAllButton = new JButton("Select All");
        selectAllButton.addActionListener(e -> {
            for (JCheckBox checkbox : checkboxes) {
                checkbox.setSelected(true);
            }
        });

        JButton unselectAllButton = new JButton("Unselect All");
        unselectAllButton.addActionListener(e -> {
            for (JCheckBox checkbox : checkboxes) {
                checkbox.setSelected(false);
            }
        });

        JButton applyButton = new JButton("Apply");
        applyButton.addActionListener(e -> {
            boolean missingDungeons = isMissingDungeonsSelected();
            selectedDungeons.clear();
            if (missingDungeons) {
                selectedDungeons.add("missingDungeons");
            }
            for (JCheckBox checkbox : checkboxes) {
                if (checkbox.isSelected()) {
                    selectedDungeons.add(checkbox.getText());
                }
            }
            saveDungeonChoices();
            Sound.keypop.play();
            configureDialog.dispose();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(selectAllButton);
        buttonPanel.add(unselectAllButton);
        buttonPanel.add(applyButton);

        JPanel checkboxPanel = new JPanel(new GridLayout(0, 3));
        for (JCheckBox checkbox : checkboxes) {
            checkboxPanel.add(checkbox);
        }

        addMissingDungeonCheckbox(configureDialog);

        configureDialog.add(checkboxPanel, BorderLayout.CENTER);
        configureDialog.add(buttonPanel, BorderLayout.SOUTH);
        configureDialog.setSize(700, 500);
        configureDialog.setLocationRelativeTo(null);
        configureDialog.setVisible(true);
    }

    /**
     * Notification selection list creation.
     *
     * @return Checkbox objects from dungeon list.
     */
    private static JCheckBox[] createDungeonCheckboxes() {
        JCheckBox[] checkboxes = new JCheckBox[CharacterStatistics.DUNGEON_NAMES.size()];
        for (int i = 0; i < CharacterStatistics.DUNGEON_NAMES.size(); i++) {
            String o = CharacterStatistics.DUNGEON_NAMES.get(i);
            checkboxes[i] = new JCheckBox(o);
            checkboxes[i].setSelected(selectedDungeons.contains(o));
        }
        return checkboxes;
    }

    /**
     * Adds a missing dungeon checkbox at the top of the dungeon notification window.
     */
    private static void addMissingDungeonCheckbox(JDialog configureDialog) {
        JCheckBox missingDungeons = new JCheckBox("Missing Dungeons");
        missingDungeons.setToolTipText("Notifies dungeon pops for current character missing dungeon completes");
        missingDungeons.setSelected(isMissingDungeonsSelected());
        configureDialog.add(missingDungeons, BorderLayout.NORTH);
        missingDungeons.addActionListener(e -> {
            if (missingDungeons.isSelected()) {
                selectedDungeons.add("missingDungeons");
            } else {
                selectedDungeons.remove("missingDungeons");
            }
        });
    }

    private static boolean isMissingDungeonsSelected() {
        return selectedDungeons.contains("missingDungeons");
    }

    /**
     * Saves selection to disk
     */
    private static void saveDungeonChoices() {
        StringBuilder sb = new StringBuilder();
        boolean first = false;
        for (String s : selectedDungeons) {
            if (first) {
                sb.append(",");
            }
            first = true;
            sb.append(s);
        }
        String string = sb.toString();
        PropertiesManager.setProperties("keypopSound", string);
    }

    /**
     * Loads selection from disk
     */
    private static void loadDungeonChoices() {
        String keySound = PropertiesManager.getProperty("keypopSound");

        if (keySound != null) {
            String[] list = keySound.split(",");
            selectedDungeons.addAll(Arrays.asList(list));
        }
    }

    /**
     * Loads logging preference from disk
     */
    private static void loadLoggingPreference() {
        String loggingPref = PropertiesManager.getProperty("keypopLogging");
        logToFile = loggingPref != null && Boolean.parseBoolean(loggingPref);
    }

    /**
     * Saves logging preference to disk
     */
    private static void saveLoggingPreference() {
        PropertiesManager.setProperties("keypopLogging", String.valueOf(logToFile));
    }
}