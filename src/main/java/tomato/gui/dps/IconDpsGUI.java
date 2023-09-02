package tomato.gui.dps;

import assets.AssetMissingException;
import assets.IdToAsset;
import assets.ImageBuffer;
import packets.incoming.MapInfoPacket;
import packets.incoming.NotificationPacket;
import tomato.backend.data.*;
import tomato.gui.SmartScroller;
import tomato.realmshark.ParseEnchants;
import tomato.realmshark.enums.CharacterClass;
import util.Pair;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class IconDpsGUI extends DisplayDpsGUI {

    private static JPanel charPanel;
    private final TomatoData data;
    private static Font mainFont;
    private ArrayList<NotificationPacket> notifications;
    private static final DecimalFormat df = new DecimalFormat("#,###,###");
    private static HashMap<Integer, Image> imgMap = new HashMap<>();
    private static final BufferedImage ig = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    public IconDpsGUI(TomatoData data) {
        this.data = data;

        setLayout(new BorderLayout());
        charPanel = new JPanel();
        charPanel.setLayout(new GridBagLayout());
        charPanel.setLayout(new BoxLayout(charPanel, BoxLayout.Y_AXIS));
        validate();

        JScrollPane scroll = new JScrollPane(charPanel);
        scroll.getVerticalScrollBar().setUnitIncrement(40);
        new SmartScroller(scroll);
        add(scroll, BorderLayout.CENTER);

//        JButton button = new JButton("String Display");
//        button.addActionListener(e -> clicked());
//        add(button, BorderLayout.SOUTH);
    }

//    private void clicked() {
//        DpsGUI.setDisplayAsString();
//    }

    private void updateDps(MapInfoPacket map, List<Entity> sortedEntityHitList, long totalDungeonPcTime) {
        ArrayList<Pair<String, Integer>> deaths = new ArrayList<>();
        for (NotificationPacket n : notifications) {
            String name = n.message.split("\"")[9];
            int graveIcon = n.pictureType;
            deaths.add(new Pair<>(name, graveIcon));
        }
        charPanel.removeAll();

        {
            JPanel dungeon = new JPanel();
            dungeon.setBorder(BorderFactory.createTitledBorder(null, map.name + DpsGUI.systemTimeToString(totalDungeonPcTime), TitledBorder.CENTER, TitledBorder.CENTER, mainFont));
//            dungeon.setBorder(BorderFactory.createTitledBorder(name));
//            dungeon.setPreferredSize(new Dimension(320, 24));
            charPanel.add(dungeon);
        }

//        sb.append("Total time in dungeon:").append(DpsGUI.systemTimeToString(totalDungeonPcTime)).append("\n");
        for (Entity e : sortedEntityHitList) {
            if (e.maxHp() <= 0) continue;
            if (CharacterClass.isPlayerCharacter(e.objectType)) continue;
            JPanel panel = createMainBox(e, deaths, data.player);
            if (panel != null) {
                charPanel.add(panel);
            }
        }
    }

    private void guiUpdate() {
        validate();
        repaint();
    }

    private static JPanel createMainBox(Entity entity, ArrayList<Pair<String, Integer>> deaths, Entity player) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        JPanel mobPanel = new JPanel();
        mobPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.GRAY), BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        mobPanel.setLayout(new BoxLayout(mobPanel, BoxLayout.X_AXIS));

        StringBuilder sb = new StringBuilder();
        sb.append(entity.name()).append(" HP: ").append(entity.maxHp()).append("\n");
        sb.append(entity.getFightTimerString());
        String mobName = sb.toString();
        JLabel l = new JLabel(mobName, new ImageIcon(getScaledImg(entity.objectType, entity.img())), JLabel.LEFT);
        int mobNameStringSize = getStringSize(mobName) + 48;
        mobPanel.setPreferredSize(new Dimension(mobNameStringSize, 48));
        mobPanel.setMaximumSize(new Dimension(mobNameStringSize, 48));

        l.setFont(mainFont);
        mobPanel.add(l);
        panel.add(mobPanel);

        List<Damage> playerDamageList = entity.getPlayerDamageList();

        JPanel panelAllPlayers = new JPanel();
        panelAllPlayers.setLayout(new BoxLayout(panelAllPlayers, BoxLayout.Y_AXIS));
        panel.add(panelAllPlayers);

        int counter = 0;
        int[] pref = new int[7];
        ArrayList<JPanel>[] panels = new ArrayList[6];
        for (int i = 0; i < panels.length; i++) {
            panels[i] = new ArrayList<>();
        }
        for (Damage dmg : playerDamageList) {
            int filter = Filter.filter(dmg.owner, player);

            boolean user = dmg.owner.isUser();
            boolean highlight = false;
            counter++;

            if (Filter.shouldFilter() && filter != 1) {
                continue;
            } else if (filter == 2) {
                highlight = true;
            }

            String name = dmg.owner.name();
            JPanel inv = equipment(DpsDisplayOptions.equipmentOption, dmg.owner, entity);

            String extra = "";
            if (dmg.oryx3GuardDmg) {
                extra = String.format("[Guarded Hits:%d Dmg:%d]", dmg.counterHits, dmg.counterDmg);
            } else if (entity.dammahCountered && dmg.chancellorDammahDmg) {
                extra = String.format("[Dammah Hits:%d Dmg:%d]", dmg.counterHits, dmg.counterDmg);
            } else if (dmg.walledGardenReflectors) {
                extra = String.format("[Garden Hits:%d Dmg:%d]", dmg.counterHits, dmg.counterDmg);
            }
            float pers = ((float) dmg.damage * 100 / (float) entity.maxHp());

            String userIndicator = String.format("%s%d", user ? " ->" : (highlight ? ">>" : "  "), counter);
            String s2 = String.format("DMG: %7d %6.3f%%", dmg.damage, pers);

            JLabel playerIconLabel = new JLabel(userIndicator, new ImageIcon(dmg.owner.img().getScaledInstance(16, 16, Image.SCALE_DEFAULT)), JLabel.LEFT);
            JLabel nameLabel = new JLabel(name);
            JLabel dpsDataLabel = new JLabel(s2);
            JLabel deathNexusLabel = new JLabel();
            for (int id : entity.playerDropped.keySet()) {
                if (dmg.owner.id == id) {
                    PlayerRemoved pr = entity.playerDropped.get(id);
                    int dead = isDeadPlayer(name, deaths);
                    if (dead != -1) {
                        try {
                            ImageIcon ii = new ImageIcon(ImageBuffer.getImage(dead).getScaledInstance(16, 16, Image.SCALE_DEFAULT));
                            deathNexusLabel = new JLabel(ii);
                        } catch (IOException | AssetMissingException e) {
                            deathNexusLabel = new JLabel("Died");
                        }
                    } else {
                        deathNexusLabel = new JLabel("Nexus");
                    }
                    deathNexusLabel.setToolTipText(String.format("%.2f%% [%s / %s]", ((float) pr.hp / pr.max) * 100, df.format(pr.hp).replaceAll(",", " "), df.format(pr.max).replaceAll(",", " ")));
                }
            }
            JLabel counterLabel = new JLabel(extra);

            playerIconLabel.setHorizontalTextPosition(SwingConstants.LEFT);
            ArrayList<Component> list = new ArrayList();
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
            panelAllPlayers.add(pp);
        }
        pref[0] = 40;
        for (int i = 0; i < panels.length; i++) {
            for (JPanel p : panels[i]) {
                Dimension preferredSize = new Dimension(pref[i], pref[pref.length - 1] + 5);
                p.setPreferredSize(preferredSize);
                p.setMaximumSize(preferredSize);
            }
        }

        if (panelAllPlayers.getComponents().length == 0) return null;

        return panel;
    }

    private static int getStringSize(String str) {
        Graphics2D g2d = ig.createGraphics();
        FontMetrics fm = g2d.getFontMetrics(mainFont);
        int size = fm.stringWidth(str);
        g2d.dispose();
        return size;
    }

    private static int isDeadPlayer(String name, ArrayList<Pair<String, Integer>> deaths) {
        for (Pair<String, Integer> p : deaths) {
            if (p.left().equals(name)) {
                return p.right();
            }
        }
        return -1;
    }

    private static Image getScaledImg(int id, BufferedImage img) {
        if (imgMap.containsKey(id)) {
            return imgMap.get(id);
        } else {
            imgMap.put(id, img.getScaledInstance(40, 40, Image.SCALE_DEFAULT));
        }
        return img.getScaledInstance(40, 40, Image.SCALE_DEFAULT);
    }

    private static boolean filter(String name) {
        if (!DpsDisplayOptions.nameFilter || DpsDisplayOptions.filteredStrings.length == 0) return false;
        for (String n : DpsDisplayOptions.filteredStrings) {
            if (name.toLowerCase().startsWith(n.toLowerCase())) {
                return false;
            }
        }
        return true;
    }

    private static JPanel equipment(int equipmentFilter, Entity owner, Entity entity) {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(76, 16));
        panel.setLayout(new GridLayout(1, 4));

        if (owner.getStatName() == null) return panel;

        HashMap<Integer, Equipment>[] inv = new HashMap[4];

        for (int i = 0; i < 4; i++) {
            AtomicInteger tot = new AtomicInteger(0);
            if (inv[i] == null) inv[i] = new HashMap<>();
            for (Damage d : entity.getDamageList()) {
                if (d.owner == null || d.owner.id != owner.id || d.ownerInvntory == null) continue;

                int finalI = i;
                Equipment equipment = inv[i].computeIfAbsent(d.ownerInvntory[i], id -> new Equipment(id, d.ownerEnchants[finalI], tot));
                equipment.add(d.damage);
            }
        }

        for (int i = 0; i < 4; i++) {
            Equipment max = inv[i].values().stream().max(Comparator.comparingInt(e -> e.dmg)).orElseThrow(NoSuchElementException::new);
            BufferedImage img;
            try {
                int eq = max.id;
                if (eq == -1) {
                    img = ImageBuffer.getEmptyImg();
                } else {
                    img = ImageBuffer.getImage(eq);
                }
                JLabel icon = new JLabel(new ImageIcon(img.getScaledInstance(16, 16, Image.SCALE_DEFAULT)));
                icon.setToolTipText(String.format("<html>%s<br>%s</html>", IdToAsset.objectName(eq), ParseEnchants.parse(max.enchant)));
                panel.add(icon);
            } catch (AssetMissingException | IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        return panel;
    }

    @Override
    protected void renderData(MapInfoPacket map, List<Entity> sortedEntityHitList, ArrayList<NotificationPacket> deathNotifications, long totalDungeonPcTime, boolean isLive) {
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
