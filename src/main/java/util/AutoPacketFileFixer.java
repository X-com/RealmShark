package util;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Crude handmade js to java string converter for packets.
 */
public class AutoPacketFileFixer {
    HashMap<String, String> variables = new HashMap();

    public void listFilesForFolder(final File folder) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                readWriteFile(fileEntry);
            }
        }
    }

    void readWriteFile(File file) {
        try {
            Scanner reader = new Scanner(file);
            StringBuilder sb = new StringBuilder();
            String fileName = null;
            int stage = 0;
            boolean write = false;

            sb.append("package packets.incoming;");
            sb.append("\n");
            sb.append("\n");
            sb.append("import packets.Packet;");
            sb.append("\n");
            sb.append("import packets.buffer.PBuffer;");
            sb.append("\n");
            sb.append("\n");

            String temp = null;
            while (reader.hasNextLine()) {
                boolean skip = false;
                String data = reader.nextLine();
                // filename
                if (data.startsWith("export class ")) {
                    Pattern p = Pattern.compile("export class ([^ ]*)");
                    Matcher m = p.matcher(data);
                    if (m.find()) {
                        fileName = m.group(1);
                    }
                }

                if (stage == 0 && data.startsWith("/")) {
                    stage++;
                    write = true;
                }
                if (stage == 1 && data.contains("export")) {
                    stage++;
                    write = false;
                    sb.append("public class " + fileName + " extends Packet {");
                    sb.append("\n");
                }
                if (stage == 2 && data.contains("/*")) {
                    stage++;
                    write = true;
                }
                if (stage == 3 && (data.contains("write") || data.contains("constructor") || data.contains("endregion"))) {
                    stage++;
                    write = false;
                }
                if (stage == 5 && data.contains("toString") || data.contains("write(")) {
                    sb.append("}");
                    stage++;
                    write = false;
                    break;
                }
                if (stage == 6 && data.contains("}")) {
                    stage++;
                    write = true;
                }
                if (write) {
                    if (stage == 3) {
                        Pattern p = Pattern.compile(" *([^ ]*): ([^ ]*);");
                        Matcher m = p.matcher(data);
                        if (m.find()) {
                            String m1 = m.group(1);
                            String m2 = m.group(2);
                            if (m2.equals("number")) m2 = "int";
                            if (m2.equals("number[]")) m2 = "int[]";
                            if (m2.equals("string")) m2 = "String";
                            if (m2.equals("string[]")) m2 = "String[]";
                            data = "   public " + m2 + " " + m1 + ";";
                            variables.put(m1, m2);
                        }
                    }
                    if (stage == 5) {
                        String shortByte = "";
                        if(data.contains("const") && data.contains("Len")){
                            if(data.contains("readShort")) shortByte = "readShort";
                            if(data.contains("readByte")) shortByte = "readByte";
                            if(data.contains("readUnsignedByte")) shortByte = "readUnsignedByte";
                            data = reader.nextLine();
                        }
                        if (data.contains("for (")) {
                            Pattern p = Pattern.compile("(.*)let([^<]*< )[^;]*(;[^{]*\\{)");
                            Matcher m = p.matcher(data);
                            if (m.find()) {
                                data = m.group(1) + "int" + m.group(2) + temp + ".length" + m.group(3);
                            }
                            temp = null;
                        }
                        if (data.contains("new Array")) {
                            Pattern p = Pattern.compile("(.*) Array<([^>]*)>[^;]*;");
                            Matcher m = p.matcher(data);
                            if (m.find()) {
                                String s1 = m.group(1).replaceAll(" *([^ ]*) [^\\n]*", "$1");
                                String s = variables.get(s1);
                                data = m.group(1) + " " + m.group(2) + "[buffer." + shortByte + "()];";
                                temp = s1;
                            }
                        }
                        if (data.contains("//")) {
                            skip = true;
                        }
                        if (data.contains("WorldPosData")) {
                            data = data.replaceAll(";", "") + ".deserialize(buffer);";
                        }
                        if (data.contains("SlotObjectData")) {
                            data = data.replaceAll(";", "") + ".deserialize(buffer);";
                        }
                        if (data.contains("readShort")) {
                            data = data.replaceAll(";", "");
                        }
                        if (data.contains("readByte")) {
                            data = data.replaceAll(";", "");
                        }
                    }
                    if (!skip) {
                        sb.append(data);
                        sb.append("\n");
                    }
                }
                if (stage == 4 && data.contains("read")) {
                    stage++;
                    write = true;
                    sb.append("    @Override");
                    sb.append("\n");
                    sb.append("    public void deserialize(PBuffer buffer) {");
                    sb.append("\n");
                }
            }
            String[] f = file.getParentFile().toString().split("\\\\");
            if (fileName != null && !f[f.length - 1].contains("incoming")) {
                fileName = f[f.length - 1] + "\\" + fileName;
            }
            fileName = fileName + ".java";
            createFile(fileName, sb);
            reader.close();
            return;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    void createFile(String filename, StringBuilder sb) {
        Writer writer;

        try {
            File myObj = new File("src\\main\\java\\packets\\incoming\\" + filename);
//            String f = writeableDir("files\\" + filename);
            String f = writeableDir(myObj.toString());
            System.out.println(myObj);
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(myObj), "utf-8"));
            writer.write(sb.toString());
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String writeableDir(String filename) {
        Pattern p = Pattern.compile("(.*)\\\\([^\\\\]*)");
        Matcher m = p.matcher(filename);
        m.find();
        new File(m.group(1)).mkdirs();
        return m.group(2);
    }

    public void run() {
        final File folder = new File("D:\\Programmering\\GitKraken\\realmlib\\src\\packets\\incoming");
        listFilesForFolder(folder);
    }
}
