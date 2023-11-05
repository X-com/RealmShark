package tomato.gui.mydmg;

import assets.IdToAsset;
import assets.ImageBuffer;
import tomato.backend.data.Entity;
import tomato.backend.data.TomatoData;
import tomato.gui.TomatoGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class MyDamageGUI extends JPanel {

    private static MyDamageGUI INSTANCE;

    private JLabel[] icons;
    private TomatoData data;
    private Entity player;
    private int[] slots;
    private JTextArea textArea;
    private Entity pet;

    public MyDamageGUI(TomatoData data) {
        INSTANCE = this;
        this.data = data;
        setLayout(new BorderLayout());

        JPanel topPanel = topPanel();
        add(topPanel, BorderLayout.NORTH);

        textArea = new JTextArea();
        textArea.setEditable(false);
        add(TomatoGUI.createTextArea(textArea), BorderLayout.CENTER);
    }

    public static void updatePlayer(Entity player) {
        INSTANCE.player = player;
        INSTANCE.updateMe();
    }

    private void updateMe() {
        if (player == null) return;
        slots = new int[4];
        if (player.stat.DEXTERITY_STAT == null) {
            System.out.println("nulled on dex stat");
            return;
        }
        int dex = player.stat.DEXTERITY_STAT.statValue;
        int atk = player.stat.ATTACK_STAT.statValue;
        int wis = player.stat.WISDOM_STAT.statValue;
        int exalt = player.stat.EXALTATION_BONUS_DAMAGE.statValue;

        slots[0] = player.stat.INVENTORY_0_STAT.statValue;
        slots[1] = player.stat.INVENTORY_1_STAT.statValue;
        slots[2] = player.stat.INVENTORY_2_STAT.statValue;
        slots[3] = player.stat.INVENTORY_3_STAT.statValue;

        for (int i = 0; i < 4; i++) {
            displayImg(icons[i], slots[i]);
        }

        if (pet == null || pet.stat.SKIN_ID == null) {
            displayImg(icons[4], 5079);
        } else {
            displayImg(icons[4], pet.stat.SKIN_ID.statValue);
        }
        icons[4].setToolTipText("Enter Pet Yard to update pet");

        Weapon w = Equip.get(slots[0]);
        float exaltDmg = exalt / 1000f;

        StringBuilder sb = new StringBuilder();
        sb.append("Dex: ").append(dex).append("\n");
        sb.append("Atk: ").append(atk).append("\n");
        sb.append("Wis: ").append(wis).append("\n");
        sb.append("Exalt Bonus: ").append(exaltDmg).append("x\n");
        sb.append("\n");
        sb.append(w.name).append("\n");

        //(((Average Weapon Damage per Shot * (0.5 + ATT/50)) - Enemy DEF) * Number of Shots) * ((1.5 + 6.5*(DEX/75)) * Weapon Rate of Fire (in decimal; 100% RoF should be written as 1.00)) = Damage per Second

        int def = 0;
        float total = 0;
        for (Bullet b : w.bullets) {
            float avg = (b.min + b.max) / 2f;
            float dps = ((avg * exaltDmg * (0.5f + atk / 50f) - def) * b.numProj) * (1.5f + 6.5f * (dex / 75f)) * b.rof;
            sb.append(String.format("Bullet: %sx  %s - %s   Rof: %s  %.1f\n", b.numProj, b.min, b.max, Float.toString(b.rof), dps));
            total += dps;
        }
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
                sb.append(String.format("Pet Electric damage: %.2f dmg/sec\n", dps));
                total += dps;
            }
            if (close != -1) {
                int dmg = (int) (6.77f * Math.exp(0.0339f * close));
                float time = (float) (5.17f * Math.exp(-0.0325f * close));
                float dps = dmg / time;
                sb.append(String.format("Pet A.Close damage: %.2f dmg/sec\n", dps));
                total += dps;
            }
            if (mid != -1) {
                int dmg = (int) (4.83f * Math.exp(0.0344f * mid));
                float time = (float) (5.17f * Math.exp(-0.0325f * mid));
                float dps = dmg / time;
                sb.append(String.format("Pet A.Mid damage: %.2f dmg/sec\n", dps));
                total += dps;
            }
            if (far != -1) {
                int dmg = (int) (2.91f * Math.exp(0.0354f * far));
                float time = (float) (5.17f * Math.exp(-0.0325f * far));
                float dps = dmg / time;
                sb.append(String.format("Pet A.Far damage: %.2f dmg/sec\n", dps));
                total += dps;
            }
        }
        sb.append("\n");
        sb.append(String.format("Total damage: %.2f dmg/sec\n", total));

        sb.append("\n");
        float manaRegen = wis * 0.12f;
        sb.append(String.format("Regen: %.2f mana/sec\n", manaRegen));
        float petManaRegen = 0;
        if (pet != null) {
            int level = getPetStat(408);

            if (level != -1) {
                int mana = (int) (1.9f * Math.exp(0.0318f * level));
                float time = (float) (10f - 4.5f * Math.log10(level));
                petManaRegen = mana / time;
                sb.append(String.format("Pet M.Heal level: %d   %.2f mana/sec   [mana: %d per %.2f sec]\n", level, petManaRegen, mana, time));
            }
        }

        sb.append(String.format("Total mana regen: %.2f mana/sec\n", manaRegen + petManaRegen));

        Weapon ability = Equip.get(slots[1]);
        sb.append("\n");
        sb.append(ability.displayName != null ? ability.displayName : ability.name).append("\n");
        sb.append("Damage not implemented");

        sb.append("\n");

        textArea.setText(String.valueOf(sb));
    }

    private int getPetStat(int type) {
        if (pet.stat.PET_FIRSTABILITY_TYPE_STAT.statValue == type) {
            return pet.stat.PET_FIRSTABILITY_POWER_STAT.statValue;
        } else if (pet.stat.PET_SECONDABILITY_TYPE_STAT.statValue == type) {
            return pet.stat.PET_SECONDABILITY_POWER_STAT.statValue;
        } else if (pet.stat.PET_THIRDABILITY_TYPE_STAT.statValue == type) {
            return pet.stat.PET_THIRDABILITY_POWER_STAT.statValue;
        }

        return -1;
    }

    public void displayImg(JLabel label, int eq) {
        try {
            BufferedImage img;
            if (eq == -1) {
                img = ImageBuffer.getEmptyImg();
            } else {
                img = ImageBuffer.getImage(eq);
            }
            label.setIcon(new ImageIcon(img.getScaledInstance(40, 40, Image.SCALE_DEFAULT)));
//            label.setText(IdToAsset.objectName(eq));
//                icon[i].setToolTipText(String.format("<html>%s<br>%s</html>", IdToAsset.objectName(eq), enchant));
            label.setToolTipText(IdToAsset.objectName(eq));
        } catch (Exception e) {
//            e.printStackTrace();
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
        INSTANCE.updateMe();
    }
}
