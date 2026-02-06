package tomato;

import com.google.gson.*;
import tomato.version.Version;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class CheckVersion {
    private static final String GITHUB_API_URL = "https://api.github.com/repos/X-com/RealmShark/releases";

    private static String getLatestVersion() throws IOException {
        URL url = new URL(GITHUB_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/vnd.github.v3+json");

        if (conn.getResponseCode() != 200) {
            throw new IOException("Failed : HTTP error code : " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        StringBuilder sb = new StringBuilder();
        String output;
        while ((output = br.readLine()) != null) {
            sb.append(output);
        }
        conn.disconnect();
        return getTomatoVersion(sb.toString());
    }

    private static String getTomatoVersion(String json) {
        JsonElement e = JsonParser.parseString(json);
        if (e.isJsonArray()) {
            for (JsonElement e2 : e.getAsJsonArray()) {
                String target = null;
                String version = null;
                if (e2.isJsonObject()) {
                    JsonObject o = e2.getAsJsonObject();
                    for (String key : o.keySet()) {
                        JsonElement e3 = o.get(key);
                        if (e3.isJsonPrimitive()) {
                            if (key.equals("tag_name")) {
                                version = e3.getAsString();
                            } else if (key.equals("target_commitish")) {
                                target = e3.getAsString();
                            }
                        }
                    }
                }

                if (target != null && version != null) {
                    if (target.equals("tomato")) return version;
                }
            }
        }

        return "";
    }

    public static boolean isLatestVersion() {
        try {
            return getLatestVersion().equals(Version.VERSION);
        } catch (IOException ignored) {
            // If we can't check the version, assume it's the latest to avoid bothering the user
        }
        return true;
    }

    private static void updateMessage() {
        JEditorPane ep = new JEditorPane("text/html", "<html>Outdated Tomato version<br>Download the latest version and replace the Tomato-v.xx.jar<br><a href=\\\\\\\"\"https://github.com/X-com/RealmShark/releases\\\\\\\">https://github.com/X-com/RealmShark/releases</a></html>");
        ep.addHyperlinkListener(e -> {
            if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                try {
                    Desktop.getDesktop().browse(new URI("https://github.com/X-com/RealmShark/releases"));
                } catch (IOException | URISyntaxException ex) {
                    ex.printStackTrace();
                }
            }
        });
        ep.setEditable(false);
        JOptionPane.showMessageDialog(null, ep);
    }

    public static void checkVersion() {
        if(!isLatestVersion()) {
            updateMessage();
        }
    }

    public static void main(String[] args) {
        checkVersion();
    }
}
