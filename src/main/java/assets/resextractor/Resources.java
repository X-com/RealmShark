package assets.resextractor;

import java.io.*;
import java.util.ArrayList;

/**
 * Class extracted from UnityPy https://github.com/K0lb3/UnityPy
 */
public class Resources {

    ArrayList<TextAsset> assetTextAsset = new ArrayList<>();
    ArrayList<SpriteAtlas> assetSpriteAtlas = new ArrayList<>();
    ArrayList<Texture2D> assetTexture2D = new ArrayList<>();
    TextAsset spritesheet;
    TextAsset manifest_json;
    TextAsset manifest_xml;

    public Resources(File file) throws IOException {
            FileHeader header = new FileHeader(file);
            if (header.type == FileHeader.ResourceFile) {
//                endianBinaryReader
            } else if (header.type == FileHeader.AssetsFile) {
                extractAssets(file, header);
            }
    }

    public void extractAssets(File f, FileHeader header) throws IOException {
        SerializedFile sf = new SerializedFile(f, header);
        parseAllResources(sf);
    }

    public void parseAllResources(SerializedFile sf) {
        for (ObjectReader o : sf.objects) {
            switch (o.type) {
                case AudioClip:
                    break;
                case BuildSettings:
                    break;
                case GameObject:
                    break;
                case TextAsset:
                    parseTextAsset(o);
                    break;
                case SpriteAtlas:
                    parseSpriteAtlas(o);
                    break;
                case MonoScript:
                    break;
                case Texture2D:
                    parseTexture2D(o);
                    break;
                case MonoBehaviour:
                    break;
            }
        }
    }

    private void parseTextAsset(ObjectReader o) {
        TextAsset t = new TextAsset(o);
        if (t.name.equals("spritesheet")) {
            spritesheet = t;
        } else if (t.name.equals("manifest")) {
            manifest_json = t;
        } else if (t.name.equals("assets_manifest")) {
            manifest_xml = t;
        }
        assetTextAsset.add(t);
    }

    private void parseSpriteAtlas(ObjectReader o) {
        SpriteAtlas s = new SpriteAtlas(o);
        assetSpriteAtlas.add(s);
    }

    private void parseTexture2D(ObjectReader o) {
        Texture2D t = new Texture2D(o);
        assetTexture2D.add(t);
    }
}