package tomato.gui.dps.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import packets.incoming.NotificationPacket;
import util.Pair;

/**
 * Utility methods for parsing death notifications and looking up grave icons.
 *
 * The current protocol encodes the player name inside the {@code NotificationPacket.message}
 * string, wrapped in double quotes. Existing code extracts it by splitting on {@code "\""}
 * and taking index 9 when present. This utility centralizes that parsing.
 */
public final class DeathParser {

    private DeathParser() {
        // no instances
    }

    /**
     * Parses a list of notification packets into a map of player name -> grave icon id.
     * Any packets that do not match the expected format are ignored.
     *
     * @param notifications list or collection of NotificationPacket; null-safe
     * @return a new map of names to grave icon ids
     */
    public static Map<String, Integer> parseDeathsToMap(
        Collection<NotificationPacket> notifications
    ) {
        Map<String, Integer> map = new HashMap<>();
        if (notifications == null) return map;

        for (NotificationPacket n : notifications) {
            if (n == null) continue;
            String name = extractName(n);
            if (name != null) {
                map.put(name, n.pictureType);
            }
        }
        return map;
    }

    /**
     * Parses a list of notification packets into a list of (name, grave icon id) pairs.
     * Any packets that do not match the expected format are ignored.
     *
     * @param notifications list or collection of NotificationPacket; null-safe
     * @return a new list of pairs (name, iconId)
     */
    public static ArrayList<Pair<String, Integer>> parseDeathsToList(
        Collection<NotificationPacket> notifications
    ) {
        ArrayList<Pair<String, Integer>> list = new ArrayList<>();
        if (notifications == null) return list;

        for (NotificationPacket n : notifications) {
            if (n == null) continue;
            String name = extractName(n);
            if (name != null) {
                list.add(new Pair<>(name, n.pictureType));
            }
        }
        return list;
    }

    /**
     * Returns true if the provided name exists in the parsed death map.
     *
     * @param deathMap map of name -> grave icon id; null-safe
     * @param name     player name to check; null-safe
     * @return true if the player is in the death map
     */
    public static boolean isDead(Map<String, Integer> deathMap, String name) {
        return deathMap != null && name != null && deathMap.containsKey(name);
    }

    /**
     * Returns the grave icon id for a given player, or -1 if not present.
     *
     * @param deathMap map of name -> grave icon id; null-safe
     * @param name     player name; null-safe
     * @return grave icon id, or -1 if not found
     */
    public static int getGraveIcon(Map<String, Integer> deathMap, String name) {
        if (deathMap == null || name == null) return -1;
        Integer v = deathMap.get(name);
        return v != null ? v : -1;
    }

    /**
     * Extracts the player name from a NotificationPacket using the current message format.
     *
     * Expected: message contains a quoted player name where index 9 after splitting by a quote (")
     * holds the name (per existing code).
     *
     * @param n notification packet
     * @return extracted name or null if it cannot be parsed
     */
    public static String extractName(NotificationPacket n) {
        if (n == null || n.message == null) return null;
        return extractNameFromMessage(n.message);
    }

    /**
     * Extracts the player name from a message string by splitting on the quote character.
     *
     * @param message notification message
     * @return extracted name or null if it cannot be parsed
     */
    public static String extractNameFromMessage(String message) {
        if (message == null) return null;
        String[] parts = message.split("\"");
        if (parts.length > 9) {
            return parts[9];
        }
        return null;
    }
}
