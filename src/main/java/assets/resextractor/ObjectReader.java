package assets.resextractor;

/**
 * Class extracted from UnityPy https://github.com/K0lb3/UnityPy
 */
public class ObjectReader {
    public long version;
    public long data_offset;
    public DataReader reader;
    public long path_id;
    public long byte_start_offset;
    public int bst_tupple;
    public long byte_start;
    public long byte_header_offset;
    public long byte_base_offset;
    public long byte_size_offset;
    public long byte_size;
    public int type_id;
    public SerializedType serialized_type;
    public int class_id;
    public ClassIDType type;
    public int is_destroyed;
    public byte stripped;
    public short script_type_index;

    public ObjectReader(SerializedFile serializedFile, DataReader reader) {
        this.version = serializedFile.version;
        this.reader = reader;
        this.data_offset = serializedFile.data_offset;

        if (serializedFile.big_id_enabled != 0) {
            path_id = reader.readLong();
        } else if (version < 14) {
            path_id = reader.readInt();
        } else {
            reader.alignStream();
            path_id = reader.readLong();
        }

        if (version >= 22) {
            byte_start_offset = reader.getPosition();
            bst_tupple = 8;
            byte_start = reader.readLong();
        } else {
            byte_start_offset = reader.getPosition();
            bst_tupple = 4;
            byte_start = reader.readInt();
        }

        byte_start += data_offset;
        byte_header_offset = data_offset;
        byte_base_offset = 0;

        byte_size_offset = reader.getPosition();
        byte_size = Integer.toUnsignedLong(reader.readInt());

        type_id = reader.readInt();
        if (version < 16) {
            short class_id = reader.readShort();
//                if types:
//                    self.serialized_type = (
//                        x for x in types if x.class_id == self.type_id).__next__()
//                    else:
//                        self.serialized_type = SerializedFile.SerializedType(
//                                reader, self.assets_file)
        } else {
            SerializedType typ = serializedFile.types[type_id];
            serialized_type = typ;
            class_id = typ.class_id;
        }

        type = ClassIDType.byOrdinal(class_id);


        if (version < 11) {
            is_destroyed = reader.readUnsignedShort();
        } else if (version < 17) {
            script_type_index = reader.readShort();
//                    if (serialized_type) {
//                        serialized_type.script_type_index = script_type_index;
//                    }
        }

        if (version == 15 || version == 16) {
            stripped = reader.readByte();
        }
    }

    @Override
    public String toString() {
        return "ObjectReader{" +
                "\n   version=" + version +
                "\n   data_offset=" + data_offset +
                "\n   path_id=" + path_id +
                "\n   byte_start_offset=" + byte_start_offset +
                "\n   bst_tupple=" + bst_tupple +
                "\n   byte_start=" + byte_start +
                "\n   byte_header_offset=" + byte_header_offset +
                "\n   byte_base_offset=" + byte_base_offset +
                "\n   byte_size_offset=" + byte_size_offset +
                "\n   byte_size=" + byte_size +
                "\n   type_id=" + type_id +
                "\n   serialized_type=" + serialized_type +
                "\n   class_id=" + class_id +
                "\n   type=" + type +
                "\n   is_destroyed=" + is_destroyed +
                "\n   stripped=" + stripped +
                "\n   script_type_index=" + script_type_index;
    }
}
