package tomato.logic;

import tomato.logic.enums.CharacterClass;

public class CharacterData {

    public static int[] statMissing(Character c, int[] missing) {
        missing[0] += (int) Math.ceil((CharacterClass.getLife(c.classNum) - c.hp) / 5.0);
        missing[1] += (int) Math.ceil((CharacterClass.getMana(c.classNum) - c.mp) / 5.0);
        missing[2] += CharacterClass.getAtk(c.classNum) - c.atk;
        missing[3] += CharacterClass.getDef(c.classNum) - c.def;
        missing[4] += CharacterClass.getSpd(c.classNum) - c.spd;
        missing[5] += CharacterClass.getDex(c.classNum) - c.dex;
        missing[6] += CharacterClass.getVit(c.classNum) - c.vit;
        missing[7] += CharacterClass.getWis(c.classNum) - c.wis;

        return missing;
    }

    public static int statsMaxed(Character c) {
        int outof8 = 0;
        if (CharacterClass.getLife(c.classNum) == c.hp) outof8++;
        if (CharacterClass.getMana(c.classNum) == c.mp) outof8++;
        if (CharacterClass.getAtk(c.classNum) == c.atk) outof8++;
        if (CharacterClass.getDef(c.classNum) == c.def) outof8++;
        if (CharacterClass.getSpd(c.classNum) == c.spd) outof8++;
        if (CharacterClass.getDex(c.classNum) == c.dex) outof8++;
        if (CharacterClass.getVit(c.classNum) == c.vit) outof8++;
        if (CharacterClass.getWis(c.classNum) == c.wis) outof8++;

        return outof8;
    }
}
