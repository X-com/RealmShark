package potato.model;

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

    private static final String[] type = {"circle", "demon", "phoenix", "cyclops", "ghost", "oasis", "ent", "lich", "parasite", "coffin", "snake", "cross", "house"};
    private static final int[] colorStates = {0x9000ff00, 0x90ff0000, 0x40ffffff}; // red, green, white

    public static BufferedImage[] loadMaps() {
        BufferedImage[] img = new BufferedImage[13];
        try {
            for (int i = 1; i <= 13; i++) {
                img[i - 1] = ImageIO.read(new File("assets/map/map" + i + ".png"));
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
                File f = new File("assets/map/heroData" + i + ".txt");
                BufferedReader br = new BufferedReader(new FileReader(f));
                coords[i - 1] = new ArrayList<>();
                String line;
                int index = 0;
                while ((line = br.readLine()) != null) {
                    try {
                        String[] s = line.split(",");
                        int x = Integer.parseInt(s[0]);
                        int y = Integer.parseInt(s[1]);
                        coords[i - 1].add(new HeroLocations(index, x, y));
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

    public static BufferedImage[] loadHeroIcons() {
        BufferedImage[] list = new BufferedImage[13];
        try (Stream<Path> filePathStream = Files.walk(Paths.get("assets/heroes"))) {
            filePathStream.forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    int typeIndex = -1;
                    for (int i = 0; i < 13; i++) {
                        if (filePath.toString().contains(type[i])) {
                            typeIndex = i;
                        }
                    }
                    try {
                        list[typeIndex] = ImageIO.read(new File(filePath.toString()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//                    for (int colorIndex = 0; colorIndex < 3; colorIndex++) {
//                        int index = typeIndex * 3 + colorIndex;
//                        if (index >= 0 && base != null) list[index] = maskImage(base, colorStates[colorIndex]);
//                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    public static HashSet<Integer>[] loadTiles() {
        HashSet<Integer>[] maps = new HashSet[13];
        try {
            for (int i = 1; i <= 13; i++) {
                File f = new File("assets/map/tileData" + i + ".png");
                if (!f.exists()) {
                    System.out.println("File missing: " + f);
                    return null;
                }
                BufferedImage bi = ImageIO.read(f);
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

    private static Image maskImage(BufferedImage base, int color) {
        BufferedImage newImage = new BufferedImage(base.getWidth(), base.getHeight(), base.getType());
        for (int x = 0; x < base.getWidth(); x++) {
            for (int y = 0; y < base.getHeight(); y++) {
                newImage.setRGB(x, y, base.getRGB(x, y) & color);
            }
        }
        return newImage;
    }
}
