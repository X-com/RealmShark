package tomato.gui.stats;

import assets.IdToAsset;
import assets.ImageBuffer;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import packets.data.StatData;
import packets.data.enums.StatType;
import packets.incoming.MapInfoPacket;
import tomato.backend.data.Entity;
import tomato.backend.data.TomatoData;
import tomato.gui.SmartScroller;
import tomato.gui.dps.IconDpsGUI;
import tomato.realmshark.*;
import tomato.realmshark.enums.CharacterStatistics;
import tomato.realmshark.enums.LootBags;

public class LootGUI extends JPanel {

    private static LootGUI INSTANCE;

    private static TomatoData data;

    private static boolean cleared = false;

    private static boolean update = false;

    private static JPanel lootPanel;

    private static Font mainFont;

    private static int lootDrops;

    // Marks loot that triggered a ping. Deliberately outside the enchant-count
    // glow palette (green/blue/purple/gold) so the two never read as the same
    // signal.
    private static final Color PING_COLOR = new Color(255, 64, 64);
    private boolean disableLootSharing = false;
    public static boolean filterWhiteBag = false;
    public static boolean filterOrangeBag = false;
    public static boolean filterRedBag = false;
    public static boolean filterGoldBag = false;
    public static boolean filterEggBag = false;
    public static boolean filterBlueBag = false;
    public static boolean filterTealBag = false;
    public static boolean filterPurpleBag = false;
    public static boolean filterPinkBag = false;
    public static boolean filterBrownBag = false;

    public LootGUI(TomatoData data) {
        LootGUI.data = data;
        lootDrops = 0;
        INSTANCE = this;
        setLayout(new BorderLayout());

        lootPanel = new JPanel();

        lootPanel.setLayout(new BoxLayout(lootPanel, BoxLayout.Y_AXIS));

        lootPanel.add(new JLabel("Change instance to see loot info."));
        validate();

        JScrollPane scroll = new JScrollPane(lootPanel);
        scroll.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        );
        // JPanel is not Scrollable, so the viewport defaults to 1px per tick.
        scroll.getVerticalScrollBar().setUnitIncrement(40);
        new SmartScroller(scroll, 0);
        add(scroll, BorderLayout.CENTER);
    }

    public static void update(
        MapInfoPacket map,
        Entity bag,
        Entity dropper,
        Entity player,
        long time
    ) {
        INSTANCE.updateGui(map, bag, dropper, player, time);
    }

    public static void updateExaltStats() {
        update = true;
        if (!cleared) {
            cleared = true;
            lootPanel.removeAll();
            INSTANCE.safeRefreshPanel();
        }
    }

    private void updateGui(
        MapInfoPacket map,
        Entity bag,
        Entity dropper,
        Entity player,
        long time
    ) {
        if (player == null || !update) return;

        JPanel panel = createMainBox(map, bag, dropper, player, time);
        lootPanel.add(panel, 0);

        panel.setVisible(isBagVisible(bag));

        if (Sound.playWhiteBagSound && isWhiteBag(bag)) Sound.whitebag.play();
        if (
            Sound.playOrangeBagSound && isOrangeBag(bag)
        ) Sound.orangebag.play();
        if (Sound.playRedBagSound && isRedBag(bag)) Sound.redbag.play();
        if (Sound.playGoldBagSound && isGoldBag(bag)) Sound.goldbag.play();
        if (Sound.playEggBagSound && isEggBag(bag)) Sound.eggbag.play();
        if (Sound.playBlueBagSound && isBlueBag(bag)) Sound.bluebag.play();

        if (!disableLootSharing) {
            SendLoot.sendLoot(data, map, bag, dropper, player, time);
        }

        safeRefreshPanel();
    }

    private boolean isBagVisible(Entity bag) {
        if (isWhiteBag(bag) && !filterWhiteBag) return false;
        if (isOrangeBag(bag) && !filterOrangeBag) return false;
        if (isRedBag(bag) && !filterRedBag) return false;
        if (isGoldBag(bag) && !filterGoldBag) return false;
        if (isEggBag(bag) && !filterEggBag) return false;
        if (isBlueBag(bag) && !filterBlueBag) return false;
        if (isTealBag(bag) && !filterTealBag) return false;
        if (isPurpleBag(bag) && !filterPurpleBag) return false;
        if (isPinkBag(bag) && !filterPinkBag) return false;
        if (isBrownBag(bag) && !filterBrownBag) return false;
        return true; // Show if no filter prevents it
    }

    public static void applyFilters() {
        Component[] components = lootPanel.getComponents();
        for (Component component : components) {
            if (component instanceof JPanel) {
                JPanel lootEntry = (JPanel) component;
                Entity bag = (Entity) lootEntry.getClientProperty("bagEntity");
                if (bag != null) {
                    lootEntry.setVisible(INSTANCE.isBagVisible(bag));
                }
            }
        }
        lootPanel.revalidate();
        lootPanel.repaint();
    }

    private boolean isBrownBag(Entity bag) {
        int id = bag.objectType;
        return (
            id == LootBags.BROWN.getId() || id == LootBags.BOOSTED_BROWN.getId()
        );
    }

    private boolean isPinkBag(Entity bag) {
        int id = bag.objectType;
        return (
            id == LootBags.PINK.getId() || id == LootBags.BOOSTED_PINK.getId()
        );
    }

    private boolean isPurpleBag(Entity bag) {
        int id = bag.objectType;
        return (
            id == LootBags.PURPLE.getId() ||
            id == LootBags.BOOSTED_PURPLE.getId()
        );
    }

    private boolean isTealBag(Entity bag) {
        int id = bag.objectType;
        return (
            id == LootBags.TEAL.getId() || id == LootBags.BOOSTED_TEAL.getId()
        );
    }

    private boolean isBlueBag(Entity bag) {
        int id = bag.objectType;
        return (
            id == LootBags.BLUE.getId() || id == LootBags.BOOSTED_BLUE.getId()
        );
    }

    private boolean isWhiteBag(Entity bag) {
        int id = bag.objectType;
        return (
            id == LootBags.WHITE.getId() || id == LootBags.BOOSTED_WHITE.getId()
        );
    }

    private boolean isOrangeBag(Entity bag) {
        int id = bag.objectType;
        return (
            id == LootBags.ORANGE.getId() ||
            id == LootBags.BOOSTED_ORANGE.getId()
        );
    }

    private boolean isRedBag(Entity bag) {
        int id = bag.objectType;
        return id == LootBags.RED.getId() || id == LootBags.BOOSTED_RED.getId();
    }

    private boolean isGoldBag(Entity bag) {
        int id = bag.objectType;
        return (
            id == LootBags.GOLD.getId() || id == LootBags.BOOSTED_GOLD.getId()
        );
    }

    private boolean isEggBag(Entity bag) {
        int id = bag.objectType;
        return id == LootBags.EGG.getId() || id == LootBags.BOOSTED_EGG.getId();
    }

    private void guiUpdate() {
        safeRefreshPanel();
    }

    private static JPanel createMainBox(
        MapInfoPacket map,
        Entity bag,
        Entity dropper,
        Entity player,
        long time
    ) {
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray),
                BorderFactory.createEmptyBorder(0, 20, 0, 20)
            )
        );
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));

        mainPanel.putClientProperty("bagEntity", bag); // Store the bag entity for filtering
        int y = 24;
        int width = 30;

        int exaltBonus = -1;
        long lootTime = 0;
        if (player != null) {
            exaltBonus = RealmCharacter.exaltLootBonus(player.objectType);
            lootTime = player.lootDropTime(time);
        }

        mainPanel.add(Box.createHorizontalGlue());

        width = displayCount(mainPanel, width);
        mainPanel.add(Box.createHorizontalStrut(10));
        width = displayTime(mainPanel, width);
        mainPanel.add(Box.createHorizontalStrut(10));
        width = displayBagDungMob(
            map,
            bag,
            player,
            dropper,
            mainPanel,
            width,
            exaltBonus,
            lootTime
        );
        mainPanel.add(Box.createHorizontalStrut(30));
        java.util.List<String> bagPings = new java.util.ArrayList<>();
        width = displayBagLootIcons(bag, mainPanel, width, bagPings);

        // A pinged bag gets a coloured stripe down its left edge so it can be
        // picked out while scrolling, without hovering every row. The left
        // inset is reduced by the stripe width so nothing shifts alignment.
        if (!bagPings.isEmpty()) {
            mainPanel.setBorder(
                BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 4, 1, 0, PING_COLOR),
                    BorderFactory.createEmptyBorder(0, 16, 0, 20)
                )
            );
        }

        mainPanel.add(Box.createHorizontalGlue());

        mainPanel.setMaximumSize(new Dimension(width, y));

        return mainPanel;
    }

    private static int displayBagDungMob(
        MapInfoPacket map,
        Entity entity,
        Entity player,
        Entity dropper,
        JPanel mainPanel,
        int width,
        int exaltBonus,
        long lootTime
    ) {
        JPanel panel = new JPanel();
        width += 100;

        panel.setPreferredSize(new Dimension(100, 24));
        panel.setMaximumSize(new Dimension(100, 24));
        panel.setLayout(new GridLayout(1, 4));

        displayBagIcon(entity, lootTime, panel);
        displayPlayerIcon(map, player, exaltBonus, panel);
        displayDungeonIcon(map, panel);
        displayMobIcon(dropper, panel);

        mainPanel.add(panel);
        return width;
    }

    private static int displayCount(JPanel mainPanel, int width) {
        JPanel o = new JPanel();
        int pannelSize = IconDpsGUI.getStringSize("#1234");
        width += pannelSize;

        o.setPreferredSize(new Dimension(pannelSize, 24));
        o.setMaximumSize(new Dimension(pannelSize, 24));
        o.setLayout(new BorderLayout());
        lootDrops++;

        try {
            String text = "#" + lootDrops;
            JLabel timeLabel = new JLabel(text, JLabel.CENTER);

            timeLabel.setAlignmentX(JLabel.LEFT);
            o.setAlignmentX(JLabel.LEFT);
            o.setAlignmentX(LEFT_ALIGNMENT);
            timeLabel.setHorizontalAlignment(SwingConstants.LEFT);
            timeLabel.setFont(getMainFont());
            o.add(timeLabel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mainPanel.add(o);
        return width;
    }

    private static int displayTime(JPanel mainPanel, int width) {
        JPanel o = new JPanel();
        int pannelSize = IconDpsGUI.getStringSize("HH:mm:ss");
        width += pannelSize;

        o.setPreferredSize(new Dimension(pannelSize, 24));
        o.setMaximumSize(new Dimension(pannelSize, 24));
        o.setLayout(new BorderLayout());

        try {
            String text = time();
            JLabel timeLabel = new JLabel(text, JLabel.CENTER);

            timeLabel.setAlignmentX(JLabel.LEFT);
            o.setAlignmentX(JLabel.LEFT);
            o.setAlignmentX(LEFT_ALIGNMENT);
            timeLabel.setHorizontalAlignment(SwingConstants.LEFT);
            timeLabel.setFont(getMainFont());
            o.add(timeLabel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mainPanel.add(o);
        return width;
    }

    private static void displayPlayerIcon(
        MapInfoPacket map,
        Entity player,
        int exaltBonus,
        JPanel panel
    ) {
        int picon = 100;
        boolean isSeasonal = false;
        String name = "Unknown";
        if (map != null) {
            StatData sd = player.stat.get(StatType.SKIN_ID.get());
            StatData sesn = player.stat.get(StatType.SEASONAL.get());
            if (sd != null) {
                picon = sd.statValue;
                if (picon == 0) picon = player.objectType;
                name = IdToAsset.objectName(picon);
            }
            if (sesn != null) {
                if (sesn.statValue == 1) {
                    isSeasonal = true;
                }
            }
        }
        JLabel icon = new JLabel(ImageBuffer.getOutlinedIcon(picon, 20));
        if (exaltBonus != -1) {
            name += "<br>Exalt Bonus: " + exaltBonus + "%";
        }
        if (isSeasonal) {
            name += "<br>Seasonal";
        }
        icon.setToolTipText("<html>" + name + "</html>");

        panel.add(icon);
    }

    private static int displayBagLootIcons(
        Entity entity,
        JPanel mainPanel,
        int width,
        java.util.List<String> bagPingsOut
    ) {
        JPanel panel = new JPanel();
        width += 200;

        panel.setPreferredSize(new Dimension(200, 24));
        panel.setMaximumSize(new Dimension(200, 24));
        panel.setLayout(new GridLayout(1, 8));

        String[] enchants = null;
        StatData udata = entity.stat.get(StatType.UNIQUE_DATA_STRING);
        if (udata != null && udata.stringStatValue != null) {
            enchants = udata.stringStatValue.split(",");
        }

        for (int i = 0; i < 8; i++) {
            StatData sd = entity.stat.get(StatType.INVENTORY_0_STAT.get() + i);
            if (sd == null || sd.statValue < 1) {
                JPanel comp = new JPanel();
                comp.setMinimumSize(new Dimension(24, 24));
                panel.add(comp);
                continue;
            }
            int statValue = sd.statValue;
            String itemName = IdToAsset.objectName(statValue);
            String enchantText = "";
            int enchantCount = 0;

            // Check for ping items and ping if found
            boolean itemPinged =
                data.isItemPing(String.valueOf(statValue)) ||
                data.isItemPing(itemName);
            if (itemPinged) {
                PingSounds.play(PingSounds.Type.ITEM);
            }

            if (
                enchants != null &&
                i < enchants.length &&
                !enchants[i].isEmpty() &&
                !enchants[i].equals("AAIE_f_9__3__f8=")
            ) {
                enchantText = ParseEnchants.parse(enchants[i]);
                if (!enchantText.isEmpty()) {
                    String[] enchantNames = enchantText.split("\n");
                    enchantCount = enchantNames.length;
                }
            }

            // Check for enchant pings. Must run AFTER enchantText is parsed
            // above - checking it earlier passes an empty string, which
            // isEnchantPing() rejects outright, so the ping never fires.
            java.util.ArrayList<String> matchedEnchants =
                data.getMatchedEnchantPings(enchantText);
            if (!matchedEnchants.isEmpty()) {
                PingSounds.play(PingSounds.Type.ENCHANT);
            }

            JLabel icon;
            if (enchantCount == 0) {
                icon = new JLabel(ImageBuffer.getOutlinedIcon(statValue, 20));
            } else {
                Color glowColor;
                switch (enchantCount) {
                    case 1:
                        glowColor = new Color(0, 255, 0); // Green
                        break;
                    case 2:
                        glowColor = new Color(0, 200, 255); // Blue
                        break;
                    case 3:
                        glowColor = new Color(200, 0, 255); // Purple
                        break;
                    case 4:
                        glowColor = new Color(255, 215, 0); // Gold
                        break;
                    default:
                        glowColor = Color.BLACK;
                }
                int glowSize = 3;
                icon = new JLabel(
                    ImageBuffer.getOutlinedIconWithGlow(
                        statValue,
                        20,
                        glowColor,
                        glowSize
                    )
                );
            }

            if (!enchantText.isEmpty()) {
                itemName += "<br>" + enchantText;
            }

            // Mark the item that actually caused a ping, so a bag full of
            // enchanted loot does not have to be hovered slot by slot to find
            // out which one fired.
            if (!matchedEnchants.isEmpty() || itemPinged) {
                icon.setText(pingBadge(matchedEnchants, itemPinged));
                // Draw the badge ON the sprite - the cell is only 24px wide, so
                // there is no room to place it beside the icon.
                icon.setHorizontalTextPosition(SwingConstants.CENTER);
                icon.setVerticalTextPosition(SwingConstants.CENTER);
                icon.setFont(icon.getFont().deriveFont(Font.BOLD, 11f));
                icon.setForeground(PING_COLOR);
                icon.setBorder(BorderFactory.createLineBorder(PING_COLOR, 1));

                StringBuilder why = new StringBuilder("<br><b>PING: ");
                if (itemPinged) why.append("item match");
                for (int m = 0; m < matchedEnchants.size(); m++) {
                    if (itemPinged || m > 0) why.append(", ");
                    why.append(matchedEnchants.get(m));
                }
                itemName += why.append("</b>").toString();

                bagPingsOut.addAll(matchedEnchants);
                if (itemPinged) bagPingsOut.add(itemName);
            }

            icon.setToolTipText("<html>" + itemName + "</html>");
            panel.add(icon);
        }

        mainPanel.add(panel);
        return width;
    }

    /**
     * Set false for strictly one letter per enchant.
     *
     * Two initials is the default because the single-letter form cannot
     * separate the cases this feature exists for: "Lucky Streak" and "Loot
     * Bonus IV" are both "L", so a lone letter does not answer "is this the
     * ring I want or the off-class armour I can skip". "LS" vs "LB" does.
     */
    private static final boolean PING_BADGE_INITIALS = true;

    /**
     * Builds the short overlay label for a pinged item. A single matching
     * enchant gets its initials (Lucky Streak -> "LS"); several matches fall
     * back to one letter each so the badge cannot swamp a 24px cell. An
     * item-list match is marked with a leading star.
     */
    private static String pingBadge(
        java.util.List<String> matchedEnchants,
        boolean itemPinged
    ) {
        StringBuilder sb = new StringBuilder();
        if (itemPinged) sb.append('*');

        boolean useInitials =
            PING_BADGE_INITIALS && matchedEnchants.size() == 1 && !itemPinged;

        for (String name : matchedEnchants) {
            if (name == null || name.isEmpty()) continue;
            if (useInitials) {
                sb.append(initials(name, 2));
            } else {
                sb.append(Character.toUpperCase(name.charAt(0)));
            }
            if (sb.length() >= 3) break; // keep the sprite readable
        }
        return sb.toString();
    }

    /**
     * First letter of up to {@code max} words: "Lucky Streak" -> "LS".
     */
    private static String initials(String name, int max) {
        StringBuilder sb = new StringBuilder();
        for (String word : name.trim().split("\\s+")) {
            if (word.isEmpty()) continue;
            sb.append(Character.toUpperCase(word.charAt(0)));
            if (sb.length() >= max) break;
        }
        return sb.toString();
    }

    private static void displayBagIcon(
        Entity entity,
        long lootTime,
        JPanel panel
    ) {
        int bag = entity.objectType;
        JLabel icon = new JLabel(ImageBuffer.getOutlinedIcon(bag, 20));
        String name = time();
        name += "<br>" + IdToAsset.objectName(bag);
        if (lootTime > 0) {
            name += "<br>Loot drop bonus 50%";
        }
        icon.setToolTipText("<html>" + name + "</html>");

        panel.add(icon);
    }

    private static void displayMobIcon(Entity dropper, JPanel panel) {
        int mob = 100;
        int sharedLoot = 0;
        if (dropper != null) {
            mob = dropper.objectType;
            sharedLoot = dropper.playersRemainAtKill();
        }
        JLabel icon = new JLabel(ImageBuffer.getOutlinedIcon(mob, 20));
        String name = "Unknown";
        if (mob != 100) {
            name = IdToAsset.objectName(mob);
        }
        if (sharedLoot != 0) {
            name += "<br>Shared loot: " + sharedLoot + " players";
        }
        icon.setToolTipText("<html>" + name + "</html>");
        panel.add(icon);
    }

    private static void displayDungeonIcon(MapInfoPacket map, JPanel panel) {
        int dungeon = 100;
        String dungeonName = "Unknown";
        String dungeonModifiers = "";
        if (map != null) {
            dungeonName = map.name;
            dungeonModifiers = dungeonBuff(
                ParseDungeon.getModifiersString(map)
            );
            dungeon = ParseDungeon.getPortalId(dungeonName);
            if (dungeon == -1) {
                CharacterStatistics cs = CharacterStatistics.statByName(
                    dungeonName
                );
                if (cs != null) {
                    dungeon = cs.getSpriteId();
                } else {
                    dungeon = 100;
                }
            }
        }

        JLabel icon = new JLabel(ImageBuffer.getOutlinedIcon(dungeon, 20));
        if (!dungeonModifiers.isEmpty()) {
            dungeonName += "<br>" + dungeonModifiers;
        }

        if (dungeonName.equals("Moonlight Village")) {
            int flames = data.getMoonlightFlameCount();
            if (flames > 0) {
                dungeonName += "<br>Flames: " + flames;

                // Reset the flames for loot tracking here after 5 seconds
                new Thread(() -> {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    data.resetMoonlightFlames();
                })
                    .start();
            }
        }

        icon.setToolTipText("<html>" + dungeonName + "</html>");
        panel.add(icon);
    }

    private static String dungeonBuff(String buffs) {
        String b = "";
        if (buffs == null || buffs.isEmpty()) return b;
        for (String s : buffs.split(";")) {
            if (s.contains("REWARDSBOOSTBOSS_")) {
                b += " Boss " + getaChar(s) * 15 + "% ";
            }
            if (s.contains("REWARDSBOOSTMINIONS_")) {
                b += " Minions " + (getaChar(s) + 2) * 50 + "% ";
            }
            if (s.contains("REWARDSDECREASEMINIONS_")) {
                b += " Minions " + (4 - getaChar(s)) * 25 + "% ";
            }
        }
        return b;
    }

    private static int getaChar(String s) {
        return s.charAt(s.length() - 1) - 48;
    }

    public static void editFont(Font font) {
        if (INSTANCE != null) {
            INSTANCE.handleFontUpdate(font);
        }
    }

    public static void lootSharing(boolean b) {
        INSTANCE.disableLootSharing = b;
    }

    private static String time() {
        return LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("HH:mm:ss")
        );
    }

    private static Font getMainFont() {
        if (mainFont == null) {
            mainFont = new Font("Arial", Font.PLAIN, 12);
        }
        return mainFont;
    }

    private void handleFontUpdate(Font font) {
        mainFont = font;
        guiUpdate();
    }

    private void safeRefreshPanel() {
        try {
            lootPanel.revalidate();
            lootPanel.repaint();
        } catch (Exception e) {
            // Ignore
        }
    }
}
