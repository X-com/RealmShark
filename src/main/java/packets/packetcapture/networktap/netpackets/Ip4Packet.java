package packets.packetcapture.networktap.netpackets;

import java.util.Arrays;

public class Ip4Packet {

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
    private static final int OPTIONS_OFFSET_IP = 20;
    private static final int MIN_IPV4_HEADER_SIZE = 20;

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
    private final byte[] optionsIP;
    private final int payloadLength;
    private final byte[] payload;

    public Ip4Packet(byte[] data) {
        int versionAndIhl = UtilTcp.getByte(data, 0);
        version = (byte) ((versionAndIhl & 0xF0) >> 4);
        ihl = (byte) (versionAndIhl & 0x0F);

        byte tosByte = (byte) UtilTcp.getByte(data, TOS_OFFSET);
        precedence = (byte) ((tosByte & 0xE0) >> 5);
        tos = (0x0F & (tosByte >> 1));
        mbz = (tosByte & 0x01) != 0;
        totalLength = UtilTcp.getShort(data, TOTAL_LENGTH_OFFSET);
        identification = UtilTcp.getShort(data, IDENTIFICATION_OFFSET);

        short flagsAndFragmentOffset = (short) UtilTcp.getShort(data, FLAGS_AND_FRAGMENT_OFFSET_OFFSET);
        reservedFlag = (flagsAndFragmentOffset & 0x8000) != 0;
        dontFragmentFlag = (flagsAndFragmentOffset & 0x4000) != 0;
        moreFragmentFlag = (flagsAndFragmentOffset & 0x2000) != 0;
        fragmentOffset = (short) (flagsAndFragmentOffset & 0x1FFF);

        ttl = UtilTcp.getByte(data, TTL_OFFSET);
        protocol = UtilTcp.getByte(data, PROTOCOL_OFFSET);
        headerChecksum = UtilTcp.getShort(data, HEADER_CHECKSUM_OFFSET);
        srcAddr = UtilTcp.getBytes(data, SRC_ADDR_OFFSET_IP, IP_ADDRESS_SIZE);
        dstAddr = UtilTcp.getBytes(data, DST_ADDR_OFFSET_IP, IP_ADDRESS_SIZE);

        int headerLengthIP = ihl * 4;
        if (headerLengthIP != OPTIONS_OFFSET_IP) {
            optionsIP = UtilTcp.getBytes(data, OPTIONS_OFFSET_IP, headerLengthIP);
        } else {
            optionsIP = new byte[0];
        }

        int dataLength = data.length - headerLengthIP;
        int length = totalLength - headerLengthIP;
        if (length > dataLength) length = dataLength;
        payloadLength = length;

        if (payloadLength != 0) {
            payload = UtilTcp.getBytes(data, headerLengthIP, payloadLength);
        } else {
            payload = new byte[0];
        }
    }

    public int getVersion() {
        return version;
    }

    public int getIhl() {
        return ihl;
    }

    public int getPrecedence() {
        return precedence;
    }

    public int getTos() {
        return tos;
    }

    public boolean isMbz() {
        return mbz;
    }

    public int getTotalLength() {
        return totalLength;
    }

    public int getIdentification() {
        return identification;
    }

    public boolean isReservedFlag() {
        return reservedFlag;
    }

    public boolean isDontFragmentFlag() {
        return dontFragmentFlag;
    }

    public boolean isMoreFragmentFlag() {
        return moreFragmentFlag;
    }

    public int getFragmentOffset() {
        return fragmentOffset;
    }

    public int getTtl() {
        return ttl;
    }

    public int getProtocol() {
        return protocol;
    }

    public int getHeaderChecksum() {
        return headerChecksum;
    }

    public byte[] getSrcAddr() {
        return srcAddr;
    }

    public byte[] getDstAddr() {
        return dstAddr;
    }

    public byte[] getOptionsIP() {
        return optionsIP;
    }

    public int getPayloadLength() {
        return payloadLength;
    }

    public byte[] getPayload() {
        return payload;
    }

    public TcpPacket getNewTcpPacket() {
        if (payload != null && protocol == 6) {
            return new TcpPacket(payload, payloadLength);
        }
        return null;
    }

    @Override
    public String toString() {
        return "Ip4Packet{" +
                "\n  version=" + version +
                "\n, ihl=" + ihl +
                "\n, precedence=" + precedence +
                "\n, tos=" + tos +
                "\n, mbz=" + mbz +
                "\n, totalLength=" + totalLength +
                "\n, identification=" + identification +
                "\n, reservedFlag=" + reservedFlag +
                "\n, dontFragmentFlag=" + dontFragmentFlag +
                "\n, moreFragmentFlag=" + moreFragmentFlag +
                "\n, fragmentOffset=" + fragmentOffset +
                "\n, ttl=" + ttl +
                "\n, protocol=" + protocol +
                "\n, headerChecksum=" + headerChecksum +
                "\n, srcAddr=" + ipToString(srcAddr) +
                "\n, dstAddr=" + ipToString(dstAddr) +
                "\n, optionsIP=" + Arrays.toString(optionsIP) +
                "\n, dataLength=" + payloadLength +
                "\n, payloadIP=" + Arrays.toString(payload) +
                "\n}";
    }

    private String ipToString(byte[] ip) {
        StringBuilder sb = new StringBuilder();
        boolean f = false;
        for (byte b : ip) {
            if (f) sb.append(".");
            String str = String.format("%d", Byte.toUnsignedInt(b));
            sb.append(str);
            f = true;
        }
        return sb.toString();
    }
}
