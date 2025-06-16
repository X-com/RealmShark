package tomato.gui.security;

import assets.IdToAsset;
import assets.ImageBuffer;
import packets.data.enums.StatType;
import tomato.backend.data.Entity;
import tomato.gui.SmartScroller;
import tomato.realmshark.ParseEnchants;
import util.PropertiesManager;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ParsePanelGUI extends JPanel {

    private static ParsePanelGUI INSTANCE;

    private final static Color seasonalColor = new Color(21, 220, 166);
    private final static Color redColor = new Color(140, 64, 64);

    private static JPanel charPanel;
    private static HashMap<Integer, PlayerBox> playerDisplay;
    private static Font mainFont;

    private static final String DISABLE_FILTER = "Default";
    private final JComboBox<String> filterComboBox;
    private final JCheckBox copyOnlyUnderReqCheckbox;
    private final JCheckBox sortCheckBox; // Declare the checkbox at the class level

    private final static TreeMap<String, SecurityFilter> filters = new TreeMap<>();
    public static SecurityFilter currentFilter = null;
    private static boolean guiUpdateSuppression = false;

    public ParsePanelGUI() {
        INSTANCE = this;
        setLayout(new BorderLayout());

        playerDisplay = new HashMap<>();
        charPanel = new JPanel();

        charPanel.setLayout(new BoxLayout(charPanel, BoxLayout.Y_AXIS));
        validate();

        JScrollPane scroll = new JScrollPane(charPanel);
        scroll.getVerticalScrollBar().setUnitIncrement(40);
        new SmartScroller(scroll, 0);
        add(scroll, BorderLayout.CENTER);

        JPanel top = new JPanel();
        JButton filterButton = new JButton("Filter");
        filterButton.addActionListener(this::filter);

        filterComboBox = new JComboBox<>(new String[]{DISABLE_FILTER});
        filterComboBox.setPreferredSize(new Dimension(10000, 0));
        filterComboBox.addActionListener(this::comboAction);

        String stateCopyOnlyUnderReqCheckbox = PropertiesManager.getProperty("copyOnlyUnderReqCheckbox");
        copyOnlyUnderReqCheckbox = new JCheckBox("Only copy under req'd");
        copyOnlyUnderReqCheckbox.setSelected(stateCopyOnlyUnderReqCheckbox != null && stateCopyOnlyUnderReqCheckbox.equals("true")); // deselected by default with no prior setting

        // Add an action listener to the checkbox to update the saved state when toggled
        copyOnlyUnderReqCheckbox.addActionListener(e -> {
            // Update the properties manager to reflect the new status of the option
            PropertiesManager.setProperties("copyOnlyUnderReqCheckbox", copyOnlyUnderReqCheckbox.isSelected() ? "true" : "false");
        });

        loadFilters();

        top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
        top.add(Box.createHorizontalGlue());
        top.add(filterButton);
        top.add(Box.createRigidArea(new Dimension(10, 0)));
        top.add(filterComboBox);
        top.add(Box.createRigidArea(new Dimension(10, 0)));
        top.add(copyOnlyUnderReqCheckbox);

        add(top, BorderLayout.NORTH);

        // Update buttons panel layout to FlowLayout to allow more components horizontally
        JPanel buttons = new JPanel();
        buttons.setLayout(new FlowLayout(FlowLayout.LEFT));  // Use FlowLayout with left alignment

        JButton buttonLeft = new JButton("Copy names to Clipboard");
        JButton buttonRight = new JButton("Copy all to Clipboard");
        buttons.add(buttonLeft);
        buttons.add(buttonRight);
        buttonLeft.setToolTipText("<html>Click: Copy names to clipboard<br>Shift+Click: Export names as text file</html>");
        buttonRight.setToolTipText("<html>Click: Copy all to clipboard<br>Shift+Click: Export as JSON file</html>");

        buttonLeft.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if ((e.getModifiers() & InputEvent.SHIFT_MASK) != 0) {
                    // Shift+click - save names to file
                    saveNamesAsText();
                } else {
                    // Normal click - copy to clipboard
                    clicked(false);
                }
            }
        });

        buttonRight.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if ((e.getModifiers() & InputEvent.SHIFT_MASK) != 0) {
                    // Shift+click - save as JSON
                    saveAsJson(getFilteredPlayers());
                } else {
                    // Normal click - copy to clipboard
                    clicked(true);
                }
            }
        });

        // Add Sort checkbox to the buttons panel
        String stateSortCheckBox = PropertiesManager.getProperty("sortCheckBox");
        sortCheckBox = new JCheckBox("Sort By Guild");
        sortCheckBox.setSelected(stateSortCheckBox == null || stateSortCheckBox.equals("true"));  // selected by default with no prior setting

        // Add an action listener to the checkbox to update the player list when toggled
        sortCheckBox.addActionListener(e -> {
            if (sortCheckBox.isSelected()) {
                update();  // Sort and update the list whenever a new player is added
            }

            // Update the properties manager to reflect the new status of the option
            PropertiesManager.setProperties("sortCheckBox", sortCheckBox.isSelected() ? "true" : "false");
        });

        // Add the Sort checkbox to the buttons panel
        buttons.add(sortCheckBox);

        // Add the updated buttons panel to the south of the main panel
        add(buttons, BorderLayout.SOUTH);
    }

    private void loadFilters() {
        String f = PropertiesManager.getProperty("securityFilters");
        if (f != null) {
            String[] split = f.split("§");
            for (String s : split) {
                SecurityFilter sf = SecurityFilter.loadJson(s);
                if (sf == null) continue;
                filters.put(sf.name, sf);
                filterComboBox.addItem(sf.name);
            }
        }

        String selectedItem = PropertiesManager.getProperty("securityFilterName");
        if (setupFilter(selectedItem)) {
            filterComboBox.setSelectedItem(selectedItem);
        } else {
            // disable the only copy under reqed checkbox
            copyOnlyUnderReqCheckbox.setEnabled(false);
        }
    }

    private boolean setupFilter(String selectedItem) {
        if (selectedItem == null) return false;
        for (SecurityFilter sf : filters.values()) {
            if (sf.name.equals(selectedItem)) {
                currentFilter = sf;
                return true;
            }
        }
        return false;
    }

    private void comboAction(ActionEvent actionEvent) {
        if (guiUpdateSuppression) return;
        String selectedItem = String.valueOf(filterComboBox.getSelectedItem());
        if (setupFilter(selectedItem)) {
            PropertiesManager.setProperties("securityFilterName", selectedItem);
        } else {
            currentFilter = null;
            PropertiesManager.setProperties("securityFilterName", "");
        }

        // disable the copy under reqs checkbox according to whether this is default filter
        copyOnlyUnderReqCheckbox.setEnabled(currentFilter != null);

        update();
    }

    private void filter(ActionEvent actionEvent) {
        SecurityFilterGUI.open(this);
    }

    private void clicked(boolean full) {
        List<Player> players = getFilteredPlayers();
        StringBuilder sb = new StringBuilder();

        if (full) {
            sb.append("[\n");
            for (int i = 0; i < players.size(); i++) {
                sb.append(players.get(i).toString());
                if (i < players.size() - 1) {
                    sb.append(",\n");
                }
            }
            sb.append("\n]");
        } else {
            for (int i = 0; i < players.size(); i++) {
                sb.append(players.get(i).playerEntity.name());
                if (i < players.size() - 1) {
                    sb.append(" ");
                }
            }
        }

        copyToClipboard(sb.toString());
    }

    private static void copyToClipboard(String s) {
        StringSelection stringSelection = new StringSelection(s);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    private List<Player> getFilteredPlayers() {
        List<Player> players = new ArrayList<>();
        boolean onlyUnderReqs = copyOnlyUnderReqCheckbox.isSelected();

        for (PlayerBox playerBox : playerDisplay.values()) {
            Player player = playerBox.player;

            if (currentFilter != null && onlyUnderReqs && !currentFilter.parsePlayer(player).isUnderReqs) {
                continue;
            }

            players.add(player);
        }

        return players;
    }

    // Method to save names as text
    private void saveNamesAsText() {
        List<Player> players = getFilteredPlayers();
        StringBuilder sb = new StringBuilder();

        for (Player player : players) {
            sb.append(player.playerEntity.name()).append("\n");
        }

        saveToFile(sb.toString(), "ExportNames", ".txt");
    }

    // Method to save as JSON
    private void saveAsJson(List<Player> players) {
        StringBuilder sb = new StringBuilder("[\n");

        for (int i = 0; i < players.size(); i++) {
            sb.append(players.get(i).toString());
            if (i < players.size() - 1) {
                sb.append(",\n");
            }
        }
        sb.append("\n]");

        saveToFile(sb.toString(), "Export", ".json");
    }

    // Common file saving method
    private void saveToFile(String content, String prefix, String extension) {
        try {
            // Create exports directory if needed
            File directory = new File("exports");
            if (!directory.exists()) {
                directory.mkdir();
            }

            // Generate filename with timestamp
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMddyyyy_HHmmss");
            String dateTimeString = dateFormat.format(new Date());
            String filename = "exports/" + prefix + dateTimeString + extension;
            File file = new File(filename);

            // Write content to file
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(content);
                writer.flush();
            }

            // Copy the FILE OBJECT to clipboard (not just path/contents)
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(
                    new Transferable() {
                        public DataFlavor[] getTransferDataFlavors() {
                            return new DataFlavor[]{DataFlavor.javaFileListFlavor};
                        }

                        public boolean isDataFlavorSupported(DataFlavor flavor) {
                            return flavor.equals(DataFlavor.javaFileListFlavor);
                        }

                        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                            if (!isDataFlavorSupported(flavor)) {
                                throw new UnsupportedFlavorException(flavor);
                            }
                            return Collections.singletonList(file);
                        }
                    },
                    null
            );

            // Show success message
            JOptionPane.showMessageDialog(this,
                    "Successfully exported data to:\n" + file.getAbsolutePath() +
                            "\n\n(File object copied to clipboard - ready to paste)",
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to export data:\n" + e.getMessage(),
                    "Export Failed",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void guiUpdate() {
        revalidate();
        repaint();
    }

    private static JPanel createMainBox(PlayerBox p) {
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray), BorderFactory.createEmptyBorder(0, 20, 0, 20)));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
        BufferedImage ig = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = ig.createGraphics();
        FontMetrics fm = g2d.getFontMetrics(mainFont);
        int y = Math.max(24, fm.getHeight());
        int width = 70;

        mainPanel.add(Box.createHorizontalGlue());

        {
            width = pointItems(p, mainPanel, fm, y, width);
        }
        mainPanel.add(Box.createHorizontalStrut(5));

        {
            width = itemIcons(p, mainPanel, width);
        }
        mainPanel.add(Box.createHorizontalStrut(5));

        {
            width = statsMaxed(p, mainPanel, fm, y, width);
        }
        mainPanel.add(Box.createHorizontalStrut(5));

        {
            width = seasonCrucibleIcon(p, mainPanel, width);
        }
        mainPanel.add(Box.createHorizontalStrut(5));

        {
            width = nameLabel(p, mainPanel, fm, y, width);
        }
        mainPanel.add(Box.createHorizontalStrut(5));

        {
            width = guildLabel(p, mainPanel, fm, y, width);
        }

        mainPanel.add(Box.createHorizontalGlue());

        mainPanel.setMaximumSize(new Dimension(width, y));

        g2d.dispose();

        return mainPanel;
    }

    private static int pointItems(PlayerBox p, JPanel mainPanel, FontMetrics fm, int y, int width) {
        if (currentFilter == null) return width;
        int x = fm.stringWidth("-- / --") + 2;
        width += x;

        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(x, y));
        panel.setMaximumSize(new Dimension(x, y));
        panel.setLayout(new BorderLayout());

        p.pointsPanel = panel;
        panel = p.updatePointsPanel();

        mainPanel.add(panel);
        return width;
    }

    private static int statsMaxed(PlayerBox p, JPanel mainPanel, FontMetrics fm, int y, int width) {
        Player player = p.player;

        JPanel panel = new JPanel();

        int x = fm.stringWidth("8 / 8") + 2;
        width += x;

        panel.setPreferredSize(new Dimension(x, y));
        panel.setMaximumSize(new Dimension(x, y));
        panel.setLayout(new BorderLayout());

        int stat = player.statsMaxed();
        JLabel stats = new JLabel(stat + " / 8");
        String toolTipStatString = p.getToolTipStatString();
        stats.setToolTipText(toolTipStatString);
        stats.setHorizontalAlignment(SwingConstants.RIGHT);
        stats.setFont(mainFont);

        panel.add(stats);
        p.statsPanel = panel;
        mainPanel.add(panel);
        return width;
    }

    private static int itemIcons(PlayerBox p, JPanel mainPanel, int width) {
        JPanel panel = new JPanel();
        width += 100;

        p.itemsPanel = panel;

        panel.setPreferredSize(new Dimension(100, 24));
        panel.setMaximumSize(new Dimension(100, 24));
        panel.setLayout(new GridLayout(1, 4));

        // Get enchant info for all items
        String[] enchants = ParseEnchants.extractEnchants(p.player.playerEntity);

        for (int i = 0; i < 4; i++) {
            int eq = p.player.inv[i];
            int enchantCount = getEnchantCount(enchants[i]);

            // Use regular outline for non-enchanted items, glow for enchanted ones
            ImageIcon icon;
            if (enchantCount == 0) {
                icon = ImageBuffer.getOutlinedIcon(eq, 20);
            } else {
                Color glowColor = getGlowColor(enchantCount);
                int glowSize = getGlowSize(enchantCount);
                icon = ImageBuffer.getOutlinedIconWithGlow(eq, 20, glowColor, glowSize);
            }

            p.icon[i] = new JLabel(icon);
            p.player.itemName[i] = IdToAsset.objectName(eq);
            panel.add(p.icon[i]);
        }
        p.updateToolTipText();
        mainPanel.add(panel);
        return width;
    }

    // Count number of enchants in the enchant string
    private static int getEnchantCount(String enchantString) {
        if (enchantString == null || enchantString.isEmpty()) {
            return 0;
        }
        // Count the number of newlines in the parsed enchant string
        return enchantString.split("\n").length;
    }

    // Determine glow color based on enchant count
    private static Color getGlowColor(int enchantCount) {
        switch (enchantCount) {
            case 1: return new Color(0, 255, 0);
            case 2: return new Color(0, 200, 255);
            case 3: return new Color(200, 0, 255);
            case 4: return new Color(255, 215, 0);
            default: return Color.BLACK;
        }
    }

    private static int getGlowSize(int enchantCount) {
        return 3;
    }

    private static int guildLabel(PlayerBox playerBox, JPanel mainPanel, FontMetrics fm, int y, int width) {
        Entity playerEntity = playerBox.player.playerEntity;
        JPanel panel = new JPanel();

        int x = fm.stringWidth("12345678901234567890123") + 2;
        width += x;

        panel.setPreferredSize(new Dimension(x, y));
        panel.setMaximumSize(new Dimension(x, y));
        panel.setLayout(new BorderLayout());

        try {
            String text = playerEntity.getStatGuild();
            JLabel characterLabel = new JLabel(text, JLabel.CENTER);

            characterLabel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.isControlDown()) {
                        openWebpage("https://www.realmeye.com/guild/" + playerEntity.getStatGuild().replace(" ", "%20"));
                    }
                }
            });

            characterLabel.setAlignmentX(JLabel.LEFT);
            panel.setAlignmentX(JLabel.LEFT);
            panel.setAlignmentX(LEFT_ALIGNMENT);
            characterLabel.setHorizontalAlignment(SwingConstants.LEFT);
            characterLabel.setFont(mainFont);
            panel.add(characterLabel);
        } catch (Exception e) {
            System.err.println("Failed to add character label to player box for IGN " + playerEntity.name());
        }
        mainPanel.add(panel);
        return width;
    }

    private static int nameLabel(PlayerBox p, JPanel mainPanel, FontMetrics fm, int y, int width) {
        Entity playerEntity = p.player.playerEntity;

        JPanel panel = new JPanel();
        p.namePanel = panel;

        int x = fm.stringWidth("12345678901234567890123") + 2;
        width += x;

        panel.setPreferredSize(new Dimension(x, y));
        panel.setMaximumSize(new Dimension(x, y));
        panel.setLayout(new BorderLayout());

        int level = playerEntity.stat.get(StatType.LEVEL_STAT).statValue;
        String text = playerEntity.name() + " [" + level + "]";
        JLabel characterLabel = new JLabel(text, ImageBuffer.getOutlinedIcon(p.player.getSkinId(), 20), JLabel.CENTER);
        characterLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.isControlDown()) {
                    openWebpage("https://www.realmeye.com/player/" + playerEntity.name());
                } else {
                    copyToClipboard(playerEntity.name());
                }
            }
        });

        characterLabel.setAlignmentX(JLabel.LEFT);
        panel.setAlignmentX(JLabel.LEFT);
        panel.setAlignmentX(LEFT_ALIGNMENT);
        characterLabel.setHorizontalAlignment(SwingConstants.LEFT);
        characterLabel.setFont(mainFont);
        panel.add(characterLabel);
//            characterLabel.setToolTipText(exaltStats(c));
        mainPanel.add(panel);
        return width;
    }

    private static int seasonCrucibleIcon(PlayerBox p, JPanel mainPanel, int width) {
        Entity playerEntity = p.player.playerEntity;

        JPanel panel = new JPanel();
        JPanel p1 = new JPanel();
        p.cruciblePanel = new JPanel();

        int x = 10;
        width += x;

        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        Color bg = playerEntity.isSeasonal() ? seasonalColor : Color.WHITE;
        p1.setBackground(bg);
        p.cruciblePanel.setBackground(playerEntity.isCrucible() ? Color.RED : bg);
        panel.add(p1);
        panel.add(p.cruciblePanel);
        panel.setLayout(new GridLayout(2, 1));

        panel.setPreferredSize(new Dimension(10, 10));
        panel.setMaximumSize(new Dimension(10, 10));

        panel.setToolTipText(p.getToolTipSeasonCrucibleString());

        mainPanel.add(panel);
        return width;
    }

    /**
     * Opens website with given URL.
     *
     * @param url Opens website with specific URL.
     */
    private static void openWebpage(String url) {
        try {
            Desktop desktop = Desktop.getDesktop();
            URI uri = new URI(url);
            desktop.browse(uri);
        } catch (Exception ex) {
            System.err.println("Failed to open webpage for URL: " + url);
        }
    }

    public static void addPlayer(int id, Entity entity) {
        Player player = new Player(entity);
        PlayerBox p = new PlayerBox(id, player);
        p.panel = createMainBox(p);
        playerDisplay.put(id, p);
        charPanel.add(p.panel);

        // Check if the "Sort" checkbox is selected, and update the list if necessary
        if (INSTANCE.sortCheckBox.isSelected()) {
            update();  // Calls update() to sort and refresh the player list
        } else {
            INSTANCE.guiUpdate();  // Simply refresh if not sorting
        }
    }

    public static void removePlayer(int dropId) {
        PlayerBox p = playerDisplay.remove(dropId);
        if (p != null) {
            charPanel.remove(p.panel);
            INSTANCE.guiUpdate();
        }
    }

    public static void update(Entity playerEntity) {
        PlayerBox p = playerDisplay.get(playerEntity.id);
        if (p != null) {
            p.update();
        }
    }

    public static void update() {
        // Sort players by guild name alphabetically, placing those without a guild name at the bottom
        ArrayList<PlayerBox> sortedPlayers = new ArrayList<>(playerDisplay.values());
        sortedPlayers.sort(null);

        // Clear the charPanel and add players in sorted order
        charPanel.removeAll();
        for (PlayerBox p : sortedPlayers) {
            p.panel = createMainBox(p);
            charPanel.add(p.panel);
        }
        INSTANCE.guiUpdate();
    }

    public static void editFont(Font font) {
        mainFont = font;
        INSTANCE.updateFont();
    }

    public static void clear() {
        playerDisplay.clear();
        charPanel.removeAll();
        INSTANCE.guiUpdate();
    }

    private void updateFont() {
        charPanel.removeAll();
        for (PlayerBox p : playerDisplay.values()) {
            p.panel = createMainBox(p);
            charPanel.add(p.panel);
        }
    }

    TreeMap<String, SecurityFilter> getFilters() {
        return filters;
    }

    public void filterUpdate() {
        if (currentFilter != null && !filters.containsKey(currentFilter.name)) {
            currentFilter = null;
            filterComboBox.setSelectedItem(DISABLE_FILTER);
        }
        guiUpdateSuppression = true;
        filterComboBox.removeAllItems();
        filterComboBox.addItem(DISABLE_FILTER);
        guiUpdateSuppression = false;
        for (SecurityFilter sf : filters.values()) {
            filterComboBox.addItem(sf.name);
            if (currentFilter != null && sf.name.equals(currentFilter.name)) {
                filterComboBox.setSelectedItem(currentFilter.name);
            }
        }
    }

    private static class PlayerBox implements Comparable<PlayerBox> {
        int id;
        Player player;
        JLabel[] icon = new JLabel[4];
        JPanel panel;
        JPanel pointsPanel;
        JPanel itemsPanel;
        JPanel cruciblePanel;
        JPanel statsPanel;
        JPanel namePanel;

        public PlayerBox(int id, Player player) {
            this.id = id;
            this.player = player;
        }

        public void update() {
            Entity playerEntity = this.player.playerEntity;
            boolean hasEquipmentChanged = player.updateInv();
            if (!hasEquipmentChanged) return;

            // Get the raw enchant strings first
            String[] enchantStrings = ParseEnchants.getEnchantStrings(playerEntity);

            // Keep original stat access pattern but pass raw enchant strings
            setIcon(0, playerEntity.stat.get(StatType.INVENTORY_0_STAT).statValue, enchantStrings[0]);
            setIcon(1, playerEntity.stat.get(StatType.INVENTORY_1_STAT).statValue, enchantStrings[1]);
            setIcon(2, playerEntity.stat.get(StatType.INVENTORY_2_STAT).statValue, enchantStrings[2]);
            setIcon(3, playerEntity.stat.get(StatType.INVENTORY_3_STAT).statValue, enchantStrings[3]);

            cruciblePanel.setBackground(playerEntity.isCrucible() ? Color.RED : playerEntity.isSeasonal() ? seasonalColor : Color.WHITE);
            updateToolTipText();
            updatePointsPanel();
        }

        private JPanel updatePointsPanel() {
			if (pointsPanel == null) {
				return null;
			}
			
            pointsPanel.removeAll();

            // Parse the player
            SecurityFilter.ParsedPlayerObject parsedPlayer = currentFilter.parsePlayer(player);

            // Set up a new label
            JLabel pointsLabel = new JLabel(parsedPlayer.points + " / " + parsedPlayer.classPoints);
            pointsLabel.setHorizontalAlignment(SwingConstants.CENTER);
            pointsLabel.setFont(mainFont);

            // Update the background
            if (parsedPlayer.isUnderReqs) pointsPanel.setBackground(redColor);
            else pointsPanel.setBackground(null);

            // Update hover text
            if (!parsedPlayer.missing.isEmpty()) {
                String missingText = String.join("<br/>", parsedPlayer.missing);
                pointsLabel.setToolTipText("<html>" + missingText + "</html>");
            }

            pointsPanel.add(pointsLabel);
            INSTANCE.updateUI();

            return pointsPanel;
        }

        private void setIcon(int i, int eq, String enchant) {
            try {
                String parsedEnchant = ParseEnchants.parse(enchant);
                int enchantCount = getEnchantCount(parsedEnchant);

                if (enchantCount == 0) {
                    // Use original outline for non-enchanted items
                    icon[i].setIcon(ImageBuffer.getOutlinedIcon(eq, 20));
                } else {
                    // Enhanced glow for enchanted items
                    Color glowColor = getGlowColor(enchantCount);
                    int glowSize = getGlowSize(enchantCount);
                    icon[i].setIcon(ImageBuffer.getOutlinedIconWithGlow(eq, 20, glowColor, glowSize));
                }
                player.itemName[i] = IdToAsset.objectName(eq);
            } catch (Exception e) {
                e.printStackTrace();
            }
            INSTANCE.updateUI();
        }

        public void updateToolTipText() {
            String[] enchant = ParseEnchants.extractEnchants(player.playerEntity);

            for (int i = 0; i < 4; i++) {
                icon[i].setToolTipText(String.format("<html>%s<br>%s</html>", player.itemName[i], enchant[i]));
            }
        }

        /**
         * Gets the tool tip seasonCrucible string from the entity.
         *
         * @return seasonCrucible as tooltip string.
         */
        public String getToolTipSeasonCrucibleString() {
            Entity playerEntity = this.player.playerEntity;
            String seasonalStr = playerEntity.isSeasonal() ? "Seasonal" : "Non-Seasonal";

            if (playerEntity.isCrucible()) {
                return String.format("<html>%s/Crucible</html>", seasonalStr);
            } else {
                return String.format("<html>%s</html>", seasonalStr);
            }
        }

        /**
         * Gets the tool tip stats string from array of stats.
         *
         * @return Stats as tooltip string.
         */
        public String getToolTipStatString() {
            int[] stats = this.player.statMissing();
            return String.format("<html>Missing<br>%d :Life<br>%d :Mana<br>%d :Atk<br>%d :Def<br>%d :Spd<br>%d :Dex<br>%d :Vit<br>%d :Wis</html>", stats[0], stats[1], stats[2], stats[3], stats[4], stats[5], stats[6], stats[7]);
        }

        @Override
        public int compareTo(PlayerBox p) {
            String guild1 = this.player.playerEntity.getStatGuild();
            String guild2 = p.player.playerEntity.getStatGuild();

            // Handle null or empty guild names by placing them at the bottom
            if (guild1 == null || guild1.isEmpty()) {
                if (guild2 == null || guild2.isEmpty()) return 0;
                else return 1;
            } else if (guild2 == null || guild2.isEmpty()) return -1;

            return guild1.compareToIgnoreCase(guild2); // Compare guild names ignoring case
        }
    }
}
