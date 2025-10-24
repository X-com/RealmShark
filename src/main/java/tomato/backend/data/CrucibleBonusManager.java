package tomato.backend.data;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import packets.Packet;
import packets.data.StatData;
import packets.data.enums.StatType;
import tomato.backend.data.Entity;

/**
 * Manages crucible bonuses dynamically by:
 * 1. Tracking the player's stat 155 string value
 * 2. Storing crucible configuration data from CrucibleResponsePacket
 * 3. Providing damage multipliers based on current crucible bonus
 */
public class CrucibleBonusManager {

    private static final Map<String, Double> crucibleDamageMultipliers =
        new HashMap<>();
    private static String currentPlayerCrucibleId128 = null;
    private static String currentPlayerCrucibleId155 = null;
    private static final Gson gson = new Gson();

    /**
     * Processes a CrucibleResponsePacket to extract and store damage multipliers
     *
     * @param packet The CrucibleResponsePacket containing crucible configuration
     */
    public static void processCrucibleResponse(Packet packet) {
        try {
            // The packet should contain JSON data with crucible configurations
            String jsonData = extractJsonFromPacket(packet);
            if (jsonData == null || jsonData.isEmpty()) {
                return;
            }

            // Debug: Print raw JSON data to see what we're working with
            System.out.println("=== RAW CRUCIBLE RESPONSE DATA ===");
            System.out.println(
                "Packet class: " + packet.getClass().getSimpleName()
            );
            System.out.println("Raw packet toString: " + packet.toString());
            System.out.println("Extracted JSON data: " + jsonData);
            System.out.println("=== END RAW DATA ===");

            // The crucibleJsons field contains an array of JSON objects
            // Each object has an "array" field containing the actual crucible data
            JsonArray crucibleJsonsArray = gson.fromJson(
                jsonData,
                JsonArray.class
            );
            if (crucibleJsonsArray == null) {
                return;
            }

            // Clear previous multipliers
            crucibleDamageMultipliers.clear();

            // Parse each crucible configuration
            for (JsonElement crucibleJsonElement : crucibleJsonsArray) {
                JsonObject crucibleJsonObj =
                    crucibleJsonElement.getAsJsonObject();

                // Each crucible JSON object contains an "array" field with the actual data
                if (
                    crucibleJsonObj.has("array") &&
                    crucibleJsonObj.get("array").isJsonArray()
                ) {
                    JsonArray crucibleArray = crucibleJsonObj
                        .get("array")
                        .getAsJsonArray();

                    for (JsonElement element : crucibleArray) {
                        JsonObject crucibleObj = element.getAsJsonObject();
                        if (crucibleObj.has("id")) {
                            String crucibleId = crucibleObj
                                .get("id")
                                .getAsString();

                            // Debug: Print crucible object structure
                            System.out.println(
                                "Processing crucible ID: " + crucibleId
                            );

                            // Recursively search for type 5 bonuses in any JSON structure
                            findAndStoreType5Bonuses(crucibleObj, crucibleId);
                        }
                    }
                }
            }

            System.out.println(
                "Loaded " +
                    crucibleDamageMultipliers.size() +
                    " crucible damage multipliers"
            );
            System.out.println("Multipliers map: " + crucibleDamageMultipliers);
        } catch (Exception e) {
            System.err.println(
                "Error processing crucible response: " + e.getMessage()
            );
        }
    }

    /**
     * Updates the current player's crucible IDs from stats 128 and 155
     *
     * @param playerEntity The player entity to check for stats 128 and 155
     */
    public static void updatePlayerCrucibleBonus(Entity playerEntity) {
        if (playerEntity == null) return;

        // Update stat 128 (CRUCIBLE_STAT)
        StatData stat128 = playerEntity.stat.get(128);
        if (
            stat128 != null &&
            stat128.stringStatValue != null &&
            !stat128.stringStatValue.isEmpty()
        ) {
            String newCrucibleId = stat128.stringStatValue;
            if (!newCrucibleId.equals(currentPlayerCrucibleId128)) {
                currentPlayerCrucibleId128 = newCrucibleId;
                System.out.println(
                    "Player crucible bonus (stat 128) updated: " +
                        currentPlayerCrucibleId128
                );
            }
        } else {
            currentPlayerCrucibleId128 = null;
        }

        // Update stat 155 (UNKNOWN_STRING_155_STAT)
        StatData stat155 = playerEntity.stat.get(155);
        if (
            stat155 != null &&
            stat155.stringStatValue != null &&
            !stat155.stringStatValue.isEmpty()
        ) {
            String newCrucibleId = stat155.stringStatValue;
            if (!newCrucibleId.equals(currentPlayerCrucibleId155)) {
                currentPlayerCrucibleId155 = newCrucibleId;
                System.out.println(
                    "Player crucible bonus (stat 155) updated: " +
                        currentPlayerCrucibleId155
                );
            }
        } else {
            currentPlayerCrucibleId155 = null;
        }
    }

    /**
     * Gets the current damage multiplier for the player based on their crucible bonuses
     * Applies bonuses multiplicatively from both stat 128 and stat 155
     *
     * @return Damage multiplier (1.0 if no bonus active)
     */
    public static double getPlayerDamageMultiplier() {
        double multiplier128 = getMultiplierForId(currentPlayerCrucibleId128);
        double multiplier155 = getMultiplierForId(currentPlayerCrucibleId155);

        // Apply bonuses multiplicatively
        return multiplier128 * multiplier155;
    }

    /**
     * Gets the multiplier for a specific crucible ID
     */
    private static double getMultiplierForId(String crucibleId) {
        if (crucibleId == null) {
            return 1.0;
        }

        Double multiplier = crucibleDamageMultipliers.get(crucibleId);
        if (multiplier != null) {
            System.out.println(
                "Found multiplier for crucible ID " +
                    crucibleId +
                    ": " +
                    multiplier
            );
            return multiplier;
        }

        // No multiplier found for current crucible ID
        System.out.println(
            "No damage multiplier found for crucible ID: " + crucibleId
        );
        System.out.println(
            "Available multipliers: " + crucibleDamageMultipliers
        );
        return 1.0;
    }

    /**
     * Gets the current crucible IDs for display purposes
     *
     * @return Array containing [stat128Id, stat155Id] or nulls if none active
     */
    public static String[] getCurrentCrucibleIds() {
        return new String[] {
            currentPlayerCrucibleId128,
            currentPlayerCrucibleId155,
        };
    }

    /**
     * Gets the current damage multiplier values for display purposes
     *
     * @return Array containing [multiplier128, multiplier155] or nulls if none active
     */
    public static Double[] getCurrentDamageMultiplierValues() {
        Double multiplier128 = currentPlayerCrucibleId128 != null
            ? crucibleDamageMultipliers.get(currentPlayerCrucibleId128)
            : null;
        Double multiplier155 = currentPlayerCrucibleId155 != null
            ? crucibleDamageMultipliers.get(currentPlayerCrucibleId155)
            : null;
        return new Double[] { multiplier128, multiplier155 };
    }

    /**
     * Gets the combined multiplicative damage multiplier for display
     *
     * @return Combined multiplier or null if no bonuses active
     */
    public static Double getCurrentCombinedMultiplier() {
        double multiplier = getPlayerDamageMultiplier();
        return multiplier != 1.0 ? multiplier : null;
    }

    /**
     * Extracts JSON data from a CrucibleResponsePacket
     * Uses reflection to dynamically extract data from unknown packet structures
     */
    private static String extractJsonFromPacket(Packet packet) {
        try {
            // Try to extract JSON data using reflection
            // First, check if packet has a toString that contains JSON
            String packetString = packet.toString();
            System.out.println("Packet toString: " + packetString);
            // Try to access packet fields via reflection
            Class<?> packetClass = packet.getClass();
            System.out.println("Packet class fields:");

            // Look for the crucibleJsons field specifically
            for (java.lang.reflect.Field field : packetClass.getDeclaredFields()) {
                System.out.println(
                    "  Field: " + field.getName() + " type: " + field.getType()
                );
                field.setAccessible(true);
                Object value = field.get(packet);
                System.out.println("  Value: " + value);

                // Look for the crucibleJsons field specifically
                if ("crucibleJsons".equals(field.getName())) {
                    if (value instanceof String[]) {
                        String[] crucibleJsonsArray = (String[]) value;
                        if (crucibleJsonsArray.length > 0) {
                            // Combine all JSON strings into a single JSON array
                            StringBuilder combinedJson = new StringBuilder("[");
                            for (
                                int i = 0;
                                i < crucibleJsonsArray.length;
                                i++
                            ) {
                                if (i > 0) combinedJson.append(",");
                                combinedJson.append(crucibleJsonsArray[i]);
                            }
                            combinedJson.append("]");
                            String crucibleJsons = combinedJson.toString();
                            System.out.println(
                                "Found crucibleJsons array: " + crucibleJsons
                            );
                            return crucibleJsons;
                        }
                    } else if (value instanceof String) {
                        String crucibleJsons = (String) value;
                        System.out.println(
                            "Found crucibleJsons field: " + crucibleJsons
                        );
                        return crucibleJsons;
                    }
                }
            }

            // If we didn't find crucibleJsons field, try alternative extraction methods
            for (java.lang.reflect.Field field : packetClass.getDeclaredFields()) {
                System.out.println(
                    "  Field: " + field.getName() + " type: " + field.getType()
                );
                field.setAccessible(true);
                Object value = field.get(packet);
                System.out.println("  Value: " + value);
                if (value instanceof String) {
                    String stringValue = (String) value;
                    if (
                        stringValue.contains("{") &&
                        stringValue.contains("}") &&
                        (stringValue.contains("\"array\"") ||
                            stringValue.contains("\"id\""))
                    ) {
                        System.out.println(
                            "Found JSON in field: " + field.getName()
                        );
                        return stringValue;
                    }
                }
            }

            // If we can't extract JSON, log the packet for debugging
            System.out.println(
                "Could not extract JSON from CrucibleResponsePacket: " +
                    packet.getClass().getSimpleName()
            );
        } catch (Exception e) {
            System.err.println(
                "Error extracting JSON from packet: " + e.getMessage()
            );
        }

        return null;
    }

    /**
     * Recursively searches for type 5 bonuses in any JSON structure
     */
    private static void findAndStoreType5Bonuses(
        JsonElement element,
        String crucibleId
    ) {
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();

            // Check if this object has a "bonuses" array
            if (obj.has("bonuses") && obj.get("bonuses").isJsonArray()) {
                JsonArray bonuses = obj.get("bonuses").getAsJsonArray();
                for (JsonElement bonusElement : bonuses) {
                    if (bonusElement.isJsonObject()) {
                        JsonObject bonus = bonusElement.getAsJsonObject();
                        if (
                            bonus.has("type") &&
                            bonus.get("type").getAsInt() == 5
                        ) {
                            // Type 5 is damage multiplier
                            if (bonus.has("amount")) {
                                double multiplier = bonus
                                    .get("amount")
                                    .getAsDouble();
                                crucibleDamageMultipliers.put(
                                    crucibleId,
                                    multiplier
                                );
                                System.out.println(
                                    "Found type 5 bonus in bonuses array: " +
                                        crucibleId +
                                        " = " +
                                        multiplier
                                );
                                return; // Found type 5, no need to search further
                            }
                        }
                    }
                }
            }

            // Recursively search all object properties
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                findAndStoreType5Bonuses(entry.getValue(), crucibleId);
                // If we found a multiplier, stop searching
                if (crucibleDamageMultipliers.containsKey(crucibleId)) {
                    return;
                }
            }
        } else if (element.isJsonArray()) {
            // Recursively search array elements
            JsonArray array = element.getAsJsonArray();
            for (JsonElement arrayElement : array) {
                findAndStoreType5Bonuses(arrayElement, crucibleId);
                // If we found a multiplier, stop searching
                if (crucibleDamageMultipliers.containsKey(crucibleId)) {
                    return;
                }
            }
        }
    }

    /**
     * Clears all stored crucible data (for testing or reset purposes)
     */
    public static void clear() {
        crucibleDamageMultipliers.clear();
        currentPlayerCrucibleId128 = null;
        currentPlayerCrucibleId155 = null;
    }
}
