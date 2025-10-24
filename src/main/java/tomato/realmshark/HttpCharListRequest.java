package tomato.realmshark;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * HTTP Requests character data from realm servers and converts data to character info.
 */
public class HttpCharListRequest {

    /**
     * Requests character list data from realm servers using the current access token of the logged in user.
     *
     * @param accessToken Access token of the currently logged in user.
     * @return Char list data as XML string.
     */
    public static String getChartList(String accessToken) throws IOException {
        return webRequest(accessToken, "char/list");
    }

    /**
     * Request exalt stats
     *
     * @param accessToken Access token of the currently logged in user.
     * @return Exalt data of all characters
     */
    public static String getPowerUpStats(String accessToken)
        throws IOException {
        return webRequest(accessToken, "account/listPowerUpStats");
    }

    /**
     * Web request packet sent.
     *
     * @return Request info
     */
    public static String webRequest(String accessToken, String requestType)
        throws IOException {
        String encoded = URLEncoder.encode(accessToken, "UTF-8");
        String s1 = "https://www.realmofthemadgod.com/" + requestType + "?";
        String s2 =
            "do_login=true&accessToken=" +
            encoded +
            "&game_net=Unity&play_platform=Unity&game_net_user_id";

        URL obj = new URL(s1 + s2);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
        con.setDoOutput(true);
        con.setRequestMethod("POST");

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = s2.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // success
            BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream())
            );
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // print result
            //System.out.println(response);
            //System.out.println(accessToken);
            return response.toString();
        } else {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getErrorStream())
            );
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine).append("\n");
            }
            in.close();
            System.out.println(response);
        }
        return null;
    }
}
