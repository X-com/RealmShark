package util;

import packets.buffer.PBuffer;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Custom buffer class to deserialize the rotmg packets.
 */
public class PBufferDebugger extends PBuffer {
    int indent = 0;

    public PBufferDebugger(ByteBuffer data) {
        super(data);
        data.position(5);
    }

    private void printDebugStart(String name){
//        indent += 5;
        try {
            System.out.printf("%s i:%d s:%d \n", name, getIndex(), size());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void printDebugEnd(String out){
        System.out.printf("%s\n", out);
//        indent -= 5;
    }

    /**
     * Returns the buffer size.
     *
     * @return size of the buffer.
     */
    public int size() {
        return super.size();
    }

    /**
     * Internal index of the buffer.
     *
     * @return Returns the internal index the buffer is at.
     */
    public int getIndex() {
        return super.getIndex();
    }

    /**
     * Deserialize a boolean.
     *
     * @return Returns a boolean that have been deserialized.
     */
    public boolean readBoolean() {
        printDebugStart("readBoolean");
        boolean b = super.readBoolean();
        printDebugEnd(b+"");
        return b;
    }

    /**
     * Deserialize a byte.
     *
     * @return Returns the byte that have been deserialized.
     */
    public byte readByte() {
        printDebugStart("readByte");
        byte b = super.readByte();
        printDebugEnd(b+"");
        return b;
    }

    /**
     * Deserialize an unsigned byte.
     *
     * @return Returns an integer containing an unsigned byte that have
     * been deserialized.
     */
    public int readUnsignedByte() {
        printDebugStart("readUnsignedByte");
        int b = super.readUnsignedByte();
        printDebugEnd(b+"");
        return b;
    }

    /**
     * Deserialize a short.
     *
     * @return Returns the short that have been deserialized.
     */
    public short readShort() {
        printDebugStart("readShort");
        short s = super.readShort();
        printDebugEnd(s+"");
        return s;
    }

    /**
     * Deserialize an unsigned short.
     *
     * @return Returns an integer containing an unsigned short that have
     * been deserialized.
     */
    public int readUnsignedShort() {
        printDebugStart("readUnsignedShort");
        int s = super.readUnsignedShort();
        printDebugEnd(s+"");
        return s;
    }

    /**
     * Deserialize an integer.
     *
     * @return Returns the integer that have been deserialized.
     */
    public int readInt() {
        printDebugStart("readInt");
        int i = super.readInt();
        printDebugEnd(i+"");
        return i;
    }

    /**
     * Deserialize an unsigned integer.
     *
     * @return Returns an integer containing an unsigned integer that have
     * been deserialized.
     */
    public long readUnsignedInt() {
        printDebugStart("readUnsignedInt");
        long i = super.readUnsignedInt();
        printDebugEnd(i+"");
        return i;
    }

    /**
     * Deserialize a float.
     *
     * @return Returns the float that have been deserialized.
     */
    public float readFloat() {
        printDebugStart("readFloat");
        float f = super.readFloat();
        printDebugEnd(f+"");
        return f;
    }

    /**
     * Deserialize a string.
     *
     * @return Returns the string that have been deserialized.
     */
    public String readString() {
        printDebugStart("readString");
        String s = super.readString();
        printDebugEnd(s);
        return s;
    }

    /**
     * Deserialize a string using UTF32 (more characters that is
     * never found in-game) not used as far as I'm aware.
     *
     * @return Returns the string that have been deserialized.
     */
    public String readStringUTF32() {
        printDebugStart("readStringUTF32");
        String s = super.readStringUTF32();
        printDebugEnd(s);
        return s;
    }

    /**
     * Deserialize a byte array
     */
    public byte[] readByteArray() {
        printDebugStart("readByteArray");
        byte[] b = super.readByteArray();
        printDebugEnd(Arrays.toString(b));
        return b;
    }

    /**
     * Deserialize a byte array.
     *
     * @param bytes Number of bytes that is contained in the array.
     * @return Returns a byte array that have been deserialized.
     */
    public byte[] readBytes(int bytes) {
        printDebugStart("readBytes");
        byte[] b = super.readByteArray();
        printDebugEnd(Arrays.toString(b));
        return b;
    }

    /**
     * Rotmg deserializer of a compressed long.
     *
     * @return Returns a long that have been deserialized.
     */
    public long readCompressedInt() {
        printDebugStart("readCompressedInt");
        long i = super.readCompressedInt();
        printDebugEnd(i+"");
        return i;
    }
}
