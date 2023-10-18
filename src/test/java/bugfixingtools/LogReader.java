package bugfixingtools;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LogReader {

    static Pattern pattern = Pattern.compile("sub server:([0-9]*) seed:([0-9]*) map:([0-9]*) x:([0-9]*) y:([0-9]*)");
    static HashMap<Point, MapCoord> maps = new HashMap<>();

    public static void main(String[] args) throws IOException {

//        FileReader in = new FileReader(file);
        URL in = LogReader.class.getClassLoader().getResource("log2.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(in.openStream()));
        String text;
        int count = 0;
        while ((text = br.readLine()) != null) {
            if (text.startsWith("sub")) {
                Matcher matcher = pattern.matcher(text);
                if (matcher.find()) {
                    String s3 = matcher.group(3);
                    String s4 = matcher.group(4);
                    String s5 = matcher.group(5);

                    int map = Integer.parseInt(s3);
                    int x = Integer.parseInt(s4);
                    int y = Integer.parseInt(s5);
                    Point p = new Point(x, y);

                    if (!maps.containsKey(p)) {
                        maps.put(p, new MapCoord(map, x, y));
                    } else if (maps.get(p).map == map) {
                        maps.get(p).add();
                    } else {
                        System.out.printf("map:%d x:%d y:%d map2:%d\n", map, x, y, maps.get(p).count);
                    }
                    count++;
                }
            }
        }

        System.out.println(count);
        System.out.println(maps.size());
        ArrayList<MapCoord> mm = (ArrayList<MapCoord>) maps.values().stream().sorted((e, f) -> e.count - f.count).collect(Collectors.toList());
        for (MapCoord m : mm) {
            if (m.count < 4) continue;
            System.out.printf("map:%d x:%d y:%d count:%d\n", m.map, m.x, m.y, m.count);
        }
    }

    static class MapCoord {
        int x;
        int y;
        int map;
        int count;

        public MapCoord(int map, int x, int y) {
            this.map = map;
            this.x = x;
            this.y = y;
            count = 1;
        }

        public void add() {
            count++;
        }
    }
}
