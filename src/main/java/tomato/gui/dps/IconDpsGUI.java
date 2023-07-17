package tomato.gui.dps;

import assets.AssetMissingException;
import assets.IdToAsset;
import assets.ImageBuffer;
import tomato.backend.data.Damage;
import tomato.backend.data.Entity;
import tomato.backend.data.Equipment;
import tomato.gui.SmartScroller;
import tomato.realmshark.enums.CharacterClass;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class IconDpsGUI extends DisplayDpsGUI {

    private static int HEIGHT = 24;
    private static JPanel charPanel;
    Entity[] data;
    private static Font mainFont;

    public IconDpsGUI() {
        setLayout(new BorderLayout());
        charPanel = new JPanel();
        charPanel.setLayout(new GridBagLayout());
        charPanel.setLayout(new BoxLayout(charPanel, BoxLayout.Y_AXIS));
        validate();

        JScrollPane scroll = new JScrollPane(charPanel);
        scroll.getVerticalScrollBar().setUnitIncrement(40);
        new SmartScroller(scroll);
        add(scroll, BorderLayout.CENTER);

        JButton button = new JButton("Clear");
        button.addActionListener(e -> clicked());
        add(button, BorderLayout.SOUTH);
    }

    private void clicked() {
        long time = System.currentTimeMillis();
        charPanel.removeAll();
        System.out.println(System.currentTimeMillis() - time);
//        updateDps(data);
//        guiUpdate();
    }

    private void updateDps(Entity[] data) {
        List<Entity> sortedList = Arrays.stream(data).sorted(Comparator.comparingLong(Entity::getLastDamageTaken).reversed()).collect(Collectors.toList());
        charPanel.removeAll();
        for (Entity e : sortedList) {
            if (e.maxHp() <= 0) continue;
            if (CharacterClass.isPlayerCharacter(e.objectType)) continue;
            charPanel.add(createMainBox(e));
        }
    }

    private void guiUpdate() {
        validate();
        repaint();
    }

    private static JPanel createMainBox(Entity entity) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel mobPanel = new JPanel();
        mobPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED), BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        mobPanel.setPreferredSize(new Dimension(370, HEIGHT + HEIGHT));
        mobPanel.setMaximumSize(new Dimension(370, HEIGHT));
        mobPanel.setLayout(new BoxLayout(mobPanel, BoxLayout.X_AXIS));

        StringBuilder sb = new StringBuilder();
        sb.append(entity.name()).append(" HP: ").append(entity.maxHp()).append("\n");
        JLabel l = new JLabel(sb.toString(), new ImageIcon(getScaledImg(entity.objectType, entity.img())), JLabel.LEFT);
        l.setFont(mainFont);
        mobPanel.add(l);
        panel.add(mobPanel);

        List<Damage> playerDamageList = entity.getPlayerDamageList();
        JPanel panelAllPlayers = new JPanel();

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(0, 5, 0, 0);

        panelAllPlayers.setLayout(new GridBagLayout());

        int counter = 0;
        for (Damage dmg : playerDamageList) {
            counter++;
            String name = dmg.owner.name();

            if (filter(name)) continue;

            JPanel playerPanel = new JPanel();
            JPanel inv = equipment(DpsDisplayOptions.equipmentOption, dmg.owner, entity);
            playerPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED), BorderFactory.createEmptyBorder(0, 0, 0, 0)));
            playerPanel.setPreferredSize(new Dimension(370, HEIGHT));
            playerPanel.setMaximumSize(new Dimension(370, HEIGHT));
            playerPanel.setLayout(new BoxLayout(playerPanel, BoxLayout.X_AXIS));

            String extra = "";
            if (dmg.oryx3GuardDmg) {
                extra = String.format("[Guarded Hits:%d Dmg:%d]", dmg.counterHits, dmg.counterDmg);
            } else if (entity.dammahCountered && dmg.chancellorDammahDmg) {
                extra = String.format("[Dammah Hits:%d Dmg:%d]", dmg.counterHits, dmg.counterDmg);
            } else if (dmg.walledGardenReflectors) {
                extra = String.format("[Garden Hits:%d Dmg:%d]", dmg.counterHits, dmg.counterDmg);
            }
            float pers = ((float) dmg.damage * 100 / (float) entity.maxHp());
            boolean user = dmg.owner.isUser();
            String s1 = String.format("%s%d", user ? ">" : " ", counter);
            String s2 = String.format("DMG: %7d %6.3f%% %s", dmg.damage, pers, extra);

            JLabel player1 = new JLabel(s1, new ImageIcon(dmg.owner.img().getScaledInstance(20, 20, Image.SCALE_DEFAULT)), JLabel.LEFT);
            JLabel player2 = new JLabel(name);
            JLabel player3 = new JLabel(s2);
            player1.setFont(mainFont);
            player2.setFont(mainFont);
            player3.setFont(mainFont);
            player1.setHorizontalTextPosition(SwingConstants.LEFT);
            g.gridx = 0;
            panelAllPlayers.add(player1, g);
            g.gridx = 1;
            panelAllPlayers.add(player2, g);
            g.gridx = 2;
            panelAllPlayers.add(player3, g);
            g.gridx = 3;
            panelAllPlayers.add(inv, g);
            g.gridy = counter;
        }
        panel.add(panelAllPlayers);

        return panel;
    }

    private static HashMap<Integer, Image> imgMap = new HashMap<>();

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
        panel.setPreferredSize(new Dimension(100, HEIGHT));
        panel.setMaximumSize(new Dimension(100, HEIGHT));
        panel.setLayout(new GridLayout(1, 4));

        if (equipmentFilter == 0 || owner.getStatName() == null) return panel;

        HashMap<Integer, Equipment>[] inv = new HashMap[4];

        for (int i = 0; i < 4; i++) {
            AtomicInteger tot = new AtomicInteger(0);
            if (inv[i] == null) inv[i] = new HashMap<>();
            for (Damage d : entity.getDamageList()) {
                if (d.owner == null || d.owner.id != owner.id || d.ownerInv == null) continue;

                Equipment equipment = inv[i].computeIfAbsent(d.ownerInv[i], id -> new Equipment(id, tot));
                equipment.add(d.damage);
            }
        }

        if (equipmentFilter == 1) {
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
                    JLabel icon = new JLabel(new ImageIcon(img.getScaledInstance(20, 20, Image.SCALE_DEFAULT)));
                    icon.setToolTipText(IdToAsset.objectName(eq));
                    panel.add(icon);
                } catch (AssetMissingException | IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            return panel;
        } else if (equipmentFilter == 2) {
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                s.append("\n");
                try {
                    Collection<Equipment> list = inv[i].values();
                    s.append("       ");
                    boolean first = true;
                    for (Equipment e : list) {
                        if (list.size() > 1) {
                            if (!first) s.append(" /");
                            s.append(String.format(" %.1f%% ", 100f * e.dmg / e.totalDmg.get()));
                            s.append(IdToAsset.objectName(e.id));
                        } else {
                            s.append(" ");
                            s.append(IdToAsset.objectName(e.id));
                        }
                        first = false;
                    }
                } catch (AssetMissingException ex) {
                    throw new RuntimeException(ex);
                }
            }
            return panel;
        }

        return panel;
    }

    @Override
    protected void renderData(Entity[] data, boolean isLive) {
        this.data = data;
        updateDps(data);
        guiUpdate();
    }

    @Override
    protected void editFont(Font font) {
        mainFont = font;
        updateUI();
    }
}
