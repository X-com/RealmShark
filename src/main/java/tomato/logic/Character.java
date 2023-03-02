package tomato.logic;

import java.util.Arrays;
import java.util.HashMap;

public class Character {

    public static HashMap<Integer, int[]> exalts = new HashMap<>();

    public int classNum;
    public String classString;
    public int level;
    public int skin;
    public long exp;
    public long fame;
    public boolean seasonal;
    public boolean backpack;
    public boolean qs3;

    public int[] equipment;
    public String[] equipQS;
    public String date;

    public int hp;
    public int mp;
    public int atk;
    public int def;
    public int spd;
    public int dex;
    public int vit;
    public int wis;

    public void setClassString() {
        classString = classType(classNum);
    }

    public static String classType(int c) {
        switch (c) {
            case 768:
                return "Rogue";
            case 775:
                return "Archer";
            case 782:
                return "Wizard";
            case 784:
                return "Priest";
            case 797:
                return "Warrior";
            case 798:
                return "Knight";
            case 799:
                return "Paladin";
            case 800:
                return "Assassin";
            case 801:
                return "Necromancer";
            case 802:
                return "Huntress";
            case 803:
                return "Mystic";
            case 804:
                return "Trickster";
            case 805:
                return "Sorcerer";
            case 806:
                return "Ninja";
            case 785:
                return "Samurai";
            case 796:
                return "Bard";
            case 817:
                return "Summoner";
            case 818:
                return "Kensei";
        }
        return "";
    }

    @Override
    public String toString() {
        return "Character{" +
                "\n   skin=" + skin +
                "\n   exp=" + exp +
                "\n   fame=" + fame +
                "\n   seasonal=" + seasonal +
                "\n   backpack=" + backpack +
                "\n   qs3=" + qs3 +
                "\n   equipment=" + Arrays.toString(equipment) +
                "\n   equipQS=" + Arrays.toString(equipQS) +
                "\n   date=" + date +
                "\n   hp=" + hp +
                "\n   mp=" + mp +
                "\n   att=" + atk +
                "\n   def=" + def +
                "\n   spd=" + spd +
                "\n   dex=" + dex +
                "\n   vit=" + vit +
                "\n   wis=" + wis;
    }
}
