package experimental;

import util.Util;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Datathingy {
    static Pattern pattern = Pattern.compile("dat server:[0-9]*map:([0-9]*)seed:([0-9]*)m:([0-9]*,)*[0-9]*");
    static ArrayList<Integer> found = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        InputStream is = Util.resourceFilePath("log.txt");
//        String result = new java.io.BufferedReader(new java.io.InputStreamReader(is)).lines().collect(java.util.stream.Collectors.joining("\n"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line = reader.readLine();
        int counter = 0;
        int[] data = new int[92];

        while (line != null) {
//            System.out.println(line);
            Matcher matcher = pattern.matcher(line);

            if (matcher.find()) {
                int map = Integer.parseInt(matcher.group(1));
                if (map == 9) {
                    String seed = matcher.group(2);
                    int seednum = Integer.parseInt(seed);
                    if (!found.contains(seednum)) {
                        counter++;
                        found.add(seednum);
                        String[] s = line.split(":");
                        String[] s2 = s[s.length - 1].split(",");
                        for (int i = 0; i < 92; i++) {
                            int n = Integer.parseInt(s2[i]);
                            data[i] += n == 0 ? 0 : 1;
                        }
                    }
                }
            }
            line = reader.readLine();
        }
        System.out.println(Arrays.toString(data));
        System.out.println(counter);
    }
}
