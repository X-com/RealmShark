package util;

import packets.Packet;
import packets.PacketType;
import packets.reader.BufferReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PacketCruncher {
    public void crunch() {
//        String s = "";
        String s = "[0, 0, 0, 18, 55, 0, 0, -20, -34, 0, 15, 66, 64, 0, 0, 10, 34, 1]";
        byte[] data = getByteArray(s);
        stringifyData(data);
//        ByteBuffer pbuff = createBuffer(data);
//        Packet p = getPacket(62);
//        int size = data.length;
//        for (int i = 0; i < size - 2; i++) {
//            int strLen = pbuff.getShort(i);
//            if (strLen < size - i && strLen > 0) {
//                System.out.println(i + " " + strLen + " " + pbuff.get(i - 1));
//                for (int j = 0; j < strLen; j++) {
//                    System.out.print((char) pbuff.get(i + j + 2));
//                }
//                System.out.println();
//            }
//        }
        deserialize(data);
    }

    public byte[] getByteArray(String byteString) {
        String[] list = byteString.replaceAll("[\\[\\] ]", "").split(",");
        byte[] b = new byte[list.length];
        int i = 0;
        for (String s : list) {
            b[i++] = Byte.parseByte(s);
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
        System.out.println(bb.position());
        Packet p = getPacket(type);
        System.out.println(PacketType.byClass(p));
        try {
            BufferReader br = new BufferReader(bb);
            p.deserialize(br);
            if (!br.isBufferFullyParsed()) {
                return;
            }
            System.out.println("Parse complete");
            System.out.println(p);
        } catch (Exception e) {
            System.out.println("Buffer blew up");
            e.printStackTrace();
        }
    }

    public void stringifyData(byte[] data) {
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
