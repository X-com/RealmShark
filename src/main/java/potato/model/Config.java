package potato.model;

import org.joml.Vector4f;

import java.io.*;

public class Config implements Serializable {

    private static transient String path = "potato.config";
    public static transient Config instance = new Config();

    public boolean showMap = true;
    public boolean showHeroes = true;
    public boolean showInfo = true;

    public int[] keyValues = {200, 201, 0, 0, 0, 0};
    public String[] keyString = {"MW_UP", "MW_DOWN", "", "", "", ""};

    public int textSize = 32;
    public int shapeSize = 32;
    public boolean singleColorShapes = false;
    public boolean alwaysShowCoords = false;

    public int textTransparency = 200;
    public int mapTransparency = 150;
    public int visitedColor = 0x00FF0064;
    public int activeColor = 0xFF000064;
    public int deadColor = 0xFFFFFF50;
    public int shapesColor = 0xFFFFFF80;
    public transient Vector4f visitedColorVec;
    public transient Vector4f activeColorVec;
    public transient Vector4f deadColorVec;
    public transient Vector4f shapesColorVec;

    public boolean manualAlignment = false;
    public int mapTopLeftX;
    public int mapTopLeftY;
    public int mapWidth = 100;
    public int mapHeight = 100;

    public boolean showPlayerCoords = false;
    public boolean saveMapInfo = false;

    public static void save() {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path));
            out.writeObject(Config.instance);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load() {
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));
            instance = (Config) in.readObject();
            in.close();
        } catch (IOException | ClassNotFoundException e) {
        }

        setColors();
    }

    public static void setColors() {
        instance.visitedColorVec = new Vector4f();
        instance.activeColorVec = new Vector4f();
        instance.deadColorVec = new Vector4f();
        instance.shapesColorVec = new Vector4f();

        intColorToVecColor(instance.visitedColor, instance.visitedColorVec);
        intColorToVecColor(instance.activeColor, instance.activeColorVec);
        intColorToVecColor(instance.deadColor, instance.deadColorVec);
        intColorToVecColor(instance.shapesColor, instance.shapesColorVec);
    }

    public static void intColorToVecColor(int intColor, Vector4f vecColor) {
        int r = intColor >>> 24;
        int g = intColor << 8 >>> 24;
        int b = intColor << 16 >>> 24;
        int a = intColor << 24 >>> 24;

        vecColor.set(r / 255f, g / 255f, b / 255f, a / 255f);
    }
}
