package experimental.map.old;

import packets.Packet;
import packets.data.GroundTileData;
import packets.incoming.MapInfoPacket;
import packets.incoming.UpdatePacket;
import packets.packetcapture.PacketProcessor;
import packets.packetcapture.register.Register;
import util.ImageBuffer;
import util.Pair;
import util.SpriteJson;
import util.Util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

public class TileSaver {

    private static int[][] mapTiles = new int[2048][2048];
    private static boolean runTiles = true;
    private static int count = 0;
    private static final SpriteJson spriteJson = new SpriteJson();

    int[][][] mapArray = new int[13][2048][2048];
    int[][][] mapArray2 = new int[13][2048][2048];
    int[][][][] mapArray3 = new int[13][2][2048][2048];
    int[][][] mapGland = new int[13][2048][2048];
    int[] colors = new int[512];
    static int blue = 0xff2588c5;
    static int red = 0xffff0000;
    static int darkBlue = 0xff294380;
    int blueGradeless = 0x2588c5;
    static PacketProcessor packetProcessor;

    public static void main(String[] args) throws IOException {
        // 1 2 3 4 5 6 7 8 9 10 11 12 13
//        new TileSaver().saveTiles();

//        for(int i = 1; i <= 13; i++)
//            new TileSaver().makeTileData(i);
//        new TileSaver().merger();
//        try {
//        for(int i = 1; i <= 13; i++)
            new TileSaver().makeImageThingy2(1, true);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            while (true) {
//                try {
//                    Thread.sleep(1);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        } finally {
//            System.out.println("closing");
//            packetProcessor.closeSniffer();
//        }
    }

    public void saveTiles() {
        packetProcessor = new PacketProcessor();
        Register.INSTANCE.registerAll(TileSaver::readAll);
        packetProcessor.start();
    }

    private void makeTileData(int i) throws IOException {
        i--;
        loadTiles(i);
//        BufferedImage bi= ImageIO.read(new File("image.jpg"));

//        loadTiles();
//        HashMap<Integer, Integer>[] list = loadTiles2();
        BufferedImage bi = new BufferedImage(2048, 2048, BufferedImage.TYPE_INT_ARGB);

//        ArrayList<Integer> exists = new ArrayList<>();
//        int doMaps = 13;
//        for (int i = 0; i < doMaps; i++) {
//            for (int id : list[i].values()) {
//                if (!exists.contains(id)) {
//                    exists.add(id);
//                    String name = IdToName.getTileTextureName(id, 0);
//                    int index = IdToName.getTileTextureIndex(id, 0);
//                    colors[id] = spriteJson.getSpriteColor(name, index);
//                    if (id == 115) colors[id]--;
//                    System.out.println("color.put(" + id + ", " + colors[id] + ");");
//                }
//            }
//            for (Map.Entry<Integer, Integer> e : list[i].entrySet()) {
//                mapArray[i][e.getKey() % 2048][e.getKey() / 2048] = e.getValue();
//            }
//        }

//        for (int i = 0; i < doMaps; i++) {
//            for (int x = 0; x < bi.getWidth(); x++) {
//                for (int y = 0; y < bi.getHeight(); y++) {
//                    if (mapArray[i][x][y] == 114) {
//                        if (checkAround(i, x, y)) {
//                            addGradientColor(i, x, y);
//                        }
//                    }
//                }
//            }
//        }

//        for (int i = 0; i < 13; i++) {
        for (int x = 0; x < bi.getWidth(); x++) {
            for (int y = 0; y < bi.getHeight(); y++) {
//                    int color = 0x00000000;

//                    if (mapArray[i][x][y] != 0) {
//                        color = colors[mapArray[i][x][y]];
//                    }
//                    if (mapArray2[i][x][y] != 0) {
//                        color = mapArray2[i][x][y];
//                    }
//                    d++;
//                    boolean skip = false;
//                    for (int j = 0; j < 13; j++) {
//                        if (i == j) continue;
//                        int v1 = mapArray[i][x][y];
//                        int v2 = mapArray[j][x][y];
//                        if (v1 == v2) {
//                            skip = true;
//                            break;
//                        }
//                    }
//                    if (skip) {
//                        bi.setRGB(x, y, 0);
//                        c++;
//                        continue;
//                    }
                bi.setRGB(x, y, mapArray[i][x][y]);
            }
        }
        ImageIO.write(bi, "PNG", new File("tiles/tileData" + (i + 1) + ".png"));
    }
//    }

    private void makeImageThingy2(int i, boolean real) throws IOException {
        i--;

//        HashMap<Integer, Integer>[] list = loadTiles2();
//        ArrayList<Integer> exists = new ArrayList<>();
//        for (int i = 0; i < doMaps; i++) {
//            for (int id : list[i].values()) {
//                if (!exists.contains(id)) {
//                    exists.add(id);
//                    String name = IdToName.getTileTextureName(id, 0);
//                    int index = IdToName.getTileTextureIndex(id, 0);
//                    colors[id] = spriteJson.getSpriteColor(name, index);
//                    if (id == 115) colors[id]--;
//                    System.out.println("colors[" + id + "] = " + colors[id] + ";");
//                }
//            }
//        }
//        if (true) return;

        loadTiles(i);
//        colors[12] = -11715543;
//        colors[28] = -12837366;
//        colors[70] = -13269443;
//        colors[71] = -7828963;
//        colors[72] = -12496343;
//        colors[86] = -13813221;
//        colors[87] = -14341060;
//        colors[96] = -13487566;
//        colors[100] = -15132391;
//        colors[112] = -1357035;
//        colors[114] = -11372347;
//        colors[115] = -11372348;
//        colors[188] = -13940085;
//        colors[189] = -4014708;
//        colors[190] = -5338036;
//        colors[208] = -5866414;
//        colors[241] = -13159638;
//        colors[289] = -7828963;
//        colors[291] = -4315595;
//        colors[292] = -11198171;
//        colors[293] = -11198171;
//        colors[294] = -7592924;
//        colors[297] = -15658730;
//        colors[304] = -15658730;
//        colors[305] = -15658730;

        BufferedImage bi = new BufferedImage(2048, 2048, BufferedImage.TYPE_INT_ARGB);

//        for (int i = 0; i < doMaps; i++) {
        for (int x = 0; x < bi.getWidth(); x++) {
            for (int y = 0; y < bi.getHeight(); y++) {
                if (mapArray[i][x][y] == 114) {
                    if (checkAround(mapArray[i], x, y)) {
                        addGradientColor(mapArray[i], i, x, y, blue, 10);
                    }
                }
            }
        }
//        }

        if (!real) {
//        for (int i = 0; i < doMaps; i++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                for (int y = 0; y < bi.getHeight(); y++) {
                    if (mapArray3[i][1][x][y] == 188) {
                        if (checkAround2(i, x, y)) {
//                            mapArray2[i][x][y] = darkBlue;
                            addGradientColor(mapArray[i], i, x, y, blue, 10);
                        }
                    }
                }
            }
//        }

            while (true) {
                if (doGland(i, bi)) break;
            }
        }
//        for (int i = 0; i < doMaps; i++) {
        for (int x = 0; x < bi.getWidth(); x++) {
            for (int y = 0; y < bi.getHeight(); y++) {
                if (real) {
                    int id = mapArray[i][x][y];
//                    if (id != 96) continue;
//                    if (id == 4062) continue;
//                    if (id == 4060) continue;
//                    if (id == 4406) continue;
//                    if (id == 14370) continue;
//                    if (id == 14373) continue;
//                    if (id == 14377) continue;
//                    if (id == 14378) continue;
//                    if (id == 25137) continue;
//                    if (id == 25138) continue;
//                    if (id == 28908) continue;
//                    if (id == 29300) continue;
//                    if (id == 29301) continue;
//                    if (id == 29302) continue;
//                    if (id == 29303) continue;
//                    if (id == 29304) continue;
//                    if (id == 29305) continue;
//                    if (id == 29820) continue;
//                    if (id == 29823) continue;
//                    if (id == 29289) continue;
//                    if (id == 29297) continue;
//                    if (id == 29821) continue;
//                    if (id == 29919) continue;
//                    if (id == 45082) continue;
//                    if (id == 45679) continue;
//                    if (id == 45680) continue;
//                    if (id == 45683) continue;
//                    bi.setRGB(x, y, colors[id]);
                    bi.setRGB(x, y, ImageBuffer.getColor(id));
                }
//                    if(x+70 >= bi.getWidth()) continue;
//                    if(y+80 >= bi.getHeight()) continue;
                if (!real) bi.setRGB(x, y, mapArray2[i][x][y]);
            }
        }
//            ImageIO.write(bi, "PNG", new File("assets/map/map" + (i + 1) + ".png"));
        ImageIO.write(bi, "PNG", new File("tiles/map" + (i + 1) + ".png"));
//        }
    }

    private boolean doGland(int i, BufferedImage bi) {
        System.out.println("doGland");
        int fillX = 0;
        int fillY = 0;

        if (i >= 3 && i <= 7) expand(bi, mapArray[i]);

        int detectSize = 12;
        for (int x = 0; x < bi.getWidth() && fillX == 0; x++) {
            for (int y = 0; y < bi.getHeight() && fillX == 0; y++) {
                if (mapArray[i][x][y] == 96 && mapGland[i][x][y] == 0) {
                    boolean found = true;
                    for (int x2 = -detectSize; x2 <= detectSize && found; x2++) {
                        for (int y2 = -detectSize; y2 <= detectSize && found; y2++) {
                            if (mapArray[i][x + x2][y + y2] != 96) {
                                found = false;
                            } else if (mapGland[i][x][y] != 0) {
                                found = false;
                            }
                        }
                    }
                    if (found) {
                        fillX = x;
                        fillY = y;
                    }
                }
            }
        }
        if (fillX == 0) return true;
        System.out.println(fillX + " " + fillY);

        ArrayList<Pair<Integer, Integer>> filler = new ArrayList<>();
        HashSet<Integer> visited = new HashSet<>();

        filler.add(new Pair<>(fillX, fillY));
        int count = 0;
        while (filler.size() > 0) {
            count++;
            Pair<Integer, Integer> p = filler.remove(0);
            int x = p.left();
            int y = p.right();
            visited.add(x + y * 2048);
            mapGland[i][x][y] = 96;
            x++;
            if (mapArray[i][x][y] == 96 && !visited.contains(x + y * 2048)) {
                filler.add(new Pair<>(x, y));
                visited.add(x + y * 2048);
            }
            x--;
            y++;
            if (mapArray[i][x][y] == 96 && !visited.contains(x + y * 2048)) {
                filler.add(new Pair<>(x, y));
                visited.add(x + y * 2048);
            }
            x--;
            y--;
            if (mapArray[i][x][y] == 96 && !visited.contains(x + y * 2048)) {
                filler.add(new Pair<>(x, y));
                visited.add(x + y * 2048);
            }
            x++;
            y--;
            if (mapArray[i][x][y] == 96 && !visited.contains(x + y * 2048)) {
                filler.add(new Pair<>(x, y));
                visited.add(x + y * 2048);
            }
        }

        for (int ii = 0; ii < 5; ii++) {
            expand(bi, mapGland[i]);
        }
        if (i == 7) expand(bi, mapGland[i]);

        for (int x = 0; x < bi.getWidth(); x++) {
            for (int y = 0; y < bi.getHeight(); y++) {
                if (mapGland[i][x][y] == 96) {
                    if (mapGland[i][x - 1][y] != 96 ||
                            mapGland[i][x + 1][y] != 96 ||
                            mapGland[i][x][y - 1] != 96 ||
                            mapGland[i][x][y + 1] != 96) {
//                        mapArray2[i][x][y] = red;
                        addGradientColor(mapGland[i], i, x, y, red, 10);
                    }
                }
            }
        }
        return false;
    }

    private void expand(BufferedImage bi, int[][] map) {
        int[][] mapTemp = new int[2048][2048];
        for (int x = 0; x < bi.getWidth(); x++) {
            for (int y = 0; y < bi.getHeight(); y++) {
                if (map[x][y] == 96) {
                    mapTemp[x - 1][y] = 96;
                    mapTemp[x + 1][y] = 96;
                    mapTemp[x][y - 1] = 96;
                    mapTemp[x][y + 1] = 96;
                }
            }
        }
        for (int x = 0; x < bi.getWidth(); x++) {
            for (int y = 0; y < bi.getHeight(); y++) {
                if (mapTemp[x][y] == 96) {
                    map[x][y] = 96;
                }
            }
        }
    }

    private void addGradientColor(int[][] mapTiles, int ii, int xx, int yy, int col, int alphaReduce) {
        mapArray2[ii][xx][yy] = col;
        int gradeless = col & 0xffffff;
//        mapArray2[ii][xx][yy] = red;
//        mapArray2[ii][xx][yy] = colors[mapArray[ii][xx][yy]];

//        if(true) return;
        int NUMBER_OF_POINTS = 10;
        int MAX_LENGTH = 25;
        // (di, dj) is a vector - direction in which we move right now
        int di = 1;
        int dj = 0;
        // length of current segment
        int segment_length = 1;
        long alphaNew = 255;

        // current position (i, j) and how much of current segment we passed
        int i = 0;
        int j = 0;
        int segment_passed = 0;
//        for (int k = 0; k < NUMBER_OF_POINTS; ++k) {
        while (true) {
            // make a step, add 'direction' vector (di, dj) to current position (i, j)
            i += di;
            j += dj;
            ++segment_passed;
//            System.out.println(i + " " + j);
            int x = xx + i;
            int y = yy + j;
//            if (alphaNew <= 0) {
            if (i > MAX_LENGTH) {
                return;
            } else if ((shouldDo(mapTiles, x, y) && col == blue) || (shouldDo2(mapTiles, x, y) && col == darkBlue) || (shouldDo3(mapTiles, x, y) && col == red)) {
                alphaNew = findAlpha(mapArray2, ii, x, y, alphaReduce);
                if (alphaNew > 0 && getAlpha(mapArray2[ii][x][y]) < alphaNew) {

                    long rgb = gradeless + (alphaNew << 24);
//                    System.out.printf("%x %x %x\n", rgb & 0xff, alphaNew, alphaNew * 0x1000000);
                    mapArray2[ii][x][y] = (int) rgb;
                }
            }

            if (segment_passed == segment_length) {
                // done with current segment
                segment_passed = 0;

                // 'rotate' directions
                int buffer = di;
                di = -dj;
                dj = buffer;

                // increase segment length if necessary
                if (dj == 0) {
                    ++segment_length;
                }
            }
        }
    }

    private int getAlpha(int i) {
        return i >>> 24;
    }

    private int findAlpha(int[][][] m, int i, int x, int y, int alphaReduce) {
        int alphaBig = 0;
        int alpha = getAlpha(m[i][x + 1][y]);
        if (alphaBig < alpha) alphaBig = alpha;
        alpha = getAlpha(m[i][x - 1][y]);
        if (alphaBig < alpha) alphaBig = alpha;
        alpha = getAlpha(m[i][x][y + 1]);
        if (alphaBig < alpha) alphaBig = alpha;
        alpha = getAlpha(m[i][x][y - 1]);
        if (alphaBig < alpha) alphaBig = alpha;

        if (alphaBig != 0) return alphaBig - alphaReduce;

        alpha = getAlpha(m[i][x + 1][y + 1]);
        if (alphaBig < alpha) alphaBig = alpha;
        alpha = getAlpha(m[i][x - 1][y - 1]);
        if (alphaBig < alpha) alphaBig = alpha;
        alpha = getAlpha(m[i][x - 1][y + 1]);
        if (alphaBig < alpha) alphaBig = alpha;
        alpha = getAlpha(m[i][x + 1][y - 1]);
        if (alphaBig < alpha) alphaBig = alpha;

        return alphaBig - alphaReduce * 2;
    }

    private boolean shouldDo(int[][] map, int x, int y) {
        if (x <= 1 || x >= 2047 || y <= 1 || y >= 2047) return false;
        return !notTiles(map[x][y]);
    }

    private boolean shouldDo2(int[][] map, int x, int y) {
        if (x <= 1 || x >= 2047 || y <= 1 || y >= 2047) return false;
        return map[x][y] == 188 || map[x][y] == 0;
    }

    private boolean shouldDo3(int[][] map, int x, int y) {
        if (x <= 1 || x >= 2047 || y <= 1 || y >= 2047) return false;
        return map[x][y] == 96;
    }

    private boolean checkAround(int[][] mapGland, int x, int y) {
        int t1 = mapGland[x + 1][y];
        int t2 = mapGland[x - 1][y];
        int t3 = mapGland[x][y + 1];
        int t4 = mapGland[x][y - 1];

        return notTiles(t1) || notTiles(t2) || notTiles(t3) || notTiles(t4);
    }

    private boolean checkAround2(int i, int x, int y) {
        int t1 = mapArray[i][x + 1][y];
        int t2 = mapArray[i][x - 1][y];
        int t3 = mapArray[i][x][y + 1];
        int t4 = mapArray[i][x][y - 1];

        return notTiles2(t1) || notTiles2(t2) || notTiles2(t3) || notTiles2(t4);
    }

    private boolean notTiles(int id) {
        return id != 114 && id != 188 && id != 0 && id != 96 && id != 112;
    }

    private boolean notTiles2(int id) {
        return id == 115;
    }

    private void merger() {
        HashMap<Integer, Integer>[] raw = loadTiles2();
        int c = 0;
        for (int i = 0; i < 13; i++) {
            ArrayList<Integer> ints = new ArrayList<>();
            for (Map.Entry<Integer, Integer> e : raw[i].entrySet()) {
                int xy = e.getKey();
                boolean skip = false;
                for (int j = 0; j < 13; j++) {
                    if (i == j) continue;
                    if (Objects.equals(raw[j].get(xy), e.getValue())) {
                        skip = true;
                        break;
                    }
                }
                if (skip) {
                    c++;
                    continue;
                }
                int number = e.getKey() + e.getValue() * 4194304;
                ints.add(number);
            }
            int[] arr = ints.stream().filter(Objects::nonNull).mapToInt(j -> j).toArray();
            saveToFile(i + 1, arr);
        }
        System.out.println(c);
    }

    private void saveToFile(int i, int[] ints) {
        File saveFile = new File("tiles/tileMap" + i + ".data");
        try {
            FileOutputStream fileOut = new FileOutputStream(saveFile);
            try (ObjectOutputStream objOut = new ObjectOutputStream(fileOut)) {
                objOut.writeObject(ints);
            }
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HashMap<Integer, Integer>[] loadTiles2() {
        HashMap<Integer, Integer>[] maps = new HashMap[13];
        try {
            for (int i = 1; i <= 13; i++) {
                File f = new File("tiles/map" + i + ".data");
                if (!f.exists()) {
                    System.out.println("missing: " + f);
                    maps[i - 1] = new HashMap<>();
                    continue;
                }
                maps[i - 1] = new HashMap<>();
                BufferedReader br = new BufferedReader(new FileReader(f));
                String line;
                while ((line = br.readLine()) != null) {
                    try {
                        String[] s = line.split(":");
                        int x = Integer.parseInt(s[0]);
                        int y = Integer.parseInt(s[1]);
                        int t = Integer.parseInt(s[2]);
                        maps[i - 1].put(x + y * 2048, t);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
                br.close();

                int subFile = 0;
                while (true) {
                    subFile++;
                    File f2 = new File("tiles/map" + i + "." + subFile + ".data");
                    if (!f2.exists()) {
                        break;
                    }
                    System.out.println(f2);

                    br = new BufferedReader(new FileReader(f2));
                    while ((line = br.readLine()) != null) {
                        try {
                            String[] s = line.split(":");
                            int x = Integer.parseInt(s[0]);
                            int y = Integer.parseInt(s[1]);
                            int t = Integer.parseInt(s[2]);
                            maps[i - 1].put(x + y * 2048, t);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                    br.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            maps = null;
        }
        return maps;
    }

    private void loadTiles(int i) {
        try {
//            for (int i = 1; i <= 13; i++) {
            File f = new File("tiles/mappedData/map" + (i + 1) + ".data");
            if (!f.exists()) {
                System.out.println("missing: " + f);
                return;
            }

            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    String[] s = line.split(":");
                    int x = Integer.parseInt(s[0]);
                    int y = Integer.parseInt(s[1]);
                    int t = Integer.parseInt(s[2]);
                    mapArray[i][x][y] = t;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            int subFile = 0;
            while (true) {
                subFile++;
                File f2 = new File("tiles/mappedData/map" + (i + 1) + "." + subFile + ".data");
                if (!f2.exists()) {
                    break;
                }
                System.out.println(f2);

                br = new BufferedReader(new FileReader(f2));
                while ((line = br.readLine()) != null) {
                    try {
                        String[] s = line.split(":");
                        int x = Integer.parseInt(s[0]);
                        int y = Integer.parseInt(s[1]);
                        int t = Integer.parseInt(s[2]);
                        mapArray[i][x][y] = t;
                        mapArray3[i][subFile - 1][x][y] = t;
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                br.close();
            }
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readAll(Packet packet) {
        if (packet instanceof UpdatePacket) {
            if (!runTiles) return;
            UpdatePacket p = (UpdatePacket) packet;
            if (p.pos.x != 0 && p.pos.y != 0) System.out.println(p.pos.x + " " + p.pos.y + " " + count);
            GroundTileData[] tiles = p.tiles;
            for (int i = 0; i < tiles.length; i++) {
                count++;
                GroundTileData gtd = tiles[i];
                mapTiles[gtd.x][gtd.y] = gtd.type;
            }
        } else if (packet instanceof MapInfoPacket) {
            MapInfoPacket p = (MapInfoPacket) packet;
            if (!p.displayName.equals("{s.rotmg}")) {
                runTiles = false;
                for (int i = 0; i < 2048; i++) {
                    for (int j = 0; j < 2048; j++) {
                        int t = mapTiles[i][j];
                        if (t != 0) {
                            Util.print("tiles/map", String.format("%d:%d:%d", i, j, t));
                        }
                    }
                }
                System.out.println("done");
            } else {
                for (int[] row : mapTiles) {
                    Arrays.fill(row, 0);
                }
                count = 0;
                runTiles = true;
            }
        }
    }
}
