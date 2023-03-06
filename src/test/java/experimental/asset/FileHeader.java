package experimental.asset;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

public class FileHeader {

    public static final int ResourceFile = 1;
    public static final int AssetsFile = 2;

    public long metadata_size;
    public long file_size;
    public long version;
    public long data_offset;

    public long size;
    public boolean bigEndian;
    public byte[] reserved;
    public long unknown;

    public int type;

    public FileHeader(File f) throws IOException {
        FileInputStream fin = new FileInputStream(f);
        DataInputStream reader = new DataInputStream(fin);

        size = f.length();
        metadata_size = Integer.toUnsignedLong(reader.readInt());
        file_size = Integer.toUnsignedLong(reader.readInt());
        version = Integer.toUnsignedLong(reader.readInt());
        data_offset = Integer.toUnsignedLong(reader.readInt());

        if (version >= 22) {
            bigEndian = reader.readBoolean();
            reserved = new byte[3];
            reader.read(reserved, 0, 3);
            metadata_size = Integer.toUnsignedLong(reader.readInt());
            file_size = reader.readLong();
            data_offset = reader.readLong();
            unknown = reader.readLong();
        }

        if (version > 100 || file_size < 0 || data_offset < 0 || file_size > size || metadata_size > size || version > size || data_offset > size || file_size < metadata_size || file_size < data_offset) {
            type = ResourceFile;
        } else {
            type = AssetsFile;
        }
    }

    @Override
    public String toString() {
        return "FileHeader{" +
                "\n   metadata_size=" + metadata_size +
                "\n   file_size=" + file_size +
                "\n   version=" + version +
                "\n   data_offset=" + data_offset +
                "\n   size=" + size +
                "\n   bigEndian=" + bigEndian +
                "\n   reserved=" + Arrays.toString(reserved) +
                "\n   unknown=" + unknown +
                "\n   type=" + type;
    }
}
