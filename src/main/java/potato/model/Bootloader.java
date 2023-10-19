package potato.model;

import util.Util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;

public class Bootloader {
    public static final int SNAKE_TILE_CENTERING = 36;

    public static BufferedImage[] loadMaps() {
        BufferedImage[] img = new BufferedImage[13];
        try {
            for (int i = 1; i <= 13; i++) {
                String file = "potatoRes/map/map" + i + ".png";
                InputStream is = Util.resourceFilePath(file);
                img[i - 1] = ImageIO.read(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
            img = null;
        }
        return img;
    }

    public static ArrayList<HeroLocations>[] loadMapCoords() {
        @SuppressWarnings("unchecked")
        ArrayList<HeroLocations>[] coords = new ArrayList[13];
        try {
            for (int i = 1; i <= 13; i++) {
                String file = "potatoRes/map/heroMap" + i + ".txt";
                InputStream is = Util.resourceFilePath(file);
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                coords[i - 1] = new ArrayList<>();
                String line;
                int index = 0;
                while ((line = br.readLine()) != null) {
                    try {
                        String[] s = line.split(" ");
                        int x = Integer.parseInt(s[0]);
                        int y = Integer.parseInt(s[1]);
                        int t = Integer.parseInt(s[2]);
                        coords[i - 1].add(new HeroLocations(index, x, y, t));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    index++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            coords = null;
        }
        return coords;
    }

    public static HashSet<Integer>[] loadTiles() {
        @SuppressWarnings("unchecked")
        HashSet<Integer>[] maps = new HashSet[13];
        try {
            for (int i = 1; i <= 13; i++) {
                String file = "potatoRes/map/tileData" + i + ".png";
                InputStream is = Util.resourceFilePath(file);
                BufferedImage bi = ImageIO.read(is);
                maps[i - 1] = new HashSet<>();
                for (int x = 0; x < bi.getWidth(); x++) {
                    for (int y = 0; y < bi.getHeight(); y++) {
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

    public static boolean[][] loadSnakePattern() {
        boolean[][] tiles = new boolean[SNAKE_TILE_CENTERING * 2][SNAKE_TILE_CENTERING * 2];
        try {
            for (int i = 1; i <= 13; i++) {
                String file = "potatoRes/snakePattern.txt";
                InputStream is = Util.resourceFilePath(file);
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line = br.readLine();
                int tileId = Integer.parseInt(line);
                while ((line = br.readLine()) != null) {
                    try {
                        String[] s = line.split(",");
                        if (s.length == 2) {
                            int x = Short.parseShort(s[0]);
                            int y = Short.parseShort(s[1]);
                            tiles[x + SNAKE_TILE_CENTERING][y + SNAKE_TILE_CENTERING] = true;
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tiles;
    }

    public static HashSet<Integer>[] loadSpawnCoords() {
        @SuppressWarnings("unchecked")
        HashSet<Integer>[] spawnCoords = new HashSet[13];
        for (int i = 0; i < 13; i++) {
            spawnCoords[i] = new HashSet<>();
        }
        try {
            String file = "potatoRes/map/spawnCoords.txt";
            InputStream is = Util.resourceFilePath(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    String[] s = line.split(" ");
                    int map = Integer.parseInt(s[0]) - 1;
                    int x = Integer.parseInt(s[1]);
                    int y = Integer.parseInt(s[2]);
                    spawnCoords[map].add(x + y * 2048);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            spawnCoords = null;
        }
        return spawnCoords;
    }
}
