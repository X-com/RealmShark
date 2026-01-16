package tomato.gui.character;

import assets.IdToAsset;
import assets.ImageBuffer;
import java.awt.*;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Collectors;
import javax.swing.*;
import packets.data.ObjectData;
import packets.data.StatData;
import packets.data.enums.StatType;
import tomato.backend.data.Entity;
import tomato.backend.data.Stat;
import tomato.backend.data.TomatoData;

public class CharacterPetsGUI extends JPanel {

    private static CharacterPetsGUI INSTANCE;

    private final TomatoData data;
    private HashMap<Integer, Pet> petList = new HashMap<>();
    private final JPanel petPanel;
    private JTextField feed;
    private static final float[] feedMultiplier = { 1f, 0.65f, 0.3f };

    private static final HashMap<Integer, String> petAbilitys = new HashMap<>();
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

        Collection<Pet> values = petList
            .values()
            .stream()
            .sorted(Comparator.comparingInt(Pet::sort).reversed())
            .collect(Collectors.toList());
        for (Pet p : values) {
            addPetToPanel(p);
        }
    }

    public static void addPet(ObjectData object) {
        if (INSTANCE == null) return; // Tab is disabled
        INSTANCE.add(object);
    }

    private void add(ObjectData object) {
        Stat stat = new Stat(object.status.stats);
        StatData id = stat.get(StatType.PET_INSTANCE_ID_STAT);
        if (id != null && petList.containsKey(id.statValue)) {
            Pet oldPet = petList.get(id.statValue);
            if (oldPet != null) {
                if (
                    oldPet.stat.get(
                            StatType.PET_FIRST_ABILITY_POINT_STAT
                        ).statValue !=
                        stat.get(
                            StatType.PET_FIRST_ABILITY_POINT_STAT
                        ).statValue ||
                    oldPet.stat.get(
                        StatType.PET_SECOND_ABILITY_POINT_STAT
                    ).statValue !=
                    stat.get(
                        StatType.PET_SECOND_ABILITY_POINT_STAT
                    ).statValue ||
                    oldPet.stat.get(
                        StatType.PET_THIRD_ABILITY_POINT_STAT
                    ).statValue !=
                    stat.get(StatType.PET_THIRD_ABILITY_POINT_STAT).statValue
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
        int skin = pet.skin();
        ImageIcon petImage = ImageBuffer.getOutlinedIcon(skin, 40);
        JLabel petIcon = new JLabel(
            petImage != null ? petImage : new ImageIcon(),
            JLabel.CENTER
        );
        box.add(petIcon, g);
        g.weightx = 0.7f;

        pet.updateLabels();
        box.add(pet.getInfoPanel(), g);

        petPanel.add(box);

        validate();
    }

    public static void clearPets() {
        if (INSTANCE == null) return; // Tab is disabled
        INSTANCE.petList.clear();
        INSTANCE.petPanel.removeAll();
    }

    public static void updateEquipedPet() {
        if (INSTANCE == null) return; // Tab is disabled
        INSTANCE.addPacketPet();
    }

    private void addPacketPet() {
        if (data.pet == null) {
            return;
        }
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
            if (stat.get(StatType.PET_INSTANCE_ID_STAT) == null) return -1;
            return stat.get(StatType.PET_INSTANCE_ID_STAT).statValue;
        }

        public JPanel getInfoPanel() {
            return info;
        }

        public String name() {
            if (stat.get(StatType.PET_NAME_STAT) == null) return "---";
            return IdToAsset.objectName(skin());
        }

        public int skin() {
            if (stat.get(StatType.SKIN_ID) == null) return 1;
            return stat.get(StatType.SKIN_ID).statValue;
        }

        public int sort() {
            if (stat.get(StatType.PET_MAX_ABILITY_POWER_STAT) == null) return 0;
            return stat.get(StatType.PET_MAX_ABILITY_POWER_STAT).statValue;
        }

        public void updateLabels() {
            int[] a = new int[9];

            int feedAmount = -1;
            try {
                String s = feed.getText();
                feedAmount = Integer.parseInt(s);
            } catch (NumberFormatException e) {}

            int maxLevel = 0;
            int cost = -1;

            if (
                stat.get(StatType.PET_MAX_ABILITY_POWER_STAT) != null
            ) maxLevel = stat.get(
                StatType.PET_MAX_ABILITY_POWER_STAT
            ).statValue;
            Integer cc = feedCost.get(maxLevel);
            if (cc != null) cost = cc;

            if (stat.get(StatType.PET_FIRST_ABILITY_POINT_STAT) != null) a[0] =
                stat.get(StatType.PET_FIRST_ABILITY_POINT_STAT).statValue;
            if (stat.get(StatType.PET_SECOND_ABILITY_POINT_STAT) != null) a[1] =
                stat.get(StatType.PET_SECOND_ABILITY_POINT_STAT).statValue;
            if (stat.get(StatType.PET_THIRD_ABILITY_POINT_STAT) != null) a[2] =
                stat.get(StatType.PET_THIRD_ABILITY_POINT_STAT).statValue;
            if (stat.get(StatType.PET_FIRST_ABILITY_POWER_STAT) != null) a[3] =
                stat.get(StatType.PET_FIRST_ABILITY_POWER_STAT).statValue;
            if (stat.get(StatType.PET_SECOND_ABILITY_POWER_STAT) != null) a[4] =
                stat.get(StatType.PET_SECOND_ABILITY_POWER_STAT).statValue;
            if (stat.get(StatType.PET_THIRD_ABILITY_POWER_STAT) != null) a[5] =
                stat.get(StatType.PET_THIRD_ABILITY_POWER_STAT).statValue;
            if (stat.get(StatType.PET_FIRST_ABILITY_TYPE_STAT) != null) a[6] =
                stat.get(StatType.PET_FIRST_ABILITY_TYPE_STAT).statValue;
            if (stat.get(StatType.PET_SECOND_ABILITY_TYPE_STAT) != null) a[7] =
                stat.get(StatType.PET_SECOND_ABILITY_TYPE_STAT).statValue;
            if (stat.get(StatType.PET_THIRD_ABILITY_TYPE_STAT) != null) a[8] =
                stat.get(StatType.PET_THIRD_ABILITY_TYPE_STAT).statValue;

            for (int i = 0; i < 3; i++) {
                String abilityName = petAbilitys.get(a[i + 6]);
                int abilityLevel = a[i + 3];
                int fp = (int) (a[i] / feedMultiplier[i]);

                labels[i * 5].setText(String.format("%s ", abilityName));
                labels[i * 5 + 1].setText(
                    String.format("[Level: %d]", abilityLevel)
                );
                labels[i * 5 + 2].setText(String.format("FP: %d", fp));

                if (i == 1 && maxLevel < 50) continue;
                if (i == 2 && maxLevel < 90) continue;

                if (feedAmount != -1) {
                    int nextLevelPet = Math.min(abilityLevel + 1, maxLevel);
                    int maxing = (int) (((20 / feedMultiplier[i]) *
                            (Math.pow(1.08, maxLevel - 1) - 1)) /
                        (1.08 - 1));
                    int levelMax = (int) (((20 / feedMultiplier[i]) *
                            (Math.pow(1.08, nextLevelPet - 1) - 1)) /
                        (1.08 - 1));
                    int leftFullMax = maxing - fp;
                    int countFullMax = (int) Math.ceil(
                        (double) leftFullMax / feedAmount
                    );
                    int leftLevelMax = levelMax - fp;
                    int countLevelMax = (int) Math.ceil(
                        (double) leftLevelMax / feedAmount
                    );

                    String s = "<html>";
                    s += "Feed Points " + fp + " / " + maxing;
                    s += "<br>";
                    s += "Next Level " + nextLevelPet;
                    if (abilityLevel != maxLevel) {
                        s += "<br>";
                        s += "Feed Missing " + leftLevelMax;
                        s += "<br>";
                        s += " Items needed " + countLevelMax;
                        s += "<br>";
                        s += " Fame needed " + countLevelMax * cost;
                    }
                    s += "</html>";
                    labels[i * 5 + 2].setToolTipText(s);

                    if (leftFullMax > 0) {
                        labels[i * 5 + 3].setText(
                            String.format(" N: %d", countFullMax)
                        );
                        labels[i * 5 + 3].setToolTipText(
                            "Number of items to max pet"
                        );
                        labels[i * 5 + 4].setText(
                            String.format(" F: %d", countFullMax * cost)
                        );
                        labels[i * 5 + 4].setToolTipText(
                            "Fame needed to max pet"
                        );
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
