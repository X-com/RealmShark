package experimental;

import packets.reader.BufferReader;
import tomato.realmshark.HttpCharListRequest;
import tomato.realmshark.PcStatsDecoder;
import tomato.backend.data.RealmCharacter;
import tomato.realmshark.enums.CharacterStatistics;
import util.Util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class WhatEncoding {
    static String token = "";

    public static void main(String[] args) throws Exception {
//        stringOutFull();
//        tryStuff();
        request();
//        extracted();
//        reverse("DfCtuv_3__8__7_8f_399wAAAACT9vkNl9WDAaqTE5bj0h2CF5YDroYfvKIBkMQJuQGCM7kBhiIDApEBBAUUr4MBARECAgECAQIFGY8BAyoEJQIBAYoBEgIDAosBAYsBIwIDCx8BAisrGTsTAgEKEjIzHiYEICqfE7BsrgqRB7ScA5aiA46fBoPiAqG4BLq9AqeSBoCGAgEDAQIB");
//        reverse("DfCtugAAAAkAAAAAAAAAAAAAAAABrkk=");
//        decode("DfCtugAAAA0AAAAAAAAAAAAAAAABAbRB");
    }

    private static int readCounter;

    private static void reverse(String build) {
        byte[] output = sixBitStringToBytes(build);

        readCounter = 0;
        while (readCounter < output.length) {
            int o = readCompressedInt(output, readCounter);
            System.out.println(o);
        }
    }

    private static byte[] sixBitStringToBytes(String string) {
        int indexPadding = string.indexOf('=');
        int stringLength = string.length();
        int padding = 0;
        if (indexPadding > -1) {
            padding = stringLength - indexPadding;
        }
        byte[] output = new byte[(stringLength / 4) * 3 - padding];
        int o = 0;
        for (int i = 0; i < stringLength; i += 4) {
            int value1 = charValue(string.charAt(i));
            int value2 = charValue(string.charAt(i + 1));
            char c3 = string.charAt(i + 2);
            char c4 = string.charAt(i + 3);
            int value3 = charValue(c3);
            int value4 = charValue(c4);

            byte o1 = (byte) (value1 << 2 | value2 >> 4);
            byte o2 = (byte) ((value2 & 0b001111) << 4 | value3 >> 2);
            byte o3 = (byte) ((value3 & 0b000011) << 6 | value4);

            output[o] = o1;
            if (c3 != '=') { // excludes padding
                output[1 + o] = o2;
                if (c4 != '=') {
                    output[2 + o] = o3;
                }
            }
            o += 3;
        }
        return output;
    }

    static int charValue(char c) {
        if (c >= 48 && c <= 57) return c + 4;
        if (c >= 65 && c <= 90) return c - 65;
        if (c >= 97 && c <= 122) return c - 71;
        if (c == '-') return 62;
        if (c == '_') return 63;
        if (c == '=') return 0;
        return c;
    }

    public static String reverseString(String str) {
        char ch[] = str.toCharArray();
        String rev = "";
        for (int i = ch.length - 1; i >= 0; i--) {
            rev += ch[i];
        }
        return rev;
    }

    static void print() {
        for (char i = 0; i < 26; i++) {
            int a = i + 97;
            int A = i + 65;
            int n = i + 48;
            System.out.print((char) A + " " + ((int) i));
            if (i < 10) System.out.print(" ");
            System.out.print("   ");
            System.out.print((char) a + " " + ((int) i + 26));
            System.out.print("   ");
            if (i < 10) {
                System.out.print((char) n + " " + ((int) i + 52));
            } else if (i == 10) {
                System.out.print("- 62");
            } else if (i == 11) {
                System.out.print("_ 63");
            }
            System.out.print("\n");
        }
    }

    private static void stringOutFull() {
        String s0 = "DfCtugAAAA0AAAAAAAAAAAAAAAABAoR5"; // 1 shot 2 abil 7748
        String s1 = "DfCtugAAAA0AAAAAAAAAAAAAAAABAbVm"; // 1 shot 1 abil 6581
        String s2 = "DfCtugAAAAwAAAAAAAAAAAAAAAABnRM="; // 1 abil 1245
        String s3 = "DfCtugAAAAkAAAAAAAAAAAAAAAABrkk="; // 1 shot 4718
        String s4 = "DfCtugAAAAgAAAAAAAAAAAAAAACGEg=="; // 1158
        String s5 = "DfCtugAAAAgAAAAAAAAAAAAAAACDEw=="; // 1219
        String s6 = "DfCtugAAAAgAAAAAAAAAAAAAAACKtAE=="; // 11530
        String s7 = "DfCtugAAAAgAAAAAAAAAAAAAAACuwgE=="; // 12462
        String s8 = "DfCtugAAAAgAAAAAAAAAAAAAAACHIQ=="; // 2119
        String s9 = "DfCtugAAAAgAAAAAAAAAAAAAAACwTA=="; // 4912
        String s13 = "DfCtugAAAAgAAAAAAAAAAAAAAAChiQQ="; // 33377
        String s14 = "DfCtugAAAAgAAAAAAAAAAAAAAACs5QU="; // 47468
        String s15 = "DfCtugAAAAgAAAAAAAAAAAAAAAC92wc="; // 63229
        String s16 = "DfCtugAAAAgAAAAAAAAAAAAAAACH-Ac="; // 65031
        String s17 = "DfCtugAAAAgAAAAAAAAAAAAAAACphgg="; // 65961
        String s18 = "DfCtugAAAA0AAAAAAAAAAAAAAAAHD4btAg=="; // 7 shot 15 abil 23366
        String bitOnes = "DfCtugAAAAgAAAAAAAAAAAAAAAC_Hw=="; // 2047
        String oneAbil1 = "DfCtugAAAAwAAAAAAAAAAAAAAAABvz8="; // 1 abil 4095
        String oneAbil2 = "DfCtugAAAAwAAAAAAAAAAAAAAAAPgEA="; // 15 abil 4096
        String abilshot = "DfCtugAAAA0AAAAAAAAAAAAAAAAHD7ZO"; // 7 shot 15 abil 5046

        String[] ss = {s0, s1, s2, s3, s4, s5, s6, s7};
        String[] ss1 = {s13, s14, s15, s16, s17};
        String[] ss2 = {bitOnes, oneAbil1, oneAbil2, abilshot};
        String[] ss3 = {s0, "DfCtuv_3__8__7_8f_399wAAAACT9vkNl9WDAaqTE5yK0x2CF5YDroYfvKIBkMQJuQGCM7kBhiIDApEBBAUUr4MBARECAgECAQIFGY8BAyoEJQIBAYoBEgIDAosBAYsBIwIDCx8BAisrGTsTAgEKEjIzHiYEICqfE7BsrgqRB7ScA5aiA46fBoPiAqG4BLq9AqeSBoCGAgEDAQIB"};

        for (int i = 0; i < ss3[0].length(); i++) {
            for (String s : ss3) {
                char c1 = s.charAt(i);
                int n1 = charValue(c1);
                String bs = Integer.toBinaryString(n1);
                String add = "";
                if (c1 != '=') {
                    for (int j = 0; j < 6 - bs.length(); j++) {
                        add += "o";
                    }
                } else {
                    for (int j = 0; j < 6 - bs.length(); j++) {
                        add += "-";
                    }
                }
                System.out.printf("%s :%s%s  ", c1, add, bs);
            }
            System.out.println();
        }
//        System.out.println("000" + Integer.toBinaryString(2047) + "  2047");
//        System.out.println("00" + Integer.toBinaryString(4095) + "  1 abil 4095");
//        System.out.println("0" + Integer.toBinaryString(4096) + "  15 abil 4096");
//        System.out.println("0" + Integer.toBinaryString(5046) + "  7 shot 15 abil 5046");

//        System.out.println(Integer.toBinaryString(33377) + "  33377");
//        System.out.println(Integer.toBinaryString(47468) + "  47468");
//        System.out.println(Integer.toBinaryString(63229) + "  63229");
//        System.out.println(Integer.toBinaryString(65031) + "  65031");
        System.out.println(Integer.toBinaryString(65961) + "  65961");
        System.out.println(Integer.toBinaryString(23366) + "  23366");

//        System.out.println(Integer.toBinaryString(7748) + "  1 shot/ 2 abil 7748");
//        System.out.println(Integer.toBinaryString(6581) + "  1 shot/abil 6581");
//        System.out.println("00" + Integer.toBinaryString(1245) + "  1 abil 1245");
//        System.out.println(Integer.toBinaryString(4718) + "  1 shot 4718");
//        System.out.println("00" + Integer.toBinaryString(1158) + "  1158");
//        System.out.println("00" + Integer.toBinaryString(1219) + "  1219");
//        System.out.println("00" + Integer.toBinaryString(2047) + "  2047");
//        System.out.println(Integer.toBinaryString(11530) + "  11530");
//        System.out.println(Integer.toBinaryString(12462) + "  12462");

//        System.out.println("0" + Integer.toBinaryString(2119) + "  2119");
//        System.out.println(Integer.toBinaryString(4912) + "  4912");
    }

    public static int readCompressedInt(byte[] bytes, int offset) {
        int uByte = readUnsignedByte(bytes, offset);
        boolean isNegative = (uByte & 64) != 0;
        int shift = 6;
        int value = uByte & 63;

        int i = offset + 1;
        while ((uByte & 128) != 0) {
            uByte = readUnsignedByte(bytes, i);
            value |= (uByte & 127) << shift;
            shift += 7;
            i++;
        }

        if (isNegative) {
            value = -value;
        }
        return value;
    }

    public static int readUnsignedByte(byte[] bytes, int offset) {
        readCounter++;
        return Byte.toUnsignedInt(bytes[offset]);
    }

    static int lastVal(char c) {
        if (c == 'A') return 0; // 000000
        if (c == 'Q') return 1; // 010000
        if (c == 'g') return 2; // 100000
        if (c == 'w') return 3; // 110000
        return 0;
    }

    static void tryStuff() {
        String[] list = {"GEg",// 1158
                "KEg",// 1162
                "aEg",// 1178
                "bEg",// 1179
                "0Eg",// 1204
                "5Eg",// 1209
                "DEw",// 1219
                "TEw",// 1235
                "hEw",// 1249
                "-Ew",// 1278
                "EFA",// 1284
                "TFA",// 1299
                "yFA",// 1330
                "EFQ"};// 1348
        int[] nums = {1158, 1162, 1178, 1179, 1204, 1209, 1219, 1235, 1249, 1278, 1284, 1299, 1330, 1348};

        for (int i = 0; i < list.length; i++) {
            String s = list[i];
            int c1 = charValue(s.charAt(0));
            int c2 = charValue(s.charAt(1));
            int c3 = charValue(s.charAt(2));

            int value = c1 | (c2 << 8) | (c3 << 2);

            System.out.println("produced value: " + value + " offset: " + (nums[i] - value));
        }
    }

    private static void extracted() {
        String zero = "DfCtugAAAAgAAAAAAAAAAAAAAACFEg=="; // 0
        String b2 = "DfCtugAAAAgAAAAAAAAAAAAAAACGEg=="; // 1158
        String b0 = "DfCtugAAAAgAAAAAAAAAAAAAAACKEg=="; // 1162
        String b4 = "DfCtugAAAAgAAAAAAAAAAAAAAACaEg=="; // 1178
        String b3 = "DfCtugAAAAgAAAAAAAAAAAAAAACbEg=="; // 1179
        String b1 = "DfCtugAAAAgAAAAAAAAAAAAAAAC0Eg=="; // 1204
        String b5 = "DfCtugAAAAgAAAAAAAAAAAAAAAC5Eg=="; // 1209
        String a0 = "DfCtugAAAAgAAAAAAAAAAAAAAACDEw=="; // 1219
        String a1 = "DfCtugAAAAgAAAAAAAAAAAAAAACTEw=="; // 1235
        String a2 = "DfCtugAAAAgAAAAAAAAAAAAAAAChEw=="; // 1249
        //z          1267
        String a3 = "DfCtugAAAAgAAAAAAAAAAAAAAAC-Ew=="; // 1278
        //_          1279
        String a4 = "DfCtugAAAAgAAAAAAAAAAAAAAACEFA=="; // 1284
        String a5 = "DfCtugAAAAgAAAAAAAAAAAAAAACTFA=="; // 1299
        String a6 = "DfCtugAAAAgAAAAAAAAAAAAAAACyFA=="; // 1330
        String a7 = "DfCtugAAAAgAAAAAAAAAAAAAAACEFQ=="; // 1348
        String bitOnes = "DfCtugAAAAgAAAAAAAAAAAAAAAC_Hw=="; // 2047
        String s1a9 = "DfCtugAAAAgAAAAAAAAAAAAAAAChIA=="; // 2081
        String s1a3 = "DfCtugAAAAgAAAAAAAAAAAAAAAC0IA=="; // 2100
        String s1b0 = "DfCtugAAAAgAAAAAAAAAAAAAAACCIQ=="; // 2114
        String s1b2 = "DfCtugAAAAgAAAAAAAAAAAAAAACcIQ=="; // 2140
        String s1b1 = "DfCtugAAAAgAAAAAAAAAAAAAAACnIQ=="; // 2151
        String s1a5 = "DfCtugAAAAgAAAAAAAAAAAAAAACBIg=="; // 2177
        String s1a6 = "DfCtugAAAAgAAAAAAAAAAAAAAACeIg=="; // 2206
        String s1a8 = "DfCtugAAAAgAAAAAAAAAAAAAAACgIg=="; // 2208
        String s1a1 = "DfCtugAAAAgAAAAAAAAAAAAAAACtIg=="; // 2221
        String s1a7 = "DfCtugAAAAgAAAAAAAAAAAAAAACvIg=="; // 2223
        String s1a2 = "DfCtugAAAAgAAAAAAAAAAAAAAAC6Ig=="; // 2234
        String s1a4 = "DfCtugAAAAgAAAAAAAAAAAAAAACuIw=="; // 2286
        String s2 = "DfCtugAAAAgAAAAAAAAAAAAAAACZMQ=="; // 3161
        String s3 = "DfCtugAAAAgAAAAAAAAAAAAAAAC9Pw=="; // 4093
        String s4 = "DfCtugAAAAgAAAAAAAAAAAAAAACdTg=="; // 5021
        String s5 = "DfCtugAAAAgAAAAAAAAAAAAAAACJXQ=="; // 5961
        String s6 = "DfCtugAAAAgAAAAAAAAAAAAAAACsaw=="; // 6892
        String s7 = "DfCtugAAAAgAAAAAAAAAAAAAAACIeg=="; // 7816
        String s8 = "DfCtugAAAAgAAAAAAAAAAAAAAACriAE=="; // 8747
        String s9 = "DfCtugAAAAgAAAAAAAAAAAAAAACOlwE=="; // 9678
        String s10 = "DfCtugAAAAgAAAAAAAAAAAAAAACupQE=="; // 10606
        String s11 = "DfCtugAAAAgAAAAAAAAAAAAAAACKtAE=="; // 11530
        String s12 = "DfCtugAAAAgAAAAAAAAAAAAAAACuwgE=="; // 12462
        String s13 = "DfCtugAAAAgAAAAAAAAAAAAAAAChiQQ="; // 33377
        String s14 = "DfCtugAAAAgAAAAAAAAAAAAAAACs5QU="; // 47468
        String oneShot1 = "DfCtugAAAAkAAAAAAAAAAAAAAAABpjc="; // 1 shot 3558
        String oneShot2 = "DfCtugAAAAkAAAAAAAAAAAAAAAABrkk="; // 1 shot 4718
        String oneAbil1 = "DfCtugAAAAwAAAAAAAAAAAAAAAABvz8="; // 1 abil 4095
        String oneAbil2 = "DfCtugAAAAwAAAAAAAAAAAAAAAAPgEA="; // 15 abil 4096
        String abilshot = "DfCtugAAAA0AAAAAAAAAAAAAAAAHD7ZO"; // 7 shot 15 abil 5046
        String abilshot2 = "DfCtugAAAA0AAAAAAAAAAAAAAAAHD4btAg=="; // 7 shot 15 abil 23366
        String oneShotoneAbil1 = "DfCtugAAAA0AAAAAAAAAAAAAAAABAbVm"; // 1 shot 1 abil 6581
        String oneShot2Abil = "DfCtugAAAA0AAAAAAAAAAAAAAAABAoR5"; // 1 shot 2 abil 7748
        String ss = "AZaz";
        String ss1 = "tIg";
        String ss2 = "ZMQ";
        String ss3 = "9Pw";

        byte[] list = ss.getBytes(StandardCharsets.UTF_8);
        byte[] list1 = ss1.getBytes(StandardCharsets.UTF_8);

        string(list);
        string(list1);

        SortedMap m = Charset.availableCharsets();
        Set k = m.keySet();
        Iterator charset = k.iterator();
        while (charset.hasNext()) {
            try {
                String n = (String) charset.next();
                System.out.println(n);
//                Charset charSetEE = (Charset) m.get(n);
//                byte[] list = ss1.getBytes(charSetEE);
                string(list);
            } catch (Exception e) {
            }
        }
    }

    static void request() throws IOException {
        System.out.println("request");
        String httpString = HttpCharListRequest.getChartList(token);
        System.out.println(httpString);
//        FileInputStream is = new FileInputStream("tiles/assets/char");

        String result = new java.io.BufferedReader(new java.io.InputStreamReader(Util.resourceFilePath("request"))).lines().collect(java.util.stream.Collectors.joining("\n"));
        ArrayList<RealmCharacter> charList = HttpCharListRequest.getCharList(result);
        for (RealmCharacter rc : charList) {
//            if (rc.hp == 246) {
//            if(rc.skin == 10153) {
//            if(rc.skin == 10197) { // knight lvl 20
//            if(rc.skin == 49814) { // kensi lvl 20
//            if(rc.skin == 913) { // rogue test
//            if(rc.fame == 3285) { // priest fame 3285
//            if(rc.fame == 25) { // huntress
//            if (rc.skin == 9521) { // sorc 275
//            if(rc.fame == 20) {
            System.out.println(rc);
//            System.out.println("---" + rc.fame + "-" + rc.classString + "-" + rc.skin + "---");
//            decode(rc.pcStats);
//            }
        }
    }

    private static void decode(String pcStats) {
        byte[] data = PcStatsDecoder.sixBitStringToBytes(pcStats);
        BufferReader reader = new BufferReader(ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN));

        int unknown = reader.readInt();

//        byte[] bitArray = new byte[16];
//        for (int i = 0; i < 16; i++) {
//            bitArray[i] = reader.readByte();
//            System.out.printf("%2d %s\n", i, bitString(bitArray[i]));
//        }

        boolean[] bitArray = new boolean[16 * 8];
        for (int i = 0; i < 16; i++) {
            int j = i * 8;
            byte b = reader.readByte();
            bitArray[j] = (b & 0x1) > 0;
            bitArray[j + 1] = (b & 0x2) > 0;
            bitArray[j + 2] = (b & 0x4) > 0;
            bitArray[j + 3] = (b & 0x8) > 0;
            bitArray[j + 4] = (b & 0x10) > 0;
            bitArray[j + 5] = (b & 0x20) > 0;
            bitArray[j + 6] = (b & 0x40) > 0;
            bitArray[j + 7] = (b & 0x80) > 0;
        }

        for (int i = 0; i < bitArray.length; i++) {
            String s = bitArray[i] ? CharacterStatistics.getName(i) : "";
            if (s == null) {
                System.out.println(i + "-" + s);
            }
        }

        if (true) {
            while (reader.getRemainingBytes() > 0) {
                reader.readCompressedInt();
//                System.out.println(reader.readCompressedInt());
            }
        }
    }

    static void string(byte[] list) {
        System.out.println(Arrays.toString(list));
    }

    void old() {
        SortedMap m = Charset.availableCharsets();
        Set k = m.keySet();
        Iterator charset = k.iterator();
        while (charset.hasNext()) {
            try {
                String n = (String) charset.next();
                Charset charSetEE = (Charset) m.get(n);

                String s = "DfCtugAAAAgAAAAAAAAAAAAAAACmEQ=="; // 1126
                String s2 = "DfCtugAAAAgAAAAAAAAAAAAAAAC8Nw=="; // 3580
                String s3 = "DfCtugAAAAkAAAAAAAAAAAAAAAACiks="; // 4810
//                String s2 = "DfCtuoOWP_8QIAAAC_3xaAAAAACUmLkEgdUckKAir9jyd7gKhwK7gxuJpwGLsQEojRoEjAgeAQeCezcXAQQDDgEDAwEBsQG0CawBj2CChgSEngOv5gmFigOYtQGllAG5nwK16gEF";
                byte[] list = s.getBytes(charSetEE);
                byte[] list2 = new byte[list.length];
                int iii = list.length;
                for (byte b : list) {
                    iii--;
                    list2[iii] = b;
                }
                System.out.println(Arrays.toString(list));


                for (int i = 0; i < list.length; i++) {
//                    long parse1 = PacketTester.decodeInt(list, i);
//                    long parse2 = PacketTester.decodeInt(list2, i);
//            String match = parse == 234731 ? " match -----------------" : "";
//            System.out.println(parse + match);
//                    if (parse1 == 1126) {
//                        System.out.println(" ----------- found --------");
//                    }
//                    if (parse2 == 1126) {
//                        System.out.println(" ----------- found --------");
//                    }
                }
            } catch (Exception ee) {
//                ee.printStackTrace();
            }
        }
    }
}
