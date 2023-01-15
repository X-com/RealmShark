package potato.model;

import util.Util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Stream;

public class Bootloader {

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
}
