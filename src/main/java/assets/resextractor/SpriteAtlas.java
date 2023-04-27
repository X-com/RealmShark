package assets.resextractor;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

/**
 * Class extracted from UnityPy https://github.com/K0lb3/UnityPy
 */
public class SpriteAtlas {
    DataReader reader;
    String name;
    int packed_sprites_size;
    PPtr[] m_PackedSprites;
    String[] m_PackedSpriteNamesToIndex;
    int m_render_data_map_size;
    RenderDataMap[] m_RenderDataMap;

    public SpriteAtlas(ObjectReader o) {
        this.reader = o.reader;
        reader.setPosition((int) o.byte_start);

        name = reader.readAlignedString();

        packed_sprites_size = reader.readInt();
        m_PackedSprites = new PPtr[packed_sprites_size];
        for (int i = 0; i < packed_sprites_size; i++) {
            m_PackedSprites[i] = new PPtr();
        }

        m_PackedSpriteNamesToIndex = reader.readStringArray();
        m_render_data_map_size = reader.readInt();
        m_RenderDataMap = new RenderDataMap[m_render_data_map_size];
        for (int i = 0; i < m_render_data_map_size; i++) {
            m_RenderDataMap[i] = new RenderDataMap();
        }
    }

    public class RenderDataMap {
        byte[] first;
        long second;
        PPtr texture;
        PPtr alphaTexture;
        Rectangle2D textureRect;
        Vec2f textureRectOffset;
        Vec2f atlasRectOffset;
        Vec4f uvTransform;
        float downscaleMultiplier;
        long settingsRaw;
        long packed;
        long packingMode;
        long packingRotation;
        long meshType;
        int secondaryTexturesSize;
        SecondarySpriteTexture[] secondaryTextures;

        public RenderDataMap() {
            first = reader.readByte(16);
            second = reader.readLong();
            spriteAtlasData();
        }

        private void spriteAtlasData() {
            texture = new PPtr();
            alphaTexture = new PPtr();
            textureRect = new Rectangle();
            textureRect.setRect(reader.readFloat(), reader.readFloat(), reader.readFloat(), reader.readFloat());

            textureRectOffset = new Vec2f(reader.readFloat(), reader.readFloat());

//            if version >= (2017, 2):  # 2017.2 and up {
            atlasRectOffset = new Vec2f(reader.readFloat(), reader.readFloat());
//            }
            uvTransform = new Vec4f(reader.readFloat(), reader.readFloat(), reader.readFloat(), reader.readFloat());
            downscaleMultiplier = reader.readFloat();
            spriteSettings();

//            if version >= (2020, 2): {
            secondaryTexturesSize = reader.readInt();
            secondaryTextures = new SecondarySpriteTexture[secondaryTexturesSize];
            for (int i = 0; i < secondaryTexturesSize; i++) {
                secondaryTextures[i] = new SecondarySpriteTexture();
            }

            reader.alignStream();
//            }
        }

        private void spriteSettings() {
            settingsRaw = reader.readUnsignedInt();
            packed = settingsRaw & 1;
//            packingMode = SpritePackingMode((self.settingsRaw >> 1) & 1)  #1
//            packingRotation = SpritePackingRotation((self.settingsRaw >> 2) & 0xF)  #4
//            meshType = SpriteMeshType((self.settingsRaw >> 6) & 1)  #1
        }

        @Override
        public String toString() {
            return "RenderDataMap{" +
                    "\n      first=" + Arrays.toString(first) +
                    "\n      second=" + second +
                    "\n      texture=" + texture +
                    "\n      alphaTexture=" + alphaTexture +
                    "\n      textureRect=" + textureRect +
                    "\n      textureRectOffset=" + textureRectOffset +
                    "\n      atlasRectOffset=" + atlasRectOffset +
                    "\n      uvTransform=" + uvTransform +
                    "\n      downscaleMultiplier=" + downscaleMultiplier +
                    "\n      settingsRaw=" + settingsRaw +
                    "\n      packed=" + packed +
                    "\n      packingMode=" + packingMode +
                    "\n      packingRotation=" + packingRotation +
                    "\n      meshType=" + meshType +
                    "\n      secondaryTexturesSize=" + secondaryTexturesSize +
                    "\n      secondaryTextures=" + Arrays.toString(secondaryTextures);
        }

        //        SpritePackingMode(IntEnum):
//        kSPMTight = 0
//        kSPMRectangle = 1

//        class SpritePackingRotation(IntEnum):
//        kSPRNone = 0
//        kSPRFlipHorizontal = 1
//        kSPRFlipVertical = 2
//        kSPRRotate180 = 3
//        kSPRRotate90 = 4

//        class SpriteMeshType(IntEnum):
//        kSpriteMeshTypeFullRect = 0
//        kSpriteMeshTypeTight = 1
    }

    public class SecondarySpriteTexture {
        PPtr texture;
        String name;

        public SecondarySpriteTexture() {
            texture = new PPtr();
            name = reader.readStringToNull();
        }

        @Override
        public String toString() {
            return "SecondarySpriteTexture{" +
                    "\n      texture=" + texture +
                    "\n      name=" + name;
        }
    }

    public class PPtr {
        int file_id;
        long path_id;

        public PPtr() {
            file_id = reader.readInt();
//            if(_version < 14) {
//                p.path_id = reader.readInt();
//            } else {
            path_id = reader.readLong();
//            }
        }

        @Override
        public String toString() {
            return "PPtr{" +
                    "\n      file_id=" + file_id +
                    "\n      path_id=" + path_id;
        }
    }

    @Override
    public String toString() {
        return "SpriteAtlas{" +
                "\n   name=" + name +
                "\n   packed_sprites_size=" + packed_sprites_size +
                "\n   m_PackedSprites=" + Arrays.toString(m_PackedSprites) +
                "\n   m_PackedSpriteNamesToIndex=" + Arrays.toString(m_PackedSpriteNamesToIndex) +
                "\n   m_render_data_map_size=" + m_render_data_map_size +
                "\n   m_RenderDataMap=" + Arrays.toString(m_RenderDataMap);
    }
}
