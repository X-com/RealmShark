package potato.control;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import potato.model.DataModel;
import potato.model.HeroLocations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ServerHTTP {
    static private final String urlString = "http://ec2-3-90-180-208.compute-1.amazonaws.com:8080/rum";
    static private URL url;
    DataModel model;

    boolean synchRequests;

    public ServerHTTP(DataModel model) {
        this.model = model;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
    }

    public void uploadSingleDot(int mapIndex, int markIndex, int colorIndex) {
        if (!synchRequests) return;
        HttpURLConnection http = null;
        try {
            http = (HttpURLConnection) url.openConnection();
            http.setRequestProperty("Content-Type", "application/json");
            http.setDoOutput(true);
            http.setRequestMethod("PUT");

            String jsonInputString = "{\"mapIdx\":\"" + mapIndex + "\",\"server\":\"...\",\"" + markIndex + "\":\"" + colorIndex + "\"}";

            try (OutputStream os = http.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            int response = http.getResponseCode();
//                System.out.println(response + " " + http.getResponseMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (http != null) {
                http.disconnect();
            }
        }
    }

    public void synchMethod() {
        if (synchRequests) return;
        synchRequests = true;
        new Thread(() -> {
            while (synchRequests) {
                synch();
                int wait = 0;
                while (synchRequests && wait < 100) {
                    wait++;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }).start();
    }

    public void uploadMap(int mapIndex, ArrayList<HeroLocations> list) {
        HttpURLConnection http = null;
        try {
            http = (HttpURLConnection) url.openConnection();
            http.setRequestProperty("Content-Type", "application/json");
            http.setDoOutput(true);
            http.setRequestMethod("POST");

//            String str = "{\"0\": \"0\", \"1\": \"0\", \"2\": \"0\", \"3\": \"0\", \"4\": \"0\", \"5\": \"0\", \"6\": \"0\", \"7\": \"0\", \"8\": \"0\", \"9\": \"0\", \"10\": \"0\", \"11\": \"0\", \"12\": \"0\", \"13\": \"0\", \"14\": \"0\", \"15\": \"0\", \"16\": \"0\", \"17\": \"0\", \"18\": \"0\", \"19\": \"0\", \"20\": \"0\", \"21\": \"0\", \"22\": \"0\", \"23\": \"0\", \"24\": \"0\", \"25\": \"0\", \"26\": \"0\", \"27\": \"0\", \"28\": \"0\", \"29\": \"0\", \"30\": \"0\", \"31\": \"0\", \"32\": \"0\", \"33\": \"0\", \"34\": \"0\", \"35\": \"0\", \"36\": \"0\", \"37\": \"0\", \"38\": \"0\", \"39\": \"0\", \"40\": \"0\", \"41\": \"0\", \"42\": \"0\", \"43\": \"0\", \"44\": \"0\", \"45\": \"0\", \"46\": \"0\", \"47\": \"0\", \"48\": \"0\", \"49\": \"0\", \"50\": \"0\", \"51\": \"0\", \"52\": \"0\", \"53\": \"0\", \"54\": \"0\", \"55\": \"0\", \"56\": \"0\", \"57\": \"0\", \"58\": \"0\", \"59\": \"0\", \"60\": \"0\", \"61\": \"0\", \"62\": \"0\", \"63\": \"0\", \"64\": \"0\", \"65\": \"0\", \"66\": \"0\", \"67\": \"0\", \"68\": \"0\", \"69\": \"0\", \"70\": \"0\", \"71\": \"0\", \"72\": \"0\", \"73\": \"0\", \"74\": \"0\", \"75\": \"0\", \"76\": \"0\", \"77\": \"0\", \"78\": \"0\", \"79\": \"0\", \"80\": \"0\", \"81\": \"0\", \"82\": \"0\", \"83\": \"0\", \"84\": \"0\", \"85\": \"0\", \"86\": \"0\", \"87\": \"0\", \"88\": \"0\", \"89\": \"0\", \"90\": \"0\", \"91\": \"0\", \"92\": \"0\", \"93\": \"0\", \"94\": \"0\", \"95\": \"0\", \"96\": \"0\", \"97\": \"0\", \"98\": \"0\", \"99\": \"0\", \"100\": \"0\", \"101\": \"0\", \"102\": \"0\", \"103\": \"0\", \"104\": \"0\", \"105\": \"0\", \"106\": \"0\", \"107\": \"0\", \"108\": \"0\", \"109\": \"0\", \"110\": \"0\", \"111\": \"0\", \"112\": \"0\", \"113\": \"0\", \"114\": \"0\", \"115\": \"0\", \"116\": \"0\", \"117\": \"0\", \"118\": \"0\", \"119\": \"0\", \"120\": \"0\", \"121\": \"0\", \"122\": \"0\", \"123\": \"0\", \"124\": \"0\", \"125\": \"0\", \"126\": \"0\", \"127\": \"0\", \"mapIdx\": \"0\", \"server\": \"...\"}";
//            JsonObject jsonObject = new JsonParser().parse(str).getAsJsonObject();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("mapIdx", Integer.toString(mapIndex));
            for (int i = 0; i < 128; i++) {
                String num = "0";
                if (i < list.size()) num = Integer.toString(list.get(i).getMarker());
                jsonObject.addProperty(Integer.toString(i), num);
            }

            try (OutputStream os = http.getOutputStream()) {
                byte[] input = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            int responseCode = http.getResponseCode();
//            System.out.println(responseCode + " " + http.getResponseMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (http != null) {
                http.disconnect();
            }
        }
    }

    public void stopSynch() {
        synchRequests = false;
    }

    public void synch() {
        HttpURLConnection http = null;
        try {
            http = (HttpURLConnection) url.openConnection();
            http.setRequestProperty("Accept", "application/json");

            String jsonString = "";
            try (BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                jsonString = response.toString();
            }
            JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
            int mapIndex = jsonObject.get("mapIdx").getAsJsonPrimitive().getAsInt();
            JsonArray ja = jsonObject.get("markerPiIdxs").getAsJsonArray();
            int[] markers = new int[ja.size()];
            for (int i = 0; i < ja.size(); i++) {
                markers[i] = ja.get(i).getAsJsonPrimitive().getAsInt();
            }
            model.synchUpdate(mapIndex, markers);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (http != null) {
                http.disconnect();
            }
        }
    }
}
