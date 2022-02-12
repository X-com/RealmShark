package packets.buffer;

import packets.Packet;
import packets.PacketType;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Custom buffer class to deserialize the rotmg packets.
 */
public class PBufferDebugOne extends PBuffer {
    ByteBuffer bufferD;

    public PBufferDebugOne(ByteBuffer data) {
        super(data);
        data.position(5);
        data.getInt();
        data.get();
        bufferD = data;
    }

    /**
     * Returns the buffer size.
     *
     * @return size of the buffer.
     */
    public int size() {
        return bufferD.capacity();
    }

    /**
     * Internal index of the buffer.
     *
     * @return Returns the internal index the buffer is at.
     */
    public int getIndex() {
        return bufferD.position();
    }

    /**
     * Deserialize a boolean.
     *
     * @return Returns a boolean that have been deserialized.
     */
    public boolean readBoolean() {
        try {
            boolean b = bufferD.get() != 0;
            return b;
        } catch (Exception e) {
            System.out.println("readBoolean " + bufferD.position() + "/" + bufferD.capacity() + " " + (bufferD.capacity() - bufferD.position()));
        }
        return false;
    }

    /**
     * Deserialize a byte.
     *
     * @return Returns the byte that have been deserialized.
     */
    public byte readByte() {
        try {
            byte b = bufferD.get();
            return b;
        } catch (Exception e) {
            System.out.println("readByte " + bufferD.position() + "/" + bufferD.capacity() + " " + (bufferD.capacity() - bufferD.position()));
        }
        return 0;
    }

    /**
     * Deserialize an unsigned byte.
     *
     * @return Returns an integer containing an unsigned byte that have
     * been deserialized.
     */
    public int readUnsignedByte() {
        try {
            int b = Byte.toUnsignedInt(bufferD.get());
            return b;
        } catch (Exception e) {
            System.out.println("readUnsignedByte " + bufferD.position() + "/" + bufferD.capacity() + " " + (bufferD.capacity() - bufferD.position()));
        }
        return 0;
    }

    /**
     * Deserialize a short.
     *
     * @return Returns the short that have been deserialized.
     */
    public short readShort() {
        try {
            short b = bufferD.getShort();
            return b;
        } catch (Exception e) {
            System.out.println("readShort " + bufferD.position() + "/" + bufferD.capacity() + " " + (bufferD.capacity() - bufferD.position()));
        }
        return 0;
    }

    /**
     * Deserialize an unsigned short.
     *
     * @return Returns an integer containing an unsigned short that have
     * been deserialized.
     */
    public int readUnsignedShort() {
        try {
            int b = Short.toUnsignedInt(bufferD.getShort());
            return b;
        } catch (Exception e) {
            System.out.println("readUnsignedShort " + bufferD.position() + "/" + bufferD.capacity() + " " + (bufferD.capacity() - bufferD.position()));
        }
        return 0;
    }

    /**
     * Deserialize an integer.
     *
     * @return Returns the integer that have been deserialized.
     */
    public int readInt() {
        try {
            int b = bufferD.getInt();
            return b;
        } catch (Exception e) {
            System.out.println("readInt " + bufferD.position() + "/" + bufferD.capacity() + " " + (bufferD.capacity() - bufferD.position()));
        }
        return 0;
    }

    /**
     * Deserialize an unsigned integer.
     *
     * @return Returns an integer containing an unsigned integer that have
     * been deserialized.
     */
    public long readUnsignedInt() {
        try {
            long b = Integer.toUnsignedLong(bufferD.getInt());
            return b;
        } catch (Exception e) {
            System.out.println("readUnsignedInt " + bufferD.position() + "/" + bufferD.capacity() + " " + (bufferD.capacity() - bufferD.position()));
        }
        return 0;
    }

    /**
     * Deserialize a float.
     *
     * @return Returns the float that have been deserialized.
     */
    public float readFloat() {
        try {
            float b = bufferD.getFloat();
            return b;
        } catch (Exception e) {
            System.out.println("readFloat " + bufferD.position() + "/" + bufferD.capacity() + " " + (bufferD.capacity() - bufferD.position()));
        }
        return 0;
    }

    /**
     * Deserialize a string.
     *
     * @return Returns the string that have been deserialized.
     */
    public String readString() {
        byte[] out = new byte[readShort()];
        try {
            bufferD.get(out);
        } catch (Exception e) {
            System.out.println("String len: " + out.length + " " + bufferD.position() + "/" + bufferD.capacity() + " " + (bufferD.capacity() - bufferD.position()));
            System.out.println(new String(bufferD.array(), StandardCharsets.UTF_8));
            System.out.println(Arrays.toString(bufferD.array()));
        }
        return new String(out);
    }

    /**
     * Deserialize a string using UTF32 (more characters that is
     * never found in-game) not used as far as I'm aware.
     *
     * @return Returns the string that have been deserialized.
     */
    public String readStringUTF32() {
        byte[] out = new byte[readShort()];
        try {
            bufferD.get(out);
        } catch (Exception e) {
            System.out.println("String len: " + out.length + " " + bufferD.position() + "/" + bufferD.capacity() + " " + (bufferD.capacity() - bufferD.position()));
            System.out.println(new String(bufferD.array(), StandardCharsets.UTF_8));
            System.out.println(Arrays.toString(bufferD.array()));
        }
        return new String(out);
    }

    /**
     * Deserialize a byte array
     */
    public byte[] readByteArray() {
        byte[] out = new byte[readShort()];
        try {
            bufferD.get(out);
        } catch (Exception e) {
            System.out.println("String len: " + out.length + " " + bufferD.position() + "/" + bufferD.capacity() + " " + (bufferD.capacity() - bufferD.position()));
            System.out.println(new String(bufferD.array(), StandardCharsets.UTF_8));
            System.out.println(Arrays.toString(bufferD.array()));
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
        byte[] out = new byte[readShort()];
        try {
            bufferD.get(out);
        } catch (Exception e) {
            System.out.println("String len: " + out.length + " " + bufferD.position() + "/" + bufferD.capacity() + " " + (bufferD.capacity() - bufferD.position()));
            System.out.println(new String(bufferD.array(), StandardCharsets.UTF_8));
            System.out.println(Arrays.toString(bufferD.array()));
        }
        return out;
    }

    /**
     * Rotmg deserializer of a compressed long.
     *
     * @return Returns a long that have been deserialized.
     */
    public int readCompressedInt() {
        int uByte = readUnsignedByte();
        boolean isNegative = (uByte & 64) != 0;
        int shift = 6;
        int value = uByte & 63;

        while ((uByte & 128) == 128) {
            uByte = readUnsignedByte();
            value = (value | (uByte & 127)) << shift;
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
        return Arrays.toString(bufferD.array());
    }

    /**
     * Returns the remaining bytes in the buffer from the current index.
     *
     * @return Returns the remaining bytes.
     */
    public byte[] giveRemainingArray() {
        return Arrays.copyOfRange(bufferD.array(), getIndex(), size());
    }

    /**
     * Error check if buffer is not finished.
     *
     * @param packetType
     * @param type
     */
    public void errorCheck(PacketType packetType, Packet type) {
        if (bufferD.capacity() != bufferD.position()) {
            System.out.println("Buffer not finished " + packetType + " : " + bufferD.position() + " " + bufferD.capacity());

            if (packetType != PacketType.UPDATE) return;

            System.out.println(PacketType.UPDATE);
            System.out.println(Arrays.toString(bufferD.array()));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bufferD.array().length; i++) {
//                String s = data.array()[i] == 10 ? "\\n" : (char) data.array()[i] + "";
                sb.append(Byte.toUnsignedInt(bufferD.array()[i]) + ", ");
//                System.out.printf("%3d %3d %3s\n", i, data.array()[i], s);
                try {
                    FileOutputStream outputStream = new FileOutputStream("bitData");
                    outputStream.write(bufferD.array());
                    outputStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println(sb);
        }
    }
}
