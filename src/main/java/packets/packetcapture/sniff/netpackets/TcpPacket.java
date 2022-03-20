package packets.packetcapture.sniff.netpackets;

import java.util.Arrays;

public class TcpPacket {

    private static final int SRC_PORT_OFFSET = 0;
    private static final int SRC_PORT_SIZE = 2;
    private static final int DST_PORT_OFFSET = 2;
    private static final int DST_PORT_SIZE = 2;
    private static final int SEQUENCE_NUMBER_OFFSET = 4;
    private static final int SEQUENCE_NUMBER_SIZE = 4;
    private static final int ACKNOWLEDGMENT_NUMBER_OFFSET = 8;
    private static final int ACKNOWLEDGMENT_NUMBER_SIZE = 4;
    private static final int DATA_OFFSET_BITS_OFFSET = 12;
    private static final int CONTROL_BITS_OFFSET = 13;
    private static final int WINDOW_OFFSET = 14;
    private static final int WINDOW_SIZE = 2;
    private static final int CHECKSUM_OFFSET = 16;
    private static final int CHECKSUM_SIZE = 2;
    private static final int URGENT_POINTER_OFFSET = 18;
    private static final int URGENT_POINTER_SIZE = 2;
    private static final int OPTIONS_OFFSET_TCP = 20;
    private static final int MIN_TCP_HEADER_SIZE = 20;

    private final byte[] rawData;
    private final int srcPort;
    private final int dstPort;
    private final long sequenceNumber;
    private final long acknowledgmentNumber;
    private final int dataOffset;
    private final int reserved;
    private final boolean urg;
    private final boolean ack;
    private final boolean psh;
    private final boolean rst;
    private final boolean syn;
    private final boolean fin;
    private final int window;
    private final int checksum;
    private final int urgentPointer;
    private final byte[] optionsTCP;
    private final byte[] payload;
    private final int payloadSize;

    public TcpPacket(byte[] data, int length) {
        rawData = data;
        srcPort = UtilTcp.getShort(data, SRC_PORT_OFFSET);
        dstPort = UtilTcp.getShort(data, DST_PORT_OFFSET);
        sequenceNumber = UtilTcp.getIntAsLong(data, SEQUENCE_NUMBER_OFFSET);
        acknowledgmentNumber = UtilTcp.getIntAsLong(data, ACKNOWLEDGMENT_NUMBER_OFFSET);

        int sizeControlBits = UtilTcp.getByte(data, DATA_OFFSET_BITS_OFFSET);
        dataOffset = (sizeControlBits & 0xF0) >> 4;
        reserved = sizeControlBits & 0xF;

        int reservedAndControlBits = UtilTcp.getByte(data, CONTROL_BITS_OFFSET);
        urg = (reservedAndControlBits & 0x0020) != 0;
        ack = (reservedAndControlBits & 0x0010) != 0;
        psh = (reservedAndControlBits & 0x0008) != 0;
        rst = (reservedAndControlBits & 0x0004) != 0;
        syn = (reservedAndControlBits & 0x0002) != 0;
        fin = (reservedAndControlBits & 0x0001) != 0;

        window = UtilTcp.getShort(data, WINDOW_OFFSET);
        checksum = UtilTcp.getShort(data, CHECKSUM_OFFSET);
        urgentPointer = UtilTcp.getShort(data, URGENT_POINTER_OFFSET);

        int headerLengthTCP = (0xFF & dataOffset) * 4;

        if (headerLengthTCP != OPTIONS_OFFSET_TCP) {
            optionsTCP = UtilTcp.getBytes(data, OPTIONS_OFFSET_TCP, headerLengthTCP - MIN_TCP_HEADER_SIZE);
        } else {
            optionsTCP = new byte[0];
        }

        payloadSize = length - headerLengthTCP;
        if (payloadSize != 0) {
            payload = UtilTcp.getBytes(data, headerLengthTCP, payloadSize);
        } else {
            payload = new byte[0];
        }
    }

    public byte[] getRawData() {
        return rawData;
    }

    public int getSrcPort() {
        return srcPort;
    }

    public int getDstPort() {
        return dstPort;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public long getAcknowledgmentNumber() {
        return acknowledgmentNumber;
    }

    public int getDataOffset() {
        return dataOffset;
    }

    public int getReserved() {
        return reserved;
    }

    public boolean isUrg() {
        return urg;
    }

    public boolean isAck() {
        return ack;
    }

    public boolean isPsh() {
        return psh;
    }

    public boolean isRst() {
        return rst;
    }

    public boolean isSyn() {
        return syn;
    }

    public boolean isFin() {
        return fin;
    }

    public boolean isResetBit() {
        return rst || syn || fin;
    }

    public int getWindow() {
        return window;
    }

    public int getChecksum() {
        return checksum;
    }

    public int getUrgentPointer() {
        return urgentPointer;
    }

    public byte[] getOptionsTCP() {
        return optionsTCP;
    }

    public byte[] getPayload() {
        return payload;
    }

    public int getPayloadSize() {
        return payloadSize;
    }

    @Override
    public String toString() {
        return "TcpPacket{" +
                "\n  srcPort=" + srcPort +
                "\n, dstPort=" + dstPort +
                "\n, sequenceNumber=" + sequenceNumber +
                "\n, acknowledgmentNumber=" + acknowledgmentNumber +
                "\n, dataOffset=" + dataOffset +
                "\n, reserved=" + reserved +
                "\n, urg=" + urg +
                "\n, ack=" + ack +
                "\n, psh=" + psh +
                "\n, rst=" + rst +
                "\n, syn=" + syn +
                "\n, fin=" + fin +
                "\n, window=" + window +
                "\n, checksum=" + checksum +
                "\n, urgentPointer=" + urgentPointer +
                "\n, optionsTCP=" + Arrays.toString(optionsTCP) +
                "\n, payloadTCP=" + Arrays.toString(payload) +
                "\n, payloadSize=" + payloadSize +
                "\n}";
    }
}
