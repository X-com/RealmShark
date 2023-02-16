package experimental;

import potato.model.data.HeroType;
import potato.model.Bootloader;
import potato.model.HeroLocations;
import util.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataLogAnalyze {
    final Pattern datPattern = Pattern.compile("dat server:(-?[0-9]*)map:([0-9]*)seed:([0-9]*)m:([0-9,]*)");
    final Pattern subPattern = Pattern.compile("sub server:(-?[0-9]*) seed:([0-9]*) map:([0-9]*) x:([0-9]*) y:([0-9]*)");
    ArrayList<Data> dataList = new ArrayList<>();
    ArrayList<MapCoord> mapCoordList = new ArrayList<>();


    public static void main(String[] args) {
        new DataLogAnalyze().read();
    }

    private void read() {
        loadFile();
//        findCoords();
//        matchData();
        findMapHeroes();
    }

    private void findMapHeroes() {
        ArrayList<Data> list = new ArrayList<>(dataList);

        System.out.println(list.size());
        list.removeIf(d -> !d.mapData || missingMarking(d));

        System.out.println(list.size());
        markerToInfo(list);
    }

    private boolean missingMarking(Data d) {
        final int[] count = {0};
        Arrays.stream(d.heroes).forEach(e -> {
            if (e != 0) count[0]++;
        });
        return count[0] < 10;
    }

    void markerToInfo(ArrayList<Data> list) {
        HashMap<Integer, int[]> allSpots = new HashMap<>();

        for (Data d : list) {
            int count = 0;
            for (int h : d.heroes) {
                int typeIndex = h % 16;
                int stateIndex = h / 16;
                int p = d.map * 1000 + count;
                int[] spots = allSpots.get(p);
                if (spots == null) {
                    spots = new int[13];
                    allSpots.put(p, spots);
                }
                spots[typeIndex]++;
                count++;
            }
        }

//        HashMap<Integer, HeroSpots> heroes = new HashMap<>();
//
//        for (Map.Entry<Integer, int[]> e : allSpots.entrySet()) {
//            int v = e.getKey();
//            HeroSpots d = heroes.get(v);
//            if (d == null) {
//                d = new HeroSpots();
//                heroes.put(e.getKey(), d);
//                d.map = v / 1000;
//                d.spot = v % 1000;
//            }
//            d.type = e.getValue();
//        }

        ArrayList<HeroLocations>[] hero = Bootloader.loadMapCoords();
        HashMap<String, Integer> types = new HashMap<>();
//        for (int i = 0; i < 14; i++) {
        {
            int i = 7;
            for (int j = 0; j < 95; j++) {
                int v = i * 1000 + j;
                int[] nums = allSpots.get(v);
                if (nums != null) {
                    String s = "";
                    for (int k = 1; k < nums.length; k++) {
                        if (nums[k] > 5) {
//                            s += HeroType.byOrdinal(k) + " " + nums[k] + " ";
                            s += HeroType.byOrdinal(k) + " ";
                        }
                    }
                    if (!types.containsKey(s)) {
                        types.put(s, 1);
                    } else {
                        types.put(s, types.get(s) + 1);
                    }

                    if (s.equals("DEMON ")) {
                        s = "1";
                    } else if (s.equals("CYCLOPS ")) {
                        s = "2";
                    } else if (s.equals("PHENIX OASIS ")) {
                        s = "8";
                    } else if (s.equals("PARASITE ")) {
                        s = "4";
                    } else if (s.equals("GHOST PARASITE MANOR ")) {
                        s = "4";
                    } else if (s.equals("ENT SNAKE ")) {
                        s = "16";
                    } else if (s.equals("LICH GRAVE ")) {
                        s = "32";
                    } else if (s.equals("HOUSE ")) {
                        s = "64";
                    } else if (s.equals("CYCLOPS LICH GRAVE ")) {
                        s = "34";
                    } else if (s.equals("DEMON PHENIX OASIS PARASITE ")) {
                        s = "9";
                    } else if (s.equals("GHOST ENT PARASITE MANOR SNAKE ")) {
                        s = "20";
                    } else if (s.equals("DEMON GHOST PARASITE MANOR ")) {
                        s = "5";
                    } else if (s.equals("DEMON CYCLOPS ")) {
                        s = "3";
                    } else if (s.equals("PHENIX OASIS PARASITE ")) {
                        s = "12";
                    } else if (s.equals("DEMON PARASITE ")) {
                        s = "5";
                    } else if (s.equals("ENT SNAKE HOUSE ")) {
                        s = "80";
//                    } else if (s.equals("GHOST ENT MANOR SNAKE ")) {
//                        s = "20";
                    }

                    /**
                     * DEMON  55 - 1 (RED)
                     * CYCLOPS  77 - 2 (ORANGE)
                     * PHENIX OASIS  17 - 8 (YELLOW)
                     * PARASITE - 4 (MAGENTA)
                     * GHOST PARASITE MANOR  124 - 4  (MAGENTA)
                     * ENT SNAKE  448 - 16 (GREEN)
                     * LICH GRAVE  167 - 32 (BLUE)
                     * HOUSE - 64 (BROWN)
                     * GHOST ENT MANOR SNAKE  1 -
                     * CYCLOPS LICH GRAVE  6 - 34 = 2+32
                     * DEMON PHENIX OASIS PARASITE  1 - 9 = 1+8
                     * GHOST ENT PARASITE MANOR SNAKE  28 - 20 = 4+16
                     * DEMON GHOST PARASITE MANOR  2 - 5 = 1+4
                     * DEMON CYCLOPS  27 - 3 = 1+2
                     * PHENIX OASIS PARASITE  15 - 12 = 8+4
                     * DEMON PARASITE  1 - 5 = 1+4
                     * ENT SNAKE HOUSE  63 - 80 = 16+64
                     */

                    if (s != "" && hero[i - 1].size() > j) {
//                        System.out.println((v / 1000) + " " + ((v % 1000) + 1) + " " + s + " " + n);
                        HeroLocations heroLocations = hero[i - 1].get(j);
                        if (i == 7 && j == 13) {
                        } else {
//                            System.out.println(i + " " + j + " " + heroLocations.getX() + " " + heroLocations.getY() + " " + s);
                            System.out.println(i + " " + heroLocations.getX() + " " + heroLocations.getY() + " " + s);
                            Util.print("D:\\Programmering\\GitKraken\\RealmShark\\src\\main\\resources\\potatoRes\\map\\heroMap" + i + ".txt-", heroLocations.getX() + " " + heroLocations.getY() + " " + s);
                        }
                    }
                }
            }
        }

        for (Map.Entry p : types.entrySet()) {
            System.out.println(p.getKey() + " " + p.getValue());
        }
    }

    private void findCoords() {
        ArrayList<Data> list = new ArrayList<>(dataList);

        System.out.println(list.size());
        list.removeIf(d -> d.mapData);

        System.out.println(list.size());
        int size = list.size();
        while (list.size() > 0) {
            Data d = list.get(0);
            list.removeIf(e -> e.x == d.x && e.y == d.y && e.map == d.map);

            mapCoordList.add(new MapCoord(d.x, d.y, d.map, size - list.size()));
            size = list.size();
        }

        System.out.println(mapCoordList.size());

        mapCoordList.sort((d, e) -> d.map - e.map);

        mapCoordList.forEach(System.out::println);

//        mapCoordList.forEach(d ->
//                mapCoordList.forEach(e -> System.out.print(e.x == d.x && e.y == d.y && e != d ? e + ":" + d + "\n" : ""))
//        );
    }

    private void matchData() {
        for (Data d : dataList) {
            for (Data f : dataList) {
                if (d.seed == f.seed && d.server != f.server) {
//                    System.out.println(d.seed + " " + d.server + " " + f.seed + " " + f.server + " " + d.map + " " + f.map);
                    if (d.map != f.map) {
                        System.out.println("---------------------------");
                        System.out.println(d);
                        System.out.println(f);
                    }
                }
            }
        }
    }

    private void loadFile() {
        int count = 0;
        try {
            File f = new File("src/test/resources/log.txt");
            if (!f.exists()) {
                System.out.println("missing: " + f);
                return;
            }

            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            while ((line = br.readLine()) != null) {
                count++;
                boolean skip = false;

                Matcher m1 = datPattern.matcher(line);
                Matcher m2 = subPattern.matcher(line);

                try {
                    Data d;
                    if (m1.matches()) { // dat server:220523707 map:11 seed:726179509 m:0,0,0,0,0,0,0,0,0,
                        int server = Integer.parseInt(m1.group(1));
                        int map = Integer.parseInt(m1.group(2));
                        int seed = Integer.parseInt(m1.group(3));
                        int[] heroes = Arrays.stream(m1.group(4).split(",")).mapToInt(Integer::parseInt).toArray();

                        d = new Data(server, seed, map, heroes, count);
                    } else if (m2.matches()) { // sub server:917488922 seed:256453846 map:8 x:1428 y:311
                        int server = Integer.parseInt(m2.group(1));
                        int seed = Integer.parseInt(m2.group(2));
                        int map = Integer.parseInt(m2.group(3));
                        int x = Integer.parseInt(m2.group(4));
                        int y = Integer.parseInt(m2.group(5));

                        if (map == 1 && x == 0 && y == 0) {
                            continue;
                        }

                        d = new Data(server, seed, map, x, y, count);
                    } else {
                        System.out.println(line);
                        throw new RuntimeException("WTF");
                    }
                    dataList.add(d);
                } catch (NumberFormatException e) {
                    System.out.println(line);
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class HeroSpots {
        int map;
        int spot;
        int[] type = new int[13];
    }

    public class MapCoord {
        int x;
        int y;
        int map;
        int size;

        public MapCoord(int x, int y, int map, int size) {
            this.x = x;
            this.y = y;
            this.map = map;
            this.size = size;
        }

        @Override
        public String toString() {
            return map + " " + x + " " + y + " " + size;
        }
    }

    public class Data {
        int server;
        int seed;
        int map;
        int x;
        int y;
        int[] heroes;
        int line;
        boolean mapData = false;

        public Data(int server, int seed, int map, int x, int y, int line) {
            this.server = server;
            this.seed = seed;
            this.map = map;
            this.x = x;
            this.y = y;
            this.line = line;
        }

        public Data(int server, int seed, int map, int[] heroes, int line) {
            this.server = server;
            this.seed = seed;
            this.map = map;
            this.heroes = heroes;
            this.line = line;
            mapData = true;
        }

        @Override
        public String toString() {
            String s = "\n   x=" + x + "\n   y=" + y;

            return "Data{" + "\n   server=" + server + "\n   seed=" + seed + "\n   map=" + map + "\n   line=" + line + (heroes == null ? s : "") + "\n   heroes=" + Arrays.toString(heroes);
        }
    }
}
