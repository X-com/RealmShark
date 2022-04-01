package packets.packetcapture.sniff.netpackets;

/**
 * Util class for Raw, Ether, Ip4 and TCP packets.
 */
public class UtilNetPackets {

    public static final int BYTE_SIZE_IN_BYTES = 1;
    public static final int SHORT_SIZE_IN_BYTES = 2;
    public static final int INT_SIZE_IN_BYTES = 4;
    public static final int LONG_SIZE_IN_BYTES = 8;

    public static void validateBounds(byte[] array, int offset, int len) {
        if (array == null) {
            throw new NullPointerException("Array is null");
        }
        if (array.length == 0) {
            throw new IllegalArgumentException("Array is empty");
        }
        if (len == 0) {
            throw new IllegalArgumentException("Zero len error");
        }
        if (offset < 0) {
            throw new ArrayIndexOutOfBoundsException("Offset negative " + offset);
        }
        if (len < 0) {
            throw new ArrayIndexOutOfBoundsException("Len negative " + len);
        }
        if (offset + len > array.length) {
            throw new ArrayIndexOutOfBoundsException("Len plus offset larger than array offset: " + offset + " len: " + len + " array: " + array.length);
        }
    }

    public static byte[] getBytes(byte[] data, int offset, int length) {
        validateBounds(data, offset, length);

        byte[] subArray = new byte[length];
        System.arraycopy(data, offset, subArray, 0, length);
        return subArray;
    }

    public static int getByte(byte[] data, int typeOffset) {
        return 0xFF & Byte.toUnsignedInt(data[typeOffset]);
    }

    public static int getShort(byte[] data, int typeOffset) {
        return 0xFFFF & ((Byte.toUnsignedInt(data[typeOffset]) << 8) | (Byte.toUnsignedInt(data[typeOffset + 1])));
    }

    public static int getInt(byte[] data, int typeOffset) {
        return (Byte.toUnsignedInt(data[typeOffset]) << 24) | (Byte.toUnsignedInt(data[typeOffset + 1]) << 16) | (Byte.toUnsignedInt(data[typeOffset + 2]) << 8) | Byte.toUnsignedInt(data[typeOffset + 3]);
    }

    public static long getIntAsLong(byte[] data, int typeOffset) {
        return Integer.toUnsignedLong((Byte.toUnsignedInt(data[typeOffset]) << 24) | (Byte.toUnsignedInt(data[typeOffset + 1]) << 16) | (Byte.toUnsignedInt(data[typeOffset + 2]) << 8) | Byte.toUnsignedInt(data[typeOffset + 3]));
    }
}
