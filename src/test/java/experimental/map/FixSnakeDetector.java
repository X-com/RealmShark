package experimental.map;

import potato.model.Bootloader;
import potato.model.HeroLocations;
import potato.model.data.IdData;

import java.util.ArrayList;

public class FixSnakeDetector {

    int SET_PIECE_SIZE = 40;

    public static void main(String[] args) {
        new FixSnakeDetector().fixer();
    }

    private void fixer() {
        ArrayList<HeroLocations>[] heroList = Bootloader.loadMapCoords();
        int[][] combined = findCombinedShapeTiles(heroList);

        int[][] overlap = new int[combined.length][combined[0].length];
        int[][] trimmed = new int[combined.length][combined[0].length];

        int colisi = 0;
        for (int h = 0; h < 13; h++) {
            ArrayList<HeroLocations> hl = heroList[h];

            for (int i = 0; i < hl.size(); i++) {
                for (int j = i + 1; j < hl.size(); j++) {
                    HeroLocations h1 = hl.get(i);
                    HeroLocations h2 = hl.get(j);

                    if (Math.abs(h1.getX() - h2.getX()) <= 82) {
                        if (Math.abs(h1.getY() - h2.getY()) <= 82) {
                            colisi++;
                            int dx = h1.getX() - h2.getX();
                            int dy = h1.getY() - h2.getY();
                            checkOverlaps(dx, dy, overlap, combined);
                        }
                    }
                }
            }
        }
        System.out.println(colisi);
        System.out.println(combined.length + " " + combined[0].length);

        MapData.printTiles(combined, "t1");
        MapData.printTiles(overlap, "t2");

        for (int i = 0; i < combined.length; i++) {
            for (int j = 0; j < combined[0].length; j++) {
                if (combined[i][j] != 0 && overlap[i][j] == 0) {
                    trimmed[i][j] = 1;
                }
            }
        }

        MapData.printTiles(trimmed, "t3");
        for (int i = 0; i < trimmed.length; i++) {
            for (int j = 0; j < trimmed[0].length; j++) {
                if (trimmed[i][j] != 0) {
                    System.out.printf("%d,%d\n", i - SET_PIECE_SIZE, j - SET_PIECE_SIZE);
                }
            }
        }
    }

    private void checkOverlaps(int dx, int dy, int[][] overlap, int[][] combined) {
        int xmax = overlap.length;
        int ymax = overlap[0].length;

        for (int i = 0; i < combined.length; i++) {
            for (int j = 0; j < combined[0].length; j++) {
                if (combined[i][j] != 0) {
                    int ddx = dx + i;
                    int ddy = dy + j;
                    if (ddx >= 0 && ddy >= 0 && ddx < xmax && ddy < ymax) {
                        overlap[ddx][ddy] = 1;
                    }
                }
            }
        }
    }

    private int[][] findCombinedShapeTiles(ArrayList<HeroLocations>[] heroList) {
        int[][] combined = new int[SET_PIECE_SIZE * 2 + 1][SET_PIECE_SIZE * 2 + 1];
        ArrayList<MapData>[] maps = LoadMapData.loadMaps();
        ArrayList<HeroLocations> h10 = heroList[9];
        MapData map1015 = maps[9].get(0);
        ArrayList<HeroLocations> hlist = new ArrayList<>();
        HeroLocations h1 = h10.get(1);
        HeroLocations h2 = h10.get(24);
        HeroLocations h3 = h10.get(43);
        HeroLocations h4 = h10.get(44);
        HeroLocations h5 = h10.get(47);
        HeroLocations h6 = h10.get(54);
        hlist.add(h1);
        hlist.add(h2);
        hlist.add(h3);
        hlist.add(h4);
        hlist.add(h5);
        hlist.add(h6);
        for (int i = 0; i < hlist.size(); i++) {
//        for (int i = 0; i < 1; i++) {
            HeroLocations h = hlist.get(i);
            int x = h.getX();
            int y = h.getY();

            int[][] tiles = getTiles(map1015, x, y);
            combined(combined, tiles);
            tiles = MapData.tileRotate(tiles);
            combined(combined, tiles);
            tiles = MapData.tileRotate(tiles);
            combined(combined, tiles);
            tiles = MapData.tileRotate(tiles);
            combined(combined, tiles);
        }
        return combined;
    }

    private void combined(int[][] combined, int[][] tiles) {
        for (int i = -SET_PIECE_SIZE; i <= SET_PIECE_SIZE; i++) {
            for (int j = -SET_PIECE_SIZE; j <= SET_PIECE_SIZE; j++) {
                if (tiles[i + SET_PIECE_SIZE][j + SET_PIECE_SIZE] == IdData.SNAKE_STONE_TILE) {
                    combined[i + SET_PIECE_SIZE][j + SET_PIECE_SIZE] = IdData.SNAKE_STONE_TILE;
                }
            }
        }
    }

    private int[][] getTiles(MapData map, int x, int y) {
        int[][] tiles = new int[SET_PIECE_SIZE * 2 + 1][SET_PIECE_SIZE * 2 + 1];

        for (int i = -SET_PIECE_SIZE; i <= SET_PIECE_SIZE; i++) {
            for (int j = -SET_PIECE_SIZE; j <= SET_PIECE_SIZE; j++) {
                if (map.tile(x + i, y + j) == IdData.SNAKE_STONE_TILE) {
                    tiles[i + SET_PIECE_SIZE][j + SET_PIECE_SIZE] = IdData.SNAKE_STONE_TILE;
                }
            }
        }
        return tiles;
    }
}
