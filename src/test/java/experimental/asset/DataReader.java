package experimental.asset;

import packets.reader.BufferReader;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;

public class DataReader extends BufferReader {

    public DataReader(ByteBuffer buffer) {
        super(buffer);
    }

    public static DataReader getReader(File f, boolean endian) throws IOException {
        byte[] data = Files.readAllBytes(f.toPath());
        ByteBuffer buffer = ByteBuffer.wrap(data).order(endian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);

        return new DataReader(buffer);
    }

    public long readLong() {
        return buffer.getLong();
    }

    public void read(byte[] reserved, int offset, int size) {
        buffer.get(reserved);
    }

    public void alignStream() {
//        (alignment - self.Position % alignment) % alignment
        int skip = (4 - buffer.position() % 4) % 4;
//        buffer.position(buffer.position() + skip);
        for (int i = 0; i < skip; i++) {
            buffer.get();
        }
    }

    public byte[] readByte(int count) {
        byte[] bytes = new byte[count];
        buffer.get(bytes);
        return bytes;
    }

    public long getPosition() {
        return buffer.position();
    }

    public void setPosition(int pos) {
        buffer.position(pos);
    }

    public String readStringToNull() {
        int c = 0;
        StringBuilder s = new StringBuilder();
        do {
            if (c != 0) s.append((char) c);
            c = buffer.get();
        } while (c != 0b0);
        return s.toString();
    }

    public String readAlignedString() {
        int lenght = readInt();
        if (0 < lenght && lenght < getRemainingBytes()) {
            byte[] str = new byte[lenght];
            buffer.get(str);
            alignStream();
            return new String(str);
        }
        return "";
    }

    public String[] readStringArray() {
        int num = readInt();
        String[] strs = new String[num];
        for(int i = 0; i < num; i++) {
            strs[i] = readAlignedString();
        }
        return strs;
    }

    public byte[] readByteArrayInt() {
        byte[] out = new byte[readInt()];
        buffer.get(out);
        return out;
    }
}
