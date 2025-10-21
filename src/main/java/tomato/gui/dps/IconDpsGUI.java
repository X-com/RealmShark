package tomato.gui.dps;

import assets.IdToAsset;
import assets.ImageBuffer;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import packets.data.ObjectStatusData;
import packets.data.StatData;
import packets.data.enums.StatType;
import packets.incoming.MapInfoPacket;
import packets.incoming.NotificationPacket;
import tomato.backend.data.*;
import tomato.gui.SmartScroller;
import tomato.gui.dps.shared.DeathParser;
import tomato.gui.dps.shared.DpsTextFormat;
import tomato.gui.dps.shared.EquipmentUsageAggregator;
import tomato.gui.dps.shared.GuardsHandler;
import tomato.realmshark.ParseEnchants;
import tomato.realmshark.enums.CharacterClass;

public class IconDpsGUI extends DisplayDpsGUI {

    private static JPanel charPanel;

    private JScrollPane scrollPane;

    private final TomatoData data;

    private static Font mainFont;

    private ArrayList<NotificationPacket> notifications;

    private static final BufferedImage ig = new BufferedImage(
        1,
        1,
        BufferedImage.TYPE_INT_ARGB
    );

    // Dynamic icon sizes based on current font size
    private static int smallIconSize() {
        int fs = (mainFont != null) ? mainFont.getSize() : 12;
        // Base 16px at 12pt font, clamp to at least 12px
        return Math.max(12, Math.round((16f * fs) / 12f));
    }

    private static int largeIconSize() {
        int fs = (mainFont != null) ? mainFont.getSize() : 12;
        // Base 40px at 12pt font, clamp to at least 24px
        return Math.max(24, Math.round((40f * fs) / 12f));
    }

    public IconDpsGUI(TomatoData data) {
        this.data = data;

        setLayout(new BorderLayout());

        charPanel = new JPanel();

        charPanel.setLayout(new BoxLayout(charPanel, BoxLayout.Y_AXIS));

        scrollPane = new JScrollPane(charPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(40);
        new SmartScroller(scrollPane);
        add(scrollPane, BorderLayout.CENTER);

        // Add screenshot button
        JButton screenshotButton = new JButton("Copy Screenshot to Clipboard");
        screenshotButton.addActionListener(e -> takeScreenshot());
        add(screenshotButton, BorderLayout.SOUTH);
    }

    //    private void clicked() {
    //        DpsGUI.setDisplayAsString();
    //    }

    private void updateDps(
        MapInfoPacket map,
        List<Entity> sortedEntityHitList,
        long totalDungeonPcTime
    ) {
        Map<String, Integer> deaths = DeathParser.parseDeathsToMap(
            notifications
        );
        charPanel.removeAll();

        {
            JPanel dungeon = new JPanel();
            dungeon.setBorder(
                BorderFactory.createTitledBorder(
                    null,
                    map.name + DpsGUI.systemTimeToString(totalDungeonPcTime),
                    TitledBorder.CENTER,
                    TitledBorder.CENTER,
                    mainFont
                )
            );
            //            dungeon.setBorder(BorderFactory.createTitledBorder(name));
            //            dungeon.setPreferredSize(new Dimension(320, 24));
            charPanel.add(dungeon);
        }

        //        sb.append("Total time in dungeon:").append(DpsGUI.systemTimeToString(totalDungeonPcTime)).append("\n");

        for (Entity e : sortedEntityHitList) {
            if (e.maxHp() <= 0) continue;

            if (CharacterClass.isPlayerCharacter(e.objectType)) continue;

            EquipmentUsageAggregator eqAgg = EquipmentUsageAggregator.of(e);
            JPanel panel = createMainBox(e, deaths, data.player, eqAgg);

            if (panel != null) {
                charPanel.add(panel);
            }
        }
    }

    private void guiUpdate() {
        validate();
        repaint();
    }

    private static JPanel createMainBox(
        Entity entity,
        Map<String, Integer> deaths,
        Entity player,
        EquipmentUsageAggregator eqAgg
    ) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        JPanel mobPanel = new JPanel();
        mobPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, Color.GRAY),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
            )
        );

        mobPanel.setLayout(new BoxLayout(mobPanel, BoxLayout.X_AXIS));

        StringBuilder sb = new StringBuilder();
        sb
            .append(entity.name())
            .append(" HP: ")
            .append(entity.maxHp())
            .append("\n");
        sb.append(entity.getFightTimerString());
        String mobName = sb.toString();

        int iconLarge = largeIconSize();
        JLabel l = new JLabel(
            mobName,
            ImageBuffer.getOutlinedIcon(entity.objectType, iconLarge),
            JLabel.LEFT
        );

        int firstHP = getHighestHP(entity);
        l.setToolTipText("Fight start HP: " + firstHP);
        int mobNameStringSize = getStringSize(mobName) + 48;
        mobPanel.setPreferredSize(new Dimension(mobNameStringSize, 48));
        mobPanel.setMaximumSize(new Dimension(mobNameStringSize, 48));

        l.setFont(mainFont);
        mobPanel.add(l);
        panel.add(mobPanel);

        List<Damage> playerDamageList = entity.getPlayerDamageList();

        JPanel panelAllPlayers = new JPanel();
        panelAllPlayers.setLayout(
            new BoxLayout(panelAllPlayers, BoxLayout.Y_AXIS)
        );
        panel.add(panelAllPlayers);

        int counter = 0;
        int[] pref = new int[7];
        ArrayList<JPanel>[] panels = new ArrayList[6];
        for (int i = 0; i < panels.length; i++) {
            panels[i] = new ArrayList<>();
        }
        for (Damage dmg : playerDamageList) {
            int filter = Filter.filter(dmg.owner, player);

            boolean user = dmg.owner.isUser() && DpsDisplayOptions.showMe;
            boolean highlight = false;
            counter++;

            if (Filter.shouldFilter() && filter != 1) {
                continue;
            } else if (filter == 2) {
                highlight = true;
            }

            String name = dmg.owner.name();

            JPanel inv = equipment(
                DpsDisplayOptions.equipmentOption,
                dmg.owner,
                eqAgg
            );

            String extra = GuardsHandler.buildExtraTag(entity, dmg);

            float pers = (((float) dmg.damage * 100) / (float) entity.maxHp());

            String userIndicator = String.format(
                "%s%d",
                user ? " ->" : (highlight ? ">>" : "  "),
                counter
            );
            String s2 = String.format("DMG: %7d %6.3f%%", dmg.damage, pers);
            int icon = 0;
            if (
                dmg.owner != null &&
                dmg.owner.stat != null &&
                dmg.owner.stat.get(StatType.SKIN_ID) != null
            ) {
                int skinId = dmg.owner.stat.get(StatType.SKIN_ID).statValue;
                if (skinId != 0) {
                    icon = skinId;
                } else {
                    icon = dmg.owner.objectType;
                }
            } else if (dmg.owner != null) {
                icon = dmg.owner.objectType;
            }

            int iconSmall = smallIconSize();
            JLabel playerIconLabel = new JLabel(
                userIndicator,
                ImageBuffer.getOutlinedIcon(icon, iconSmall),
                JLabel.LEFT
            );

            JLabel nameLabel = new JLabel(name);
            JLabel dpsDataLabel = new JLabel(s2);
            JLabel deathNexusLabel = new JLabel();
            for (int id : entity.playerDropped.keySet()) {
                if (dmg.owner.id == id) {
                    PlayerRemoved pr = entity.playerDropped.get(id);
                    int dead = DeathParser.getGraveIcon(deaths, name);
                    if (dead != -1) {
                        try {
                            ImageBuffer.getImage(dead);
                            deathNexusLabel = new JLabel(
                                ImageBuffer.getOutlinedIcon(dead, iconSmall)
                            );
                        } catch (IOException e) {
                            deathNexusLabel = new JLabel("Died");
                        }
                    } else {
                        deathNexusLabel = new JLabel("Nexus");
                    }

                    deathNexusLabel.setToolTipText(
                        String.format(
                            "%.2f%% [%s / %s]",
                            ((float) pr.hp / pr.max) * 100,
                            DpsTextFormat.grouped(pr.hp),
                            DpsTextFormat.grouped(pr.max)
                        )
                    );
                }
            }
            JLabel counterLabel = new JLabel(extra);

            playerIconLabel.setHorizontalTextPosition(SwingConstants.LEFT);
            ArrayList<Component> list = new ArrayList<>();
            list.add(deathNexusLabel);
            list.add(playerIconLabel);
            list.add(nameLabel);
            list.add(dpsDataLabel);
            list.add(counterLabel);
            list.add(inv);

            JPanel pp = new JPanel();
            pp.setLayout(new BoxLayout(pp, BoxLayout.X_AXIS));
            for (int i = 0; i < list.size(); i++) {
                Component c = list.get(i);
                c.setFont(mainFont);
                int width = c.getPreferredSize().width + 5;
                if (pref[i] < width) {
                    pref[i] = width;
                }
                JPanel ppp = new JPanel();
                if (i == 4 && extra.length() > 0) {
                    ppp.setLayout(new BoxLayout(ppp, BoxLayout.X_AXIS));
                    ppp.add(Box.createHorizontalGlue());
                }
                ppp.add(c);
                panels[i].add(ppp);
                pp.add(ppp);
            }

            int height = nameLabel.getPreferredSize().height;
            if (pref[pref.length - 1] < height) {
                pref[pref.length - 1] = height;
            }

            float fightDuration = entity.getFightDuration() / 60000f;

            float damagePerMinute = (float) dmg.damage / fightDuration;

            double guardedDamagePercentage =
                GuardsHandler.guardedDamagePercentage(entity, dmg);

            boolean hasGuardedDamage = GuardsHandler.hasGuardedDamage(
                entity,
                dmg
            );

            int[] damageFight = dmg.owner.damageTaken(entity);
            int[] damageTotal = dmg.owner.damageTaken(null);
            String tooltipText = "";
            if (damageFight[1] > 0) {
                tooltipText = String.format(
                    "Damage taken: %s (hits: %s)\n",
                    damageFight[0],
                    damageFight[1]
                );
            }
            if (damageTotal[1] > 0) {
                tooltipText += String.format(
                    "Total damage taken: %s (hits: %s)\n\n",
                    damageTotal[0],
                    damageTotal[1]
                );
            }

            tooltipText +=
                "Damage per Minute: " +
                DpsTextFormat.fixed2GroupedComma(damagePerMinute);

            if (hasGuardedDamage) {
                tooltipText +=
                    "\nGuarded Damage: " +
                    DpsTextFormat.percent2(guardedDamagePercentage) +
                    "%";
            }

            pp.setToolTipText(
                "<html>" + tooltipText.replace("\n", "<br>") + "</html>"
            );

            panelAllPlayers.add(pp);
        }

        int minFirstCol = Math.max(smallIconSize(), getStringSize("Nexus") + 6);
        pref[0] = Math.max(pref[0], minFirstCol);
        for (int i = 0; i < panels.length; i++) {
            for (JPanel p : panels[i]) {
                Dimension preferredSize = new Dimension(
                    pref[i],
                    pref[pref.length - 1] + 5
                );
                p.setPreferredSize(preferredSize);
                p.setMaximumSize(preferredSize);
            }
        }

        if (panelAllPlayers.getComponents().length == 0) return null;

        return panel;
    }

    private static int getHighestHP(Entity entity) {
        int hp = -1;
        for (ObjectStatusData o : entity.statUpdates) {
            for (StatData sd : o.stats) {
                if (sd.statType == StatType.MAX_HP_STAT) {
                    if (hp < sd.statValue) {
                        hp = sd.statValue;
                    }
                }
            }
        }
        return hp;
    }

    public static int getStringSize(String str) {
        Graphics2D g2d = ig.createGraphics();
        FontMetrics fm = g2d.getFontMetrics(mainFont);
        int size = fm.stringWidth(str);
        g2d.dispose();
        return size;
    }

    private static boolean filter(String name) {
        if (
            !DpsDisplayOptions.nameFilter ||
            DpsDisplayOptions.filteredStrings.length == 0
        ) return false;
        for (String n : DpsDisplayOptions.filteredStrings) {
            if (name.toLowerCase().startsWith(n.toLowerCase())) {
                return false;
            }
        }
        return true;
    }

    private static JPanel equipment(
        int equipmentFilter,
        Entity owner,
        EquipmentUsageAggregator eqAgg
    ) {
        JPanel panel = new JPanel();

        int s = smallIconSize();
        panel.setPreferredSize(new Dimension(s * 4 + 12, s));

        panel.setLayout(new GridLayout(1, 4));

        if (
            owner.getStatName() == null || equipmentFilter == 0 || eqAgg == null
        ) return panel;

        for (int i = 0; i < 4; i++) {
            Equipment max = eqAgg.getMostUsedItem(owner.id, i);
            int eq = (max != null) ? max.id : 0;

            // Get enchant count similar to ParsePanelGUI

            String parsedEnchant = (max != null)
                ? ParseEnchants.parse(max.enchant)
                : "";

            int enchantCount = parsedEnchant.isEmpty()
                ? 0
                : parsedEnchant.split("\n").length;

            // Apply glow based on enchant count

            JLabel icon;

            if (enchantCount == 0) {
                icon = new JLabel(ImageBuffer.getOutlinedIcon(eq, s));
            } else {
                Color glowColor;

                switch (enchantCount) {
                    case 1:
                        glowColor = new Color(0, 255, 0);

                        break;
                    case 2:
                        glowColor = new Color(0, 200, 255);

                        break;
                    case 3:
                        glowColor = new Color(200, 0, 255);

                        break;
                    case 4:
                        glowColor = new Color(255, 215, 0);

                        break;
                    default:
                        glowColor = Color.BLACK;
                }

                int glowSize = 3; // Same as ParsePanelGUI

                icon = new JLabel(
                    ImageBuffer.getOutlinedIconWithGlow(
                        eq,
                        s,
                        glowColor,
                        glowSize
                    )
                );
            }

            icon.setToolTipText(
                String.format(
                    "<html>%s<br>%s</html>",
                    IdToAsset.objectName(eq),
                    parsedEnchant
                )
            );

            panel.add(icon);
        }

        return panel;
    }

    private void takeScreenshot() {
        try {
            // Get the visible rectangle of the scroll pane
            Rectangle visibleRect = scrollPane.getViewport().getViewRect();

            // Create a buffered image of the visible area
            BufferedImage screenshot = new BufferedImage(
                visibleRect.width,
                visibleRect.height,
                BufferedImage.TYPE_INT_RGB
            );

            // Paint the visible content to the screenshot
            Graphics2D g2d = screenshot.createGraphics();
            g2d.translate(-visibleRect.x, -visibleRect.y);
            charPanel.paint(g2d);
            g2d.dispose();

            // Create filename with timestamp for reference
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd_HH-mm-ss"
            );
            String timestamp = dateFormat.format(new Date());
            String filename = "DamageLog_Realmshark_" + timestamp + ".png";

            // Copy image to clipboard
            Clipboard clipboard =
                Toolkit.getDefaultToolkit().getSystemClipboard();
            ImageTransferable transferable = new ImageTransferable(screenshot);
            clipboard.setContents(transferable, null);

            JOptionPane.showMessageDialog(
                this,
                "Screenshot copied to clipboard!\nFilename: " + filename,
                "Screenshot Copied",
                JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "Error copying screenshot to clipboard: " + ex.getMessage(),
                "Screenshot Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // Helper class to make BufferedImage transferable to clipboard
    private static class ImageTransferable implements Transferable {

        private final BufferedImage image;

        public ImageTransferable(BufferedImage image) {
            this.image = image;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { DataFlavor.imageFlavor };
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException {
            if (!DataFlavor.imageFlavor.equals(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return image;
        }
    }

    @Override
    protected void renderData(
        MapInfoPacket map,
        List<Entity> sortedEntityHitList,
        ArrayList<NotificationPacket> deathNotifications,
        long totalDungeonPcTime,
        boolean isLive
    ) {
        this.notifications = deathNotifications;
        updateDps(map, sortedEntityHitList, totalDungeonPcTime);
        guiUpdate();
    }

    @Override
    protected void editFont(Font font) {
        mainFont = font;
        updateUI();
    }
}
