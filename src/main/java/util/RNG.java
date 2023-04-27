package util;

public class RNG {
    private long seed;

    public RNG(long seed) {
        this.seed = seed;
    }

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
}