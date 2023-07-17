package bugfixingtools;

import packets.Packet;
import packets.PacketType;
import packets.packetcapture.encryption.RC4;
import packets.packetcapture.encryption.RotMGRC4Keys;
import packets.packetcapture.encryption.TickAligner;
import packets.packetcapture.sniff.RingBuffer;
import packets.packetcapture.sniff.assembly.Ip4Defragmenter;
import packets.packetcapture.sniff.netpackets.Ip4Packet;
import packets.packetcapture.sniff.netpackets.RawPacket;
import packets.packetcapture.sniff.netpackets.TcpPacket;
import packets.packetcapture.sniff.netpackets.EthernetPacket;
import packets.reader.BufferReader;
import util.Util;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PacketTester {
    static final String FILE_NAME = "error/error-2022-07-30-12.23.49.data";
    private static boolean incoming = false;

    public static void main(String[] args) {
        System.out.println("clearconsole");
        try {
            Util.setSaveLogs(false);
            new PacketTester().crunch();
//            new PacketTester().errorSimulator();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void crunch() {
        String s = "";
        byte[] data = getByteArray(s);
//        byte[] data2 = getByteArray(s2);
//        byte[] data3 = getByteArray(s3);
//        System.out.println(data1.length);
//        System.out.println(data2.length);
//        System.out.println(data3.length);
//        System.out.println(decodeInt(data2, 5));


//        findShort(data);
//        buildPacket(data);
//        stringifyData(data);
        deserialize(data);
//        bruteforce(data);
//        decrype(data);
//        findFits(data);
//        findPacketIndex(data, true);
//        findPacketIndex(data, false);
    }

    public void findFits(byte[] data) {
        int size = Util.decodeInt(data);
        int type = data[4];

        for (int i = 5; i < data.length; i++) {
            int left = readCompressedInt(data, i);
            if (left > 0 && left < size - i) {
                System.out.println(i + " " + left);
            }
        }
    }

    public void decrype(byte[] data) {
        System.out.println(Arrays.toString(data));
        RC4 rc4 = new RC4(RotMGRC4Keys.INCOMING_STRING);

        int i = 0;
        while (i < 2000000) {
            RC4 rcopy = rc4.fork();
            byte[] dcopy = Arrays.copyOfRange(data, 5, data.length);
            rcopy.decrypt(dcopy);
            int int1 = decodeInt(dcopy, 0);
            int int2 = decodeInt(dcopy, 4);
            int short1 = decodeShort(dcopy, 8);
            if (int1 == 352852614) {
                System.out.printf("%d %d %d %d\n", i, int1, int2, short1);
            }
            rc4.getXor();
            i++;
        }
    }

    public void findShort(byte[] data) {
        for (int i = 5; i < data.length; i++) {
            float f1 = decodeFloat(data, i);
            float f2 = decodeFloat(data, i + 4);
            byte b = data[i + 8];
            int c = readCompressedInt(data, i + 9);
            if (
//                    realFloatValue(f1) &&
                    realFloatValue(f2)
//                    c > 0 && c < 1000
            ) {
                System.out.println("----");
                System.out.printf("i:%d\n", i);
                System.out.printf("pos:(%f, %f)\n", f1, f2);
                System.out.printf("level:%d\n", b);
                System.out.printf("tiles:%d\n", c);
            }
        }
    }

    boolean realFloatValue(float f) {
        return f < 0 ? (f < -0.0001f && f > -10000f) : (f > 0.0001f && f < 10000f);
    }

    public static int readUnsignedByte(byte[] bytes, int offset) {
        return Byte.toUnsignedInt(bytes[offset]);
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

    public static short decodeShort(byte[] bytes, int offset) {
        return (short) ((Byte.toUnsignedInt(bytes[0 + offset]) << 8) | Byte.toUnsignedInt(bytes[1 + offset]));
    }

    public static float decodeFloat(byte[] bytes, int offset) {
        int asInt = (bytes[0 + offset] & 0xFF)
                | ((bytes[1 + offset] & 0xFF) << 8)
                | ((bytes[2 + offset] & 0xFF) << 16)
                | ((bytes[3 + offset] & 0xFF) << 24);
        return Float.intBitsToFloat(asInt);
    }

    public static int decodeInt(byte[] bytes, int offset) {
        return (Byte.toUnsignedInt(bytes[0 + offset]) << 24) | (Byte.toUnsignedInt(bytes[1 + offset]) << 16) | (Byte.toUnsignedInt(bytes[2 + offset]) << 8) | Byte.toUnsignedInt(bytes[3 + offset]);
    }

    public static long decodeLong(byte[] bytes, int offset) {
        long l = 0;
        int shift = 56;
        for (int i = 0; i < 8; i++) {
            l |= Byte.toUnsignedLong(bytes[i + offset]) << shift;
            shift -= 8;
        }
        return l;
    }

    public void buildPacket(byte[] data) {
        RawPacket packet = RawPacket.newPacket(data, null);

        try {
            EthernetPacket ethernetPacket = packet.getNewEthernetPacket();
            if (ethernetPacket != null) {
                Ip4Packet ip4packet = ethernetPacket.getNewIp4Packet();
                Ip4Packet assembledIp4packet = Ip4Defragmenter.defragment(ip4packet);
                if (assembledIp4packet != null) {
                    TcpPacket tcpPacket = assembledIp4packet.getNewTcpPacket();
                    if (tcpPacket != null) {
                        System.out.println(tcpPacket);
                        System.out.println(tcpPacket.getPayload().length);
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException | NullPointerException e) {
            Util.printLogs(e.getMessage());
            Util.printLogs(Arrays.toString(packet.getPayload()));
            e.printStackTrace();
        }
    }

    public void testRingbuff() {
        RingBuffer<Integer> ringBuffer = new RingBuffer(32);
        int test;
        for (test = 0; test < 16; test++) {
            System.out.println("push: " + test);
            ringBuffer.push(test);
        }
        for (int i = 0; i < 15; i++) {
            System.out.println("pop : " + ringBuffer.pop());
        }

        for (; test < (16 + 32); test++) {
            System.out.println("push: " + test);
            ringBuffer.push(test);
        }
        for (int i = 0; i < 34; i++) {
            System.out.println("pop : " + ringBuffer.pop());
        }
    }

    public void bruteforce(byte[] data) {
        int size = Util.decodeInt(data);
        int type = data[4];
        for (int i = 0; i < 5000; i++) {
//            System.out.println("------start " + i + " -----");
            ByteBuffer bb = createBuffer(data);
            bb.position(5);
            Packet p = getPacket(type);
//            System.out.println(PacketType.byClass(p) + " " + type);
            try {
                BufferReader br = new BufferReader(bb);
                for (int j = 0; j < i; j++) {
                    br.readByte();
                }
                p.deserialize(br);
                if (!br.isBufferFullyParsed()) {
                    System.out.println(p);
                    continue;
                }
                System.out.println("Parse complete " + i);
                System.out.println(p);
                return;
            } catch (Exception e) {
//                System.out.println("Buffer blew up " + i);
//                e.printStackTrace();
            }
        }
    }

    public void findPacketIndex(byte[] data, boolean isIncoming) {
        System.out.println("-----Type: " + (isIncoming ? "Incoming" : "Outgoing"));
        for (int type : PacketType.getPacketTypeByDirection(isIncoming)) {
            ByteBuffer bb = createBuffer(data);
            bb.position(5);
            Packet p = getPacket(type);
            BufferReader br = new BufferReader(bb);
            try {
                p.deserialize(br);
                if (!br.isBufferFullyParsed()) {
                    continue;
                }
                System.out.println(PacketType.byClass(p) + " " + type);
            } catch (Exception e) {
            }
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
                System.out.println("Buffer not finished " + br.getIndex() + " / " + br.size());
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
        if (incoming) rc4 = new RC4(RotMGRC4Keys.INCOMING_STRING);
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
                if (counter > 190) System.out.println(Arrays.toString(b));
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
                        Util.printLogs("Oversize packet construction.");
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
                    if (tickAligner.checkRC4Alignment(packetData, size, type)) {
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
                i++;
                if (i < 125) continue;
//                System.out.println(line);
                Matcher m = p.matcher(line);
                if (m.matches()) {
//                    list.add(Pair.create(Long.parseLong(m.group(1)), i));
//                    System.out.println(m.group(1));
                }
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
                if (splits.length < 37) continue;
                int shift = 2;
                if (incoming) shift = 0;

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
                System.out.println("string at:" + i + " len:" + strLen);
                for (int j = 0; j < strLen; j++) {
                    System.out.print((char) pbuff.get(i + j + 2));
                }
                System.out.println();
            } else if (strLen == 0) {
                System.out.println("zerostring " + i + " " + strLen);
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
        for (int i = 0; i < list.length; i++) {
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
