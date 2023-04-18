package experimental.map;

import util.ImageBuffer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MapData {
    public ArrayList<Entity> entitys;
    public int[][] mapArray;
    public int width, height;

    public MapData() {
//        entitys = new ArrayList<>();
//        mapArray = new int[2048][2048];
    }

    public int tile(int x, int y) {
        return mapArray[x][y];
    }

    public static int[][] tileRotate(int[][] tiles) {
        if (tiles.length == 0 || tiles[0].length == 0) return null;
        int[][] rotated = new int[tiles[0].length][tiles.length];
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[0].length; j++) {
                rotated[tiles.length - 1 - i][j] = tiles[j][i];
            }
        }
        return rotated;
    }

    public static void printTiles(int[][] tiles, String name) {
        if (tiles.length == 0 || tiles[0].length == 0) return;
        BufferedImage bi = new BufferedImage(tiles.length, tiles[0].length, BufferedImage.TYPE_INT_ARGB);

        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[0].length; j++) {
                if (tiles[i][j] != 0) {
                    int color = ImageBuffer.getColor(tiles[i][j]);
                    bi.setRGB(i, j, color);
                }
            }
        }

        try {
            ImageIO.write(bi, "PNG", new File(name + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
