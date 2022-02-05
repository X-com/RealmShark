package util;

import javafx.util.Pair;

import java.io.*;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Crude handmade json to java string converter for packets.
 */
public class SimpleIndexToPacketType {
    final File filejson = new File("D:\\Programmering\\GitKraken\\realmlib\\src\\packets.json");
    final File filets = new File("D:\\Programmering\\GitKraken\\realmlib\\src\\create-packet.ts");

    HashMap<String, Pair<String, String>> fix = new HashMap<>();
    public void readWriteFile() {
        Scanner reader;
        Scanner readerts;
        try {
            reader = new Scanner(filejson);
            readerts = new Scanner(filets);
            StringBuilder sb = new StringBuilder();
            String fileName = "pp";

            while (readerts.hasNextLine()) {
                String data = readerts.nextLine();
                Pattern p = Pattern.compile("    case PacketType.([^ ]*):");
                Matcher m = p.matcher(data);

                if (m.find()) {
                    String s = m.group(1);
                    data = readerts.nextLine();
                    Pattern p2 = Pattern.compile(".*?new ([a-zA-Z]*).([a-zA-Z]*)\\(\\);");
                    Matcher m2 = p2.matcher(data);
                    if (m2.find()) {
                        String s1 = m2.group(1);
                        String s2 = m2.group(2);
                        fix.put(s, new Pair<>(s1, s2));
                    }
                }

                if (data.contains("CHANGE_ALLYSHOOT")) break;
            }

            while (reader.hasNextLine()) {
                String data = reader.nextLine();
                Pattern p = Pattern.compile("  \"([0-9]*)\": \"([^ ]*)\",");
                Matcher m = p.matcher(data);
                if (m.find()) {
                    String s = m.group(0);
                    String s1 = m.group(1);
                    String s2 = m.group(2);
                    Pair asdf = fix.get(s2);
                    if(asdf != null)
                        sb.append(String.format("%s(%s, %s, %s),\n", s2, s1, asdf.getKey().equals("IncomingPackets")? "Incoming":"Outgoing", asdf.getValue()+ "::new"));
                    else
                        sb.append(String.format("%s(%s, %s, %s),\n", s2, s1, "null", "null"));
                }

                if (data.contains("CHANGE_ALLYSHOOT")) break;
            }
            System.out.println(sb);
//            createFile(fileName, sb);
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void createFile(String filename, StringBuilder sb) {
        Writer writer;

        try {
            File myObj = new File("src\\main\\java\\packets\\" + filename + ".java");
//            File myObj = new File("files\\" + filename + ".java");
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(myObj), "utf-8"));
            writer.write(sb.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
