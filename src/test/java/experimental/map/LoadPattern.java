package experimental.map;

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

public class LoadPattern {
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
                            MapData md = loadFile(name);
                            if (maps[i - 1] == null) maps[i - 1] = new ArrayList<>();
                            maps[i - 1].add(md);
                            System.out.println(name);
                        }
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return maps;
    }

    private static MapData loadFile(String filename) {
        MapData mapData = new MapData();
        try {
            File f = new File("tiles/pattern/" + filename);
            if (!f.exists()) {
                throw new RuntimeException("missing: " + f);
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
                        mapData.mapArray[x][y] = t;
                        tiles++;
                    } else if (s.length == 4) {
                        String type = s[0];
                        String x = s[1];
                        String y = s[2];
                        String[] stats = s[3].split(";");
                        mapData.entitys.add(new Entity(type, x, y, stats));
                    }
                } catch (NumberFormatException e) {
                    System.out.println(line);
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mapData;
    }
}
