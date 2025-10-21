package tomato.gui.myinfo;

import assets.IdToAsset;
import assets.ImageBuffer;
import java.awt.*;
import javax.swing.*;
import packets.data.StatData;
import packets.data.enums.StatType;
import tomato.backend.data.Entity;
import tomato.backend.data.TomatoData;
import tomato.gui.TomatoGUI;
import tomato.realmshark.ParseEnchants;

public class MyInfoGUI extends JPanel {

    private static MyInfoGUI INSTANCE;

    private JLabel[] icons;
    private TomatoData data;
    private Entity player;
    private int[] slots;
    private JTextArea textArea;
    private Entity pet;
    private JCheckBox outOfCombatCheck;
    private JComboBox<String> abilityModeCombo;
    private String selectedAbilityMode = "All";
    private static final float[] petRegenTimeMpHp = {
        10.00f,
        9.35f,
        8.69f,
        8.04f,
        7.39f,
        7.13f,
        6.88f,
        6.62f,
        6.37f,
        6.11f,
        5.94f,
        5.76f,
        5.59f,
        5.42f,
        5.25f,
        5.13f,
        5.01f,
        4.90f,
        4.78f,
        4.67f,
        4.58f,
        4.49f,
        4.40f,
        4.31f,
        4.22f,
        4.14f,
        4.06f,
        3.98f,
        3.90f,
        3.82f,
        3.75f,
        3.69f,
        3.62f,
        3.55f,
        3.48f,
        3.43f,
        3.39f,
        3.34f,
        3.29f,
        3.24f,
        3.19f,
        3.13f,
        3.08f,
        3.03f,
        2.97f,
        2.91f,
        2.85f,
        2.78f,
        2.72f,
        2.66f,
        2.61f,
        2.56f,
        2.51f,
        2.46f,
        2.41f,
        2.38f,
        2.34f,
        2.30f,
        2.26f,
        2.22f,
        2.19f,
        2.15f,
        2.12f,
        2.08f,
        2.05f,
        2.03f,
        2.01f,
        1.99f,
        1.98f,
        1.96f,
        1.93f,
        1.91f,
        1.88f,
        1.85f,
        1.83f,
        1.79f,
        1.74f,
        1.70f,
        1.65f,
        1.61f,
        1.57f,
        1.53f,
        1.50f,
        1.46f,
        1.42f,
        1.42f,
        1.42f,
        1.42f,
        1.42f,
        1.42f,
        1.38f,
        1.34f,
        1.29f,
        1.25f,
        1.21f,
        1.17f,
        1.13f,
        1.08f,
        1.04f,
        1.00f,
    };
    private static final int[] petManaPerLevel = {
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        2,
        2,
        2,
        2,
        2,
        2,
        2,
        2,
        3,
        3,
        3,
        3,
        3,
        3,
        4,
        4,
        4,
        4,
        4,
        5,
        5,
        5,
        5,
        6,
        6,
        6,
        7,
        7,
        7,
        8,
        8,
        8,
        9,
        9,
        9,
        10,
        10,
        11,
        11,
        11,
        12,
        12,
        13,
        13,
        14,
        14,
        15,
        16,
        16,
        17,
        17,
        18,
        19,
        19,
        20,
        21,
        21,
        22,
        23,
        24,
        24,
        25,
        26,
        27,
        28,
        28,
        29,
        30,
        31,
        32,
        33,
        34,
        35,
        36,
        38,
        39,
        40,
        41,
        42,
        44,
        45,
    };
    private static final int[] petHpPerLevel = {
        10,
        10,
        10,
        10,
        10,
        10,
        10,
        10,
        10,
        10,
        10,
        10,
        10,
        10,
        11,
        11,
        11,
        11,
        11,
        11,
        12,
        12,
        12,
        12,
        12,
        13,
        13,
        13,
        14,
        14,
        14,
        14,
        15,
        15,
        16,
        16,
        16,
        17,
        17,
        18,
        18,
        19,
        19,
        20,
        20,
        21,
        21,
        22,
        22,
        23,
        24,
        24,
        25,
        26,
        26,
        27,
        28,
        29,
        29,
        30,
        31,
        32,
        33,
        34,
        35,
        36,
        37,
        38,
        39,
        40,
        41,
        42,
        43,
        45,
        46,
        47,
        48,
        50,
        51,
        53,
        54,
        55,
        57,
        59,
        60,
        62,
        64,
        65,
        67,
        69,
        71,
        73,
        75,
        77,
        79,
        81,
        83,
        85,
        88,
        90,
    };

    public MyInfoGUI(TomatoData data) {
        INSTANCE = this;
        this.data = data;
        setLayout(new BorderLayout());

        JPanel topPanel = topPanel();

        add(topPanel, BorderLayout.NORTH);
        outOfCombatCheck = new JCheckBox("Out of combat");
        outOfCombatCheck.setToolTipText(
            "Double regen bonuses while out of combat"
        );
        outOfCombatCheck.addActionListener(e -> updateMe());

        topPanel.add(outOfCombatCheck);

        textArea = new JTextArea();

        textArea.setEditable(false);
        add(TomatoGUI.createTextArea(textArea, true), BorderLayout.CENTER);
    }

    public static void updatePlayer(Entity player) {
        INSTANCE.player = player;
        INSTANCE.updateMe();
    }

    private void updateMe() {
        if (player == null) return;
        slots = new int[4];
        if (player.stat.get(StatType.DEXTERITY_STAT) == null) {
            System.out.println("nulled on dex stat");
            return;
        }

        int dex = player.stat.get(StatType.DEXTERITY_STAT).statValue;
        int atk = player.stat.get(StatType.ATTACK_STAT).statValue;
        int wis = player.stat.get(StatType.WISDOM_STAT).statValue;
        int exalt = player.stat.get(StatType.EXALTATION_BONUS_DAMAGE).statValue;

        slots[0] = player.stat.get(StatType.INVENTORY_0_STAT).statValue;
        slots[1] = player.stat.get(StatType.INVENTORY_1_STAT).statValue;
        slots[2] = player.stat.get(StatType.INVENTORY_2_STAT).statValue;
        slots[3] = player.stat.get(StatType.INVENTORY_3_STAT).statValue;

        //        String[] enchants = null;
        //        StatData udata = player.stat.get(StatType.UNIQUE_DATA_STRING);
        //        if (udata != null && udata.stringStatValue != null) {
        //            enchants = udata.stringStatValue.split(",");
        //            System.out.println("---");
        //            for (String e : enchants) {
        //                String name = ParseEnchants.parse(e);
        //                if (name.isEmpty()) continue;
        //                System.out.println(name);
        //            }
        //        }

        int dust[] = null;
        try {
            String[] max = player.stat
                .get(StatType.DUST_AMOUNT_STAT)
                .stringStatValue.split(",");
            String[] amount = player.stat
                .get(StatType.DUST_STAT)
                .stringStatValue.split(",");
            dust = new int[] {
                Integer.parseInt(amount[0].split(":")[1]),
                Integer.parseInt(max[0].split(":")[1]),
                Integer.parseInt(amount[4].split(":")[1]),
                Integer.parseInt(max[4].split(":")[1]),
                Integer.parseInt(amount[3].split(":")[1]),
                Integer.parseInt(max[3].split(":")[1]),
            };
        } catch (Exception ignore) {}

        for (int i = 0; i < 4; i++) {
            displayImg(icons[i], slots[i]);
        }

        if (pet == null || pet.stat.get(StatType.SKIN_ID) == null) {
            displayImg(icons[4], 5079);
        } else {
            displayImg(icons[4], pet.stat.get(StatType.SKIN_ID).statValue);
        }

        Weapon w = Equip.get(slots[0]);
        if (w == null) return;
        float exaltDmg = exalt / 1000f;

        StringBuilder sb = new StringBuilder();
        if (dust != null) {
            sb
                .append("Dust Green: ")
                .append(dust[0])
                .append(" / ")
                .append(dust[1]);
            sb.append("   Red: ").append(dust[2]).append(" / ").append(dust[3]);
            sb
                .append("   Purple: ")
                .append(dust[4])
                .append(" / ")
                .append(dust[5]);
            sb.append("\n\n");
        }
        sb.append("Dex: ").append(dex).append("\n");
        sb.append("Atk: ").append(atk).append("\n");
        sb.append("Wis: ").append(wis).append("\n");
        sb.append("Exalt Bonus: ").append(exaltDmg).append("x\n");
        sb.append("\n");
        sb.append(w.displayName != null ? w.displayName : w.name).append("\n");

        //(((Average Weapon Damage per Shot * (0.5 + ATT/50)) - Enemy DEF) * Number of Shots) * ((1.5 + 6.5*(DEX/75)) * Weapon Rate of Fire (in decimal; 100% RoF should be written as 1.00)) = Damage per Second

        int def = 0;
        float total = 0;
        for (Bullet b : w.bullets) {
            float avg = (b.min + b.max) / 2f;
            float dps =
                ((avg * exaltDmg * (0.5f + atk / 50f) - def) * b.numProj) *
                (1.5f + 6.5f * (dex / 75f)) *
                b.rof;
            sb.append(
                String.format(
                    "Bullet: %sx  %s - %s   Rof: %s  Dps: %.1f\n",
                    b.numProj,
                    b.min,
                    b.max,
                    Float.toString(b.rof),
                    dps
                )
            );
            total += dps;
        }
        sb.append(String.format("Weapon %.1f dmg/sec\n", total));
        sb.append("\n");
        if (pet != null) {
            int electric = getPetStat(406);
            int close = getPetStat(402);
            int mid = getPetStat(404);
            int far = getPetStat(405);

            if (electric != -1) {
                int dmg = (int) (4.8f * Math.exp(0.04138f * electric));
                float time = (float) (1.02f * Math.exp(-0.0163 * electric));
                float dps = dmg / time;
                sb.append(
                    String.format("Pet Electric damage: %.2f dmg/sec\n", dps)
                );
                total += dps;
            }
            if (close != -1) {
                int dmg = (int) (6.77f * Math.exp(0.0339f * close));
                float time = (float) (5.17f * Math.exp(-0.0325f * close));
                float dps = dmg / time;
                sb.append(
                    String.format("Pet A.Close damage: %.2f dmg/sec\n", dps)
                );
                total += dps;
            }
            if (mid != -1) {
                int dmg = (int) (4.83f * Math.exp(0.0344f * mid));
                float time = (float) (5.17f * Math.exp(-0.0325f * mid));
                float dps = dmg / time;
                sb.append(
                    String.format("Pet A.Mid damage: %.2f dmg/sec\n", dps)
                );
                total += dps;
            }
            if (far != -1) {
                int dmg = (int) (2.91f * Math.exp(0.0354f * far));
                float time = (float) (5.17f * Math.exp(-0.0325f * far));
                float dps = dmg / time;
                sb.append(
                    String.format("Pet A.Far damage: %.2f dmg/sec\n", dps)
                );
                total += dps;
            }
        }
        sb.append("\n");
        sb.append(String.format("Total damage: %.2f dmg/sec\n", total));

        sb.append("\n");

        int maxMp = 0;
        StatData maxMpStat = player.stat.get(StatType.MAX_MP_STAT);
        if (maxMpStat != null) {
            maxMp = maxMpStat.statValue;
        }

        String[] enchantStringsAll = ParseEnchants.getEnchantStrings(player);

        boolean outOfCombat = (outOfCombatCheck != null &&
            outOfCombatCheck.isSelected());
        float enchantManaRegen =
            ParseEnchants.getManaRegenPerSecondFromEnchants(
                enchantStringsAll,
                maxMp,
                outOfCombat
            );

        float manaRegenBase = wis * 0.12f;
        float manaRegen = manaRegenBase + enchantManaRegen;

        sb.append(
            String.format(
                "Regen: %.2f mana/sec%s\n",
                manaRegen,
                outOfCombat ? " (OOC)" : ""
            )
        );

        float petManaRegen = 0;

        if (pet != null) {
            int level = getPetStat(408);

            if (level != -1) {
                //                int mana = (int) (1.9f * Math.exp(0.0318f * level));
                //                float time = (float) (10f - 4.5f * Math.log10(level));
                int mana = petManaPerLevel[level - 1];
                float time = petRegenTimeMpHp[level - 1];
                petManaRegen = mana / time;
                sb.append(
                    String.format(
                        "Pet M.Heal level: %d   %.2f mana/sec   [mana: %d per %.2f sec]\n",
                        level,
                        petManaRegen,
                        mana,
                        time
                    )
                );
            }
        }

        sb.append(
            String.format(
                "Total mana regen: %.2f mana/sec\n",
                manaRegen + petManaRegen
            )
        );

        // HP regen section (enchants + placeholder for base VIT-based regen)
        int maxHp = 0;
        StatData maxHpStat = player.stat.get(StatType.MAX_HP_STAT);
        if (maxHpStat != null) {
            maxHp = maxHpStat.statValue;
        }
        float hpEnchantRegen = ParseEnchants.getLifeRegenPerSecondFromEnchants(
            enchantStringsAll,
            maxHp,
            outOfCombat
        );
        float hpBaseRegen = 0f; // Base HP regen from VIT not implemented here
        sb.append("\n");
        sb.append(
            String.format(
                "HP Regen (enchants): %.2f hp/sec%s\n",
                hpEnchantRegen,
                outOfCombat ? " (OOC)" : ""
            )
        );
        sb.append(
            String.format(
                "Total HP regen: %.2f hp/sec\n",
                hpBaseRegen + hpEnchantRegen
            )
        );

        Weapon ability = Equip.get(slots[1]);

        if (ability != null) {
            sb
                .append(
                    ability.displayName != null
                        ? ability.displayName
                        : ability.name
                )
                .append("\n");

            sb.append("Damage not implemented");
        }

        sb.append("\n");

        textArea.setText(String.valueOf(sb));
    }

    private int getPetStat(int type) {
        if (
            pet.stat.get(StatType.PET_FIRST_ABILITY_TYPE_STAT).statValue == type
        ) {
            return pet.stat.get(
                StatType.PET_FIRST_ABILITY_POWER_STAT
            ).statValue;
        } else if (
            pet.stat.get(StatType.PET_SECOND_ABILITY_TYPE_STAT).statValue ==
            type
        ) {
            return pet.stat.get(
                StatType.PET_SECOND_ABILITY_POWER_STAT
            ).statValue;
        } else if (
            pet.stat.get(StatType.PET_THIRD_ABILITY_TYPE_STAT).statValue == type
        ) {
            return pet.stat.get(
                StatType.PET_THIRD_ABILITY_POWER_STAT
            ).statValue;
        }

        return -1;
    }

    public void displayImg(JLabel label, int eq) {
        try {
            label.setIcon(ImageBuffer.getOutlinedIcon(eq, 40));
            //            label.setText(IdToAsset.objectName(eq));
            //                icon[i].setToolTipText(String.format("<html>%s<br>%s</html>", IdToAsset.objectName(eq), enchant));
            label.setToolTipText(IdToAsset.objectName(eq));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JPanel topPanel() {
        JPanel top = new JPanel(new FlowLayout());
        top.setAlignmentX(0.3f);
        JPanel equiped = new JPanel(new GridLayout(1, 5));
        icons = new JLabel[5];
        for (int i = 0; i < icons.length; i++) {
            icons[i] = new JLabel();
            icons[i].setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            equiped.add(icons[i]);
        }
        top.add(equiped);
        return top;
    }

    public static void updatePet(Entity pet) {
        INSTANCE.pet = pet;
    }
}
