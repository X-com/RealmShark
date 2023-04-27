package assets.resextractor;

/**
 * Class extracted from UnityPy https://github.com/K0lb3/UnityPy
 */
public class TextAsset {
    public static final String[] NON_XML_FILES = {
            "manifest_xml", "COPYING", "Errors", "ExplainUnzip", "cloth_bazaar", "Cursors", "Dialogs", "Keyboard",
            "LICENSE", "LineBreaking Following Characters", "LineBreaking Leading Characters", "manifest_json", "spritesheet",
            "iso_4217", "data", "manifest", "BillingMode"
    };

    DataReader reader;
    String name;
    byte[] m_Script;

    public TextAsset(ObjectReader o) {
        this.reader = o.reader;
        reader.setPosition((int) o.byte_start);

        name = reader.readAlignedString();

        int bytes = reader.readInt();
        m_Script = reader.readByte(bytes);
    }
}
