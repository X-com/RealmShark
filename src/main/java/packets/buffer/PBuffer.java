package packets.buffer;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Custom buffer class to deserialize the rotmg packets.
 */
public class PBuffer {
    ByteBuffer buffer;

    public PBuffer(ByteBuffer data) {
        buffer = data;
    }

    /**
     * Returns the buffer size.
     *
     * @return size of the buffer.
     */
    public int size() {
        return buffer.capacity();
    }

    /**
     * Internal index of the buffer.
     *
     * @return Returns the internal index the buffer is at.
     */
    public int getIndex() {
        return buffer.position();
    }

    /**
     * Deserialize a boolean.
     *
     * @return Returns a boolean that have been deserialized.
     */
    public boolean readBoolean() {
        return buffer.get() != 0;
    }

    /**
     * Deserialize a byte.
     *
     * @return Returns the byte that have been deserialized.
     */
    public byte readByte() {
        return buffer.get();
    }

    /**
     * Deserialize an unsigned byte.
     *
     * @return Returns an integer containing an unsigned byte that have
     * been deserialized.
     */
    public int readUnsignedByte() {
        return Byte.toUnsignedInt(buffer.get());
    }

    /**
     * Deserialize a short.
     *
     * @return Returns the short that have been deserialized.
     */
    public short readShort() {
        return buffer.getShort();
    }

    /**
     * Deserialize an unsigned short.
     *
     * @return Returns an integer containing an unsigned short that have
     * been deserialized.
     */
    public int readUnsignedShort() {
        return Short.toUnsignedInt(buffer.getShort());
    }

    /**
     * Deserialize an integer.
     *
     * @return Returns the integer that have been deserialized.
     */
    public int readInt() {
        return buffer.getInt();
    }

    /**
     * Deserialize an unsigned integer.
     *
     * @return Returns an integer containing an unsigned integer that have
     * been deserialized.
     */
    public long readUnsignedInt() {
        return Integer.toUnsignedLong(buffer.getInt());
    }

    /**
     * Deserialize a float.
     *
     * @return Returns the float that have been deserialized.
     */
    public float readFloat() {
        return buffer.getFloat();
    }

    /**
     * Deserialize a string.
     *
     * @return Returns the string that have been deserialized.
     */
    public String readString() {
        short len = readShort();
        byte[] str = new byte[len];
        buffer.get(str);
        return new String(str);
    }

    /**
     * Deserialize a string using UTF32 (more characters that is
     * never found in-game) not used as far as I'm aware.
     *
     * @return Returns the string that have been deserialized.
     */
    public String readStringUTF32() {
        int len = readInt();
        byte[] str = new byte[len];
        buffer.get(str);
        return new String(str);
    }

    /**
     * Deserialize a byte array
     */
    public byte[] readByteArray() {
        short arraylen = readShort();
        byte[] out = new byte[arraylen];
        for (int i = 0; i < arraylen; i++) {
            out[i] = readByte();
        }
        return out;
    }

    /**
     * Deserialize a byte array.
     *
     * @param bytes Number of bytes that is contained in the array.
     * @return Returns a byte array that have been deserialized.
     */
    public byte[] readBytes(int bytes) {
        byte[] out = new byte[bytes];
        for (int i = 0; i < bytes; i++) {
            out[i] = buffer.get();
        }
        return out;
    }

    /**
     * Rotmg deserializer of a compressed long.
     *
     * @return Returns a long that have been deserialized.
     */
    public long readCompressedInt() {
        int uByte = readUnsignedByte();
        boolean isNegative = (uByte & 64) != 0;
        int shift = 6;
        long value = uByte & 63;

        while ((uByte & 128) != 0) {
            uByte = readUnsignedByte();
            value = value | ((long) (uByte & 127)) << shift;
            shift += 7;
        }

        if (isNegative) {
            value = -value;
        }
        return value;
    }

    /**
     * Debug print command to print the buffer byte data.
     *
     * @return String representation of buffer data in a byte array format.
     */
    public String printBufferArray() {
        return Arrays.toString(buffer.array());
    }
}
