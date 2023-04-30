package tomato.logic;

import tomato.logic.enums.CharacterClass;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Basic data class to store Character info.
 */
public class RealmCharacter {

    public static HashMap<Integer, int[]> exalts = new HashMap<>();

    public int charId;
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

    /**
     * Simple setter for the class string from the class id.
     */
    public void setClassString() {
        classString = CharacterClass.getName(classNum);
    }

    @Override
    public String toString() {
        return "Character{" +
                "\n   charId=" + charId +
                "\n   classNum=" + classNum +
                "\n   classString=" + classString +
                "\n   level=" + level +
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
                "\n   atk=" + atk +
                "\n   def=" + def +
                "\n   spd=" + spd +
                "\n   dex=" + dex +
                "\n   vit=" + vit +
                "\n   wis=" + wis;
    }
}
