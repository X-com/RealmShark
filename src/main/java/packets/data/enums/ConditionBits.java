package packets.data.enums;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public enum ConditionBits implements Serializable {
    DEAD(0x1),
    QUIET(0x2),
    WEAK(0x4),
    SLOWED(0x8),
    SICK(0x10),
    DAZED(0x20),
    STUNNED(0x40),
    BLIND(0x80),
    HALLUCINATING(0x100),
    DRUNK(0x200),
    CONFUSED(0x400),
    STUNIMMUME(0x800),
    INVISIBLE(0x1000),
    PARALYZED(0x2000),
    SPEEDY(0x4000),
    BLEEDING(0x8000),
    ARMORBREAKIMMUNE(0x10000),
    HEALING(0x20000),
    DAMAGING(0x40000),
    BERSERK(0x80000),
    INCOMBAT(0x100000),
    STASIS(0x200000),
    STASISIMMUNE(0x400000),
    INVINCIBLE(0x800000),
    INVULNERABLE(0x1000000),
    ARMORED(0x2000000),
    ARMORBROKEN(0x4000000),
    HEXED(0x8000000),
    NINJASPEEDY(0x10000000),
    UNSTABLE(0x20000000),
    DARKNESS(0x40000000),
    UNKNOWN_31(0x80000000);

    private final int bitMask;

    public static void main(String[] args) {
        for (ConditionBits o : values()) {
            System.out.printf("0x%x\n", (1 << o.bitMask-1));
        }
    }

    ConditionBits(int i) {
        bitMask = i;
    }

    public static ConditionBits[] getEffects(int effects) {
        ArrayList<ConditionBits> list = new ArrayList<>();
        for (ConditionBits e : ConditionBits.values()) {
            if ((effects & e.bitMask) != 0) {
                list.add(e);
            }
        }

        return list.toArray(new ConditionBits[0]);
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
