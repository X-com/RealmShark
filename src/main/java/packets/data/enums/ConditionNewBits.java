package packets.data.enums;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public enum ConditionNewBits implements Serializable {
    SLOWED_IMMUNE(0x1),
    DAZED_IMMUNE(0x2),
    PARALYZED_IMMUNE(0x4),
    PETRIFIED(0x8),
    PETRIFIED_IMMUNE(0x10),
    PET_STASIS(0x20),
    CURSE(0x40),
    CURSE_IMMUNE(0x80),
    HEALTH_BOOST(0x100),
    MANA_BOOST(0x200),
    ATTACK_BOOST(0x400),
    DEFENSE_BOOST(0x800),
    SPEED_BOOST(0x1000),
    VITALITY_BOOST(0x2000),
    WISDOM_BOOST(0x4000),
    DEXTERITY_BOOST(0x8000),
    SILENCED(0x10000),
    EXPOSED(0x20000),
    ENERGIZED(0x40000),
    HEALTH_DEBUFF(0x80000),
    MANA_DEBUFF(0x100000),
    ATTACK_DEBUFF(0x200000),
    DEFENSE_DEBUFF(0x400000),
    SPEED_DEBUFF(0x800000),
    VITALITY_DEBUFF(0x1000000),
    WISDOM_DEBUFF(0x2000000),
    DEXTERITY_DEBUFF(0x4000000),
    INSPIRED(0x8000000),
    DRUID_TRANSFORMED(0x10000000),
    DROUGHT(0x20000000),
    LETHAL_STRIKE(0x40000000),
    UNKNOWN_31(0x80000000);

    private final int bitMask;

    ConditionNewBits(int i) {
        bitMask = i;
    }

    public static ConditionNewBits[] getEffects(int effects) {
        ArrayList<ConditionNewBits> list = new ArrayList<>();
        for (ConditionNewBits e : ConditionNewBits.values()) {
            if ((effects & e.bitMask) != 0) {
                list.add(e);
            }
        }

        return list.toArray(new ConditionNewBits[0]);
    }

    public static String effectsToString(int effects) {
        return Arrays.toString(getEffects(effects));
    }

    public boolean effect(int mask) {
        return (bitMask & mask) != 0;
    }

    public int value() {
        return bitMask;
    }
}
