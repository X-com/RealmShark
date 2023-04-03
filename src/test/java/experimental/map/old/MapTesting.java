package experimental.map.old;

import packets.Packet;
import packets.data.GroundTileData;
import packets.incoming.MapInfoPacket;
import packets.incoming.UpdatePacket;
import packets.packetcapture.PacketProcessor;
import packets.packetcapture.register.Register;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;

public class MapTesting {

    public static void main(String[] args) {
        new MapTesting().test();
    }

    private HashSet<Integer>[] loadTiles2() {
//        HashMap<Integer, Integer>[] maps = new HashMap[13];
        HashSet<Integer>[] maps = new HashSet[13];
        try {
            for (int i = 1; i <= 13; i++) {
                File f = new File("assets/map/tileMap" + i + ".data");
                if (!f.exists()) {
                    System.out.println("File missing: " + f);
                    return null;
                }
                System.out.println(f);
                FileInputStream fileIn = new FileInputStream(f);
                maps[i - 1] = new HashSet<>();
                try (ObjectInputStream objIn = new ObjectInputStream(fileIn)) {
                    int[] array = (int[]) objIn.readObject();
                    for (int num : array) {
//                        int xy = num % 4194304;
//                        int t = num / 4194304;
                        maps[i - 1].add(num);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return maps;
    }

    private HashSet<Integer>[] loadTiles3() {
        HashSet<Integer>[] maps = new HashSet[13];
        try {
            for (int i = 1; i <= 13; i++) {
                File f = new File("tiles/tileData" + i + ".png");
                if (!f.exists()) {
                    System.out.println("File missing: " + f);
                    return null;
                }
                BufferedImage bi = ImageIO.read(f);
                maps[i - 1] = new HashSet<>();
                for (int x = 0; x < bi.getWidth(); x++) {
                    for (int y = 0; y < bi.getHeight(); y++) {
                        /* Apply the green mask */
                        int num = bi.getRGB(x, y);
                        if (num != 0) {
                            maps[i - 1].add(x + y * 2048 + num * 4194304);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return maps;
    }

    private HashMap<Integer, Integer>[] loadTiles() {
        HashMap<Integer, Integer>[] maps = new HashMap[13];
        try {
            for (int i = 1; i <= 13; i++) {
                File f = new File("assets/map/tileMap" + i + ".data");
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
            }
        } catch (IOException e) {
            e.printStackTrace();
            maps = null;
        }
        return maps;
    }

    HashSet<Integer>[] list2;
    HashSet<Integer>[] list3;

    private void test() {
        list2 = loadTiles2();
        list3 = loadTiles3();
        System.out.println("done loading file");
        Register.INSTANCE.registerAll(this::readAll);
        PacketProcessor packetProcessor = new PacketProcessor();
        packetProcessor.start();
    }

    int check = 0;

    public void readAll(Packet packet) {
        if (packet instanceof UpdatePacket) {
            if (check > 0) {
                UpdatePacket p = (UpdatePacket) packet;
                findMapIndex(p.tiles);
                check--;
            }
//            if (p.pos.x != 0 && p.pos.y != 0) System.out.println(p.pos.x + " " + p.pos.y + " " + count);
//            GroundTileData[] tiles = p.tiles;
//            for (int i = 0; i < tiles.length; i++) {
//                GroundTileData gtd = tiles[i];
//                mapTiles[gtd.x][gtd.y] = gtd.type;
//            }
        } else if (packet instanceof MapInfoPacket) {
            MapInfoPacket p = (MapInfoPacket) packet;
            if (p.displayName.equals("{s.rotmg}")) {
                check = 1;
            }
        }
    }

    private void findMapIndex(GroundTileData[] tiles) {
        int[] maps2 = new int[13];
        int[] maps3 = new int[13];
        for (GroundTileData t : tiles) {
//            int index = t.x + t.y * 2048;
//            int type = t.type;
            int test = -1;
            int num = t.x + t.y * 2048 + t.type * 4194304;
            for (int map = 0; map < list2.length; map++) {
//                Integer i = list[map].get(index);
                if (list2[map].contains(num)) {
//                    System.out.println("map found: " + map);
                    maps2[map]++;
//                    if (test != -1) System.out.println("dual tile " + t + " " + (map + 1) + " " + (test + 1));
//                    test = map + type * 100000;
                }
                if (list3[map].contains(num)) {
//                    System.out.println("map found: " + map);
                    maps3[map]++;
//                    if (test != -1) System.out.println("dual tile " + t + " " + (map + 1) + " " + (test + 1));
//                    test = map + type * 100000;
                }
            }
        }
        System.out.println("-----------");
        int i2 = 0;
        int win2 = 0;
        int mapWin2 = 0;
        int tot2 = 0;
        for (int m : maps2) {
            i2++;
            if (m > 0) {
                System.out.println("map" + i2 + ":" + m);
                if (m > win2) {
                    win2 = m;
                    mapWin2 = i2;
                }
            }
            tot2 += m;
        }

        int i3 = 0;
        int win3 = 0;
        int mapWin3 = 0;
        int tot3 = 0;
        for (int m : maps3) {
            i3++;
            if (m > 0) {
                System.out.println("map" + i3 + ":" + m);
                if (m > win3) {
                    win3 = m;
                    mapWin3 = i3;
                }
            }
            tot3 += m;
        }
        System.out.println("2:" + tot2 + " / " + tiles.length);
        if (win3 > 500) System.out.println("IT IS MAP " + mapWin2);
        else System.out.println("maybe map " + mapWin2);
        System.out.println();
        System.out.println("3:" + tot3 + " / " + tiles.length);
        if (win3 > 500) System.out.println("IT IS MAP " + mapWin3);
        else System.out.println("maybe map " + mapWin3);
    }
}
