package packets.packetcapture.networktap;

import util.Util;

import java.util.ArrayList;
import java.util.List;

public class EtherIpTcp {
    public static final int BYTE_SIZE_IN_BYTES = 1;
    public static final int SHORT_SIZE_IN_BYTES = 2;
    public static final int INT_SIZE_IN_BYTES = 4;
    public static final int LONG_SIZE_IN_BYTES = 8;
    // ------------------ Ethernet Packet -----------------
    public static final int IEEE802_3_MAX_LENGTH = 1500;
    private static final int MIN_ETHERNET_PAYLOAD_LENGTH = 46; // [bytes]
    private static final int MAX_ETHERNET_PAYLOAD_LENGTH = 1500; // [bytes]

    private static final int ETHERNET_HEADER_SIZE = 14;
    public static final int MAC_SIZE_IN_BYTES = 6;
    private static final int DST_ADDR_OFFSET_ETHER = 0;
    private static final int SRC_ADDR_OFFSET_ETHER = 6;
    private static int ETHER_OFFSET;
    private static final int TYPE_OFFSET = 12;

    private final byte[] macDest;
    private final byte[] macSrc;
    private final int etherType;
    private final int etherPayloadSize;

    // -------------------- IP4 Packet --------------------

    private static final int VERSION_AND_IHL_OFFSET = 0;
    private static final int VERSION_AND_IHL_SIZE = 1;
    private static final int TOS_OFFSET = 1;
    private static final int TOS_SIZE = 1;
    private static final int TOTAL_LENGTH_OFFSET = 2;
    private static final int TOTAL_LENGTH_SIZE = 2;
    private static final int IDENTIFICATION_OFFSET = 4;
    private static final int IDENTIFICATION_SIZE = 2;
    private static final int FLAGS_AND_FRAGMENT_OFFSET_OFFSET = 6;
    private static final int FLAGS_AND_FRAGMENT_OFFSET_SIZE = 2;
    private static final int TTL_OFFSET = 8;
    private static final int TTL_SIZE = 1;
    private static final int PROTOCOL_OFFSET = 9;
    private static final int PROTOCOL_SIZE = 1;
    private static final int HEADER_CHECKSUM_OFFSET = 10;
    private static final int HEADER_CHECKSUM_SIZE = 2;
    private static final int IP_ADDRESS_SIZE = 4;
    private static final int SRC_ADDR_OFFSET_IP = 12;
    private static final int DST_ADDR_OFFSET_IP = 16;
    private static final int OPTIONS_OFFSET_IP = 4;
    private static final int MIN_IPV4_HEADER_SIZE = 4;

    private final int version;
    private final int ihl;
    private final int precedence;
    private final int tos;
    private final boolean mbz;
    private final int totalLength;
    private final int identification;
    private final boolean reservedFlag;
    private final boolean dontFragmentFlag;
    private final boolean moreFragmentFlag;
    private final int fragmentOffset;
    private final int ttl;
    private final int protocol;
    private final int headerChecksum;
    private final byte[] srcAddr;
    private final byte[] dstAddr;
    //    private final List<IpV4Packet.IpV4Option> optionsIP;
    private final byte[] paddingIP;

    // -------------------- TCP Packet -----------------------
    private static final int SRC_PORT_OFFSET = 0;
    private static final int SRC_PORT_SIZE = 2;
    private static final int DST_PORT_OFFSET = 2;
    private static final int DST_PORT_SIZE = 2;
    private static final int SEQUENCE_NUMBER_OFFSET = 4;
    private static final int SEQUENCE_NUMBER_SIZE = 4;
    private static final int ACKNOWLEDGMENT_NUMBER_OFFSET = 8;
    private static final int ACKNOWLEDGMENT_NUMBER_SIZE = 4;
    private static final int DATA_OFFSET_AND_RESERVED_AND_CONTROL_BITS_OFFSET = 12;
    private static final int DATA_OFFSET_AND_RESERVED_AND_CONTROL_BITS_SIZE = 2;
    private static final int WINDOW_OFFSET = 14;
    private static final int WINDOW_SIZE = 2;
    private static final int CHECKSUM_OFFSET = 16;
    private static final int CHECKSUM_SIZE = 2;
    private static final int URGENT_POINTER_OFFSET = 18;
    private static final int URGENT_POINTER_SIZE = 2;
    private static final int OPTIONS_OFFSET_TCP = 20;
    private static final int MIN_TCP_HEADER_SIZE = 20;

    private static final int IPV4_PSEUDO_HEADER_SIZE = 12;
    private static final int IPV6_PSEUDO_HEADER_SIZE = 40;

    private final int srcPort;
    private final int dstPort;
    private final int sequenceNumber;
    private final int acknowledgmentNumber;
    private final byte dataOffset;
    private final byte reserved;
    private final boolean urg;
    private final boolean ack;
    private final boolean psh;
    private final boolean rst;
    private final boolean syn;
    private final boolean fin;
    private final int window;
    private final int checksum;
    private final int urgentPointer;
    //    private final List<TcpPacket.TcpOption> optionsTCP;
    private final byte[] paddingTCP;

    public EtherIpTcp(byte[] data) {
        // ------------------ Ethernet Packet -----------------
        macDest = UtilTcp.getBytes(data, DST_ADDR_OFFSET_ETHER, MAC_SIZE_IN_BYTES);
        macSrc = UtilTcp.getBytes(data, SRC_ADDR_OFFSET_ETHER, MAC_SIZE_IN_BYTES);
        etherType = UtilTcp.getShort(data, TYPE_OFFSET);
        ETHER_OFFSET = etherType <= IEEE802_3_MAX_LENGTH ? 0 : 4;
        etherPayloadSize = UtilTcp.getShort(data, TYPE_OFFSET + ETHER_OFFSET);
        UtilTcp.validateBounds(data, etherPayloadSize, data.length);
        // ----------------------  IP4  -----------------------
        int offsetIP = ETHERNET_HEADER_SIZE + ETHER_OFFSET;
        int versionAndIhl = UtilTcp.getByte(data, offsetIP);
        version = (byte) ((versionAndIhl & 0xF0) >> 4);
        ihl = (byte) (versionAndIhl & 0x0F);

        byte tosByte = (byte) UtilTcp.getByte(data, TOS_OFFSET + offsetIP);
        precedence = (byte) ((tosByte & 0xE0) >> 5);
        tos = (0x0F & (tosByte >> 1));
        mbz = (tosByte & 0x01) != 0;
        totalLength = UtilTcp.getShort(data, TOTAL_LENGTH_OFFSET + offsetIP);
        identification = UtilTcp.getShort(data, IDENTIFICATION_OFFSET + offsetIP);

        short flagsAndFragmentOffset = (short) UtilTcp.getShort(data, FLAGS_AND_FRAGMENT_OFFSET_OFFSET + offsetIP);
        reservedFlag = (flagsAndFragmentOffset & 0x8000) != 0;
        dontFragmentFlag = (flagsAndFragmentOffset & 0x4000) != 0;
        moreFragmentFlag = (flagsAndFragmentOffset & 0x2000) != 0;
        fragmentOffset = (short) (flagsAndFragmentOffset & 0x1FFF);

        ttl = UtilTcp.getByte(data, TTL_OFFSET + offsetIP);
        protocol = UtilTcp.getByte(data, PROTOCOL_OFFSET + offsetIP);
        headerChecksum = UtilTcp.getShort(data, HEADER_CHECKSUM_OFFSET + offsetIP);
        srcAddr = UtilTcp.getBytes(data, DST_ADDR_OFFSET_IP + offsetIP, IP_ADDRESS_SIZE);
        dstAddr = UtilTcp.getBytes(data, DST_ADDR_OFFSET_IP + offsetIP, IP_ADDRESS_SIZE);

        int headerLengthIP = ihl * 4;

//        optionsIP = new ArrayList<>();
        int currentOffsetInHeaderIP = OPTIONS_OFFSET_IP;
//        try {
//            while (currentOffsetInHeaderIP < headerLengthIP) {
//                byte type = data[currentOffsetInHeaderIP + offsetIP];
////                IpV4Packet.IpV4Option newOne = newInstanceOption(
////                        data,
////                        currentOffsetInHeaderIP + offsetIP,
////                        headerLengthIP - currentOffsetInHeaderIP);
////                options.add(newOne);
//                currentOffsetInHeaderIP += newOne.length();
//
//                if (newOne.getType() == 0) { // END_OF_OPTION_LIST == 0
//                    break;
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        int paddingLengthIP = headerLengthIP - currentOffsetInHeaderIP;
        if (paddingLengthIP != 0) {
            paddingIP = UtilTcp.getBytes(data, currentOffsetInHeaderIP + offsetIP, paddingLengthIP);

            if (moreFragmentFlag || fragmentOffset != 0) {
                Util.print("Fragmented packet");
                // TODO: add fragmented ip packet reconstructor here.
                // Not added given realm packets always send don't fragment flag true.
            }
        } else {
            paddingIP = new byte[0];
        }
        // -------------------- TCP Packet -----------------------
        int offsetTCP = currentOffsetInHeaderIP + offsetIP;
        int length = paddingLengthIP;
        srcPort = UtilTcp.getShort(data, SRC_PORT_OFFSET + offsetTCP);
        dstPort = UtilTcp.getShort(data, DST_PORT_OFFSET + offsetTCP);
        sequenceNumber = UtilTcp.getInt(data, SEQUENCE_NUMBER_OFFSET + offsetTCP);
        acknowledgmentNumber = UtilTcp.getInt(data, ACKNOWLEDGMENT_NUMBER_OFFSET + offsetTCP);

        int dataOffsetAndReservedAndControlBits = UtilTcp.getShort(data, DATA_OFFSET_AND_RESERVED_AND_CONTROL_BITS_OFFSET + offsetTCP);

        dataOffset = (byte) ((dataOffsetAndReservedAndControlBits & 0xF000) >> 12);
        reserved = (byte) ((dataOffsetAndReservedAndControlBits & 0x0FC0) >> 6);
        urg = (dataOffsetAndReservedAndControlBits & 0x0020) != 0;
        ack = (dataOffsetAndReservedAndControlBits & 0x0010) != 0;
        psh = (dataOffsetAndReservedAndControlBits & 0x0008) != 0;
        rst = (dataOffsetAndReservedAndControlBits & 0x0004) != 0;
        syn = (dataOffsetAndReservedAndControlBits & 0x0002) != 0;
        fin = (dataOffsetAndReservedAndControlBits & 0x0001) != 0;

        window = UtilTcp.getShort(data, WINDOW_OFFSET + offsetTCP);
        checksum = UtilTcp.getShort(data, CHECKSUM_OFFSET + offsetTCP);
        urgentPointer = UtilTcp.getShort(data, URGENT_POINTER_OFFSET + offsetTCP);

        int headerLength = (0xFF & dataOffset) * 4;
        if (length < headerLength) {
            StringBuilder sb = new StringBuilder(110);
            sb.append("The data is too short to build this header(").append(headerLength).append(" bytes). data: ")/*.append(ByteArrays.toHexString(data, " "))*/.append(", offsetTCP: ").append(offsetTCP).append(", length: ").append(length);
            throw new RuntimeException(sb.toString());
        }
        if (headerLength < OPTIONS_OFFSET_TCP) {
            StringBuilder sb = new StringBuilder(100);
            sb.append("The data offsetTCP must be equal or more than ").append(OPTIONS_OFFSET_TCP / 4).append(", but it is: ").append(0xFF & dataOffset);
            throw new RuntimeException(sb.toString());
        }

//        optionsTCP = new ArrayList<TcpPacket.TcpOption>();
        int currentOffsetInHeader = OPTIONS_OFFSET_TCP;

        int paddingLengthTCP = headerLength - currentOffsetInHeader;
        if (paddingLengthTCP != 0) { // paddingLengthTCP is positive.
            paddingTCP = UtilTcp.getBytes(data, currentOffsetInHeader + offsetTCP, paddingLengthTCP);
        } else {
            paddingTCP = new byte[0];
        }
    }
}
