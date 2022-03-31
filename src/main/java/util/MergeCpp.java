package util;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ignore this ugly code. It's used for making an output file related to packets.
 */
public class MergeCpp {
    String folder = "D:/Programmering/rotmg/";

    private static PrintWriter printWriter;

    public static void main(String[] args) {
        new MergeCpp().run();
    }

    private void run() {
        System.out.println("clearconsole");
        try {
            makeCppFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        fixTranslate();
    }

    public HashMap<String, String> fixTranslate() {
        ArrayList<String> bb = readBB();
        ArrayList<Pair<String, String>> translation = readNameMatch();
        HashMap<String, String> list = new HashMap<>();

        for (int i = 0; i < bb.size(); i += 4) {
            String obf = bb.get(i).replaceAll("[^A-Z]", "");
            String name = bb.get(i + 1).substring(18).replaceAll("[^a-zA-Z]", "");
            String type = bb.get(i + 2).substring(17).replaceAll("[^a-zA-Z]", "");
            for (Pair<String, String> p : translation) {
                if (p.left().equals(obf)) {
                    list.put(p.right(), "Name:" + name + " Type:" + type);
                    print(String.format("%s %s %s", p.right(), type, name));
                }
            }
        }

        return list;
    }

    public void makeCppFile() {
        ArrayList<String> names = readNames();
        System.out.println(names.size());
        ArrayList<ArrayList<String>> list = readDump(names);
        System.out.println(list.size());
        HashMap<String, String> translate = fixTranslate();
        print("");
        print("");
        for (ArrayList<String> l : list) {
            String name = l.get(0);
            String t = translate.get(name);
            if (t != null) print(t);
            for (int i = 1; i < l.size(); i++) {
                print(l.get(i));
            }
            HashMap<String, ArrayList<ArrayList<String>>> methods = readMethods();

            ArrayList<ArrayList<String>> meth = methods.get(name);
            if (meth != null) {
                for (ArrayList<String> m : meth) {
                    for (String s : m) {
                        print(s);
                    }
                }
            } else {
                System.out.println(name);
            }

            print("}");
            print("");
        }
    }

    public static void print(String s) {
        if (printWriter == null) {
            try {
                File f = new File("error/cpp.txt");
                if (!f.exists()) {
                    f.getParentFile().mkdirs();
                    f.createNewFile();
                }
                FileWriter fileWriter = new FileWriter(f);
                printWriter = new PrintWriter(fileWriter);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        printWriter.print("\n" + s);
        printWriter.flush();
    }

    private ArrayList<Pair<String, String>> readNameMatch() {
        String fileName = folder + "list2.txt";
        ArrayList<Pair<String, String>> list = new ArrayList<>();
        Pattern p = Pattern.compile("([A-Z]*) *// *0x[0-9]* *([A-Z]*) *// *0x[0-9]*");

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line;

            while ((line = br.readLine()) != null) {
                Matcher m = p.matcher(line);
                if (m.matches()) {
                    String oldName = m.group(1);
                    String newName = m.group(2);
                    list.add(new Pair(oldName, newName));
                }
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    private ArrayList<String> readBB() {
        String fileName = folder + "bb.json";
        ArrayList<String> list = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line;

            while ((line = br.readLine()) != null) {
                list.add(line);
                if (line.equals("end")) break;
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    private static int fakeCounter = 0;

    private HashMap<String, ArrayList<ArrayList<String>>> readMethods() {
        String fileName = folder + "file";
        ArrayList<String> fakes = readFakes();
        ArrayList<String> list = new ArrayList<>();
        HashMap<String, ArrayList<ArrayList<String>>> fragments = new HashMap<>();
        ArrayList<String> addlines = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line;
            boolean fake = false;

            while ((line = br.readLine()) != null) {
                list.add(line);
                if (line.contains("-+-+-")) {
                    String name = line.substring(5);
                    if (addlines != null && !fake) fragments.computeIfAbsent(name, k -> new ArrayList<>()).add(addlines);

                    addlines = new ArrayList<>();
                    addlines.add("");
                    fake = false;
                    continue;
                }

                if (addlines != null) {
                    for (String ff : fakes) {
                        if (line.contains(ff)) fake = true;
                    }
                    String l = line;
                    int space = 0;
                    while (l.length() > 0) {
                        if (l.charAt(0) != ' ') break;
                        l = l.substring(1);
                        space++;
                    }
                    space /= 2;
                    String s = "";
                    for (int i = 0; i <= space; i++) {
                        s = s + "    ";
                    }
                    l = s + l;
                    addlines.add(l);
                }
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fragments;
    }

    private ArrayList<String> readFakes() {
        String fileName = folder + "fake.txt";
        ArrayList<String> list = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line;

            while ((line = br.readLine()) != null) {
                list.add(line);
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    private ArrayList<String> readNames() {
        String fileName = folder + "list.txt";
        ArrayList<String> list = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line;

            while ((line = br.readLine()) != null) {
                list.add(line);
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    private ArrayList<ArrayList<String>> readDump(ArrayList<String> names) {
        String fileName = folder + "dump3-21.cs";
        Pattern p = Pattern.compile("public class ([^ ]*) : [^ ]* // TypeDefIndex: [0-9]*");
        ArrayList<ArrayList<String>> list = new ArrayList<>();

        try {
            String line;
            ArrayList<String> addlines = null;

            for (String name : names) {
                boolean read = false;
                BufferedReader br = new BufferedReader(new FileReader(fileName));

                while ((line = br.readLine()) != null) {
                    Matcher m = p.matcher(line);
                    if (m.matches()) {
                        String s = m.group(1);
                        if (s.equals(name)) {
                            addlines = new ArrayList<>();
                            addlines.add(s);
                        }
                    }
                    if (addlines != null && (line.contains("Properties") || line.contains("Methods"))) {
                        list.add(addlines);
                        addlines = null;
                    }
                    if (addlines != null) {
                        addlines.add(line);
                        read = true;
                    }
                }
                br.close();
                if (!read) System.out.println(name);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }
}