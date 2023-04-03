package experimental.map.old;

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

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
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

public class SetPieces extends JFrame {
    JFrame frame;
    private boolean running;
    private Canvas canvas;
    private BufferStrategy strategy;

    ArrayList<HeroLocations>[] locs;
    ArrayList<Pair<String, ArrayList<GroundTileData>>> shapes;
    int[][] mapArray;
    ArrayList<Entity> entitys;
    int rad = 35;

    int map = 6;
    int mapindex = 1;
    int hero;
    boolean renderWhat = true;

    public SetPieces() {
        locs = Bootloader.loadMapCoords();
        loadShapes();

        frame = new JFrame();
        JPanel panel = new JPanel();

        canvas = new Canvas() {
            @Override
            public void paint(Graphics g) {
                render((Graphics2D) g);
            }
        };
        canvas.setBackground(new Color(0, 0, 0));
        canvas.setSize(72 * 14, 72 * 14);
        panel.add(canvas);

        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.CENTER);

        JButton prev = new JButton("Prev");
        JButton next = new JButton("Next");
        JButton zoom = new JButton("Zoom");
        JButton save = new JButton("Save");
        JButton mapPrev = new JButton("M-Prev");
        JButton mapNext = new JButton("M-Next");

        prev.addActionListener(e -> prevHero());
        next.addActionListener(e -> nextHero());
        zoom.addActionListener(e -> zoomMap());
        save.addActionListener(e -> saveHero());
        mapPrev.addActionListener(e -> mapPrev());
        mapNext.addActionListener(e -> mapNext());

        Panel buttons = new Panel();
        buttons.add(prev);
        buttons.add(next);
        buttons.add(zoom);
        buttons.add(save);
        buttons.add(mapPrev);
        buttons.add(mapNext);

        frame.pack();
        frame.add(buttons, BorderLayout.SOUTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        frame.createBufferStrategy(3);
        strategy = frame.getBufferStrategy();
    }

    private void mapPrev() {
        mapindex--;
        if (mapindex <= 0) mapindex = 1;

        loadMap();
    }

    private void mapNext() {
        mapindex++;
//        if (mapindex > 15) mapindex = 15;

        loadMap();
    }

    private void prevHero() {
        hero--;
        while (hero >= 0) {
            HeroLocations h = locs[map - 1].get(hero);
            if (!findStructure(0, 0, h)) {
                break;
            }
            hero--;
        }

        if (hero < 0) {
            hero = 0;
            System.out.println("start " + hero);
            return;
        }

        frame.setTitle("Map " + map + "-" + mapindex + "  Hero: " + (hero + 1));
        canvas.repaint();
    }

    private void nextHero() {
        hero++;
        while (hero < locs[map - 1].size()) {
            HeroLocations h = locs[map - 1].get(hero);
            if (!findStructure(0, 0, h)) {
                break;
            }
            hero++;
        }

        if (hero > locs[map - 1].size()) {
            hero = locs[map - 1].size();
            return;
        }

        frame.setTitle("Map " + map + "-" + mapindex + "  Hero: " + (hero + 1));
        canvas.repaint();
    }

    private void saveHero() {
        HeroLocations h = locs[map - 1].get(hero);
        saveToFile(h);
//        nextHero();
    }

    private void zoomMap() {
        renderWhat = !renderWhat;
        canvas.repaint();
    }

    public void loadMap() {
        loadFile("map" + map + "-" + mapindex + ".data");
        entityToTile();
//        hero = 0;
//        while (hero < locs[map - 1].size()) {
//            HeroLocations h = locs[map - 1].get(hero);
//            if (!findStructure(0, 0, h)) {
//                break;
//            }
//            hero++;
//        }
        frame.setTitle("Map " + map + "-" + mapindex + "  Hero: " + (hero + 1));

        canvas.repaint();
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

    public void render(Graphics2D g) {
        if (renderWhat) {
            renderMap(g);
        } else {
            renderHero(g);
        }
        g.dispose();
        strategy.show();
    }

    public void renderMap(Graphics2D g) {
        HeroLocations h;
        if (locs[map - 1].size() == hero) {
            h = locs[map - 1].get(hero - 1);
        } else {
            h = locs[map - 1].get(hero);
        }


        BufferedImage bi = new BufferedImage(2048, 2048, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < bi.getWidth(); x++) {
            for (int y = 0; y < bi.getHeight(); y++) {
                int id = mapArray[x][y];
                bi.setRGB(x, y, getColor(id));
            }
        }

        {
            Graphics2D gg = (Graphics2D) bi.getGraphics();
            gg.setStroke(new BasicStroke(5));
            gg.setColor(Color.red);
            System.out.printf("%d %d\n", h.getX() - rad, h.getY() - rad);
            gg.drawRect(h.getX() - rad, h.getY() - rad, 70, 70);
            bi.setRGB(h.getX(), h.getY(), Color.red.getRGB());
            gg.setFont(new Font(Font.SERIF, Font.PLAIN, 50));
            String s = h.getIndexString();
            gg.drawString(s, h.getX(), h.getY() - 37);
            gg.dispose();
        }

        g.drawImage(bi, 0, 0, canvas.getWidth(), canvas.getHeight(), null);
    }

    public void renderHero(Graphics2D g) {
        if (locs[map - 1].size() == hero) {
            g.drawString("End", 0, 0);
            return;
        }

        BufferedImage bi = new BufferedImage(71, 71, BufferedImage.TYPE_INT_ARGB);

        HeroLocations h = locs[map - 1].get(hero);
        int xx = h.getX();
        int yy = h.getY();
        int xOff = 35;
        int yOff = 35;

        for (int x = 0; x < bi.getWidth(); x++) {
            for (int y = 0; y < bi.getHeight(); y++) {
                int id = mapArray[xx + x - xOff][y + yy - yOff];
                bi.setRGB(x, y, getColor(id));
            }
        }

        int x3 = xx - rad;
        int y3 = yy - rad;

//        for (Entity e : entitys) {
//            if (e.type == IdData.CHEST) {
////            if(!list.contains(e.type)) {
////                list.add(e.type);
////                System.out.println(e);
////            }
//                int x4 = (int) (e.x - x3);
//                int y4 = (int) (e.y - y3);
//
////                int color = ImageBuffer.getColor(e.type);
//
//                int color = Color.GREEN.getRGB();
//                if (x4 >= 0 && x4 <= rad * 2) {
//                    if (y4 >= 0 && y4 <= rad * 2) {
//                        bi.setRGB(x4, y4, color);
//                    }
//                }
//            }
//        }

        bi.setRGB(35, 35, Color.red.getRGB());
        g.drawImage(bi, 0, 0, canvas.getWidth(), canvas.getHeight(), null);
    }

    private int getColor(int id) {
        int color = ImageBuffer.getColor(id);
        if (id == IdData.ENT_CHERRY_TREE) color = Color.PINK.getRGB();
        if (id == IdData.GRAY_WALL) color = Color.GRAY.getRGB();
        if (id == IdData.LAVA_PATH) color = Color.YELLOW.getRGB();
        if (id == IdData.WOODEN_WALL_HOUSE) color = Color.YELLOW.getRGB();
        if (id == IdData.DEATH_TREE_MANOR) color = Color.ORANGE.getRGB();
        if (id == IdData.LILLYPAD) color = Color.ORANGE.getRGB();
        return color;
    }

    private boolean findStructure(int i, int j, HeroLocations h) {
        int x = h.getX();
        int y = h.getY();

        System.out.println();
        for (Pair<String, ArrayList<GroundTileData>> shape : shapes) {
            boolean found = false;
            int count = 0;
            for (GroundTileData gtd : shape.right()) {
                count++;
                int dx = gtd.x;
                int dy = gtd.y;
                int type = gtd.type;
                int xx = x + i + dx;
                int yy = y + j + dy;

                if (Math.abs(dx) > rad) continue;
                if (Math.abs(dy) > rad) continue;

                int id = mapArray[xx][yy];
                if (tileCheck(type, id)) {
//                    if (shape.left().contains("cyclops")){
                    if (count > 4) {
                        System.out.println(shape.left() + " " + count + " " + IdToName.tileName(id) + " " + id + " " + xx + " " + yy);
                    }
                    found = false;
                    break;
                } else if (id == type) {
                    found = true;
                }
            }
            if (found) {
//                System.out.println(h.getIndex() + " " + shape.left());
//                System.out.println("structure: " + temp.getIndexString() + " matches: " + shape.left() + " rotation: " + (shapes.indexOf(shape) % 4) + " map: " + map.left() + " offset: (" + i + "," + j + ")");
//                    if (i != 0 || j != 0) System.out.println((temp.getX() + i) + "," + (temp.getY() + j));
//                    System.out.println((temp.getX() + i) + "," + (temp.getY() + j));
//                verifier.add(temp.getIndexString());
                return true;
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

        if (id == 245) return false; // Gold Sand (sphinx)
        if (id == 247) return false; // Sand Tile (sphinx)

        if (id == 250) return false; // GhostWater (ghost ship)
        if (id == 237) return false; // Ghost Water Beach (ghost ship)

        if (id == 45718) return false; // AI Forax Ground 2 (alien)
        if (id == 45722) return false; // AI Katalund Ground 2 (alien)

        return id != 0 && id != type;
    }

    public void saveToFile(HeroLocations h) {
        String name = "";
        int type = 0;

        int X = h.getX();
        int Y = h.getY();

        for (Entity e : entitys) {
            if (e.dist(X, Y) > 30) continue;

//            System.out.println(e.x + " " + e.y);
//            System.out.println(IdToName.objectName(e.type) + " " + h.getX() + " " + h.getY() + " " + e.type + " " + e.dist(h.getX(), h.getY()));
            if (e.type == IdData.LILLYPAD) {
                type = IdData.LILLYPAD;
                name = "oasis";
                break;
            } else if (e.type == IdData.GRAVEYARD_CROSS) {
                type = IdData.GRAY_WALL;
                name = "grave";
                break;
            } else if (e.type == IdData.DEATH_TREE_MANOR) {
                type = IdData.DEATH_TREE_MANOR;
                name = "manor";
                break;
            } else if (e.type == IdData.LAVA_PATH) {
                type = IdData.LAVA_PATH;
                name = "lava";
                break;
            } else if (e.type == IdData.ENT_CHERRY_TREE) {
                type = IdData.ENT_CHERRY_TREE;
                name = "ent";
                break;
            } else if (e.type == IdData.WOODEN_WALL_HOUSE) {
                type = IdData.WOODEN_WALL_HOUSE;
                name = "house";
                break;
            }
        }
        if (name.equals("")) {
            for (int x = -rad; x <= rad; x++) {
                for (int y = -rad; y <= rad; y++) {
                    int id = mapArray[x + X][y + Y];
                    if (id == IdData.PHENIX_BLACK_TILE) {
                        type = IdData.PHENIX_BLACK_TILE;
                        name = "phenix";
                        break;
                    } else if (id == IdData.LICH_BLUE_TILE) {
                        type = IdData.LICH_BLUE_TILE;
                        name = "lich";
                        break;
                    } else if (id == IdData.GRAY_WALL) {
                        type = IdData.GRAY_WALL;
                        name = "ghost";
                        if (mapArray[x + X + 1][y + Y] == IdData.WATER_DEEP_TILE) {
                            name = "cyclops";
                            break;
                        } else if (mapArray[x + X - 1][y + Y] == IdData.WATER_DEEP_TILE) {
                            name = "cyclops";
                            break;
                        } else if (mapArray[x + X][y + Y + 1] == IdData.WATER_DEEP_TILE) {
                            name = "cyclops";
                            break;
                        } else if (mapArray[x + X][y + Y - 1] == IdData.WATER_DEEP_TILE) {
                            name = "cyclops";
                            break;
                        }
                    }
                }
            }
        }
        if (name.equals("")) return;

        System.out.println("type: " + type + " name: " + name);

//        mapArrayAll.add(new Pair(mapName, mapArray));
//        fixShapes();
//        pf.temp = locs[map - 1].get(location - 1);
//        if (pf.findStructure(0, 0)) {
//            System.out.println("already exists");
//            return;
//        }

        String filename;
        {
            String path = "tiles/setPieces/";
            int num = 0;
            while (true) {
                num++;
                filename = path + name + num + ".data";
//                System.out.println(filename);
                if (!new File(filename).exists()) {
                    break;
                }
            }
        }
        ArrayList<GroundTileData> tiles = new ArrayList<>();
        for (int x = -rad; x <= rad; x++) {
            for (int y = -rad; y <= rad; y++) {
                int i = mapArray[x + X][y + Y];
                if (i == type) {
                    GroundTileData gtd = new GroundTileData();
                    gtd.x = (short) x;
                    gtd.y = (short) y;
                    gtd.type = type;
                    tiles.add(gtd);
                }
            }
        }

        if (tiles.size() == 0) return;

        Util.print(filename + "-", String.valueOf(type));
        for (GroundTileData gtd : tiles) {
            Util.print(filename + "-", gtd.x + "," + gtd.y);
        }

        shapes.add(new Pair<>(filename, tiles));
    }

    private void loadFile(String filename) {
        mapArray = new int[2048][2048];
        entitys = new ArrayList<>();

        try {
            File f = new File("tiles/pattern/" + filename);
            if (!f.exists()) {
                System.out.println("missing: " + f);
                return;
            }

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

    private void loadShapes() {
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
            return "Entity{" + "\n   type=" + type + " " + IdToName.objectName(type) + "\n   x=" + x + "\n   y=" + y + "\n   stats=" + Arrays.toString(stats);
        }

        public double dist(float x, float y) {
            return Math.sqrt(Math.pow(this.x - x, 2) * Math.pow(this.y - y, 2));
        }
    }

    public static void main(String[] args) {
        new SetPieces().loadMap();
    }
}
