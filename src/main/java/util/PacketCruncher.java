package util;

import jdk.nashorn.internal.codegen.CompileUnit;
import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.TcpPacket;
import packets.Packet;
import packets.PacketType;
import packets.reader.BufferReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PacketCruncher {
    public void crunch() {
//        String s = "";
//        String s = "[0, 0, 0, 22, 126, 0, 3, -42, 119, 1, -73, -23, 36, 68, -127, -112, 48, 68, -116, -120, 72, 0]";
//        String s1 = "  Hex stream: 00 00 00 05 51 00 00 00 27 2a 9d 81 b3 47 d1 98 65 fc a3 de 6f ec 25 e9 70 c4 60 85 21 31 a6 41 f2 23 3a 60 8c a4 5d b6 57 e3 d7 17 00 00 00 0d 1f 6b e8 1e fe ac 11 7e 63 00 00 00 05 51";
//        String s2 = "  Hex stream: 00 00 00 05 51 00 00 00 33 2a 2d 09 c3 79 d6 2a 40 90 03 d3 cd 91 24 8e 00 71 4a ec af 9f d3 be 51 c1 12 e3 be ec 64 be 70 71 30 f9 ec df 73 9b 9a 7d cc 8a 51 8f bf 22 00 00 00 05 51";
//        String s3 = "  Hex stream: 00 00 00 33 2a 03 a2 5d 1f ba 82 e6 5b 2d 5a 81 44 0f 65 36 3c 00 86 48 76 70 46 4c 00 1f 9c fa dd f3 27 13 83 2b 1e 71 bc 7c 3c 40 5f 14 cc 2f a1 26 9c";
//        byte[] data = getByteArray(s);
//        byte[] data2 = getByteArray(s2);
//        byte[] data3 = getByteArray(s3);
//        System.out.println(data1.length);
//        System.out.println(data2.length);
//        System.out.println(data3.length);


//        stringifyData(data);
//        deserialize(data);
    }

    private void run() {
        System.out.println("Sta");

        ArrayList<byte[]> list = readFile();
        boolean first = true;
        int id = 0;
        int counter = 0;
        long sequenseNumber = 0;
        long nextSeq = 0;
        long dif;
        long difdif = 0;
        int starting = 15;
        LinkedHashMap<Long, TcpPacket> packetMap = new LinkedHashMap<>();

        for (int i = 0; i < list.size(); i++) {
            byte[] b = list.get(i);
            counter++;
            if (counter > starting) System.out.printf("%3d\n", counter);
            try {
                EthernetPacket epacket = EthernetPacket.newPacket(b, 0, b.length);
                IpV4Packet ip4 = epacket.get(IpV4Packet.class);
                TcpPacket tcpPacket = epacket.get(TcpPacket.class);
                if(counter >= 0 && counter < 1000) System.out.println(ip4.getHeader().getIdentification());

                if (tcpPacket.getHeader().getSyn()) {
                    packetMap.clear();
//                    sequenseNumber = tcpPacket.getHeader().getSequenceNumber();
                    sequenseNumber = 0;
                    System.out.println("-----------------------------------------------");
                    System.out.println("-----------------------------------------------");
                    System.out.println("-----------------------------------------------");
                    System.out.println("-----------------------------------------------");
                    continue;
                }

                id++;
                if (first) id = ip4.getHeader().getIdentification();
                first = false;

                long currentSeq = Integer.toUnsignedLong(tcpPacket.getHeader().getSequenceNumber());
                if (tcpPacket.getPayload() != null) {
                    nextSeq = tcpPacket.getPayload().length() + currentSeq;
                }
                if (ip4.getHeader().getMoreFragmentFlag()) System.out.println("fragmentation");
//                if (counter > starting) System.out.println("Aa: " + sequenseNumber + " " + tcpPacket.getHeader().getSyn());
                if (counter > starting) System.out.println("C: " + currentSeq + " + " + (tcpPacket.getPayload() == null ? 0 : tcpPacket.getPayload().length()) + " = D: " + nextSeq + "=" + sequenseNumber + " dif: (" +(nextSeq - currentSeq) +")");
                if (sequenseNumber == 0) {
                    sequenseNumber = currentSeq;
//                    if (counter > starting) System.out.println("A: " + sequenseNumber);
                }
                packetMap.put(currentSeq, tcpPacket);
                int size = packetMap.size();
                if (size > 1) System.out.println("OVERSIZE " + size + " " + counter);

//                if (counter >= 410) {
//                    dif = nextSeq - sequenseNumber - (packet.getPayload() == null ? 0 : packet.getPayload().length());
//                    if (packet.getPayload() != null) {
//                        System.out.println("(" + packet.getPayload().length() + "->" + (sequenseNumber == nextSeq) + ")");
//                    } else {
//                        System.out.println("(" + 0 + "->" + (sequenseNumber == nextSeq) + ")");
//                    }
//                }
                while (packetMap.containsKey(sequenseNumber)) {
                    TcpPacket packetSeqed = packetMap.remove(sequenseNumber);
                    if (packetSeqed.getPayload() != null) {
                        sequenseNumber += packetSeqed.getPayload().length();
                        if (counter > starting) System.out.println("B: " + sequenseNumber);
                    }
                }
//                if(packet.getPayload() != null) sequenseNumber += packet.getPayload().length();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (counter > starting) System.out.println("END");
        }
        System.out.println("Fin " + packetMap.size() + " " + id + " " + list.size());
    }

    private ArrayList<byte[]> readFile() {
        String fileName = "error/data5.data";
        Pattern p = Pattern.compile("  Sequence Number: ([0-9]*)");
//        ArrayList<Pair<Long, Integer>> list = new ArrayList<>();
        ArrayList<byte[]> list2 = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line;
            int i = 0;
            while ((line = br.readLine()) != null) {
                Matcher m = p.matcher(line);
                if (m.matches()) {
//                    list.add(Pair.create(Long.parseLong(m.group(1)), i));
//                    System.out.println(m.group(1));
                }
                i++;
                if (line.startsWith("[-104")) {
//                if (line.startsWith("[12")) {
                    byte[] b = getByteArray(line);
//                    if (computeChecksum(b)) list2.add(b);
//                    else System.out.println("Checksum fail " + i);
                    list2.add(b);
                }
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list2;
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
            br.append((char) data[i]);
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
        for (int i = 0; i < list.length - 1; i++) {
            String s = list[i];
            if (hex) {
                b[i] = (byte) ((Character.digit(s.charAt(0), 16) << 4) + Character.digit(s.charAt(1), 16));
            } else {
                b[i] = Byte.parseByte(s);
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
        try {
            new PacketCruncher().run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
