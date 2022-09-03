package experimental;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.RescaleOp;
import java.io.*;
import java.util.Scanner;

//import com.google.gson.*;
//import com.google.gson.stream.JsonReader;
//import com.google.gson.stream.JsonToken;

import javax.imageio.ImageIO;
import javax.swing.*;

//import javax.json.Json;
//import javax.json.*;

public class JsonRead {

    private static String assetEquip = "assets/spritesheet.json";
    private static String assetObjects = "assets/objects.png";
    public static BufferedImage sprites;

    public static void main(String[] args) {
        System.out.println("clearconsole");
        try {
//            new JsonRead().test11();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getSprite(int x, int y, int w, int h) throws IOException {
        BufferedImage bigImg = ImageIO.read(new File(assetObjects));

        sprites = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
        sprites = bigImg.getSubimage(x, y, w, h);
        makeFrame();
//        public void paint(Graphics g) {
//            Graphics2D g2 = (Graphics2D) g;
//            g2.drawImage(sprites, null, 10, 10);
//        }
    }

    public void makeFrame() {
        transform.translate(150, 140);
        transform.scale(10, 10);

        mainPanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g;
                g2.drawImage(sprites, transform, null);
            }
        };

        JFrame frame = new JFrame("    Tomato    ");
        frame.setContentPane(mainPanel);
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    static AffineTransform transform = new AffineTransform();
    JPanel mainPanel;

    String textureName = "lofiObj5";
    int textureInt = 0x30;

//    public void test11() throws IOException {
//        FileReader f = new FileReader(assetEquip);
//        JsonReader reader = Json.createReader(f);
//        JsonObject obj = reader.readObject();
//        reader.close();
//
//        JsonArray arraySprites = obj.getJsonArray("sprites");
//
//        for (int i1 = 0; i1 < arraySprites.size(); i1++) {
//            JsonObject o1 = arraySprites.getJsonObject(i1);
//            String spriteName = o1.getJsonString("spriteSheetName").getString();
//            if (spriteName.equals(textureName)) {
//                JsonArray elements = o1.getJsonArray("elements");
//                for (int i2 = 0; i2 < elements.size(); i2++) {
//                    JsonObject o2 = elements.getJsonObject(i2);
//                    System.out.println(o2);
//                    if (o2.getJsonNumber("index").intValue() == textureInt) {
//                        JsonObject o3 = o2.getJsonObject("position");
//                        int x = o3.getInt("x");
//                        int y = o3.getInt("y");
//                        int w = o3.getInt("w");
//                        int h = o3.getInt("h");
//                        System.out.println(o3);
//                        getSprite(x, y, w, h);
//                    }
//                }
//                return;
//            }
//            JsonArray array2 = obj.getJsonArray("elements");
//            for(int i2 = 0; i2 < array2.size(); i2++) {
//                JsonObject o2 = array2.getJsonObject(i2);
//                System.out.println(o2.keySet());
//            }
//        }
//
//        for (String o : obj.keySet()) {
//            System.out.println(o);
//            JsonArray arr = obj.getJsonArray(o);
//            System.out.println(arr.size());
//            for (JsonValue v : arr) {
//                v.getValueType()
//                System.out.println(v.getValueType());
//            }
//        }
//    }

    public void test13() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String myJson = new Scanner(new File(assetEquip)).useDelimiter("\\Z").next();
        String json = gson.toJson(myJson);
        BufferedWriter writer = new BufferedWriter(new FileWriter("assets/pretty.json"));
        writer.append(json);
        writer.close();
    }

    public void test12() throws FileNotFoundException {
        GsonBuilder builder = new GsonBuilder();
        String myJson = new Scanner(new File(assetEquip)).useDelimiter("\\Z").next();
        Object o = builder.create().fromJson(myJson, Object.class);
        System.out.println(o);
    }

//    public void test10() throws FileNotFoundException {
//        FileReader f = new FileReader(assetEquip);
//        JsonReader reader = Json.createReader(f);
//        JsonObject empObj = reader.readObject();
////        reader.close();
//
//        System.out.println(empObj);
//    }

//    public void test9() throws IOException, InterruptedException {
//        FileReader f = new FileReader(assetEquip);
//        JsonReader jr = Json.createReader(f);
//        System.out.println(jr.read());
//        while (jr.hasNext()) {
//            JsonToken jt = jr.peek();
//            if (jt == JsonToken.BEGIN_OBJECT) {
//                jr.beginObject();
//                System.out.println("BEGIN_OBJECT");
//            } else if (jt == JsonToken.BEGIN_ARRAY) {
//                jr.beginArray();
//                System.out.println("BEGIN_ARRAY");
//            } else if (jt == JsonToken.END_ARRAY) {
//                jr.endArray();
//                System.out.println("END_ARRAY");
//            } else if (jt == JsonToken.NAME) {
//                System.out.printf("NAME = %s\n", jr.nextName());
//            } else if (jt == JsonToken.STRING) {
//                System.out.printf("STRING = %s\n", jr.nextString());
//            } else if (jt == JsonToken.NUMBER) {
//                System.out.printf("NUMBER = %d\n", jr.nextInt());
//            } else if (jt == JsonToken.BOOLEAN) {
//                System.out.printf("BOOLEAN = %b\n", jr.nextBoolean());
//            } else if (jt == JsonToken.END_OBJECT) {
//                jr.endObject();
//                System.out.println("END_OBJECT");
//            } else if (jt == JsonToken.END_DOCUMENT) {
//                System.out.println("END_DOCUMENT");
//            } else {
//                System.out.println(jt+"--------------");
//                Thread.sleep(100);
//            }
//        }
//    }

    public void test8() throws FileNotFoundException {
//        Gson gson = new GsonBuilder()
//                .setLenient()
//                .create();
//
//        JsonParser jp = new JsonParser();
//        JsonReader jr = new JsonReader(new FileReader(assetEquip));
//        jr.setLenient(true);
//        JsonElement je = jp.parse(assetEquip);
//        System.out.println(jr.parse(assetEquip));
    }

    public void test7() {
//        Gson gson = new GsonBuilder()
//                .setLenient()
//                .setPrettyPrinting()
//                .create();
//        JsonParser jp = new JsonParser();
//        JsonElement je = jp.parse(assetEquip);
//        System.out.println(gson.toJson(je));
    }

    public void test6() throws FileNotFoundException/*, JsonIOException, JsonSyntaxException */ {
//        GsonBuilder gb = new GsonBuilder();
//        gb.setLenient();
//        Gson gson = gb.setPrettyPrinting().create();
//        String myJson = new Scanner(new File(assetEquip)).useDelimiter("\\Z").next();
//        JsonParser jp = new JsonParser();
//        JsonReader jr = new JsonReader(new StringReader(assetEquip));
//        jr.setLenient(true);
//        JsonElement je = jp.parse(new StringReader(assetEquip));
//        String prettyJsonString = gson.toJson(je);
//        System.out.println(jp.parse(assetEquip));
//        System.out.println(myJson);
    }

    public void test5() {
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        JsonParser jp = new JsonParser();
//        System.out.println(gson.toJson(jp.parse(assetEquip)));
    }

    public void test4() throws Exception {
        File f = new File(assetEquip);
        if (f.exists()) {
//            JSONParser parser = new JSONParser();
//            InputStream is = new FileInputStream(f);
//            String jsonTxt = IOUtils.toString(is, "UTF-8");
//            System.out.println(is);
//            JSONObject json = new JSONObject(is);
//            String a = json.getString("1000");
//            System.out.println(new JSONObject().toString(2));
        }
    }

    public void test3() {
//        try {
//            JSONParser parser = new JSONParser();
//            //Use JSONObject for simple JSON and JSONArray for array of JSON.
////            String myJson = new Scanner(new File("/assets/spritesheet.json")).useDelimiter("\\Z").next();
//            Object data = (Object) parser.parse("assets/spritesheet.json");//path to the JSON file.
//
//            String json = data.toJSONString();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public void test2() {
//from object to JSON
//        Gson gson = new Gson();
//        gson.toJson(yourObject);

// from JSON to object
//        Object o = new Gson().fromJson(new FileReader("c:\\exer4-courses.json"),Object.class);
    }

    public void test1() {
//        String jsonString = ...; //assign your JSON String here
//        JSONObject obj = new JSONObject(jsonString);
//        String pageName = obj.getJSONObject("pageInfo").getString("pageName");
//
//        JSONArray arr = obj.getJSONArray("posts"); // notice that `"posts": [...]`
//        for (int i = 0; i < arr.length(); i++) {
//            String post_id = arr.getJSONObject(i).getString("post_id");
//    ......
//        }
    }
}
