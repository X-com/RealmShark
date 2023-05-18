package assets.resextractor;

import java.io.IOException;
import java.util.Arrays;

/**
 * Class extracted from UnityPy https://github.com/K0lb3/UnityPy
 */
public class Texture2D {

    public static final String[] SPRITESHEET_NAMES = {"characters", "characters_masks", "groundTiles", "mapObjects"};

    DataReader reader;

    String name;
    String path;

    boolean m_DownscaleFallback;
    boolean m_IsAlphaChannelOptional;
    boolean m_IsReadable;
    boolean m_IsPreProcessed;
    boolean m_IgnoreMasterTextureLimit;
    boolean m_StreamingMipmaps;

    int m_ForcedFallbackFormat;
    int m_Width;
    int m_Height;
    int m_CompleteImageSize;
    int m_MipsStripped;
    int m_MipCount;
    int m_StreamingMipmapsPriority;
    int m_ImageCount;
    int m_TextureDimension;
    int m_FilterMode;
    int m_Aniso;
    int m_WrapMode;
    int m_WrapV;
    int m_WrapW;
    int m_LightmapFormat;
    int m_ColorSpace;
    int image_data_size;

    float m_MipBias;

    long offset;
    long size;

    byte[] m_PlatformBlob;
    byte[] image_data;

    TextureFormat m_TextureFormat;

    public Texture2D(ObjectReader o) throws IOException {
        this.reader = o.reader;
        reader.setPosition((int) o.byte_start);

        name = reader.readAlignedString();

//        if self.version >= (2017, 3){
        m_ForcedFallbackFormat = reader.readInt();
        m_DownscaleFallback = reader.readBoolean();
//        if self.version >= (2020,2): # 2020.2 and up {
        m_IsAlphaChannelOptional = reader.readBoolean();
//        }
        reader.alignStream();
//        }

        m_Width = reader.readInt();
        m_Height = reader.readInt();
        m_CompleteImageSize = reader.readInt();
//        if version >= (2020,):  # 2020.1 and up {
        m_MipsStripped = reader.readInt();
//        }
        m_TextureFormat = TextureFormat.byOrdinal(reader.readInt());
//        if version[:2] < (5, 2):  # 5.2 down {
//            self.m_MipMap = reader.read_boolean()
//        }else{
        m_MipCount = reader.readInt();
//        }

//        if version >= (2, 6):  # 2.6 and up {
        m_IsReadable = reader.readBoolean();
//        }
//        if version >= (2020,):  # 2020.1 and up {
        m_IsPreProcessed = reader.readBoolean();
//        }
//        if version >= (2019, 3):  # 2019.3 and up {
        m_IgnoreMasterTextureLimit = reader.readBoolean();
//        }
//        if (3,) <= version[:2] <= (5, 4):  # 3.0 - 5.4 {
//            self.m_ReadAllowed = reader.read_boolean()
//        }
//        if version >= (2018, 2):  # 2018.2 and up {
        m_StreamingMipmaps = reader.readBoolean();
//        }
        reader.alignStream();
//        if version >= (2018, 2):  # 2018.2 and up {
        m_StreamingMipmapsPriority = reader.readInt();
//        }
        m_ImageCount = reader.readInt();
        m_TextureDimension = reader.readInt();

        gLTextureSettings();

//        if version >= (3,):  # 3.0 and up {
        m_LightmapFormat = reader.readInt();
//        }
//        if version >= (3, 5):  # 3.5 and up {
        m_ColorSpace = reader.readInt();
//        }
//        if version >= (2020, 2):  # 2020.2 and up {
        m_PlatformBlob = reader.readByteArrayInt();
//        }
        reader.alignStream();

        image_data_size = reader.readInt();

        if (image_data_size != 0) {
            image_data = reader.readByte(image_data_size);
        }

//        if version >= (5, 3):  # 5.3 and up {
        streamingInfo();
        if (image_data_size == 0 && path != null) {
//            _image_data = get_resource_data(
//                    self.m_StreamData.path,
//                    self.assets_file,
//                    self.m_StreamData.offset,
//                    self.m_StreamData.size,
//                    )
        }
//        } // # 5.3 and up end
    }

    private void gLTextureSettings() throws IOException {
        m_FilterMode = reader.readInt();
        m_Aniso = reader.readInt();
        m_MipBias = reader.readFloat();
        m_WrapMode = reader.readInt();
        m_WrapV = reader.readInt();
        m_WrapW = reader.readInt();
    }

    private void streamingInfo() throws IOException {
        offset = reader.readLong();
        size = reader.readUnsignedInt();
        path = reader.readAlignedString();
    }

    @Override
    public String toString() {
        return "Texture2D{" +
                "\n   m_DownscaleFallback=" + m_DownscaleFallback +
                "\n   m_IsAlphaChannelOptional=" + m_IsAlphaChannelOptional +
                "\n   m_IsReadable=" + m_IsReadable +
                "\n   m_IsPreProcessed=" + m_IsPreProcessed +
                "\n   m_IgnoreMasterTextureLimit=" + m_IgnoreMasterTextureLimit +
                "\n   m_StreamingMipmaps=" + m_StreamingMipmaps +
                "\n   m_ForcedFallbackFormat=" + m_ForcedFallbackFormat +
                "\n   m_Width=" + m_Width +
                "\n   m_Height=" + m_Height +
                "\n   m_CompleteImageSize=" + m_CompleteImageSize +
                "\n   m_MipsStripped=" + m_MipsStripped +
                "\n   m_MipCount=" + m_MipCount +
                "\n   m_StreamingMipmapsPriority=" + m_StreamingMipmapsPriority +
                "\n   m_ImageCount=" + m_ImageCount +
                "\n   m_TextureDimension=" + m_TextureDimension +
                "\n   m_FilterMode=" + m_FilterMode +
                "\n   m_Aniso=" + m_Aniso +
                "\n   m_WrapMode=" + m_WrapMode +
                "\n   m_WrapV=" + m_WrapV +
                "\n   m_WrapW=" + m_WrapW +
                "\n   m_LightmapFormat=" + m_LightmapFormat +
                "\n   m_ColorSpace=" + m_ColorSpace +
                "\n   image_data_size=" + image_data_size +
                "\n   m_MipBias=" + m_MipBias +
                "\n   offset=" + offset +
                "\n   size=" + size +
                "\n   m_PlatformBlob=" + Arrays.toString(m_PlatformBlob) +
                "\n   image_data=" + Arrays.toString(image_data) +
                "\n   name=" + name +
                "\n   path=" + path +
                "\n   m_TextureFormat=" + m_TextureFormat;
    }
}
