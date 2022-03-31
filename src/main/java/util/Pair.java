package util;

public class Pair<A, B> {

    private final A fst;
    private final B snd;

    public Pair(A fst, B snd) {
        this.fst = fst;
        this.snd = snd;
    }

    public A left() {
        return fst;
    }

    public B right() {
        return snd;
    }

    public String toString() {
        return "Pair[" + fst + "," + snd + "]";
    }
}
