package util;

/**
 * Basic pair class
 *
 * @param <A> Type of objects to the left.
 * @param <B> Type of objects to the right.
 */
public class Pair<A, B> {

    private final A fst;
    private final B snd;

    /**
     * Constructor initilizing the pair
     *
     * @param fst Left object
     * @param snd Right object
     */
    public Pair(A fst, B snd) {
        this.fst = fst;
        this.snd = snd;
    }

    /**
     * Returns the left object
     *
     * @return The left object
     */
    public A left() {
        return fst;
    }

    /**
     * Returns the right object
     *
     * @return The right object
     */
    public B right() {
        return snd;
    }

    /**
     * toString of the pair
     */
    public String toString() {
        return "Pair[" + fst + "," + snd + "]";
    }
}
