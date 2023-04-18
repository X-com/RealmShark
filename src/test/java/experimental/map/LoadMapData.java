package experimental.map;

import packets.data.GroundTileData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class LoadMapData {
    private static Pattern pattern = Pattern.compile("map([0-9]*)-([0-9]*).data");

    public static void main(String[] args) {
        loadMaps();
    }

    public static ArrayList<MapData>[] loadMaps() {
        ArrayList<MapData>[] maps = new ArrayList[13];

        try (Stream<Path> filePathStream = Files.walk(Paths.get("tiles/pattern"))) {
            filePathStream.forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    String name = filePath.getFileName().toString();
                    Matcher matcher = pattern.matcher(name);
                    if (matcher.find()) {
                        int i = Integer.parseInt(matcher.group(1));
                        int j = Integer.parseInt(matcher.group(2));
//                        System.out.println(i + "-" + j);
                        if (i == 10 && j == 15) {
                            MapData md = loadMap(name + ".mapdata");
                            if (maps[i - 1] == null) maps[i - 1] = new ArrayList<>();
                            maps[i - 1].add(md);
                        }
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return maps;
    }

    public static MapData loadMap(String filename) {
        try {
            File f = new File(filename);
            if (!f.exists()) {
                System.out.println(filename);
                throw new RuntimeException("missing: " + f.getAbsolutePath());
            }
            if (!extention(filename)[1].equals("mapdata")) return null;
            ArrayList<Entity> entitys = new ArrayList<>();
            ArrayList<GroundTileData> map = new ArrayList<>();
            BufferedReader br = new BufferedReader(new FileReader(f));
            int width = 0, height = 0;
            String line;
            int tiles = 0;
            while ((line = br.readLine()) != null) {
                try {
                    String[] s = line.split(":");
                    if (s.length == 3) {
                        GroundTileData gtd = new GroundTileData();
                        gtd.x = Short.parseShort(s[0]);
                        gtd.y = Short.parseShort(s[1]);
                        gtd.type = Integer.parseInt(s[2]);
                        map.add(gtd);
                        tiles++;
                    } else if (s.length == 4) {
                        String type = s[0];
                        String x = s[1];
                        String y = s[2];
                        String[] stats = s[3].split(";");
//                        entitys.add(new Entity(type, x, y, stats));
                    } else if (line.contains("width")) {
                        s = line.split("=");
                        width = Integer.parseInt(s[1]);
                    } else if (line.contains("height")) {
                        s = line.split("=");
                        height = Integer.parseInt(s[1]);
                    }
                } catch (NumberFormatException e) {
                    System.out.println(line);
                    e.printStackTrace();
                }
            }
            br.close();

            if (width == 0 || height == 0) {
                width = 2048;
                height = 2048;
            }
            int[][] mapArray = new int[width][height];
            for (GroundTileData gtd : map) {
                mapArray[gtd.x][gtd.y] = gtd.type;
            }

            MapData mapData = new MapData();
            mapData.entitys = entitys;
            mapData.mapArray = mapArray;
            mapData.width = width;
            mapData.height = height;

            return mapData;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String[] extention(String filename) {
        int i = filename.lastIndexOf('.');
        if (i > 0) {
            return new String[]{filename.substring(0, i), filename.substring(i + 1)};
        }
        return new String[]{"", ""};
    }

    public static ArrayList<String> getMapNames(String folder, String extension) {
        ArrayList<String> list = new ArrayList<>();
        File f = new File(folder);
        if (!f.exists()) {
            throw new RuntimeException(f.getAbsolutePath() + " folder doesn't exist");
        }
        File[] listOfFiles = f.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                String filename = listOfFiles[i].getName();
                String[] file = extention(filename);
                if (file[1].equals(extension)) {
                    list.add(file[0]);
                }
//                System.out.println("File " + listOfFiles[i].getName());
            } else if (listOfFiles[i].isDirectory()) {
//                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
        return list;
    }
}
