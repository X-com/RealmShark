package experimental;

import packets.data.GroundTileData;
import packets.data.StatData;
import packets.data.enums.StatType;
import potato.model.data.IdData;
import potato.model.Bootloader;
import potato.model.HeroLocations;
import util.IdToName;
import util.ImageBuffer;
import util.Pair;
import util.Util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public class PatternFinder {

    private static final int CHEST = 1281;
    private static final int LAVA_PATH = 339;

    PatternFinder pf;
    static int[][] mapArray;
    static ArrayList<Pair<String, int[][]>> mapArrayAll = new ArrayList<>();
    static ArrayList<Entity> entitys = new ArrayList<>();
    static ArrayList<HeroLocations>[] locs;
    static ArrayList<Pair<String, ArrayList<GroundTileData>>> shapes;
    HeroLocations temp;
    int foundCounter = 0;
    int[] verified10 = {2, 3, 4, 6, 7, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 43, 44, 45, 46, 47, 48, 49, 53, 54, 55, 56, 57, 59, 60, 61, 62, 63, 64, 66};
    int[] verified13 = {1, 2, 3, 4, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 76, 77};
    ArrayList<String> verifier = new ArrayList<>();

    public static void main(String[] args) {
        new PatternFinder().run();
    }

    public void run() {
        locs = Bootloader.loadMapCoords();

        pf = new PatternFinder();

        lookForPattern();
//        for (int mapIndex = 0; mapIndex < 13; mapIndex++)
//            fileWalker(mapIndex+1);
//        saveType();
//        makeSkipMap();
//        findObject();
//        makeSkipMap();
    }

    private void findObject() {
//        int map = 10;
//        int location = 30;
//        float x = locs[map - 1].get(location - 1).getX();
//        float y = locs[map - 1].get(location - 1).getY();
//        loadFile("map10.1.data");
//        for (Entity e : entitys) {
//            if (e.dist(x, y) < 3) {
//                System.out.println(e);
//            }
//        }

        loadFile("map1-4.data");
        entityToTile();
        System.out.println(mapArray[1149][848]);
//        for (int x = 0; x < 2048; x++) {
//            for (int y = 0; y < 2048; y++) {
//                int id = mapArray[x][y];
//                if (id == 275) System.out.println(x + " " + y);
//            }
//        }
    }

    private void makeSkipMap() {
        loadFile("allObjects-2022-11-07-10.19.30.data");
        createMap("map10", 9);
//        loadFile("map13-2.data");
//        createMap("skip2", 12);
//        loadFile("map13-3.data");
//        createMap("skip3", 12);
    }

    private void lookForPattern() {
        for (int mapIndex = 0; mapIndex < 13; mapIndex++) {
            System.out.println("---map" + (mapIndex + 1) + "---");
            if(mapIndex == 9) continue;
//            int mapIndex = 7 - 1;
//            {
            try (Stream<Path> filePathStream = Files.walk(Paths.get("tiles/pattern"))) {
                int finalMapIndex = mapIndex;
                filePathStream.forEach(filePath -> {
                    if (Files.isRegularFile(filePath)) {
                        String name = filePath.getFileName().toString();
                        if (name.contains("map" + (finalMapIndex + 1) + "-")) {
                            loadFile(name);
                            entityToTile();
                            mapArrayAll.add(new Pair(name, mapArray));
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

            SpiralOut spiral = new SpiralOut(pf::findStructure);

            fixShapes();

            int count = 0;
            for (HeroLocations h : locs[mapIndex]) {
                count++;
                pf.temp = h;
                if (!spiral.spiral(Integer.MAX_VALUE, 10, 0, 0)) {
                    System.out.println(h.getIndexString() + " " + h.getX() + "," + h.getY());
//                    System.out.println(h.getX() + "," + h.getY());
                }
//                if (pf.findStructure(0, 0)) {
////                System.out.println(h.getX() + "," + h.getY());
//                }
            }

            System.out.println(locs[mapIndex].size() - pf.foundCounter);
//            System.out.println(pf.foundCounter + " " + verified.length);
            for (String s : pf.verifier) System.out.print(", " + s);
            System.out.println();
            mapArrayAll.clear();
            pf.foundCounter = 0;
        }
    }

    private void entityToTile() {
        for (Entity e : entitys) {
            if (e.type == IdData.ENT_CHERRY_TREE) {
                mapArray[(int) e.x][(int) e.y] = IdData.ENT_CHERRY_TREE;
            } else if (e.type == IdData.GRAY_WALL) {
                mapArray[(int) e.x][(int) e.y] = IdData.GRAY_WALL;
            } else if (e.type == IdData.LAVA_PATH) {
                mapArray[(int) e.x][(int) e.y] = IdData.LAVA_PATH;
            } else if (e.type == IdData.WOODEN_WALL_HOUSE) {
                mapArray[(int) e.x][(int) e.y] = IdData.WOODEN_WALL_HOUSE;
            } else if (e.type == IdData.DEATH_TREE_MANOR) {
                mapArray[(int) e.x][(int) e.y] = IdData.DEATH_TREE_MANOR;
            } else if (e.type == IdData.LILLYPAD) {
                mapArray[(int) e.x][(int) e.y] = IdData.LILLYPAD;
            }
        }
    }

    private void fixShapes() {
        shapes = new ArrayList<>();
        try (Stream<Path> filePathStream = Files.walk(Paths.get("tiles/setPieces"))) {
            filePathStream.forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    String name = filePath.getFileName().toString();
                    ArrayList<GroundTileData> tiles = loadFileSetPiece(name);
                    shapes.add(new Pair<>(name, tiles));

                    for (int q = 0; q < 3; q++) {
                        ArrayList<GroundTileData> rotation = new ArrayList<>();
                        ArrayList<GroundTileData> last = shapes.get(shapes.size() - 1).right();
                        for (GroundTileData l : last) {
                            GroundTileData gtd = new GroundTileData();
                            gtd.x = l.y;
                            gtd.y = (short) -l.x;
                            gtd.type = l.type;
                            rotation.add(gtd);
                        }
                        shapes.add(new Pair<>(name, rotation));
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean findStructure(int i, int j) {
        int x = temp.getX();
        int y = temp.getY();

        for (Pair<String, int[][]> map : mapArrayAll) {
            for (Pair<String, ArrayList<GroundTileData>> shape : shapes) {
                boolean found = false;
                int count = 0;
                for (GroundTileData gtd : shape.right()) {
                    int dx = gtd.x;
                    int dy = gtd.y;
                    int type = gtd.type;
                    int id = map.right()[x + i + dx][y + j + dy];
                    if (tileCheck(type, id)) {
//                        if(temp.getIndex() == 5-1) System.out.println("hero:" + temp.getIndexString() + " map:" + map.left() + " " + shape.left() + " count:" + count + " x:" + (x + i + dx) + " y:" + (y + j + dy) + " should:" + type + " is:" + id + " " + IdToName.tileName(id));
//                    if (id != 0 && (id == IdData.YELLOW_HIGHLAND_TILE || id == IdData.GRAY_GODLAND_TILE)) {
                        found = false;
                        break;
                    } else if (id == type) {
                        found = true;
                    }
                }
                if (found) {
                    foundCounter++;
//                    System.out.println((temp.getX() + i) + "," + (temp.getY() + j));
                    if (i != 0 || j != 0) {
                        System.out.println("structure: " + temp.getIndexString() + " matches: " + shape.left() + " rotation: " + (shapes.indexOf(shape) % 4) + " map: " + map.left() + " offset: (" + i + "," + j + ")");
                        System.out.println((temp.getX() + i) + "," + (temp.getY() + j));
                    }
                    verifier.add(temp.getIndexString());
//                    return true;
                }
            }
        }
        return false;
    }

    private boolean tileCheck(int type, int id) {
        if (id == 289) return false; // Dragon Egg Grass

        if (id == 29821) return false; // Shatters Bridge Rim H (ava)
        if (id == 29289) return false; // Shatters Castle Brick Damaged (ava)
        if (id == 29300) return false; // Shatters Bookshelf Floor (ava)
        if (id == 29301) return false; // Shatters Gravel (ava)

        if (id == 4250) return false; // EH Floor (nest)
        if (id == 6813) return false; // EH UnderPartialFloor Venom (nest)

        if (id == 4062) return false; // Hanami Grass (temple)

        if (id == 45082) return false; // LH Main Tile (sentry)
        if (id == 45104) return false; // LH Sentry Tile (sentry)

        if (id == 25) return false; // Castle Stone Floor Tile Dark (skull)
        if (id == 24) return false; // Castle Stone Floor Tile (skull)

//        if (id == 245) return false; // Gold Sand (sphinx)
//        if (id == 247) return false; // Sand Tile (sphinx)

        if (id == 250) return false; // GhostWater (ghost ship)
        if (id == 237) return false; // Ghost Water Beach (ghost ship)

        if (id == 45718) return false; // AI Forax Ground 2 (alien)
        if (id == 45722) return false; // AI Katalund Ground 2 (alien)

        if (id == 6174) return type != 292; // remove dead tree check from para (6174 dead tree)

        return id != 0 && id != type;
    }

    private void saveType() {
        int map = 10;
        int index = 11;
//        int location = 14;
//        int location = 28;
//        int location = 29;
//        int location = 30;
        int location = 38;
        String name = "lava";

        int rad = 35;
        String mapName = "map" + map + "-" + index;
        loadFile(mapName + ".data");
        int X = locs[map - 1].get(location - 1).getX();
        int Y = locs[map - 1].get(location - 1).getY();
        int type = IdData.DEMON_LAVA_TILE;

        if (false) {
            for (int i = 0; i < 20; i++) {
                int a = mapArray[627 + i][893];
                System.out.println(a + " " + IdToName.tileName(a));
            }
//            for(int i = 0; i < para2.length; i+=2){
//                int x = para2[i];
//                int y = para2[i+1];
//                if(x < 5 && x > -5 && y < 4 && y > -5) continue;
//                System.out.printf(", %d, %d", x, y);
//            }

            return;
        }
        mapArrayAll.add(new Pair(mapName, mapArray));
        fixShapes();
        pf.temp = locs[map - 1].get(location - 1);
        if (pf.findStructure(0, 0)) {
            System.out.println("already exists");
            return;
        }

        String filename;
        {
            String path = "tiles/setPieces/";
            int num = 0;
            while (true) {
                num++;
                filename = path + name + num + ".data";
                System.out.println(filename);
                if (!new File(filename).exists()) {
                    break;
                }
            }
        }
        if (true) {
            System.out.println("saving set piece");
            Util.print(filename + "-", String.valueOf(type));
            for (int x = -rad; x <= rad; x++) {
                for (int y = -rad; y <= rad; y++) {
                    int i = mapArray[x + X][y + Y];
                    if (i == type) {
                        Util.print(filename + "-", x + "," + y);
                    }
                }
            }
            return;
        }
        if (false) {
            int[] AA = null;
            int[] BB = Arrays.copyOf(AA, AA.length);
            for (int q = 1; q <= 3; q++) {
                AA = Arrays.copyOf(BB, BB.length);
                for (int i = 0; i < BB.length; i += 2) {
                    BB[i] = AA[i + 1];
                    BB[i + 1] = -AA[i];
                }
            }
            for (int i : BB) System.out.print("," + i);
            return;
        }

        BufferedImage bi = new BufferedImage(500, 200, BufferedImage.TYPE_INT_ARGB);

        ArrayList<GroundTileData> list = new ArrayList<>();
        for (int x = -rad; x <= rad; x++) {
            for (int y = -rad; y <= rad; y++) {
                int i = mapArray[x + X][y + Y];
                if (i == type) {
                    GroundTileData g = new GroundTileData();
                    g.x = (short) x;
                    g.y = (short) y;
                    list.add(g);
                }
            }
        }
        ArrayList<GroundTileData> A;
        ArrayList<GroundTileData> B = list;

        for (int q = 1; q <= 4; q++) {
            A = B;
            B = new ArrayList<>();
            for (GroundTileData g : A) {
                int x = g.x + 100 * q;
                int y = g.y + 100;
                bi.setRGB(x, y, Color.red.getRGB());
                GroundTileData gr = new GroundTileData();
                gr.x = g.y;
                gr.y = (short) -g.x;
                B.add(gr);
            }
            bi.setRGB(100 * q, 100, Color.GREEN.getRGB());
        }

        try {
            ImageIO.write(bi, "PNG", new File("tiles/" + "t" + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fileWalker(int map) {
        try (Stream<Path> filePathStream = Files.walk(Paths.get("tiles/pattern"))) {
            filePathStream.forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    String name = filePath.getFileName().toString();
                    if (name.contains("map" + map + "-")) {
                        loadFile(name);
                        String[] s = name.split("\\.");
                        createMap(s[0], map - 1);
                        clear();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void read() {
        loadFile("map10-2022-10-21-20.24.04.data");

        int rngItems = 0;
        int chests = 0;
        for (Entity e : entitys) {
            if (e.type == CHEST) {
                chests++;
                int items = 0;
                int pot = 0;
                int slots = 0;
                for (StatData sd : e.stats) {
                    if (sd.statTypeNum >= 8 && sd.statTypeNum <= 15) {
                        if (sd.statValue != -1) {
                            items++;
                        } else {
                            break;
                        }
                        slots++;
                        if (sd.statValue == 2595 || sd.statValue == 2594) {
                            pot++;
                        }
                    }
                }
                if (pot != 2) {
                    System.out.println("incorrect pots " + pot);
                    System.out.println(e);
                }
                if (slots != items) System.out.println("missing item");
                rngItems += items - 2;
            }
        }
        System.out.println("chests:" + chests + " rngItems:" + rngItems);

        createMap("entityMap", 9);
    }

    private void createMap(String name, int map) {
        BufferedImage bi = new BufferedImage(2048, 2048, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < bi.getWidth(); x++) {
            for (int y = 0; y < bi.getHeight(); y++) {
                int id = mapArray[x][y];
                bi.setRGB(x, y, ImageBuffer.getColor(id));
            }
        }
        Graphics g = bi.getGraphics();
        for (HeroLocations h : locs[map]) {
            if (name.startsWith("skip") && verified(h.getIndex() + 1)) continue;
            g.setColor(Color.red);
            g.drawRect(h.getX() - 35, h.getY() - 35, 70, 70);
            g.drawRect(h.getX(), h.getY(), 0, 0);
            g.setFont(new Font(Font.SERIF, Font.PLAIN, 10));
//            String s = h.getIndexString() + (verified(h.getIndex() + 1) ? "V" : "");
            String s = h.getIndexString();
            g.drawString(s, h.getX(), h.getY() - 37);
        }
        for (Entity e : entitys) {
            if (e.type == CHEST) {
                g.setColor(Color.ORANGE);
                g.setFont(new Font(Font.SERIF, Font.PLAIN, 10));
                g.drawString(String.valueOf(e.x), (int) e.x, (int) e.y + 20);
                g.drawString(String.valueOf(e.y), (int) e.x, (int) e.y + 29);
                g.drawRect((int) e.x, (int) e.y, 0, 0);
            } else if (e.type == LAVA_PATH) {
                g.setColor(Color.YELLOW);
                g.drawRect((int) e.x, (int) e.y, 0, 0);
            }
        }
        g.dispose();

        try {
            ImageIO.write(bi, "PNG", new File("tiles/" + name + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean verified(int i) {
        for (int v : verified13) {
            if (v == i) return true;
        }
        return false;
    }

    private ArrayList<GroundTileData> loadFileSetPiece(String filename) {
        ArrayList<GroundTileData> tiles = new ArrayList<>();
        try {
            File f = new File("tiles/setPieces/" + filename);
            if (!f.exists()) {
                System.out.println("missing: " + f);
                return null;
            }

            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            int tileCount = 0;
            line = br.readLine();
            int tile = Integer.parseInt(line);
            while ((line = br.readLine()) != null) {
                try {
                    String[] s = line.split(",");
                    if (s.length == 2) {
                        short x = Short.parseShort(s[0]);
                        short y = Short.parseShort(s[1]);
                        GroundTileData gtd = new GroundTileData();
                        gtd.x = x;
                        gtd.y = y;
                        gtd.type = tile;
                        tiles.add(gtd);
                        tileCount++;
                    }
                } catch (NumberFormatException e) {
                    System.out.println(line);
                    e.printStackTrace();
                }
            }
//            System.out.println("tiles: " + tiles + " entitys: " + entitys.size());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tiles;
    }

    private void loadFile(String filename) {
        try {
            File f = new File("tiles/pattern/" + filename);
            if (!f.exists()) {
                System.out.println("missing: " + f);
                return;
            }

            entitys.clear();
            mapArray = new int[2048][2048];
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            int tiles = 0;
            while ((line = br.readLine()) != null) {
                try {
                    String[] s = line.split(":");
                    if (s.length == 3) {
                        int x = Integer.parseInt(s[0]);
                        int y = Integer.parseInt(s[1]);
                        int t = Integer.parseInt(s[2]);
                        mapArray[x][y] = t;
                        tiles++;
                    } else if (s.length == 4) {
                        String type = s[0];
                        String x = s[1];
                        String y = s[2];
                        String[] stats = s[3].split(";");
                        entitys.add(new Entity(type, x, y, stats));
                    }
                } catch (NumberFormatException e) {
                    System.out.println(line);
                    e.printStackTrace();
                }
            }
//            System.out.println("tiles: " + tiles + " entitys: " + entitys.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clear() {
        for (int[] row : mapArray) {
            Arrays.fill(row, 0);
        }
        entitys.clear();
    }

    public class Entity {

        int type;
        float x;
        float y;
        StatData[] stats;

        public Entity(String t, String x, String y, String[] s) {
            this.type = Integer.parseInt(t);
            this.x = Float.parseFloat(x);
            this.y = Float.parseFloat(y);
            this.stats = new StatData[s.length / 4];
            for (int i = 0; i < stats.length; i++) {
                StatData sd = new StatData();
                sd.statValue = Integer.parseInt(s[i * 4]);
                sd.statValueTwo = Integer.parseInt(s[i * 4 + 1]);
                sd.stringStatValue = s[i * 4 + 2];
                sd.statTypeNum = Integer.parseInt(s[i * 4 + 3]);
                sd.statType = StatType.byOrdinal(sd.statTypeNum);
                stats[i] = sd;
            }
        }

        @Override
        public String toString() {
            return "Entity{" +
                    "\n   type=" + type + " " + IdToName.objectName(type) +
                    "\n   x=" + x +
                    "\n   y=" + y +
                    "\n   stats=" + Arrays.toString(stats);
        }

        public double dist(float x, float y) {
            return Math.sqrt(Math.pow(this.x - x, 2) * Math.pow(this.y - y, 2));
        }
    }

    private void distanceCheck() {
        double dist = Double.MAX_VALUE;
        ArrayList<HeroLocations>[] locs = Bootloader.loadMapCoords();
        for (int i = 0; i < 13; i++) {
            for (int j = 0; j < locs[i].size(); j++) {
                HeroLocations h1 = locs[i].get(j);
                for (int k = j + 1; k < locs[i].size(); k++) {
                    HeroLocations h2 = locs[i].get(k);
                    double d = Math.sqrt(h1.squareDistTo(h2.getX(), h2.getY()));
                    if (d < dist) {
                        dist = d;
                        System.out.println(dist + " " + j + " " + k);
                        System.out.println(h1.getX() - h2.getX());
                        System.out.println(h1.getY() - h2.getY());
                    }
                }
            }
        }
    }
}
