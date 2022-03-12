package packets.packetcapture.networktap;

public class UtilTcp {

    public static void validateBounds(byte[] array, int offset, int len) {
        if (array == null) {
            throw new NullPointerException("Array is null");
        }
        if (array.length == 0) {
            throw new IllegalArgumentException("Array is empty");
        }
        if (len == 0) {
            throw new RuntimeException("Zero len error");
        }
        if (offset < 0 || len < 0) {
            throw new ArrayIndexOutOfBoundsException("Offset or len negative");
        }
        if (offset + len > array.length) {
            throw new ArrayIndexOutOfBoundsException("Len plus offset larger than array");
        }
    }

    public static byte[] getBytes(byte[] data, int offset, int length) {
        validateBounds(data, offset, length);

        byte[] subArray = new byte[length];
        System.arraycopy(data, offset, subArray, 0, length);
        return subArray;
    }

    public static int getByte(byte[] data, int typeOffset) {
        return Byte.toUnsignedInt(data[typeOffset]);
    }

    public static int getShort(byte[] data, int typeOffset) {
        return (Byte.toUnsignedInt(data[typeOffset]) << 8) | (Byte.toUnsignedInt(data[typeOffset + 1]));
    }

    public static int getInt(byte[] data, int typeOffset) {
        return (Byte.toUnsignedInt(data[typeOffset]) << 24) | (Byte.toUnsignedInt(data[typeOffset + 1]) << 16) | (Byte.toUnsignedInt(data[typeOffset + 2]) << 8) | Byte.toUnsignedInt(data[typeOffset + 3]);
    }
}
