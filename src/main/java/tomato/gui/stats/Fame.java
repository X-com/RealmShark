package tomato.gui.stats;

import java.util.Objects;

/**
 * Represents a fame value at a specific point in time.
 * Immutable data class for tracking fame progression.
 */
public class Fame {

    private final double fame;
    private final long time;

    public Fame(double fame, long time) {
        this.fame = fame;
        this.time = time;
    }

    public double getFame() {
        return fame;
    }

    public long getTime() {
        return time;
    }

    /**
     * Calculate fame gained between two Fame instances
     */
    public double fameGainedSince(Fame other) {
        return this.fame - other.fame;
    }

    /**
     * Calculate time difference in milliseconds between two Fame instances
     */
    public long timeDifferenceSince(Fame other) {
        return this.time - other.time;
    }

    /**
     * Calculate fame per minute between two Fame instances
     */
    public double famePerMinuteSince(Fame other) {
        long timeDiff = timeDifferenceSince(other);
        if (timeDiff <= 0) {
            return 0.0;
        }
        double fameGain = fameGainedSince(other);
        return fameGain / (timeDiff / 60000.0);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Fame other = (Fame) obj;
        return Double.compare(other.fame, fame) == 0 && time == other.time;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fame, time);
    }

    @Override
    public String toString() {
        return String.format("Fame{fame=%.1f, time=%d}", fame, time);
    }
}
