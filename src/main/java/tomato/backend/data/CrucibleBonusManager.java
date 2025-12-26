package tomato.backend.data;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import packets.Packet;
import packets.data.StatData;
import tomato.realmshark.CrucibleApiClient;

/**
 * Manages crucible bonuses dynamically by:
 * 1. Tracking the player's stat 128 & 155 string value
 * 2. Storing crucible configuration data from CrucibleResponsePacket or API
 * 3. Providing damage multipliers based on current crucible bonus
 */
public class CrucibleBonusManager {

    private static final Map<String, Double> crucibleDamageMultipliers =
        new HashMap<>();
    private static final Gson gson = new Gson();
    private static String currentPlayerCrucibleId128 = null;
    private static String currentPlayerCrucibleId155 = null;
    private static boolean apiDataLoaded = false;

    /**
     * Processes a CrucibleResponsePacket to extract and store damage multipliers.
     */
    public static void processCrucibleResponse(Packet packet) {
        System.out.println("[Crucible] Processing packet from game server...");

        try {
            String jsonData = extractJsonFromPacket(packet);
            if (jsonData == null || jsonData.isEmpty()) {
                System.err.println(
                    "[Crucible] Failed to extract JSON from packet"
                );
                return;
            }

            apiDataLoaded = false;
            processJsonData(jsonData, "packet");
        } catch (Exception e) {
            System.err.println(
                "Error processing crucible response: " + e.getMessage()
            );
        }
    }

    /**
     * Fetches crucible data from the RealmShark API.
     */
    public static void fetchCrucibleDataFromApi() {
        System.out.println("[Crucible] Fetching data from API...");

        try {
            if (apiDataLoaded) return;

            String jsonData = CrucibleApiClient.fetchCrucibleData();
            if (
                jsonData != null &&
                CrucibleApiClient.validateCrucibleData(jsonData)
            ) {
                processJsonData(jsonData, "API");
                apiDataLoaded = true;
            } else {
                System.err.println(
                    "[Crucible] API data not available or invalid"
                );
            }
        } catch (Exception e) {
            System.err.println(
                "[Crucible] Error fetching data: " + e.getMessage()
            );
        }
    }

    private static void processJsonData(String jsonData, String source) {
        JsonArray crucibleJsonsArray = gson.fromJson(jsonData, JsonArray.class);
        if (crucibleJsonsArray == null) return;

        crucibleDamageMultipliers.clear();

        for (JsonElement crucibleJsonElement : crucibleJsonsArray) {
            JsonObject crucibleJsonObj = crucibleJsonElement.getAsJsonObject();

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
                        String crucibleId = crucibleObj.get("id").getAsString();
                        findAndStoreType5Bonuses(crucibleObj, crucibleId);
                    }
                }
            }
        }

        System.out.println(
            "[Crucible] Loaded " +
                crucibleDamageMultipliers.size() +
                " damage multipliers from " +
                source
        );
    }

    /**
     * Updates the current player's crucible IDs from stats 128 and 155.
     */
    public static void updatePlayerCrucibleBonus(Entity playerEntity) {
        if (playerEntity == null || playerEntity.stat == null) return;

        currentPlayerCrucibleId128 = updateCrucibleStat(
            playerEntity,
            128,
            currentPlayerCrucibleId128,
            "stat 128"
        );
        currentPlayerCrucibleId155 = updateCrucibleStat(
            playerEntity,
            155,
            currentPlayerCrucibleId155,
            "stat 155"
        );
    }

    private static String updateCrucibleStat(
        Entity player,
        int statId,
        String currentValue,
        String statName
    ) {
        if (player == null || player.stat == null) return null;

        StatData stat = player.stat.get(statId);

        if (
            stat != null &&
            stat.stringStatValue != null &&
            !stat.stringStatValue.isEmpty()
        ) {
            String newValue = stat.stringStatValue;
            if (!newValue.equals(currentValue)) {
                System.out.println(
                    "Player crucible bonus (" +
                        statName +
                        ") updated: " +
                        newValue
                );
            }
            return newValue;
        }
        return null;
    }

    /**
     * Gets the current damage multiplier for the player based on their crucible bonuses.
     * Applies bonuses multiplicatively from both stat 128 and stat 155.
     */
    public static double getPlayerDamageMultiplier() {
        return (
            getMultiplierForId(currentPlayerCrucibleId128) *
            getMultiplierForId(currentPlayerCrucibleId155)
        );
    }

    private static double getMultiplierForId(String crucibleId) {
        if (crucibleId == null) return 1.0;
        Double multiplier = crucibleDamageMultipliers.get(crucibleId);
        return (multiplier != null) ? multiplier : 1.0;
    }

    /**
     * Gets the current crucible IDs for display purposes.
     */
    public static String[] getCurrentCrucibleIds() {
        return new String[] {
            currentPlayerCrucibleId128,
            currentPlayerCrucibleId155,
        };
    }

    /**
     * Gets the current damage multiplier values for display purposes.
     */
    public static Double[] getCurrentDamageMultiplierValues() {
        Double multiplier128 = (currentPlayerCrucibleId128 != null)
            ? crucibleDamageMultipliers.get(currentPlayerCrucibleId128)
            : null;
        Double multiplier155 = (currentPlayerCrucibleId155 != null)
            ? crucibleDamageMultipliers.get(currentPlayerCrucibleId155)
            : null;
        return new Double[] { multiplier128, multiplier155 };
    }

    /**
     * Gets the combined multiplicative damage multiplier for display.
     */
    public static Double getCurrentCombinedMultiplier() {
        double multiplier = getPlayerDamageMultiplier();
        return (multiplier != 1.0) ? multiplier : null;
    }

    public static boolean isApiDataLoaded() {
        return apiDataLoaded;
    }

    /**
     * Clears all stored crucible data.
     */
    public static void clear() {
        crucibleDamageMultipliers.clear();
        currentPlayerCrucibleId128 = null;
        currentPlayerCrucibleId155 = null;
        apiDataLoaded = false;
    }

    private static String extractJsonFromPacket(Packet packet) {
        if (packet == null) return null;

        try {
            Class<?> packetClass = packet.getClass();

            // Look for crucibleJsons field first
            for (Field field : packetClass.getDeclaredFields()) {
                if ("crucibleJsons".equals(field.getName())) {
                    field.setAccessible(true);
                    Object value = field.get(packet);

                    if (value instanceof String[]) {
                        return combineJsonArray((String[]) value);
                    } else if (value instanceof String) {
                        return (String) value;
                    }
                }
            }

            // Fallback: look for any JSON-like string field
            for (Field field : packetClass.getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(packet);

                if (value instanceof String) {
                    String stringValue = (String) value;
                    if (isJsonLike(stringValue)) {
                        return stringValue;
                    }
                }
            }

            System.out.println(
                "Could not extract JSON from CrucibleResponsePacket"
            );
        } catch (Exception e) {
            System.err.println(
                "Error extracting JSON from packet: " + e.getMessage()
            );
        }
        return null;
    }

    private static String combineJsonArray(String[] jsonArray) {
        if (jsonArray == null || jsonArray.length == 0) return null;

        StringBuilder combined = new StringBuilder("[");
        for (int i = 0; i < jsonArray.length; i++) {
            if (i > 0) combined.append(",");
            combined.append(jsonArray[i]);
        }
        combined.append("]");
        return combined.toString();
    }

    private static boolean isJsonLike(String str) {
        if (str == null) return false;
        return (
            str.contains("{") &&
            str.contains("}") &&
            (str.contains("\"array\"") || str.contains("\"id\""))
        );
    }

    private static void findAndStoreType5Bonuses(
        JsonElement element,
        String crucibleId
    ) {
        if (element == null || crucibleId == null) return;
        if (crucibleDamageMultipliers.containsKey(crucibleId)) return;

        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();

            if (obj.has("bonuses") && obj.get("bonuses").isJsonArray()) {
                JsonArray bonuses = obj.get("bonuses").getAsJsonArray();

                for (JsonElement bonusElement : bonuses) {
                    if (bonusElement.isJsonObject()) {
                        JsonObject bonus = bonusElement.getAsJsonObject();
                        if (
                            bonus.has("type") &&
                            bonus.get("type").getAsInt() == 5 &&
                            bonus.has("amount")
                        ) {
                            double multiplier = bonus
                                .get("amount")
                                .getAsDouble();
                            crucibleDamageMultipliers.put(
                                crucibleId,
                                multiplier
                            );
                            return;
                        }
                    }
                }
            }

            // Recursively search all properties
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                findAndStoreType5Bonuses(entry.getValue(), crucibleId);
                if (crucibleDamageMultipliers.containsKey(crucibleId)) return;
            }
        } else if (element.isJsonArray()) {
            for (JsonElement arrayElement : element.getAsJsonArray()) {
                findAndStoreType5Bonuses(arrayElement, crucibleId);
                if (crucibleDamageMultipliers.containsKey(crucibleId)) return;
            }
        }
    }
}
