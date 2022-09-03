package util;

import static com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;

import com.google.gson.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Json parser specific made for realm sprite json.
 */
public class SpriteJson implements JsonDeserializer<SpriteJson> {
    private static String spriteJson = "assets/spritesheet.json";

    private static HashMap<String, HashMap<Integer, Sprite>> sprites;
    private static HashMap<String, Sprite> animatedSprites;

    /**
     * Static class used to load the json file and parse the json.
     */
    static {
        try {
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(SpriteJson.class, new SpriteJson());
            builder.registerTypeAdapter(Sprite.class, new Sprite());
            Gson gson = builder.setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES).create();
            String json = new Scanner(new File(spriteJson)).useDelimiter("\\Z").next();
            gson.fromJson(json, SpriteJson.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Simple constructor used in json parsing.
     */
    public SpriteJson() {
    }

    /**
     * Main constructor used for constructing the json sprite data.
     *
     * @param sprites         Hash list of all sprites.
     * @param animatedSprites Hash list of all animated sprites. (currently not implemented fully)
     */
    public SpriteJson(HashMap<String, HashMap<Integer, Sprite>> sprites, HashMap<String, Sprite> animatedSprites) {
        this.sprites = sprites;
        this.animatedSprites = animatedSprites;
    }

    /**
     * Retrieves sprite coordinates used in sprite atlases based on sprite group name and index.
     *
     * @param name  Name of the sprite group.
     * @param index Index of the sprite in the group.
     * @return Integer
     */
    public int[] getSprite(String name, int index) {
        HashMap<Integer, Sprite> list = sprites.get(name);
        Sprite sprite = list.get(index);
        return new int[]{sprite.position.x, sprite.position.y, sprite.position.w, sprite.position.h, sprite.aId};
    }

    /**
     * Returns all sprite data parsed by the json.
     *
     * @return Hash of the sprite data.
     */
    public HashMap<String, HashMap<Integer, Sprite>> getSprites() {
        return sprites;
    }


    /**
     * Returns all animated sprite data parsed by the json.
     *
     * @return Hash of the animated sprite data.
     */
    public HashMap<String, Sprite> getAnimatedSprites() {
        return animatedSprites;
    }

    /**
     * Json parser used to unwrap the Unity sprite json.
     */
    @Override
    public SpriteJson deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        HashMap<String, HashMap<Integer, Sprite>> sprites = new HashMap<>(); // sprites
        HashMap<String, Sprite> animatedSprites = new HashMap<>(); // animatedSprites
        JsonObject jsonObject = element.getAsJsonObject();

        JsonArray spritesArray = jsonObject.getAsJsonArray("sprites");
        for (JsonElement spriteArray : spritesArray) {
            JsonObject spriteObject = spriteArray.getAsJsonObject();
            String name = spriteObject.getAsJsonPrimitive("spriteSheetName").getAsString();
            JsonArray elements = spriteObject.getAsJsonArray("elements");
            HashMap<Integer, Sprite> list = new HashMap<>();
            for (JsonElement spriteElement : elements) {
                Sprite sprite = context.deserialize(spriteElement, Sprite.class);
                if (!list.containsKey(sprite.index)) {
                    list.put(sprite.index, sprite);
                } else {
                    throw new RuntimeException("Double insertion to same sprite index.");
                }
            }
            sprites.put(name, list);
        }

        // TODO: fix animated sprites.
//        JsonArray animatedArray = jsonObject.getAsJsonArray("animatedSprites");
//        for (JsonElement animated : animatedArray) {
//            JsonObject animatedObject = animated.getAsJsonObject();
//            int index = animatedObject.getAsJsonPrimitive("index").getAsInt();
//            String name = animatedObject.getAsJsonPrimitive("spriteSheetName").getAsString();
//            int direction = animatedObject.getAsJsonPrimitive("direction").getAsInt();
//            String action = animatedObject.getAsJsonPrimitive("action").getAsString();
//            int set = animatedObject.getAsJsonPrimitive("set").getAsInt();
//            JsonObject spriteData = animatedObject.getAsJsonObject("spriteData");
//            Sprite sprite = context.deserialize(spriteData, Sprite.class);
//            sprite.setAnimationVars(index, direction, action, set);
//            animatedSprites.put(name, sprite);
//        }

        return new SpriteJson(sprites, animatedSprites);
    }

    /**
     * Sprite class used to store sprite atlas coordinate data parsed by the json.
     */
    static class Sprite implements JsonDeserializer<Sprite> {
        int padding;
        int index;
        int aId;
        boolean isT;
        ImgCoords position;
        ImgCoords maskPosition;
        Color mostCommonColor;

        int animatedIndex;
        int animatedDirection;
        String animatedAction;
        int animatedSet;

        public void setAnimationVars(int index, int direction, String action, int set) {
            animatedIndex = index;
            animatedDirection = direction;
            animatedAction = action;
            animatedSet = set;
        }

        @Override
        public Sprite deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Sprite s = new Sprite();
            JsonObject jsonObject = json.getAsJsonObject();

            s.padding = jsonObject.getAsJsonPrimitive("padding").getAsInt();
            s.index = jsonObject.getAsJsonPrimitive("index").getAsInt();
            s.aId = jsonObject.getAsJsonPrimitive("aId").getAsInt();
            s.isT = jsonObject.getAsJsonPrimitive("isT").getAsBoolean();

            JsonElement pos = jsonObject.getAsJsonObject("position");
            s.position = new ImgCoords();
            s.position.x = pos.getAsJsonObject().getAsJsonPrimitive("x").getAsInt();
            s.position.y = pos.getAsJsonObject().getAsJsonPrimitive("y").getAsInt();
            s.position.w = pos.getAsJsonObject().getAsJsonPrimitive("w").getAsInt();
            s.position.h = pos.getAsJsonObject().getAsJsonPrimitive("h").getAsInt();

            JsonElement mask = jsonObject.getAsJsonObject("maskPosition");
            s.maskPosition = new ImgCoords();
            s.maskPosition.x = mask.getAsJsonObject().getAsJsonPrimitive("x").getAsInt();
            s.maskPosition.y = mask.getAsJsonObject().getAsJsonPrimitive("y").getAsInt();
            s.maskPosition.w = mask.getAsJsonObject().getAsJsonPrimitive("w").getAsInt();
            s.maskPosition.h = mask.getAsJsonObject().getAsJsonPrimitive("h").getAsInt();

            JsonElement color = jsonObject.getAsJsonObject("mostCommonColor");
            s.mostCommonColor = new Color();
            s.mostCommonColor.r = color.getAsJsonObject().getAsJsonPrimitive("r").getAsInt();
            s.mostCommonColor.g = color.getAsJsonObject().getAsJsonPrimitive("g").getAsInt();
            s.mostCommonColor.b = color.getAsJsonObject().getAsJsonPrimitive("b").getAsInt();
            s.mostCommonColor.a = color.getAsJsonObject().getAsJsonPrimitive("a").getAsInt();

//            for (Map.Entry<String, JsonElement> entry : pos.getAsJsonObject().entrySet()) {
//                System.out.println(entry.getKey());
//            }
//            JsonArray spritesArray = jsonObject.getAsJsonArray("sprites");

            return s;
        }

        class ImgCoords {
            int x;
            int y;
            int w;
            int h;

            @Override
            public String toString() {
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("(x:%d, y:%d, w:%d, h:%d)", x, y, w, h));
                return sb.toString();
            }
        }

        /**
         * Color class storing basic color data.
         */
        class Color {
            int r;
            int g;
            int b;
            int a;

            @Override
            public String toString() {
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("(r:%d, g:%d, b:%d, a:%d)", r, g, b, a));
                return sb.toString();
            }
        }

        /**
         * To string for debuging Json parsing.
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("padding:%d, index:%d, aId%d, isT:%b, position:%s, maskPosition:%s, mostCommonColor:%s", padding, index, aId, isT, position, maskPosition, mostCommonColor));
            return sb.toString();
        }
    }
}