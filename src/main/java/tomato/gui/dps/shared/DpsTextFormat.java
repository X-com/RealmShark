package tomato.gui.dps.shared;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Shared lightweight text/number formatting helpers for DPS views.
 *
 * - Thread-safe via ThreadLocal DecimalFormats (DecimalFormat is not thread-safe).
 * - Uses a space ' ' as the grouping separator for "grouped" (1 234 567).
 * - Provides simple padding helpers for fixed-width columns.
 */
public final class DpsTextFormat {

    private DpsTextFormat() {
        // no instances
    }

    // Thread-local DecimalFormats
    private static final ThreadLocal<DecimalFormat> GROUPED =
        ThreadLocal.withInitial(() -> {
            DecimalFormatSymbols s = DecimalFormatSymbols.getInstance(
                Locale.ROOT
            );
            s.setGroupingSeparator(' ');
            DecimalFormat df = new DecimalFormat("#,###");
            df.setDecimalFormatSymbols(s);
            df.setGroupingUsed(true);
            df.setMaximumFractionDigits(0);
            df.setMinimumFractionDigits(0);
            return df;
        });

    private static final ThreadLocal<DecimalFormat> P1 =
        ThreadLocal.withInitial(() -> new DecimalFormat("0.0"));
    private static final ThreadLocal<DecimalFormat> P2 =
        ThreadLocal.withInitial(() -> new DecimalFormat("0.00"));
    private static final ThreadLocal<DecimalFormat> P3 =
        ThreadLocal.withInitial(() -> new DecimalFormat("0.000"));

    // General fixed decimal (non-percent specific) formatter(s)

    private static final ThreadLocal<DecimalFormat> F2 =
        ThreadLocal.withInitial(() -> new DecimalFormat("0.00"));
    // Grouped with comma thousands and two decimals (e.g., 12,345.67) for values like DPM
    private static final ThreadLocal<DecimalFormat> F2_GROUPED_COMMA =
        ThreadLocal.withInitial(() -> {
            DecimalFormatSymbols s = DecimalFormatSymbols.getInstance(
                Locale.ROOT
            );
            s.setGroupingSeparator(',');
            s.setDecimalSeparator('.');
            DecimalFormat df = new DecimalFormat("#,##0.00");
            df.setDecimalFormatSymbols(s);
            return df;
        });

    /**
     * Formats an int with space-separated grouping (e.g., 1 234 567).
     */
    public static String grouped(int value) {
        return GROUPED.get().format(value);
    }

    /**
     * Formats a long with space-separated grouping (e.g., 1 234 567).
     */
    public static String grouped(long value) {
        return GROUPED.get().format(value);
    }

    /**
     * Formats a number with 1 decimal (e.g., 12.3).
     */
    public static String percent1(double value) {
        return P1.get().format(value);
    }

    /**
     * Formats a number with 2 decimals (e.g., 12.34).
     */
    public static String percent2(double value) {
        return P2.get().format(value);
    }

    /**
     * Formats a number with 3 decimals (e.g., 12.345).
     */
    public static String percent3(double value) {
        return P3.get().format(value);
    }

    /**

     * Formats a number with 2 decimals for general use (e.g., 12.34).

     * Intended for non-percent values like Damage per Minute.

     */

    public static String fixed2(double value) {
        return F2.get().format(value);
    }

    /**
     * Formats a number with comma grouping and two decimals (e.g., 12,345.67).
     * Use for values like Damage per Minute when punctuation is desired.
     */
    public static String fixed2GroupedComma(double value) {
        return F2_GROUPED_COMMA.get().format(value);
    }

    /**
     * Right-aligns an integer within a fixed width by left-padding spaces, then appends to sb.
     * Example: appendPaddedInt(sb, 42, 5) -> "   42"
     */
    public static void appendPaddedInt(StringBuilder sb, int value, int width) {
        String s = Integer.toString(value);
        for (int i = s.length(); i < width; i++) sb.append(' ');
        sb.append(s);
    }

    /**
     * Left-aligns a string within a fixed width by right-padding spaces, then appends to sb.
     * Example: appendPaddedRight(sb, "Bob", 6) -> "Bob   "
     */
    public static void appendPaddedRight(
        StringBuilder sb,
        String value,
        int width
    ) {
        if (value != null) sb.append(value);
        int len = (value == null) ? 0 : value.length();
        for (int i = len; i < width; i++) sb.append(' ');
    }

    /**

     * Appends a grouped int to the builder (uses a space as thousands separator).

     */

    public static void appendGrouped(StringBuilder sb, int value) {
        sb.append(grouped(value));
    }

    /**

     * Appends a grouped long to the builder (uses a space as thousands separator).

     */

    public static void appendGrouped(StringBuilder sb, long value) {
        sb.append(grouped(value));
    }

    /**
     * Formats a percentage with a tiny-threshold rule to avoid showing 0.0% for
     * very small non-zero values.
     *
     * Rule:
     * - 0 < pct < 0.1 => "< 0.1%"
     * - otherwise => one-decimal formatting + "%" (e.g., "0.1%")
     */
    public static String formatPercentWithTinyThreshold(double pct) {
        if (pct > 0.0 && pct < 0.1) {
            return "< 0.1%";
        }
        return percent1(pct) + "%";
    }
}
