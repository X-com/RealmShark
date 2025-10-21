package tomato.gui.dps;

import java.util.HashSet;
import java.util.Locale;
import tomato.backend.data.Entity;
import tomato.realmshark.enums.CharacterClass;

/**
 * Centralized filter/highlight logic for DPS views.
 *
 * Semantics:
 * - filter == 0 => disabled
 * - filter == 1 => "Filter" mode (only show matching entries)
 * - filter == 2 => "Highlight" mode (show all, mark matching entries)
 *
 * Matching is true if ANY of the following are true (in order):
 * - My Guild matches (when enabled)
 * - My Class matches (when enabled)
 * - Name is in filterNames
 * - Guild is in filterGuilds
 * - Class type is in filterClasses
 */
public final class Filter {

    // Preset name (purely informational for UI)
    public static String name;

    // 0: disabled, 1: filter, 2: highlight
    public static int filter = 1;

    // "My ..." toggles (driven by part-4 fields of the preset)
    public static boolean myGuildFilter;
    public static boolean myClassFilter;

    // Matching sets (lowercased for names/guilds)
    public static final HashSet<String> filterNames = new HashSet<>();
    public static final HashSet<String> filterGuilds = new HashSet<>();
    public static final HashSet<Integer> filterClasses = new HashSet<>();

    private Filter() {
        // no instances
    }

    /**
     * Returns the filter decision for the given owner vs. the current player.
     * - 0 => NOT matched (in filter mode, row is hidden; in highlight mode, normal row)
     * - 1 => matched while in filter mode (row is shown)
     * - 2 => matched while in highlight mode (row is shown and highlighted)
     */

    public static int filter(Entity owner, Entity player) {
        if (filter == 0 || owner == null) return 0;

        int classType = owner.objectType;

        String ownerName = lower(owner.name());

        String ownerGuild = lower(owner.getStatGuild());

        String myGuild = (player != null) ? lower(player.getStatGuild()) : "";

        // Only perform player-dependent checks when player is available
        if (player != null) {
            if (
                myGuildFilter &&
                !myGuild.isEmpty() &&
                myGuild.equals(ownerGuild)
            ) {
                return filter;
            } else if (myClassFilter && player.objectType == classType) {
                return filter;
            }
        }

        // Class/name/guild filters work even when player is null
        if (!ownerName.isEmpty() && filterNames.contains(ownerName)) {
            return filter;
        } else if (!ownerGuild.isEmpty() && filterGuilds.contains(ownerGuild)) {
            return filter;
        } else if (filterClasses.contains(classType)) {
            return filter;
        }

        return 0;
    }

    /**
     * True when the current mode is "Filter" (1). In this mode, only matching entries are shown.
     * When false, the mode is "Highlight" (2) or disabled (0).
     */
    public static boolean shouldFilter() {
        return filter == 1;
    }

    /**
     * Disables all filtering/highlighting (filter == 0).
     */
    public static void disable() {
        filter = 0;
    }

    /**
     * Parses and applies a serialized filter preset.
     *
     * Format (comma-separated fields, with "-" to advance to the next section):
     * - Part 0: preset name
     * - Part 1: mode flags: "F" => filter(1), "H" => highlight(2)
     * - Part 2: names (multiple entries)
     * - Part 3: guilds (multiple entries)
     * - Part 4: flags and class toggles:
     *     index 0 => "My Guild" (1/0)
     *     index 1 => "My Class" (1/0)
     *     index 2+ => class toggles aligned to CharacterClass.CHAR_CLASS_LIST[index-2]
     */
    public static void selectFilter(String ss) {
        // reset toggles and sets
        myGuildFilter = false;
        myClassFilter = false;
        filterNames.clear();
        filterGuilds.clear();
        filterClasses.clear();

        if (ss == null || ss.isEmpty()) return;

        int part = 0;
        int fieldIndex = 0;

        for (String raw : ss.split(",")) {
            String s = (raw == null) ? "" : raw.trim();
            if (s.equals("-")) {
                part++;
                continue;
            }

            switch (part) {
                case 0:
                    // preset name (for UI)
                    name = s;
                    break;
                case 1:
                    // mode selection
                    if (s.equals("F")) filter = 1;
                    if (s.equals("H")) filter = 2;
                    break;
                case 2:
                    // names (lowercased)
                    if (!s.isEmpty()) filterNames.add(lower(s));
                    break;
                case 3:
                    // guilds (lowercased)
                    if (!s.isEmpty()) filterGuilds.add(lower(s));
                    break;
                case 4:
                    // flags/classes
                    if (fieldIndex == 0) {
                        myGuildFilter = s.equals("1");
                    } else if (fieldIndex == 1) {
                        myClassFilter = s.equals("1");
                    } else if (s.equals("1")) {
                        int idx = fieldIndex - 2;
                        if (
                            idx >= 0 &&
                            idx < CharacterClass.CHAR_CLASS_LIST.length
                        ) {
                            filterClasses.add(
                                CharacterClass.CHAR_CLASS_LIST[idx].getId()
                            );
                        }
                    }
                    fieldIndex++;
                    break;
                default:
                    // ignore unknown parts to be forward compatible
                    break;
            }
        }
    }

    private static String lower(String s) {
        return (s == null) ? "" : s.toLowerCase(Locale.ROOT);
    }
}
