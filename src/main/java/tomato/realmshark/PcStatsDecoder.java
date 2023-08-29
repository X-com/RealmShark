package tomato.realmshark;

import java.util.ArrayList;

public class PcStatsDecoder {

    private static int readCounter;

    public static int[] decodePsStats(String string) {
        byte[] byteArray = sixBitStringToBytes(string);
        ArrayList<Integer> stats = new ArrayList<>();

        readCounter = 0;
        while (readCounter < byteArray.length) {
            int o = readCompressedInt(byteArray, readCounter);
            stats.add(o);
        }

        return stats.stream().mapToInt(Integer::intValue).toArray();
    }

    private static int readCompressedInt(byte[] bytes, int offset) {
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

    private static int readUnsignedByte(byte[] bytes, int offset) {
        readCounter++;
        return Byte.toUnsignedInt(bytes[offset]);
    }

    public static byte[] sixBitStringToBytes(String string) {
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

    private static int charValue(char c) {
        if (c >= 48 && c <= 57) return c + 4; // 0(52) - 9(61)
        if (c >= 65 && c <= 90) return c - 65; // A(0) - Z(25)
        if (c >= 97 && c <= 122) return c - 71; // a(26) - z(51)
        if (c == '-') return 62; // (62)
        if (c == '_') return 63; // (63)
        if (c == '=') return 0; // (padding)
        return c;
    }
}
