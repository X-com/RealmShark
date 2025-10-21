package tomato.gui.stats;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Shared formatting helpers for stats views.
 *
 * Centralizes common number and time formatting so the UI stays consistent
 * and logic isn't duplicated across panels.
 */
public final class Formatters {

    // Common date/time patterns used across the UI
    public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter TIME_SHORT = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter DATE_TIME_COMPACT = DateTimeFormatter.ofPattern("yyyy/MM/dd-HH:mm:ss");

    private Formatters() {
        // no instances
    }

    // -----------------------
    // Number formatting
    // -----------------------

    /**
     * If the value is an integer, render without decimals.
     * Otherwise, render with the raw double representation (no rounding).
     * Mirrors legacy behavior used in FameTablePanel.
     */
    public static String formatNumberExact(double number) {
        if (!Double.isFinite(number)) {
            return String.valueOf(number);
        }
        return number == (long) number
            ? String.format(Locale.ROOT, "%d", (long) number)
            : String.valueOf(number);
    }

    /**
     * Render a double with a fixed number of decimal places.
     */
    public static String formatNumber(double number, int decimals) {
        if (!Double.isFinite(number)) {
            return String.valueOf(number);
        }
        if (decimals < 0) decimals = 0;
        return String.format(Locale.ROOT, "%." + decimals + "f", number);
    }

    /**
     * Render Fame per hour with 2 decimals by default.
     */
    public static String formatFamePerHour(double famePerHour) {
        return formatNumber(famePerHour, 2);
    }

    /**
     * Render Fame per minute with 2 decimals by default.
     */
    public static String formatFamePerMinute(double famePerMinute) {
        return formatNumber(famePerMinute, 2);
    }

    /**
     * Render a fame delta with the given precision (useful for "Session Gain" or map fame tables).
     */
    public static String formatFame(double fame, int decimals) {
        return formatNumber(fame, decimals);
    }

    // -----------------------
    // Time + duration formatting
    // -----------------------

    /**
     * Format an epoch millis timestamp using the local system zone and "yyyy-MM-dd HH:mm:ss".
     */
    public static String formatTimestamp(long epochMillis) {
        return DATE_TIME.format(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()));
    }

    /**
     * Format current local time as "HH:mm:ss".
     */
    public static String formatNowShort() {
        return TIME_SHORT.format(Instant.now().atZone(ZoneId.systemDefault()));
    }

    /**
     * Format current local date-time as "yyyy/MM/dd-HH:mm:ss".
     */
    public static String formatNowCompact() {
        return DATE_TIME_COMPACT.format(Instant.now().atZone(ZoneId.systemDefault()));
    }

    /**
     * Format a duration (milliseconds) as HH:mm:ss.
     * Negative values are clamped to 0 for display.
     */
    public static String formatDurationHMS(long durationMillis) {
        if (durationMillis < 0) durationMillis = 0;
        Duration d = Duration.ofMillis(durationMillis);
        long hours = d.toHours();
        long minutes = d.minusHours(hours).toMinutes();
        long seconds = d.minusHours(hours).minusMinutes(minutes).getSeconds();
        return String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * Convenience for converting fame per hour to per minute.
     */
    public static double toPerMinute(double perHour) {
        return perHour / 60.0;
    }

    /**
     * Convenience for converting fame per minute to per hour.
     */
    public static double toPerHour(double perMinute) {
        return perMinute * 60.0;
    }
}
