package util;

import packets.Packet;
import packets.PacketType;
import packets.reader.BufferReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PacketCruncher {
    public void crunch() {
//        String s = "";
        String s = "[0, 0, 0, 22, 126, 0, 3, -42, 119, 1, -73, -23, 36, 68, -127, -112, 48, 68, -116, -120, 72, 0]";
//        String s1 = "  Hex stream: 00 00 00 05 51 00 00 00 27 2a 9d 81 b3 47 d1 98 65 fc a3 de 6f ec 25 e9 70 c4 60 85 21 31 a6 41 f2 23 3a 60 8c a4 5d b6 57 e3 d7 17 00 00 00 0d 1f 6b e8 1e fe ac 11 7e 63 00 00 00 05 51";
//        String s2 = "  Hex stream: 00 00 00 05 51 00 00 00 33 2a 2d 09 c3 79 d6 2a 40 90 03 d3 cd 91 24 8e 00 71 4a ec af 9f d3 be 51 c1 12 e3 be ec 64 be 70 71 30 f9 ec df 73 9b 9a 7d cc 8a 51 8f bf 22 00 00 00 05 51";
//        String s3 = "  Hex stream: 00 00 00 33 2a 03 a2 5d 1f ba 82 e6 5b 2d 5a 81 44 0f 65 36 3c 00 86 48 76 70 46 4c 00 1f 9c fa dd f3 27 13 83 2b 1e 71 bc 7c 3c 40 5f 14 cc 2f a1 26 9c";
        byte[] data = getByteArray(s);
//        byte[] data2 = getByteArray(s2);
//        byte[] data3 = getByteArray(s3);
//        System.out.println(data1.length);
//        System.out.println(data2.length);
//        System.out.println(data3.length);


        stringifyData(data);
        deserialize(data);
    }

    public void stringifyData(byte[] data) {
        int size = data.length;
        ByteBuffer pbuff = createBuffer(data);
        StringBuilder br = new StringBuilder();
        for (int i = 0; i < size - 2; i++) {
            int strLen = pbuff.getShort(i);
            if (strLen < size - i && strLen > 0) {
                System.out.println(i + " " + strLen + " " + pbuff.get(i - 1));
                for (int j = 0; j < strLen; j++) {
                    System.out.print((char) pbuff.get(i + j + 2));
                }
                System.out.println();
            } else if (strLen == 0) {
                System.out.println(i + " " + strLen);
            }
            br.append((char)data[i]);
        }
        System.out.println(br);
    }

    public static byte[] getByteArray(String byteString) {
        String[] list;
        boolean hex = false;
        if (byteString.contains("Hex stream")) {
            hex = true;
            list = byteString.replace("  Hex stream: ", "").split(" ");
        } else {
            list = byteString.replaceAll("[\\[\\] ]", "").split(",");
        }
        byte[] b = new byte[list.length];
        int i = 0;
        for (String s : list) {
            if (hex) {
                b[i++] = (byte) ((Character.digit(s.charAt(0), 16) << 4) + Character.digit(s.charAt(1), 16));
            } else {
                b[i++] = Byte.parseByte(s);
            }
        }
        return b;
    }

    public ByteBuffer createBuffer(byte[] data) {
        return ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);
    }

    public Packet getPacket(int type) {
        return PacketType.getPacket(PacketType.byOrdinal(type).getIndex()).factory();
    }

    public void deserialize(byte[] data) {
        int size = Util.decodeInt(data);
        int type = data[4];
        ByteBuffer bb = createBuffer(data);
        bb.position(5);
        Packet p = getPacket(type);
        System.out.println(PacketType.byClass(p) + " " + type);
        try {
            BufferReader br = new BufferReader(bb);
            p.deserialize(br);
            if (!br.isBufferFullyParsed()) {
                System.out.println(p);
                return;
            }
            System.out.println("Parse complete");
            System.out.println(p);
        } catch (Exception e) {
            System.out.println("Buffer blew up");
            e.printStackTrace();
        }
    }

    public void stringify(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
//            sb.append(Integer.toHexString(data[i]));
            sb.append((char) data[i]);
        }
        System.out.println(sb);
    }

    public static void main(String[] args) {
        new PacketCruncher().crunch();
    }
}
