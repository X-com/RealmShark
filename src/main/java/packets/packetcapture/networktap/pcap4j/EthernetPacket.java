package packets.packetcapture.networktap.pcap4j;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static packets.packetcapture.networktap.pcap4j.ByteArrays.SHORT_SIZE_IN_BYTES;

/**
 * This Class handles from DA to data. Both preamble, SFD, and FCS are not contained.
 *
 * @author Kaito Yamada
 * @since pcap4j 0.9.1
 */
public final class EthernetPacket extends Packet {

    public static final int IEEE802_3_MAX_LENGTH = 1500;

    private static final int MIN_ETHERNET_PAYLOAD_LENGTH = 46; // [bytes]
    private static final int MAX_ETHERNET_PAYLOAD_LENGTH = 1500; // [bytes]

    private final EthernetHeader header;
    private final Packet payload;

    // Ethernet frame must be at least 60 bytes except FCS.
    // If it's less than 60 bytes, it's padded with this field.
    // Although this class handles pad, it's actually responsibility of NIF.
    private final byte[] pad;

    /**
     * A static factory method. This method validates the arguments by {@link
     * ByteArrays#validateBounds(byte[], int, int)}, which may throw exceptions undocumented here.
     *
     * @param rawData rawData
     * @param offset  offset
     * @param length  length
     * @param ts
     * @return a new EthernetPacket object.
     * @throws IllegalRawDataException if parsing the raw data fails.
     */
    public static EthernetPacket newPacket(byte[] rawData, int offset, int length, Instant ts) {
        instant = ts;
        ByteArrays.validateBounds(rawData, offset, length);
        return new EthernetPacket(rawData, offset, length);
    }

    private EthernetPacket(byte[] rawData, int offset, int length) {
        this.header = new EthernetHeader(rawData, offset, length);

        if ((header.getType() & 0xFFFF) <= IEEE802_3_MAX_LENGTH) {
            int payloadLength = header.getType();
            int padLength = length - header.length() - payloadLength;
            int payloadOffset = offset + header.length();

            if (padLength < 0) {
                throw new RuntimeException(
                        "The value of the ether type (length) field seems to be wrong: "
                                + header.getType());
            }

            if (payloadLength > 0) {
                this.payload = IpV4Packet.newPacket(rawData, payloadOffset, payloadLength);
            } else { // payloadLength == 0
                this.payload = null;
            }

            if (padLength > 0) {
                this.pad = ByteArrays.getSubArray(rawData, payloadOffset + payloadLength, padLength);
            } else {
                this.pad = new byte[0];
            }
        } else {
            int payloadAndPadLength = length - header.length();
            if (payloadAndPadLength > 0) {
                int payloadOffset = offset + header.length();
                this.payload = IpV4Packet.newPacket(rawData, payloadOffset, payloadAndPadLength);

                int padLength = payloadAndPadLength - payload.length();
                if (padLength > 0) {
                    this.pad = ByteArrays.getSubArray(rawData, payloadOffset + payload.length(), padLength);
                } else {
                    this.pad = new byte[0];
                }
            } else {
                this.payload = null;
                this.pad = new byte[0];
            }
        }
    }

    @Override
    public EthernetHeader getHeader() {
        return header;
    }

    @Override
    public Packet getPayload() {
        return payload;
    }

    /**
     * @return pad
     */
    public byte[] getPad() {
        byte[] copy = new byte[pad.length];
        System.arraycopy(pad, 0, copy, 0, pad.length);
        return copy;
    }

    @Override
    protected int calcLength() {
        int length = super.calcLength();
        length += pad.length;
        return length;
    }

    @Override
    protected byte[] buildRawData() {
        byte[] rawData = super.buildRawData();
        if (pad.length != 0) {
            System.arraycopy(pad, 0, rawData, rawData.length - pad.length, pad.length);
        }
        return rawData;
    }

    @Override
    protected String buildString() {
        StringBuilder sb = new StringBuilder();

        sb.append(header.toString());
        if (payload != null) {
            sb.append(payload.toString());
        }
        if (pad.length != 0) {
            String ls = System.getProperty("line.separator");
            sb.append("[Ethernet Pad (")
                    .append(pad.length)
                    .append(" bytes)]")
                    .append(ls)
                    .append("  Hex stream: ")
                    .append(ByteArrays.toHexString(pad, " "))
                    .append(ls);
        }

        return sb.toString();
    }

    @Override
    protected int calcHashCode() {
        return 31 * super.calcHashCode() + Arrays.hashCode(pad);
    }

    /**
     * @author Kaito Yamada
     * @since pcap4j 0.9.1
     */
    public static final class EthernetHeader extends PacketHeader {

        /*
         *  0                            15
         * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
         * |    Dst Hardware Address       |
         * +                               +
         * |                               |
         * +                               +
         * |                               |
         * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
         * |    Src Hardware Address       |
         * +                               +
         * |                               |
         * +                               +
         * |                               |
         * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
         * |         Type                  |
         * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
         */

        /**
         *
         */
        private static final long serialVersionUID = -8271269099161190389L;

        private static final int DST_ADDR_OFFSET = 0;
        private static final int DST_ADDR_SIZE = ByteArrays.MAC_SIZE_IN_BYTES;
        private static final int SRC_ADDR_OFFSET = DST_ADDR_OFFSET + DST_ADDR_SIZE;
        private static final int SRC_ADDR_SIZE = ByteArrays.MAC_SIZE_IN_BYTES;
        private static final int TYPE_OFFSET = SRC_ADDR_OFFSET + SRC_ADDR_SIZE;
        private static final int TYPE_SIZE = SHORT_SIZE_IN_BYTES;
        private static final int ETHERNET_HEADER_SIZE = TYPE_OFFSET + TYPE_SIZE;

        private final byte[] dstAddr;
        private final byte[] srcAddr;
        private final short type;

        private EthernetHeader(byte[] rawData, int offset, int length) {
            if (length < ETHERNET_HEADER_SIZE) {
                StringBuilder sb = new StringBuilder(100);
                sb.append("The data is too short to build an Ethernet header(")
                        .append(ETHERNET_HEADER_SIZE)
                        .append(" bytes). data: ")
                        .append(ByteArrays.toHexString(rawData, " "))
                        .append(", offset: ")
                        .append(offset)
                        .append(", length: ")
                        .append(length);
                throw new RuntimeException(sb.toString());
            }

            this.dstAddr = ByteArrays.getMacAddress(rawData, DST_ADDR_OFFSET + offset);
            this.srcAddr = ByteArrays.getMacAddress(rawData, SRC_ADDR_OFFSET + offset);
            this.type = ByteArrays.getShort(rawData, TYPE_OFFSET + offset);
        }

        /**
         * @return dstAddr
         */
        public byte[] getDstAddr() {
            return dstAddr;
        }

        /**
         * @return srcAddr
         */
        public byte[] getSrcAddr() {
            return srcAddr;
        }

        /**
         * @return type
         */
        public short getType() {
            return type;
        }

        @Override
        protected List<byte[]> getRawFields() {
            List<byte[]> rawFields = new ArrayList<byte[]>();
            rawFields.add(dstAddr);
            rawFields.add(srcAddr);
            rawFields.add(ByteArrays.toByteArray(type));
            return rawFields;
        }

        @Override
        public int length() {
            return ETHERNET_HEADER_SIZE;
        }

        @Override
        protected String buildString() {
            StringBuilder sb = new StringBuilder();
            String ls = System.getProperty("line.separator");

            sb.append("[Ethernet Header (").append(length()).append(" bytes)]").append(ls);
            sb.append("  Destination address: ").append(dstAddr).append(ls);
            sb.append("  Source address: ").append(srcAddr).append(ls);
            sb.append("  Type: ").append(type).append(ls);

            return sb.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!this.getClass().isInstance(obj)) {
                return false;
            }

            EthernetHeader other = (EthernetHeader) obj;
            return dstAddr.equals(other.dstAddr)
                    && srcAddr.equals(other.srcAddr)
                    && type == other.type;
        }

        @Override
        protected int calcHashCode() {
            int result = 17;
            result = 31 * result + dstAddr.hashCode();
            result = 31 * result + srcAddr.hashCode();
            result = 31 * result + type;
            return result;
        }
    }
}