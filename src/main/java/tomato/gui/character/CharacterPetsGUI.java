package tomato.gui.character;

import assets.AssetMissingException;
import assets.IdToAsset;
import assets.ImageBuffer;
import packets.data.ObjectData;
import packets.data.StatData;
import tomato.backend.data.Stat;
import tomato.backend.data.TomatoData;

import javax.swing.*;
import javax.swing.plaf.synth.SynthUI;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Collectors;

public class CharacterPetsGUI extends JPanel {

    private static CharacterPetsGUI INSTANCE;

    private final TomatoData data;
    private HashMap<Integer, Pet> petList = new HashMap<>();
    private final JPanel petPanel;
    private JTextField feed;
    private static final float[] feedMultiplier = {1f, 0.65f, 0.3f};

    private static final HashMap<Integer, String> petAbilitys = new HashMap<>();
    private static final HashMap<Integer, Integer> feedMax = new HashMap<>();
    private static final HashMap<Integer, Integer> feedCost = new HashMap<>();

    static {
        petAbilitys.put(402, "Attack Close");
        petAbilitys.put(404, "Attack Mid");
        petAbilitys.put(405, "Attack Far");
        petAbilitys.put(406, "Electric");
        petAbilitys.put(407, "Heal");
        petAbilitys.put(408, "Magic Heal");
        petAbilitys.put(409, "Savage");
        petAbilitys.put(410, "Decoy");
        petAbilitys.put(411, "Rising Fury");

        feedMax.put(0, -1);
        feedMax.put(30, 2000);
        feedMax.put(50, 10000);
        feedMax.put(70, 50000);
        feedMax.put(90, 235000);
        feedMax.put(100, 509000);

        feedCost.put(0, -1);
        feedCost.put(30, 15);
        feedCost.put(50, 50);
        feedCost.put(70, 175);
        feedCost.put(90, 625);
        feedCost.put(100, 1750);
    }

    public CharacterPetsGUI(TomatoData data) {
        INSTANCE = this;
        this.data = data;

        setLayout(new BorderLayout());
        petPanel = new JPanel();
        petPanel.add(new Label("Enter Pet Yard to see pets"));
        petPanel.setLayout(new BoxLayout(petPanel, BoxLayout.Y_AXIS));
        petPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        petPanel.add(Box.createVerticalGlue());

        JScrollPane scrollPaneChars = new JScrollPane(petPanel);
        scrollPaneChars.getVerticalScrollBar().setUnitIncrement(40);
        add(scrollPaneChars);

        JPanel bot = new JPanel(new GridLayout(1, 2));
        JButton button = new JButton("Update");
        button.addActionListener(e -> update());

        feed = new JTextField("500");
        bot.add(button);
        bot.add(feed);
        add(bot, BorderLayout.SOUTH);
    }

    private void update() {
        petPanel.removeAll();

        Collection<Pet> values = petList.values().stream().sorted(Comparator.comparingInt(Pet::sort).reversed()).collect(Collectors.toList());
        for (Pet p : values) {
            addPetToPanel(p);
        }
    }

    public static void addPet(ObjectData object) {
        INSTANCE.add(object);
    }

    private void add(ObjectData object) {
        int objectId = object.status.objectId;
        if (petList.containsKey(objectId)) {
            Pet oldPet = petList.get(objectId);
            if (oldPet != null) {
                Stat stat = new Stat(object.status.stats);
                if (
                        oldPet.stat.PET_FIRSTABILITY_POINT_STAT.statValue != stat.PET_FIRSTABILITY_POINT_STAT.statValue ||
                                oldPet.stat.PET_SECONDABILITY_POINT_STAT.statValue != stat.PET_SECONDABILITY_POINT_STAT.statValue ||
                                oldPet.stat.PET_THIRDABILITY_POINT_STAT.statValue != stat.PET_THIRDABILITY_POINT_STAT.statValue
                ) {
                    oldPet.update(object);
                }
            }
            return;
        }

        for (StatData sd : object.status.stats) {
            if (sd.statTypeNum > 80 && sd.statTypeNum < 96) {
                Pet newPet = new Pet(object);

                petList.put(objectId, newPet);
                addPetToPanel(newPet);
                return;
            }
        }
    }

    private void addPetToPanel(Pet pet) {
        JPanel box = CharacterPanelGUI.createMainBox();

        box.setLayout(new GridBagLayout());
//        box.setBorder(BorderFactory.createLineBorder(Color.black));

        GridBagConstraints g = new GridBagConstraints();
        g.gridwidth = 2;
        g.gridheight = 1;
//        g.weightx = 0.3f;
        try {
            int skin = pet.skin();
            BufferedImage img = ImageBuffer.getImage(skin);
            ImageIcon icon = new ImageIcon(img.getScaledInstance(40, 40, Image.SCALE_DEFAULT));
//            JLabel petIcon = new JLabel(pet.name(), icon, JLabel.CENTER);
            JLabel petIcon = new JLabel(icon, JLabel.CENTER);
//            JPanel lab = new JPanel(new BorderLayout());
//            lab.setBorder(BorderFactory.createLineBorder(Color.black));
//            lab.add(petIcon);
            box.add(petIcon, g);

//            characterLabel.setToolTipText(exaltStats(c));
        } catch (Exception e) {
            e.printStackTrace();
        }
        g.weightx = 0.7f;

        pet.updateLabels();
        box.add(pet.getInfoPanel(), g);

        petPanel.add(box);

        validate();
    }

    private Component leftColumn() {
        return null;
    }

    private Component rightColumn() {
        return null;
    }

    public static void updatePet(ObjectData object) {

    }

    public static void clearPets() {
        INSTANCE.petList.clear();
        INSTANCE.petPanel.removeAll();
    }

    private class Pet {
        Stat stat;
        JLabel[] labels = new JLabel[15];
        JPanel info = new JPanel(new GridLayout(3, 5));

        public Pet(ObjectData object) {
            stat = new Stat();
            stat.setStats(object.status.stats);
            for (int i = 0; i < labels.length; i++) {
                labels[i] = new JLabel();
                info.add(labels[i]);
            }
        }

        public JPanel getInfoPanel() {
            return info;
        }

        public String name() {
            if (stat.PET_NAME_STAT == null) return "---";
            try {
                return IdToAsset.objectName(skin());
            } catch (AssetMissingException e) {
                e.printStackTrace();
            }
            return stat.PET_NAME_STAT.stringStatValue;
        }

        public int skin() {
            if (stat.SKIN_ID == null) return 1;
            return stat.SKIN_ID.statValue;
        }

        public int sort() {
            if (stat.PET_MAXABILITYPOWER_STAT == null) return 0;
            return stat.PET_MAXABILITYPOWER_STAT.statValue;
        }

        public void updateLabels() {
            int[] a = new int[9];

            int feedAmount = -1;
            try {
                String s = feed.getText();
                feedAmount = Integer.parseInt(s);
            } catch (NumberFormatException e) {
            }

            int maxLevel = 0;
            int maxing = -1;
            int cost = -1;

            if (stat.PET_MAXABILITYPOWER_STAT != null) maxLevel = stat.PET_MAXABILITYPOWER_STAT.statValue;
            Integer mm = feedMax.get(maxLevel);
            Integer cc = feedCost.get(maxLevel);
            if (mm != null) maxing = mm;
            if (cc != null) cost = cc;

            if (stat.PET_FIRSTABILITY_POINT_STAT != null) a[0] = stat.PET_FIRSTABILITY_POINT_STAT.statValue;
            if (stat.PET_SECONDABILITY_POINT_STAT != null) a[1] = stat.PET_SECONDABILITY_POINT_STAT.statValue;
            if (stat.PET_THIRDABILITY_POINT_STAT != null) a[2] = stat.PET_THIRDABILITY_POINT_STAT.statValue;
            if (stat.PET_FIRSTABILITY_POWER_STAT != null) a[3] = stat.PET_FIRSTABILITY_POWER_STAT.statValue;
            if (stat.PET_SECONDABILITY_POWER_STAT != null) a[4] = stat.PET_SECONDABILITY_POWER_STAT.statValue;
            if (stat.PET_THIRDABILITY_POWER_STAT != null) a[5] = stat.PET_THIRDABILITY_POWER_STAT.statValue;
            if (stat.PET_FIRSTABILITY_TYPE_STAT != null) a[6] = stat.PET_FIRSTABILITY_TYPE_STAT.statValue;
            if (stat.PET_SECONDABILITY_TYPE_STAT != null) a[7] = stat.PET_SECONDABILITY_TYPE_STAT.statValue;
            if (stat.PET_THIRDABILITY_TYPE_STAT != null) a[8] = stat.PET_THIRDABILITY_TYPE_STAT.statValue;

            labels[0].setText(String.format("%s ", petAbilitys.get(a[6])));
            labels[1].setText(String.format("[Level: %d]", a[3]));
            labels[2].setText(String.format("FP: %d", a[0]));

            if (maxing != -1 && feedAmount != -1) {
                int points = a[0];
                int left = maxing - points;
                int count = left / feedAmount;

                if (left > 0) {
                    labels[3].setText(String.format("#: %d", count));
                    labels[4].setText(String.format("F: %d", count * cost));
                } else {
                    labels[3].setText("-");
                    labels[4].setText("-");
                }
            }

            labels[5].setText(String.format("%s ", petAbilitys.get(a[7])));
            labels[6].setText(String.format("[Level: %d]", a[4]));
            labels[7].setText(String.format("FP: %.0f", a[1] / feedMultiplier[1]));
            labels[8].setText("-");
            labels[9].setText("-");

            if (maxing != -1 && feedAmount != -1 && maxLevel >= 50) {
                int points = a[1];
                int left = maxing - points;
                int count = (int) (left / (feedAmount * 0.65f));

                if (left > 0) {
                    labels[8].setText(String.format("#: %d", count));
                    labels[9].setText(String.format("F: %d", count * cost));
                }
            }

            labels[10].setText(String.format("%s ", petAbilitys.get(a[8])));
            labels[11].setText(String.format("[Level: %d]", a[5]));
            labels[12].setText(String.format("FP: %.0f", a[2] / feedMultiplier[2]));
            labels[13].setText("-");
            labels[14].setText("-");

            if (maxing != -1 && feedAmount != -1 && maxLevel >= 90) {
                int points = a[2];
                int left = maxing - points;
                int count = (int) (left / (feedAmount * 0.3f));

                if (left > 0) {
                    labels[13].setText(String.format("#: %d", count));
                    labels[14].setText(String.format("F: %d", count * cost));
                }
            }
        }

        public void update(ObjectData object) {
            stat.setStats(object.status.stats);
            updateLabels();
        }
    }
}
