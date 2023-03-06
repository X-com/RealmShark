package experimental.asset;

import java.io.DataInputStream;
import java.io.IOException;

public class SerializedType {
    public int class_id;
    public boolean is_stripped_type;
    public short script_type_index;
    //        nodes: list = []  # TypeTreeNode
    public byte[] script_id = new byte[16];
    public byte[] old_type_hash = new byte[16];

    public static SerializedType[] getTypes(SerializedFile serializedFile, int type_count, DataReader reader) throws IOException {
        SerializedType[] types = new SerializedType[type_count];
        for (int i = 0; i < type_count; i++) {
            SerializedType st = new SerializedType();

            st.class_id = reader.readInt();
            if (serializedFile.version >= 16) {
                st.is_stripped_type = reader.readBoolean();
            }
            if (serializedFile.version >= 17) {
                st.script_type_index = reader.readShort();
            }
            if (serializedFile.version >= 13) {
                if ((serializedFile.version < 16 && st.class_id < 0) || (serializedFile.version >= 16 && st.class_id == 114)) {
                    reader.read(st.script_id, 0, 16);
                }
                reader.read(st.old_type_hash, 0, 16);
            }

            if (serializedFile.enable_type_tree) {
                // TODO fix this odd magic bs
//                type_tree = []
                if (serializedFile.version >= 12 || serializedFile.version == 10) {
                    read_type_tree_blob();
//                    self.string_data = serialized_file.read_type_tree_blob(type_tree)
                } else {
                    read_type_tree();
//                    serialized_file.read_type_tree(type_tree)
                }

                if (serializedFile.version >= 21) {
                    int type_dep_size = reader.readInt();
                    serializedFile.type_dependencies = new long[type_dep_size];
                    for (int j = 0; j < type_dep_size; j++) {
                        serializedFile.type_dependencies[j] = Integer.toUnsignedLong(reader.readInt());
                    }
                }
//                st.nodes = type_tree
                throw new IOException("enable_type_tree");
            }
            types[i] = st;
        }

        return types;
    }

    private static void read_type_tree_blob() {
//                    def read_type_tree_blob(self, type_tree):
//                        reader = self.reader
//                        number_of_nodes = self.reader.read_int()
//                        string_buffer_size = self.reader.read_int()
//
//                        for _ in range(number_of_nodes):
//                            node = TypeTreeNode()
//                            type_tree.append(node)
//                            node.version = reader.read_u_short()
//                            node.level = reader.read_byte()
//                            node.is_array = reader.read_boolean()
//                            node.type_str_offset = reader.read_u_int()
//                            node.name_str_offset = reader.read_u_int()
//                            node.byte_size = reader.read_int()
//                            node.index = reader.read_int()
//                            node.meta_flag = reader.read_int()
//
//                            if self.header.version >= 19:
//                                node.ref_type_hash = reader.read_u_long()
//
//                        string_buffer_reader = EndianBinaryReader(
//                                reader.read(string_buffer_size), reader.endian
//                        )
//                        for node in type_tree:
//                            node.type = read_string(string_buffer_reader, node.type_str_offset)
//                            node.name = read_string(string_buffer_reader, node.name_str_offset)
//
//                        return string_buffer_reader.bytes
    }

    private static void read_type_tree() {

//                    def read_type_tree(self, type_tree):
//                        level_stack = [[0, 1]]
//                        while level_stack:
//                            level, count = level_stack[-1]
//                            if count == 1:
//                                level_stack.pop()
//                        else:
//                            level_stack[-1][1] -= 1
//
//                        type_tree_node = TypeTreeNode()
//                        type_tree.append(type_tree_node)
//                        type_tree_node.level = level
//                        type_tree_node.type = self.reader.read_string_to_null()
//                        type_tree_node.name = self.reader.read_string_to_null()
//                        type_tree_node.byte_size = self.reader.read_int()
//                        if self.header.version == 2:
//                            type_tree_node.variable_count = self.reader.read_int()
//
//                        if self.header.version != 3:
//                            type_tree_node.index = self.reader.read_int()
//
//                        type_tree_node.is_array = self.reader.read_int()
//                        type_tree_node.version = self.reader.read_int()
//                        if self.header.version != 3:
//                            type_tree_node.meta_flag = self.reader.read_int()
//
//                        children_count = self.reader.read_int()
//                        if children_count:
//                            level_stack.append([level + 1, children_count])
//
//                        return type_tree
    }
}