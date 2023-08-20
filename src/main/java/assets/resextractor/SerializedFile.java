package assets.resextractor;

import assets.AssetExtractor;

import java.io.*;

/**
 * Class extracted from UnityPy https://github.com/K0lb3/UnityPy
 */
public class SerializedFile {
    public long version;
    public long data_offset;

    public String unity_version;
    public Platform m_target_platform;
    public boolean enable_type_tree = false;
    public int big_id_enabled;
    public int script_count;
    public ScriptTypes[] script_types;
    public int externals_count;
    public Externals[] externals;
    public int ref_type_count;
    public SerializedType[] ref_types;
    public String userInformation;
    public SerializedType[] types;
    public long[] type_dependencies;
    public ObjectReader[] objects;

    public SerializedFile(File f, FileHeader header) throws IOException {
        this.version = header.version;
        this.data_offset = header.data_offset;

        DataReader reader = DataReader.getReader(f, header.bigEndian);

        for (int i = 0; i < 12; i++) {
            reader.readInt();
        }

        if (version >= 7) {
            unity_version = reader.readStringToNull();
        }

        if (version >= 8) {
            m_target_platform = Platform.byOrdinal(reader.readInt());
        }

        if (version >= 13) {
            enable_type_tree = reader.readBoolean();
        }

//       # ReadTypes
        int type_count = reader.readInt();
        types = SerializedType.getTypes(this, type_count, reader);

        big_id_enabled = 0;
        if (7 <= version && version < 14) {
            big_id_enabled = reader.readInt();
        }

//       #ReadObjects
        int object_count = reader.readInt();
        {
            objects = new ObjectReader[object_count];
            for (int i = 0; i < object_count; i++) {
                if (i % 1000 == 0) {
                    AssetExtractor.setDisplay("Reading Assets " + (i / 1000) + "K");
                }
                objects[i] = new ObjectReader(this, reader);
            }
        }

//      # Read Scripts
        if (version >= 11) {
            script_count = reader.readInt();
            script_types = new ScriptTypes[script_count];
            for (int i = 0; i < script_count; i++) {
                ScriptTypes st = new ScriptTypes();
                st.local_serialized_file_index = reader.readInt();
                if (version < 14) {
                    st.local_identifier_in_file = reader.readInt();
                } else {
                    reader.alignStream();
                    st.local_identifier_in_file = reader.readLong();
                }
                script_types[i] = st;
            }
        }

//      # Read Externals
        externals_count = reader.readInt();
        externals = new Externals[externals_count];
        for (int i = 0; i < externals_count; i++) {
            Externals ex = new Externals();
            if (version >= 6) {
                ex.temp_empty = reader.readStringToNull();
            }
            if (version >= 5) {
                ex.guid = reader.readByte(16);
                ex.type = reader.readInt();
            }
            ex.path = reader.readStringToNull();
            externals[i] = ex;
        }

        if (version >= 20) {
            ref_type_count = reader.readInt();
            ref_types = SerializedType.getTypes(this, ref_type_count, reader);
        }

        if (version >= 5) {
            userInformation = reader.readStringToNull();
        }

//        # read the asset_bundles to get the containers
        for (ObjectReader obj : objects) {
            if (obj.type == ClassIDType.AssetBundle) {
                throw new RuntimeException("Reading unimplemented bundles");
//                data = obj.read()
//                for container, asset_info in data.m_Container.items():
//                    asset = asset_info.asset
//                    self.container_[container] = asset
//                    if hasattr(asset, "path_id"):
//                        self._container[asset.path_id] = container
            }
        }
//        # if environment is not None:
//        #    environment.container = {**environment.container, **self.container}
    }

    public class ScriptTypes {
        int local_serialized_file_index;
        long local_identifier_in_file;
    }

    public class Externals {
        String temp_empty;
        byte[] guid;
        int type;
        String path;
    }
}
