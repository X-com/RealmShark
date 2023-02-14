package potato.control;

import com.google.gson.*;
import potato.model.DataModel;
import util.WebSocket;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class ServerSynch {
    static private final String urlString = "ws://217.27.177.69:6000";
    private final DataModel model;
    private WebSocket webSocket;

    boolean synchRequests = false;

    public static void main(String[] args) {
        ServerSynch s = new ServerSynch(null);
        s.temp();
    }

    private void temp() {
        System.out.println("test 1");
        startSynch(1, 2, 4, 4, 5, 6);
        System.out.println("test 2");
        uploadSingleHero(1, 2, 3);
        System.out.println("test 3");
        webSocket.close();
    }

    public ServerSynch(DataModel model) {
        this.model = model;
        try {
            webSocket = new WebSocket(urlString) {
                @Override
                public void onMessage(String message) {
                    incoming(message);
                }
            };
            webSocket.connectBlocking(10, TimeUnit.SECONDS);
        } catch (URISyntaxException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void startSynch(int myId, int locationIp, long seed, int map, int x, int y) {
        if (synchRequests) return;
        synchRequests = true;

        sub(myId, locationIp, seed, map, x, y);
    }

    public void stopSynch(int myId) {
        unsub(myId);
        synchRequests = false;
    }

    public void uploadSingleHero(int myId, int markIndex, int colorIndex) {
        if (!synchRequests) return;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("packet", "hero");
        jsonObject.addProperty("user", myId);
        jsonObject.addProperty("heroId", markIndex);
        jsonObject.addProperty("state", colorIndex);
//        System.out.println(jsonObject);
        byte[] out = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
        webSocket.sendBytes(out);
    }

    public void sub(int myId, int locationIp, long seed, int map, int x, int y) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("packet", "sub");
        jsonObject.addProperty("user", myId);
        jsonObject.addProperty("ip", locationIp);
        jsonObject.addProperty("seed", seed);
        jsonObject.addProperty("map", map);
        jsonObject.addProperty("x", x);
        jsonObject.addProperty("y", y);
//        System.out.println(jsonObject);

        byte[] out = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
        webSocket.sendBytes(out);
    }

    public void unsub(int myId) {
        if (!synchRequests) return;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("packet", "unsub");
        jsonObject.addProperty("user", myId);
//        System.out.println(jsonObject);

        byte[] out = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
        webSocket.sendBytes(out);
    }

//    private void uploadMap(int myId, long seed, int map) {
//        JsonObject jsonObject = new JsonObject();
//        jsonObject.addProperty("packet", "update");
//        jsonObject.addProperty("user", myId);
//        jsonObject.addProperty("seed", seed);
//        jsonObject.addProperty("map", map);
//        System.out.println(jsonObject);
//        byte[] out = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
//        webSocket.sendBytes(out);
//    }

    private void incoming(String message) {
//        System.out.println(message);
        JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
        String type = jsonObject.get("packet").getAsJsonPrimitive().getAsString();
        if (type.equals("hero")) {
            int hero = jsonObject.get("heroId").getAsJsonPrimitive().getAsInt();
            int state = jsonObject.get("state").getAsJsonPrimitive().getAsInt();
            if (model != null) model.heroSynch(hero, state);
        } else if (type.equals("subbed")) {
            int mapIndex = jsonObject.get("map").getAsJsonPrimitive().getAsInt();
            JsonArray ja = jsonObject.get("markers").getAsJsonArray();
            int[] markers = new int[ja.size()];
            for (int i = 0; i < ja.size(); i++) {
                markers[i] = ja.get(i).getAsJsonPrimitive().getAsInt();
            }
            if (model != null) model.initSynch(mapIndex, markers);
        }
    }

    public void dispose() {
        unsub(0);
    }
}
