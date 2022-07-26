package example.damagecalc;

public class RNG {
    private long seed; // this value is given by MapInfoPacket:seed

    public RNG(long seed) {
        this.seed = seed;
    }
    // dungeons usually have the same seed

    public long next() {
        long right16 = (seed >> 16);
        right16 *= 0x41A7;
        long left = seed & 0xFFFF;
        left *= 0x41A7;
        long right15 = right16 >> 15;
        long rightAnd = (right16 & 0x7FFF) << 16;

        long sum = left + rightAnd + right15;
        long finalValue = Integer.toUnsignedLong((int) sum - 0x7FFFFFFF);
        if (sum <= 0x7FFFFFFF) {
            finalValue = sum;
        }
        seed = finalValue;

        return finalValue;
    }

//    public static void main(String[] args) {
//        float multiplier = 1.9f; // this value is given by POKEIKCJILC__NKFPCGAENCF
//        float baseWeaponDmg = 150f; // ray katana
//        long rng = new RNG().next();
//        float f = attackMultiplier();
////        System.out.println((190 + rng % (215-190)) * multiplier);
//        System.out.println(f * baseWeaponDmg);
//    }

    private static float attackMultiplier() {
        boolean weakEffect = false;
        float attack = 79;
        boolean isDamaging = false;
        float exaltationDamageMultiplier = 110f;

        if (weakEffect) {
            return 0.5f;
        }
        float number = (attack + 25) * 0.02f;
        if (isDamaging) {
            number *= 1.25;
        }
        return number * exaltationDamageMultiplier / 100;
    }
}