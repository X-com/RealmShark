package bugfixingtools;

import packets.Packet;
import packets.PacketType;
import packets.packetcapture.encryption.RC4;
import packets.packetcapture.encryption.RotMGRC4Keys;
import packets.packetcapture.encryption.TickAligner;
import packets.packetcapture.sniff.RingBuffer;
import packets.packetcapture.sniff.netpackets.Ip4Packet;
import packets.packetcapture.sniff.netpackets.RawPacket;
import packets.packetcapture.sniff.netpackets.TcpPacket;
import packets.packetcapture.sniff.netpackets.EthernetPacket;
import packets.reader.BufferReader;
import util.Util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PacketTester {
    static final String FILE_NAME = "error/2022-04-01-19.31.48.data";
    private static boolean incoming = true;

    public static void main(String[] args) {
        System.out.println("clearconsole");
        try {
            Util.saveLogs = false;
//            new PacketTester().crunch();
            new PacketTester().errorSimulator();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void crunch() {
//        String s = "";
        String s = "[0, 0, 1, 22, 46, 0, 16, 53, 49, 55, 48, 55, 53, 57, 53, 53, 49, 57, 52, 50, 54, 53, 54, -120, 5, 0, 11, 70, 105, 114, 101, 32, 80, 111, 114, 116, 97, 108, 0, 0, 7, 45, -125, 19, 10, 0, 10, 77, 97, 120, 101, 100, 95, 76, 105, 102, 101, 1, -114, 3, 0, 10, 77, 97, 120, 101, 100, 95, 77, 97, 110, 97, 1, -114, 3, 0, 12, 77, 97, 120, 101, 100, 95, 65, 116, 116, 97, 99, 107, 1, -89, 1, 0, 13, 77, 97, 120, 101, 100, 95, 68, 101, 102, 101, 110, 115, 101, 1, -89, 1, 0, 11, 77, 97, 120, 101, 100, 95, 83, 112, 101, 101, 100, 1, -89, 1, 0, 15, 77, 97, 120, 101, 100, 95, 68, 101, 120, 116, 101, 114, 105, 116, 121, 1, -89, 1, 0, 14, 77, 97, 120, 101, 100, 95, 86, 105, 116, 97, 108, 105, 116, 121, 1, -89, 1, 0, 12, 77, 97, 120, 101, 100, 95, 87, 105, 115, 100, 111, 109, 1, -89, 1, 0, 14, 80, 111, 116, 105, 111, 110, 68, 114, 105, 110, 107, 101, 114, 13, 1, 20, 0, 16, 80, 111, 116, 105, 111, 110, 69, 110, 116, 104, 117, 115, 105, 97, 115, 116, 1, 50, 0, 56, 68, 102, 67, 116, 117, 103, 65, 81, 69, 101, 56, 65, 65, 65, 65, 65, 65, 112, 66, 65, 65, 65, 65, 65, 65, 65, 67, 77, 105, 103, 79, 109, 70, 103, 113, 81, 108, 104, 67, 88, 65, 113, 77, 66, 71, 119, 69, 66, 73, 81, 69, 115, 77, 119, 81, 61]";
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

    public void testRingbuff() {
        RingBuffer<Integer> ringBuffer = new RingBuffer(32);
        int test;
        for(test = 0; test < 16; test++){
            System.out.println("push: " + test);
            ringBuffer.push(test);
        }
        for(int i = 0; i < 15; i++){
            System.out.println("pop : " + ringBuffer.pop());
        }

        for(; test < (16+32); test++){
            System.out.println("push: " + test);
            ringBuffer.push(test);
        }
        for(int i = 0; i < 34; i++){
            System.out.println("pop : " + ringBuffer.pop());
        }
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

    private void errorSimulator() {
        System.out.println("Sta");

        ArrayList<byte[]> list = readFile(incoming);
        boolean first = true;
        int id = 0;
        int counter = 0;
        long sequenseNumber = 0;
        long nextSeq = 0;
        int starting = 0;
        LinkedHashMap<Long, TcpPacket> packetMap = new LinkedHashMap<>();

        RC4 rc4;
        if(incoming) rc4 = new RC4(RotMGRC4Keys.INCOMING_STRING);
        else rc4 = new RC4(RotMGRC4Keys.OUTGOING_STRING);
        TickAligner tickAligner = new TickAligner(rc4);

        for (byte[] b : list) {
            counter++;
            try {
                RawPacket rawPacket = RawPacket.newPacket(b, Instant.now());
                EthernetPacket ether = rawPacket.getNewEthernetPacket();
                Ip4Packet ip4 = ether.getNewIp4Packet();
                TcpPacket tcpPacket = ip4.getNewTcpPacket();
//            EthernetPacket epacket = EthernetPacket.newPacket(b, 0, b.length);
//            IpV4Packet ip4 = epacket.get(IpV4Packet.class);
//            TcpPacket tcpPacket = epacket.get(TcpPacket.class);
//            if (counter >= 0 && counter < 1000) System.out.println(ip4.getIdentification());

//                if(counter > 145) {
//                    packets.packetcapture.networktap.pcap4j.TcpPacket.newPacket(ip4.getPayload(), 0, ip4.getPayloadLength());
//                    System.out.println(tcpPacket);
//                }
                if (tcpPacket.isResetBit()) {
                    packetMap.clear();
//                    sequenseNumber = tcpPacket.getHeader().getSequenceNumber();
                    sequenseNumber = 0;
                    System.out.println("-----------------------------------------------");
                    System.out.println("-----------------------------------------------isFin " + tcpPacket.isFin());
                    System.out.println("-----------------------------------------------isSyn " + tcpPacket.isSyn());
                    System.out.println("-----------------------------------------------isRst " + tcpPacket.isRst());
                    tickAligner.reset();
                    continue;
                }

                id++;
                if (first) id = ip4.getIdentification();
                first = false;

                long currentSeq = tcpPacket.getSequenceNumber();
                if (tcpPacket.getPayload() != null) {
                    nextSeq = tcpPacket.getPayloadSize() + currentSeq;
                }

                if (ip4.isMoreFragmentFlag()) System.out.println("fragmentation");
//                if (counter > starting) System.out.println("Aa: " + sequenseNumber + " " + tcpPacket.getHeader().getSyn());
//                if (counter > starting) {
//                    System.out.println("C: " + currentSeq + " + " + (tcpPacket.getPayload() == null ? 0 : tcpPacket.getPayloadSize()) + " = D: " + nextSeq + "=" + sequenseNumber + " dif: (" + (nextSeq - currentSeq) + ")");
//                }

                if (sequenseNumber == 0) {
                    sequenseNumber = currentSeq;
//                    System.out.println("A: " + sequenseNumber + " " + counter);
                }
                packetMap.put(currentSeq, tcpPacket);
                int size = packetMap.size();
                System.out.println("PACKETMAP " + size + " " + counter + " " + ip4.getIdentification());

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
                        sequenseNumber += packetSeqed.getPayloadSize();
//                        if (counter > starting) System.out.println("B: " + sequenseNumber);
                        build(tcpPacket.getPayload(), tickAligner, rc4);
                    }
                }
//                if (packet.getPayload() != null) sequenseNumber += packet.getPayload().length();
            } catch (Exception e) {
                e.printStackTrace();
            }
//            if (counter > starting) System.out.println("END");
        }

        System.out.println("Fin " + packetMap.size() + " " + id + " " + list.size());
    }

    private byte[] bytes = new byte[200000];
    private int index;
    private int pSize = 0;
    private boolean firstNonLargePacket = true;
    public void build(byte[] data, TickAligner tickAligner, RC4 r) {
        if (firstNonLargePacket) {  // start listening after a non-max packet
            // prevents errors in pSize.
            if (data.length < 1460) firstNonLargePacket = false;
            return;
        }
        for (byte b : data) {
            bytes[index++] = b;
            if (index >= 4) {
                if (pSize == 0) {
                    pSize = Util.decodeInt(bytes);
                    if (pSize > 200000) {
                        Util.print("Oversize packet construction.");
                        pSize = 0;
                        return;
                    }
                }

                if (index == pSize) {
                    index = 0;
                    byte[] realmPacket = Arrays.copyOfRange(bytes, 0, pSize);
                    pSize = 0;
                    ByteBuffer packetData = ByteBuffer.wrap(realmPacket).order(ByteOrder.BIG_ENDIAN);
                    int size = packetData.getInt();
                    byte type = packetData.get();
                    if(tickAligner.checkRC4Alignment(packetData, size, type)){
                        r.skip(size - 5);
                    }
                }
            }
        }
    }

    private ArrayList<byte[]> readFile(boolean incoming) {
        Pattern p = Pattern.compile("  Sequence Number: ([0-9]*)");
//        ArrayList<Pair<Long, Integer>> list = new ArrayList<>();
        ArrayList<byte[]> list2 = new ArrayList<>();
        boolean firstBatch = false;
        try {
            BufferedReader br = new BufferedReader(new FileReader(FILE_NAME));
            String line;
            int i = 0;
            while ((line = br.readLine()) != null) {
                Matcher m = p.matcher(line);
                if (m.matches()) {
//                    list.add(Pair.create(Long.parseLong(m.group(1)), i));
//                    System.out.println(m.group(1));
                }
                i++;
                if (line.startsWith("[")) {
                    firstBatch = true;
                } else if (firstBatch) {
//                    firstBatch = false;
                    return list2;
                }

                String[] splits = line.split(",");
//                if(splits.length > 40){
//                    for(int s = 0; s < splits.length-1; s++){
//                        if(splits[s].equals(" 8") && splits[s+1].equals(" 2")){
//                            System.out.println(s);
//                        }
//                    }
//                }
//                String in = "[4";
//                if(incoming) in = "[5";
                if(splits.length < 37) continue;
                int shift = 2;
                if(incoming) shift = 0;

                boolean isIn = splits[34 + shift].equals(" 8") && splits[35 + shift].equals(" 2");

                if (isIn) {
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
                System.out.println(i + " " + strLen + " " + (i+strLen));
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
}
