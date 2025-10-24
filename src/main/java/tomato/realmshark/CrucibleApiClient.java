package tomato.realmshark;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * HTTP client for fetching crucible data from the RealmShark API
 */
public class CrucibleApiClient {

    private static final String CRUCIBLE_API_URL =
        "https://api.realmshark.cc/crucible";
    private static final Gson gson = new Gson();

    /**
     * Fetches crucible data from the RealmShark API
     *
     * @return JSON string containing crucible data, or null if the request fails
     */
    public static String fetchCrucibleData() {
        System.out.println("[Crucible] Fetching data from API...");

        try {
            URL url = new URL(CRUCIBLE_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty(
                "User-Agent",
                "RealmShark/" + tomato.version.Version.VERSION
            );
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(10000); // 10 seconds
            conn.setReadTimeout(10000); // 10 seconds

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
                );
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String jsonResponse = response.toString();

                // Validate the response before returning
                if (validateCrucibleData(jsonResponse)) {
                    System.out.println(
                        "[Crucible] API data loaded successfully"
                    );
                } else {
                    System.err.println("[Crucible] API data validation failed");
                }

                return jsonResponse;
            } else {
                System.err.println(
                    "[Crucible] API request failed with HTTP code: " +
                        responseCode
                );
                return null;
            }
        } catch (Exception e) {
            System.err.println(
                "[Crucible] Error fetching data: " + e.getMessage()
            );
            return null;
        }
    }

    /**
     * Validates that the crucible data contains the expected structure
     *
     * @param jsonData The JSON data to validate
     * @return true if the data appears valid, false otherwise
     */
    public static boolean validateCrucibleData(String jsonData) {
        if (jsonData == null || jsonData.isEmpty()) {
            System.err.println(
                "[Crucible] Validation failed: JSON data is null or empty"
            );
            return false;
        }

        try {
            JsonArray crucibleArray = gson.fromJson(jsonData, JsonArray.class);
            if (crucibleArray == null) {
                System.err.println(
                    "[Crucible] Validation failed: JSON could not be parsed as array"
                );
                return false;
            }

            if (crucibleArray.size() == 0) {
                System.err.println(
                    "[Crucible] Validation failed: Crucible array is empty"
                );
                return false;
            }

            // Check if the first element has the expected structure
            JsonElement firstElement = crucibleArray.get(0);
            if (firstElement.isJsonObject()) {
                JsonObject firstObj = firstElement.getAsJsonObject();
                if (
                    firstObj.has("array") && firstObj.get("array").isJsonArray()
                ) {
                    JsonArray innerArray = firstObj
                        .get("array")
                        .getAsJsonArray();

                    // Count total crucible entries across all arrays
                    int totalEntries = 0;
                    for (JsonElement element : crucibleArray) {
                        if (
                            element.isJsonObject() &&
                            element.getAsJsonObject().has("array")
                        ) {
                            JsonArray inner = element
                                .getAsJsonObject()
                                .get("array")
                                .getAsJsonArray();
                            totalEntries += inner.size();
                        }
                    }
                    return true;
                } else {
                    System.err.println(
                        "[Crucible] Validation failed: Missing 'array' field"
                    );
                    return false;
                }
            } else {
                System.err.println(
                    "[Crucible] Validation failed: First element is not a JSON object"
                );
                return false;
            }
        } catch (Exception e) {
            System.err.println(
                "[Crucible] Error validating data: " + e.getMessage()
            );
            return false;
        }
    }
}
