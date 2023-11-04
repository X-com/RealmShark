package tomato.gui.character;

import assets.AssetMissingException;
import assets.IdToAsset;
import assets.ImageBuffer;
import packets.data.ObjectData;
import packets.data.StatData;
import tomato.backend.data.Entity;
import tomato.backend.data.Stat;
import tomato.backend.data.TomatoData;

import javax.swing.*;
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
        petAbilitys.put(402, "A. Close");
        petAbilitys.put(404, "A. Mid");
        petAbilitys.put(405, "A. Far");
        petAbilitys.put(406, "Elec");
        petAbilitys.put(407, "Heal");
        petAbilitys.put(408, "M. Heal");
        petAbilitys.put(409, "Savage");
        petAbilitys.put(410, "Decoy");
        petAbilitys.put(411, "R. Fury");

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
        Stat stat = new Stat(object.status.stats);
        StatData id = stat.PET_INSTANCEID_STAT;
        if (id != null && petList.containsKey(id.statValue)) {
            Pet oldPet = petList.get(id.statValue);
            if (oldPet != null) {
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

                petList.put(newPet.getId(), newPet);
                addPetToPanel(newPet);
                return;
            }
        }
    }

    private void addPetToPanel(Pet pet) {
        JPanel box = CharacterPanelGUI.createMainBox();

        box.setLayout(new GridBagLayout());

        GridBagConstraints g = new GridBagConstraints();
        g.gridwidth = 2;
        g.gridheight = 1;
        try {
            int skin = pet.skin();
            BufferedImage img = ImageBuffer.getImage(skin);
            ImageIcon icon = new ImageIcon(img.getScaledInstance(40, 40, Image.SCALE_DEFAULT));
//            JLabel petIcon = new JLabel(pet.name(), icon, JLabel.CENTER);
            JLabel petIcon = new JLabel(icon, JLabel.CENTER);
            box.add(petIcon, g);
        } catch (Exception e) {
            e.printStackTrace();
        }
        g.weightx = 0.7f;

        pet.updateLabels();
        box.add(pet.getInfoPanel(), g);

        petPanel.add(box);

        validate();
    }

    public static void clearPets() {
        INSTANCE.petList.clear();
        INSTANCE.petPanel.removeAll();
    }

    public static void updateEquipedPet() {
        INSTANCE.addPacketPet();
    }

    private void addPacketPet() {
        Pet newPet = new Pet(data.pet);

        petList.put(newPet.getId(), newPet);
        addPetToPanel(newPet);
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

        public Pet(Entity object) {
            stat = object.stat;
            for (int i = 0; i < labels.length; i++) {
                labels[i] = new JLabel();
                info.add(labels[i]);
            }
        }

        public Integer getId() {
            if (stat.PET_INSTANCEID_STAT == null) return -1;
            return stat.PET_INSTANCEID_STAT.statValue;
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

            for (int i = 0; i < 3; i++) {
                labels[i * 5].setText(String.format("%s ", petAbilitys.get(a[i + 6])));
                labels[i * 5 + 1].setText(String.format("[Level: %d]", a[i + 3]));
                labels[i * 5 + 2].setText(String.format("FP: %d", a[i]));

                if (i == 1 && maxLevel < 50) continue;
                if (i == 2 && maxLevel < 90) continue;

                if (maxing != -1 && feedAmount != -1) {
                    int points = a[i];
                    int left = maxing - points;
                    int count = (int) (left / (feedAmount * feedMultiplier[i]));

                    if (left > 0) {
                        labels[i * 5 + 3].setText(String.format(" Items: %d", count));
                        labels[i * 5 + 4].setText(String.format(" F: %d", count * cost));
                    } else {
                        labels[i * 5 + 3].setText(" -");
                        labels[i * 5 + 4].setText(" -");
                    }
                }
            }
        }

        public void update(ObjectData object) {
            stat.setStats(object.status.stats);
            updateLabels();
        }
    }
}
