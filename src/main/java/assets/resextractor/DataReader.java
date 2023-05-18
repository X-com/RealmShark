package assets.resextractor;

import java.io.*;

/**
 * Class extracted from UnityPy https://github.com/K0lb3/UnityPy
 */
public class DataReader {
    private final FileInputStream fileIo;
    private final DataInputStream buffer;
    private final long length;
    private final byte[] readBuffer = new byte[8];
    private final boolean isBigEndian;

    public DataReader(FileInputStream fileInputStream, DataInputStream buffer, long length, boolean isBig) {
        this.fileIo = fileInputStream;
        this.buffer = buffer;
        this.length = length;
        this.isBigEndian = isBig;
    }

    public static DataReader getReader(File f, boolean endian) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(f);
        DataInputStream dataInputStream = new DataInputStream(fileInputStream);

        return new DataReader(fileInputStream, dataInputStream, f.length(), endian);
    }

    // changes with endian

    public long readLong() throws IOException {
        if (isBigEndian) return buffer.readLong();

        buffer.readFully(readBuffer, 0, 8);
        return (((long) readBuffer[7] << 56) +
                ((long) (readBuffer[6] & 255) << 48) +
                ((long) (readBuffer[5] & 255) << 40) +
                ((long) (readBuffer[4] & 255) << 32) +
                ((long) (readBuffer[3] & 255) << 24) +
                ((readBuffer[2] & 255) << 16) +
                ((readBuffer[1] & 255) << 8) +
                ((readBuffer[0] & 255)));
    }

    public int readInt() throws IOException {
        if (isBigEndian) return buffer.readInt();

        int ch4 = buffer.read();
        int ch3 = buffer.read();
        int ch2 = buffer.read();
        int ch1 = buffer.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4));
    }

    public short readShort() throws IOException {
        if (isBigEndian) return buffer.readShort();

        int ch2 = buffer.read();
        int ch1 = buffer.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (short) ((ch1 << 8) + (ch2));
    }

    // endian end

    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    public long readUnsignedInt() throws IOException {
        return Integer.toUnsignedLong(readInt());
    }

    public int readUnsignedShort() throws IOException {
        return Short.toUnsignedInt(readShort());
    }

    public void read(byte[] reserved) throws IOException {
        buffer.read(reserved);
    }

    public boolean readBoolean() throws IOException {
        return buffer.readBoolean();
    }

    public void alignStream() throws IOException {
//        (alignment - self.Position % alignment) % alignment
        long skip = (4 - getPosition() % 4) % 4;
//        buffer.position(buffer.position() + skip);
        for (int i = 0; i < skip; i++) {
            buffer.readByte();
        }
    }

    public byte readByte() throws IOException {
        return buffer.readByte();
    }

    public byte[] readByte(int count) throws IOException {
        byte[] bytes = new byte[count];
        buffer.read(bytes);
        return bytes;
    }

    public long getPosition() throws IOException {
        return fileIo.getChannel().position();
    }

    public void setPosition(int pos) throws IOException {
        fileIo.getChannel().position(pos);
    }

    public String readStringToNull() throws IOException {
        int c = 0;
        StringBuilder s = new StringBuilder();
        do {
            if (c != 0) s.append((char) c);
            c = buffer.read();
        } while (c != 0b0);
        return s.toString();
    }

    public String readAlignedString() throws IOException {
        int lenght = readInt();
        if (0 < lenght && lenght < getRemainingBytes()) {
            byte[] str = new byte[lenght];
            buffer.read(str);
            alignStream();
            return new String(str);
        }
        return "";
    }

    private long getRemainingBytes() throws IOException {
        return length - getPosition();
    }

    public String[] readStringArray() throws IOException {
        int num = readInt();
        String[] strs = new String[num];
        for (int i = 0; i < num; i++) {
            strs[i] = readAlignedString();
        }
        return strs;
    }

    public byte[] readByteArrayInt() throws IOException {
        byte[] out = new byte[readInt()];
        buffer.read(out);
        return out;
    }
}
