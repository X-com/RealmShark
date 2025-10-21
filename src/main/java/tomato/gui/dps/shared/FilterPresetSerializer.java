package tomato.gui.dps.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import tomato.realmshark.enums.CharacterClass;

/**
 * Centralizes serialization/deserialization of DPS Filter presets used by FilterGUI and Filter.
 *
 * Preset wire format (CSV-like with "-" section delimiters):
 *   part 0: preset name
 *   part 1: mode flags:
 *           "F" => Filter mode, "H" => Highlight mode (order-independent; presence matters)
 *   part 2: names (strings)
 *   part 3: guilds (strings)
 *   part 4: checkbox flags in GUI order:
 *           index 0 => "My Guild" (1/0)
 *           index 1 => "My Class" (1/0)
 *           index 2+ => per-class toggles aligned to CharacterClass.CHAR_CLASS_LIST
 *
 * Example:
 *   MyPreset,-,H,F,-,bob,alice,-,cool dudes,-,1,0,1,0,0,1,...
 */
public final class FilterPresetSerializer {

    private static final String SEP = ",";
    private static final String PART_BREAK = "-";

    private FilterPresetSerializer() {
        // no instances
    }

    /**
     * Immutable value holding a parsed filter preset.
     * flagsInGuiOrder: [myGuild, myClass, classes aligned to CharacterClass.CHAR_CLASS_LIST...]
     */
    public static final class FilterPreset {

        public final String name;
        public final boolean filterMode;
        public final boolean highlightMode;
        public final List<String> names;
        public final List<String> guilds;
        public final List<Boolean> flagsInGuiOrder;

        public FilterPreset(
            String name,
            boolean filterMode,
            boolean highlightMode,
            List<String> names,
            List<String> guilds,
            List<Boolean> flagsInGuiOrder
        ) {
            this.name = safe(name);
            this.filterMode = filterMode;
            this.highlightMode = highlightMode;
            this.names = unmodifiableTrimmed(names);
            this.guilds = unmodifiableTrimmed(guilds);
            this.flagsInGuiOrder = unmodifiableFlags(flagsInGuiOrder);
        }

        public boolean myGuild() {
            return flagsInGuiOrder.size() > 0 && flagsInGuiOrder.get(0);
        }

        public boolean myClass() {
            return flagsInGuiOrder.size() > 1 && flagsInGuiOrder.get(1);
        }

        /**
         * Returns a view of per-class toggles aligned to CharacterClass.CHAR_CLASS_LIST.
         * Size is min(flags length - 2, number of classes).
         */
        public List<Boolean> classToggles() {
            int start = 2;
            if (flagsInGuiOrder.size() <= start) return Collections.emptyList();
            int len = Math.min(
                flagsInGuiOrder.size() - start,
                CharacterClass.CHAR_CLASS_LIST.length
            );
            return Collections.unmodifiableList(
                new ArrayList<>(flagsInGuiOrder.subList(start, start + len))
            );
        }
    }

    /**
     * Serialize a preset following the exact format that FilterGUI currently uses.
     *
     * The flagsInGuiOrder list must follow GUI order:
     * [myGuild, myClass, classes aligned to CharacterClass.CHAR_CLASS_LIST...]
     */
    public static String serialize(
        String presetName,
        boolean filterMode,
        boolean highlightMode,
        List<String> names,
        List<String> guilds,
        List<Boolean> flagsInGuiOrder
    ) {
        StringBuilder sb = new StringBuilder();

        // part 0: preset name
        sb.append(safe(presetName)).append(SEP);

        // part break
        sb.append(PART_BREAK).append(SEP);

        // part 1: mode flags (presence matters; order-independent)
        if (highlightMode) sb.append("H").append(SEP);
        if (filterMode) sb.append("F").append(SEP);

        // part break
        sb.append(PART_BREAK).append(SEP);

        // part 2: names
        if (names != null) {
            for (String n : names) {
                String t = trimToEmpty(n);
                if (!t.isEmpty()) sb.append(t).append(SEP);
            }
        }

        // part break
        sb.append(PART_BREAK).append(SEP);

        // part 3: guilds
        if (guilds != null) {
            for (String g : guilds) {
                String t = trimToEmpty(g);
                if (!t.isEmpty()) sb.append(t).append(SEP);
            }
        }

        // part break
        sb.append(PART_BREAK).append(SEP);

        // part 4: GUI-ordered flags (myGuild, myClass, classes...)
        if (flagsInGuiOrder != null) {
            for (Boolean b : flagsInGuiOrder) {
                sb.append(Boolean.TRUE.equals(b) ? "1" : "0").append(SEP);
            }
        }

        return sb.toString();
    }

    /**
     * Deserialize a preset string to a typed FilterPreset value.
     * Unknown/extra parts are ignored; missing parts default to empty/false.
     */
    public static FilterPreset deserialize(String preset) {
        if (preset == null) {
            return new FilterPreset(
                "",
                false,
                false,
                Collections.<String>emptyList(),
                Collections.<String>emptyList(),
                Collections.<Boolean>emptyList()
            );
        }

        String[] tokens = preset.split(SEP, -1);
        int part = 0;
        String name = "";
        boolean filterMode = false;
        boolean highlightMode = false;
        List<String> names = new ArrayList<>();
        List<String> guilds = new ArrayList<>();
        List<Boolean> flags = new ArrayList<>();

        int i = 0;
        while (i < tokens.length) {
            String raw = tokens[i++];
            String s = trimToEmpty(raw);
            if (s.equals(PART_BREAK)) {
                part++;
                continue;
            }

            switch (part) {
                case 0:
                    name = s;
                    break;
                case 1:
                    // mode flags: presence matters
                    if (s.equals("F")) filterMode = true;
                    if (s.equals("H")) highlightMode = true;
                    break;
                case 2:
                    if (!s.isEmpty()) names.add(s);
                    break;
                case 3:
                    if (!s.isEmpty()) guilds.add(s);
                    break;
                case 4:
                    flags.add(s.equals("1"));
                    break;
                default:
                    // ignore
                    break;
            }
        }

        return new FilterPreset(
            name,
            filterMode,
            highlightMode,
            names,
            guilds,
            flags
        );
    }

    /**
     * Utility to build GUI-ordered flags from sets of state:
     * [myGuild, myClass, class-by-class toggles following CharacterClass.CHAR_CLASS_LIST]
     */
    public static List<Boolean> buildFlagsGuiOrder(
        boolean myGuild,
        boolean myClass,
        Set<Integer> enabledClassIds
    ) {
        List<Boolean> flags = new ArrayList<>(
            2 + CharacterClass.CHAR_CLASS_LIST.length
        );
        flags.add(myGuild);
        flags.add(myClass);
        for (CharacterClass cc : CharacterClass.CHAR_CLASS_LIST) {
            boolean v =
                enabledClassIds != null && enabledClassIds.contains(cc.getId());
            flags.add(v);
        }
        return flags;
    }

    /**
     * Converts GUI-ordered flags to a set of enabled class ids using CharacterClass.CHAR_CLASS_LIST.
     * Flags indices: 0 => myGuild, 1 => myClass, 2+ => per-class toggles.
     */
    public static List<Integer> flagsToClassIds(List<Boolean> flagsInGuiOrder) {
        if (flagsInGuiOrder == null || flagsInGuiOrder.size() <= 2) {
            return Collections.<Integer>emptyList();
        }
        List<Integer> ids = new ArrayList<>();
        for (int idx = 0; idx < CharacterClass.CHAR_CLASS_LIST.length; idx++) {
            int flagIndex = 2 + idx;
            if (
                flagIndex < flagsInGuiOrder.size() &&
                Boolean.TRUE.equals(flagsInGuiOrder.get(flagIndex))
            ) {
                ids.add(CharacterClass.CHAR_CLASS_LIST[idx].getId());
            }
        }
        return ids;
    }

    // --- small helpers ---

    private static String safe(String s) {
        return (s == null) ? "" : s;
    }

    private static String trimToEmpty(String s) {
        return (s == null) ? "" : s.trim();
    }

    private static List<String> unmodifiableTrimmed(List<String> in) {
        if (in == null || in.isEmpty()) return Collections.<String>emptyList();
        List<String> out = new ArrayList<>(in.size());
        for (String s : in) {
            String t = trimToEmpty(s);
            if (!t.isEmpty()) out.add(t);
        }
        return Collections.unmodifiableList(out);
    }

    private static List<Boolean> unmodifiableFlags(List<Boolean> in) {
        if (in == null || in.isEmpty()) return Collections.<Boolean>emptyList();
        List<Boolean> out = new ArrayList<>(in.size());
        for (Boolean b : in) out.add(Boolean.TRUE.equals(b));
        return Collections.unmodifiableList(out);
    }
}
