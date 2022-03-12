/*_##########################################################################
  _##
  _##  Copyright (C) 2011-2019 Pcap4J.org
  _##
  _##########################################################################
*/

package packets.packetcapture.networktap.pcap4j;

import java.io.Serializable;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static packets.packetcapture.networktap.pcap4j.ByteArrays.*;

/**
 * @author Kaito Yamada
 * @since pcap4j 0.9.1
 */
public final class IpV4Packet extends Packet {

    // http://tools.ietf.org/html/rfc791

    private final IpV4Header header;
    private Packet payload;

    /**
     * A static factory method. This method validates the arguments by {@link
     * ByteArrays#validateBounds(byte[], int, int)}, which may throw exceptions undocumented here.
     *
     * @param rawData rawData
     * @param offset  offset
     * @param length  length
     * @return a new IpV4Packet object.
     */
    public static IpV4Packet newPacket(byte[] rawData, int offset, int length) {
        ByteArrays.validateBounds(rawData, offset, length);
        return new IpV4Packet(rawData, offset, length);
    }

    private IpV4Packet(byte[] rawData, int offset, int length) {
        this.header = new IpV4Header(rawData, offset, length);

        int remainingRawDataLength = length - header.length();
        int totalLength = header.getTotalLengthAsInt();
        int payloadLength;
        if (totalLength == 0) {
            // logger.debug("Total Length is 0. Assuming segmentation offload to be working.");
            payloadLength = remainingRawDataLength;
        } else {
            payloadLength = totalLength - header.length();
            if (payloadLength < 0) {
                throw new RuntimeException(
                        "The value of total length field seems to be wrong: " + totalLength);
            }

            if (payloadLength > remainingRawDataLength) {
                payloadLength = remainingRawDataLength;
            }
        }

        if (payloadLength != 0) { // payloadLength is positive.
            if (header.getMoreFragmentFlag() || header.getFragmentOffset() != 0) {
//        this.payload =
//            PacketFactories.getFactory(Packet.class, NotApplicable.class)
//                .newInstance(
//                    rawData, header.length() + offset, payloadLength, NotApplicable.FRAGMENTED);
            } else {
                this.payload = TcpPacket.newPacket(rawData, header.length() + offset, payloadLength);
            }
        } else {
            this.payload = null;
        }
    }

    @Override
    public IpV4Header getHeader() {
        return header;
    }

    @Override
    public Packet getPayload() {
        return payload;
    }

    /**
     * @author Kaito Yamada
     * @since pcap4j 0.9.1
     */
    public static final class IpV4Header extends PacketHeader {

        /*  0                              16                            31
         * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
         * |Version|  IHL  |Type of Service|           Total Length        |
         * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
         * |         Identification        |Flags|      Fragment Offset    |
         * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
         * |  Time to Live |    Protocol   |         Header Checksum       |
         * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
         * |                       Source Address                          |
         * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
         * |                    Destination Address                        |
         * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
         * |                    Options                    |    Padding    |
         * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
         */

        /**
         *
         */
        private static final long serialVersionUID = -7583326842445453539L;

        private static final int VERSION_AND_IHL_OFFSET = 0;
        private static final int VERSION_AND_IHL_SIZE = BYTE_SIZE_IN_BYTES;
        private static final int TOS_OFFSET = VERSION_AND_IHL_OFFSET + VERSION_AND_IHL_SIZE;
        private static final int TOS_SIZE = BYTE_SIZE_IN_BYTES;
        private static final int TOTAL_LENGTH_OFFSET = TOS_OFFSET + TOS_SIZE;
        private static final int TOTAL_LENGTH_SIZE = SHORT_SIZE_IN_BYTES;
        private static final int IDENTIFICATION_OFFSET = TOTAL_LENGTH_OFFSET + TOTAL_LENGTH_SIZE;
        private static final int IDENTIFICATION_SIZE = SHORT_SIZE_IN_BYTES;
        private static final int FLAGS_AND_FRAGMENT_OFFSET_OFFSET =
                IDENTIFICATION_OFFSET + IDENTIFICATION_SIZE;
        private static final int FLAGS_AND_FRAGMENT_OFFSET_SIZE = SHORT_SIZE_IN_BYTES;
        private static final int TTL_OFFSET =
                FLAGS_AND_FRAGMENT_OFFSET_OFFSET + FLAGS_AND_FRAGMENT_OFFSET_SIZE;
        private static final int TTL_SIZE = BYTE_SIZE_IN_BYTES;
        private static final int PROTOCOL_OFFSET = TTL_OFFSET + TTL_SIZE;
        private static final int PROTOCOL_SIZE = BYTE_SIZE_IN_BYTES;
        private static final int HEADER_CHECKSUM_OFFSET = PROTOCOL_OFFSET + PROTOCOL_SIZE;
        private static final int HEADER_CHECKSUM_SIZE = SHORT_SIZE_IN_BYTES;
        private static final int SRC_ADDR_OFFSET = HEADER_CHECKSUM_OFFSET + HEADER_CHECKSUM_SIZE;
        private static final int SRC_ADDR_SIZE = INET4_ADDRESS_SIZE_IN_BYTES;
        private static final int DST_ADDR_OFFSET = SRC_ADDR_OFFSET + SRC_ADDR_SIZE;
        private static final int DST_ADDR_SIZE = INET4_ADDRESS_SIZE_IN_BYTES;
        private static final int OPTIONS_OFFSET = DST_ADDR_OFFSET + DST_ADDR_SIZE;

        private static final int MIN_IPV4_HEADER_SIZE = DST_ADDR_OFFSET + DST_ADDR_SIZE;

        private final byte version;
        private final byte ihl;
        private final IpV4Tos tos;
        private final short totalLength;
        private final short identification;
        private final boolean reservedFlag;
        private final boolean dontFragmentFlag;
        private final boolean moreFragmentFlag;
        private final short fragmentOffset;
        private final byte ttl;
        private final byte protocol;
        private final short headerChecksum;
        private final Inet4Address srcAddr;
        private final Inet4Address dstAddr;
        private final List<IpV4Option> options;
        private final byte[] padding;

        private IpV4Header(byte[] rawData, int offset, int length) {
            if (length < MIN_IPV4_HEADER_SIZE) {
                StringBuilder sb = new StringBuilder(110);
                sb.append("The data is too short to build an IPv4 header. ")
                        .append("It must be at least ")
                        .append(MIN_IPV4_HEADER_SIZE)
                        .append(" bytes. data: ")
                        .append(ByteArrays.toHexString(rawData, " "))
                        .append(", offset: ")
                        .append(offset)
                        .append(", length: ")
                        .append(length);
                throw new RuntimeException(sb.toString());
            }

            byte versionAndIhl = ByteArrays.getByte(rawData, VERSION_AND_IHL_OFFSET + offset);
            this.version = (byte) ((versionAndIhl & 0xF0) >> 4);
            this.ihl = (byte) (versionAndIhl & 0x0F);

            this.tos = newInstanceTos(rawData, TOS_OFFSET + offset, BYTE_SIZE_IN_BYTES);
            this.totalLength = ByteArrays.getShort(rawData, TOTAL_LENGTH_OFFSET + offset);
            this.identification = ByteArrays.getShort(rawData, IDENTIFICATION_OFFSET + offset);

            short flagsAndFragmentOffset =
                    ByteArrays.getShort(rawData, FLAGS_AND_FRAGMENT_OFFSET_OFFSET + offset);
            this.reservedFlag = (flagsAndFragmentOffset & 0x8000) != 0;
            this.dontFragmentFlag = (flagsAndFragmentOffset & 0x4000) != 0;
            this.moreFragmentFlag = (flagsAndFragmentOffset & 0x2000) != 0;
            this.fragmentOffset = (short) (flagsAndFragmentOffset & 0x1FFF);

            this.ttl = ByteArrays.getByte(rawData, TTL_OFFSET + offset);
            this.protocol = ByteArrays.getByte(rawData, PROTOCOL_OFFSET + offset);
            this.headerChecksum = ByteArrays.getShort(rawData, HEADER_CHECKSUM_OFFSET + offset);
            this.srcAddr = ByteArrays.getInet4Address(rawData, SRC_ADDR_OFFSET + offset);
            this.dstAddr = ByteArrays.getInet4Address(rawData, DST_ADDR_OFFSET + offset);

            int headerLength = getIhlAsInt() * 4;
            if (length < headerLength) {
                StringBuilder sb = new StringBuilder(110);
                sb.append("The data is too short to build an IPv4 header(")
                        .append(headerLength)
                        .append(" bytes). data: ")
                        .append(ByteArrays.toHexString(rawData, " "))
                        .append(", offset: ")
                        .append(offset)
                        .append(", length: ")
                        .append(length);
                throw new RuntimeException(sb.toString());
            }
            if (headerLength < OPTIONS_OFFSET) {
                StringBuilder sb = new StringBuilder(100);
                sb.append("The ihl must be equal or more than")
                        .append(OPTIONS_OFFSET / 4)
                        .append("but it is: ")
                        .append(getIhlAsInt());
                throw new RuntimeException(sb.toString());
            }

            this.options = new ArrayList<>();
            int currentOffsetInHeader = OPTIONS_OFFSET;
            try {
                while (currentOffsetInHeader < headerLength) {
                    byte type = rawData[currentOffsetInHeader + offset];
                    IpV4Option newOne = newInstanceOption(
                            rawData,
                            currentOffsetInHeader + offset,
                            headerLength - currentOffsetInHeader);
                    options.add(newOne);
                    currentOffsetInHeader += newOne.length();

                    if (newOne.getType() == 0) { // END_OF_OPTION_LIST == 0
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            int paddingLength = headerLength - currentOffsetInHeader;
            if (paddingLength != 0) {
                this.padding =
                        ByteArrays.getSubArray(rawData, currentOffsetInHeader + offset, paddingLength);
            } else {
                this.padding = new byte[0];
            }
        }

        private short calcHeaderChecksum(boolean zeroInsteadOfChecksum) {
            return ByteArrays.calcChecksum(buildRawData(zeroInsteadOfChecksum));
        }

        public byte getVersion() {
            return version;
        }

        /**
         * @return ihl
         */
        public byte getIhl() {
            return ihl;
        }

        /**
         * @return ihl
         */
        public int getIhlAsInt() {
            return 0xFF & ihl;
        }

        /**
         * @return tos
         */
        public IpV4Tos getTos() {
            return tos;
        }

        /**
         * @return totalLength
         */
        public short getTotalLength() {
            return totalLength;
        }

        /**
         * @return totalLength
         */
        public int getTotalLengthAsInt() {
            return 0xFFFF & totalLength;
        }

        /**
         * @return identification
         */
        public short getIdentification() {
            return identification;
        }

        /**
         * @return identification
         */
        public int getIdentificationAsInt() {
            return 0xFFFF & identification;
        }

        /**
         * @return reservedFlag
         */
        public boolean getReservedFlag() {
            return reservedFlag;
        }

        /**
         * @return dontFragmentFlag
         */
        public boolean getDontFragmentFlag() {
            return dontFragmentFlag;
        }

        /**
         * @return moreFragmentFlag
         */
        public boolean getMoreFragmentFlag() {
            return moreFragmentFlag;
        }

        /**
         * @return fragmentOffset
         */
        public short getFragmentOffset() {
            return fragmentOffset;
        }

        /**
         * @return ttl
         */
        public byte getTtl() {
            return ttl;
        }

        /**
         * @return ttl
         */
        public int getTtlAsInt() {
            return 0xFF & ttl;
        }

        public byte getProtocol() {
            return protocol;
        }

        /**
         * @return headerChecksum
         */
        public short getHeaderChecksum() {
            return headerChecksum;
        }

        public Inet4Address getSrcAddr() {
            return srcAddr;
        }

        public Inet4Address getDstAddr() {
            return dstAddr;
        }

        /**
         * @return options
         */
        public List<IpV4Option> getOptions() {
            return new ArrayList<IpV4Option>(options);
        }

        /**
         * @return padding
         */
        public byte[] getPadding() {
            byte[] copy = new byte[padding.length];
            System.arraycopy(padding, 0, copy, 0, padding.length);
            return copy;
        }

        /**
         * @param acceptZero acceptZero
         * @return true if the packet represented by this object has a valid checksum; false otherwise.
         */
        public boolean hasValidChecksum(boolean acceptZero) {
            short calculatedChecksum = calcHeaderChecksum(false);
            if (calculatedChecksum == 0) {
                return true;
            }

            if (headerChecksum == 0 && acceptZero) {
                return true;
            }

            return false;
        }

        @Override
        protected List<byte[]> getRawFields() {
            return getRawFields(false);
        }

        private List<byte[]> getRawFields(boolean zeroInsteadOfChecksum) {
            byte flags = 0;
            if (moreFragmentFlag) {
                flags = (byte) 1;
            }
            if (dontFragmentFlag) {
                flags = (byte) (flags | 2);
            }
            if (reservedFlag) {
                flags = (byte) (flags | 4);
            }

            List<byte[]> rawFields = new ArrayList<byte[]>();
            rawFields.add(ByteArrays.toByteArray((byte) ((version << 4) | ihl)));
            rawFields.add(new byte[]{tos.value()});
            rawFields.add(ByteArrays.toByteArray(totalLength));
            rawFields.add(ByteArrays.toByteArray(identification));
            rawFields.add(ByteArrays.toByteArray((short) ((flags << 13) | fragmentOffset)));
            rawFields.add(ByteArrays.toByteArray(ttl));
            rawFields.add(ByteArrays.toByteArray(protocol));
            rawFields.add(ByteArrays.toByteArray(zeroInsteadOfChecksum ? (short) 0 : headerChecksum));
            rawFields.add(ByteArrays.toByteArray(srcAddr));
            rawFields.add(ByteArrays.toByteArray(dstAddr));
            for (IpV4Option o : options) {
                rawFields.add(o.getRawData());
            }
            rawFields.add(padding);
            return rawFields;
        }

        private byte[] buildRawData(boolean zeroInsteadOfChecksum) {
            return ByteArrays.concatenate(getRawFields(zeroInsteadOfChecksum));
        }

        private int measureLengthWithoutPadding() {
            int len = 0;
            for (IpV4Option o : options) {
                len += o.length();
            }
            return len + MIN_IPV4_HEADER_SIZE;
        }

        @Override
        protected int calcLength() {
            return measureLengthWithoutPadding() + padding.length;
        }

        @Override
        protected String buildString() {
            StringBuilder sb = new StringBuilder();
            String ls = System.getProperty("line.separator");

            sb.append("[IPv4 Header (").append(length()).append(" bytes)]").append(ls);
            sb.append("  Version: ").append(version).append(ls);
            sb.append("  IHL: ").append(ihl).append(" (").append(ihl * 4).append(" [bytes])").append(ls);
            sb.append("  TOS: ").append(tos).append(ls);
            sb.append("  Total length: ").append(getTotalLengthAsInt()).append(" [bytes]").append(ls);
            sb.append("  Identification: ").append(getIdentificationAsInt()).append(ls);
            sb.append("  Flags: (Reserved, Don't Fragment, More Fragment) = (")
                    .append(getReservedFlag())
                    .append(", ")
                    .append(getDontFragmentFlag())
                    .append(", ")
                    .append(getMoreFragmentFlag())
                    .append(")")
                    .append(ls);
            sb.append("  Fragment offset: ")
                    .append(fragmentOffset)
                    .append(" (")
                    .append(fragmentOffset * 8)
                    .append(" [bytes])")
                    .append(ls);
            sb.append("  TTL: ").append(getTtlAsInt()).append(ls);
            sb.append("  Protocol: ").append(protocol).append(ls);
            sb.append("  Header checksum: 0x")
                    .append(ByteArrays.toHexString(headerChecksum, ""))
                    .append(ls);
            sb.append("  Source address: ").append(srcAddr).append(ls);
            sb.append("  Destination address: ").append(dstAddr).append(ls);
            for (IpV4Option opt : options) {
                sb.append("  Option: ").append(opt).append(ls);
            }
            if (padding.length != 0) {
                sb.append("  Padding: 0x").append(ByteArrays.toHexString(padding, " ")).append(ls);
            }

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

            IpV4Header other = (IpV4Header) obj;
            return identification == other.identification
                    && headerChecksum == other.headerChecksum
                    && srcAddr.equals(other.srcAddr)
                    && dstAddr.equals(other.dstAddr)
                    && totalLength == other.totalLength
                    && protocol == other.protocol
                    && ttl == other.ttl
                    && fragmentOffset == other.fragmentOffset
                    && reservedFlag == other.reservedFlag
                    && dontFragmentFlag == other.dontFragmentFlag
                    && moreFragmentFlag == other.moreFragmentFlag
                    && tos.equals(other.tos)
                    && ihl == other.ihl
                    && version == other.version
                    && options.equals(other.options)
                    && Arrays.equals(padding, other.padding);
        }

        @Override
        protected int calcHashCode() {
            int result = 17;
            result = 31 * result + version;
            result = 31 * result + ihl;
            result = 31 * result + tos.hashCode();
            result = 31 * result + totalLength;
            result = 31 * result + identification;
            result = 31 * result + (reservedFlag ? 1231 : 1237);
            result = 31 * result + (dontFragmentFlag ? 1231 : 1237);
            result = 31 * result + (moreFragmentFlag ? 1231 : 1237);
            result = 31 * result + fragmentOffset;
            result = 31 * result + ttl;
            result = 31 * result + protocol;
            result = 31 * result + headerChecksum;
            result = 31 * result + srcAddr.hashCode();
            result = 31 * result + dstAddr.hashCode();
            result = 31 * result + Arrays.hashCode(padding);
            result = 31 * result + options.hashCode();
            return result;
        }
    }

    /**
     * The interface representing an IPv4 TOS. If you use {link
     * org.pcap4j.packet.factory.propertiesbased.PropertiesBasedPacketFactory
     * PropertiesBasedPacketFactory}, classes which implement this interface must implement the
     * following method: {@code public static IpV4Tos newInstance(byte value)}
     *
     * @author Kaito Yamada
     * @since pcap4j 0.9.11
     */
    public interface IpV4Tos extends Serializable {

        /**
         * @return value
         */
        public byte value();
    }

    public static IpV4Packet.IpV4Tos newInstanceTos(byte[] rawData, int offset, int length) {
        ByteArrays.validateBounds(rawData, offset, length);
        return IpV4Rfc1349Tos.newInstance(rawData[offset]);
    }

    static public class IpV4Rfc1349Tos implements IpV4Packet.IpV4Tos {

        /**
         *
         */
        private static final long serialVersionUID = 1760697525836662144L;

        /* http://www.ietf.org/rfc/rfc1349.txt
         *
         *    0     1     2     3     4     5     6     7
         * +-----+-----+-----+-----+-----+-----+-----+-----+
         * |                 |                       |     |
         * |   PRECEDENCE    |          TOS          | MBZ |
         * |                 |                       |     |
         * +-----+-----+-----+-----+-----+-----+-----+-----+
         */

        private final byte precedence;
        private final byte tos;
        private final boolean mbz;

        /**
         * @param value value
         * @return a new IpV4Rfc1349Tos object.
         */
        public static IpV4Rfc1349Tos newInstance(byte value) {
            return new IpV4Rfc1349Tos(value);
        }

        private IpV4Rfc1349Tos(byte value) {
            this.precedence = (byte) ((value & 0xE0) >> 5);
            this.tos = (byte) (0x0F & (value >> 1));
            this.mbz = (value & 0x01) != 0;
        }

        /**
         * @return precedence
         */
        public byte getPrecedence() {
            return precedence;
        }

        /**
         * @return tos
         */
        public byte getTos() {
            return tos;
        }

        /**
         * @return mbz
         */
        public boolean mbz() {
            return mbz;
        }

        public byte value() {
            byte value = (byte) (precedence << 5);
            value = (byte) (value | tos << 1);
            if (mbz) {
                value = (byte) (value | 0x01);
            }
            return value;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[precedence: ")
                    .append(precedence)
                    .append("] [tos: ")
                    .append(tos)
                    .append("] [mbz: ")
                    .append(mbz ? 1 : 0)
                    .append("]");

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
            return (getClass().cast(obj)).value() == this.value();
        }

        @Override
        public int hashCode() {
            return value();
        }
    }

    public interface IpV4Option extends Serializable {

        /**
         * @return type
         */
        public byte getType();

        /**
         * @return length
         */
        public int length();

        /**
         * @return raw data
         */
        public byte[] getRawData();
    }

    /**
     * This method is a variant of {@link #newInstance(byte[], int, int, IpV4OptionType...)} and
     * exists only for performance reason.
     */
    public static IpV4Option newInstanceOption(byte[] rawData, int offset, int length) {
        return UnknownIpV4Option.newInstance(rawData, offset, length);
    }

    static public class UnknownIpV4Option implements IpV4Option {

        private final byte type;
        private final byte length;
        private final byte[] data;

        /**
         * A static factory method. This method validates the arguments by {@link
         * ByteArrays#validateBounds(byte[], int, int)}, which may throw exceptions undocumented here.
         *
         * @param rawData rawData
         * @param offset  offset
         * @param length  length
         * @return a new UnknownIpV4Option object.
         */
        public static UnknownIpV4Option newInstance(byte[] rawData, int offset, int length)
                throws RuntimeException {
            ByteArrays.validateBounds(rawData, offset, length);
            return new UnknownIpV4Option(rawData, offset, length);
        }

        private UnknownIpV4Option(byte[] rawData, int offset, int length) {
            if (length < 2) {
                StringBuilder sb = new StringBuilder(100);
                sb.append("The raw data length must be more than 1. rawData: ")
                        .append(ByteArrays.toHexString(rawData, " "))
                        .append(", offset: ")
                        .append(offset)
                        .append(", length: ")
                        .append(length);
                throw new RuntimeException(sb.toString());
            }

            this.type = rawData[offset];
            this.length = rawData[1 + offset];
            int lengthFieldAsInt = getLengthAsInt();
            if (length < lengthFieldAsInt) {
                StringBuilder sb = new StringBuilder(100);
                sb.append("The raw data is too short to build this option (")
                        .append(lengthFieldAsInt)
                        .append("). data: ")
                        .append(ByteArrays.toHexString(rawData, " "))
                        .append(", offset: ")
                        .append(offset)
                        .append(", length: ")
                        .append(length);
                throw new RuntimeException(sb.toString());
            }

            this.data = ByteArrays.getSubArray(rawData, 2 + offset, lengthFieldAsInt - 2);
        }

        @Override
        public byte getType() {
            return type;
        }

        /**
         * @return length
         */
        public byte getLength() {
            return length;
        }

        /**
         * @return length
         */
        public int getLengthAsInt() {
            return 0xFF & length;
        }

        /**
         * @return data
         */
        public byte[] getData() {
            byte[] copy = new byte[data.length];
            System.arraycopy(data, 0, copy, 0, data.length);
            return copy;
        }

        public byte[] getRawData() {
            byte[] rawData = new byte[length()];
            rawData[0] = type;
            rawData[1] = length;
            System.arraycopy(data, 0, rawData, 2, data.length);
            return rawData;
        }

        public int length() {
            return data.length + 2;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[option-type: ")
                    .append(type)
                    .append("] [option-length: ")
                    .append(getLengthAsInt())
                    .append(" bytes] [option-data: 0x")
                    .append(ByteArrays.toHexString(data, ""))
                    .append("]");
            return sb.toString();
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!this.getClass().isInstance(obj)) {
                return false;
            }

            UnknownIpV4Option other = (UnknownIpV4Option) obj;
            return type == other.type && length == other.length && Arrays.equals(data, other.data);
        }

        public int hashCode() {
            int result = 17;
            result = 31 * result + type;
            result = 31 * result + length;
            result = 31 * result + Arrays.hashCode(data);
            return result;
        }
    }
}
