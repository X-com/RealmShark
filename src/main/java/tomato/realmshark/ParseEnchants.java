package tomato.realmshark;

import org.xml.sax.SAXException;
import packets.data.StatData;
import tomato.backend.data.Entity;
import util.StringXML;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Enchantment parser using the sixBitStringToBytes system from the player packets.
 * Converting the encoded string to an encoded byte array then extract the enchant ID.
 * The enchant ID is then used to get the enchant description from the XML assets.
 */
public class ParseEnchants {

    private static final String ENCHANT_XML_PATH = "assets/xml/enchantments.xml";
    private static final HashMap<Short, String> ENCHANTS = new HashMap<>();

    /**
     * Load Enchant XML data to get names from file.
     */
    static {
        try {
            FileInputStream file = new FileInputStream(ENCHANT_XML_PATH);
            String result = new BufferedReader(new InputStreamReader(file)).lines().collect(Collectors.joining("\n"));
            StringXML base = StringXML.getParsedXML(result);
            for (StringXML xml : base) {
                if (Objects.equals(xml.name, "Enchantment")) {
                    Enchantment enchantment = new Enchantment();

                    for (StringXML info : xml) {
                        if (Objects.equals(info.name, "id")) {
                            enchantment.name = info.value;
                        }
                        if (Objects.equals(info.name, "type")) {
                            enchantment.enchantId = Short.decode(info.value);
                        }
                    }
                    ENCHANTS.put(enchantment.enchantId, enchantment.name);
                }
            }
            ENCHANTS.put((short) -1, "[empty]");
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Test class. Ignore.
     */
    public static void main(String[] args) {
        Byte[] bMid = {-3, -1, -3, -1, -3, -1};
        Byte[] empty = {};
        String temp = "[0, 2, 4, 11, 2, -3, -1, -3, -1, -3, -1]\n";
//        String temp = "[0, 2, 4, 11, 2, -3, -1, -3, -1, -3, -1]\n" + "[0, 2, 4, -3, -1, -3, -1, -3, -1, -3, -1]\n" + "[]\n" + "[0, 2, 4, -1, -1, -3, -1, -3, -1, -3, -1]\n" + "[0, 2, 4, -2, -1, -3, -1, -3, -1, -3, -1]\n" + "[0, 2, 4, 23, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 23, 0]\n" + "[0, 2, 4, 0, 2, -3, -1, -3, -1, -3, -1]\n" + "[0, 2, 4, 26, 2, -3, -1, -3, -1, -3, -1]\n" + "[0, 2, 4, 29, 2, -3, -1, -3, -1, -3, -1]\n" + "[0, 2, 4, 15, 2, -3, -1, -3, -1, -3, -1]\n" + "[0, 2, 4, 30, 2, -3, -1, -3, -1, -3, -1]\n" + "[0, 2, 4, 8, 2, -3, -1, -3, -1, -3, -1]\n" + "[0, 2, 4, 47, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 47, 0]\n" + "[0, 2, 4, 7, 2, -3, -1, -3, -1, -3, -1]\n" + "[0, 2, 4, 20, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 20, 0]\n" + "[0, 2, 4, 3, 2, -3, -1, -3, -1, -3, -1]\n" + "[0, 2, 4, 27, 2, -3, -1, -3, -1, -3, -1]\n" + "[0, 2, 4, 31, 2, -3, -1, -3, -1, -3, -1]\n" + "[0, 2, 4, 21, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 21, 0]\n" + "[0, 2, 4, 2, 2, -3, -1, -3, -1, -3, -1]\n" + "[0, 2, 4, 24, 2, -3, -1, -3, -1, -3, -1]\n" + "[0, 2, 4, 21, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 118, 0, 21, 0]\n" + "[0, 2, 4, 22, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 22, 0]\n" + "[0, 2, 4, 17, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 17, 0]\n" + "[0, 2, 4, 37, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 37, 0]\n" + "[0, 2, 4, 23, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 40, 0, 23, 0]\n" + "[0, 2, 4, 25, 2, -3, -1, -3, -1, -3, -1]\n" + "[0, 2, 4, -2, -1, -3, -1, -3, -1, -3, -1, 3, 1, 0, 23, 0]\n" + "[0, 2, 4, -124, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, -124, 0]\n" + "[0, 2, 4, -110, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, -110, 0]\n" + "[0, 2, 4, 103, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 103, 0]\n" + "[0, 2, 4, 116, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 116, 0]\n" + "[0, 2, 4, 28, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 28, 0]\n" + "[0, 2, 4, 88, 0, -3, -1, -3, -1, -3, -1, 3, 3, 0, 88, 0, 29, 0, 32, 0]\n" + "[0, 2, 4, 23, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 23, 0, 22, 0]\n" + "[0, 2, 4, 18, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 37, 0, 18, 0]\n" + "[0, 2, 4, 29, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 29, 0]\n" + "[0, 2, 4, 19, 2, -3, -1, -3, -1, -3, -1]\n" + "[0, 2, 4, 21, 2, -3, -1, -3, -1, -3, -1]\n" + "[0, 2, 4, 5, 2, -3, -1, -3, -1, -3, -1]\n" + "[0, 2, 4, 9, 2, -3, -1, -3, -1, -3, -1]\n" + "[0, 2, 4, 12, 2, -3, -1, -3, -1, -3, -1]\n" + "[0, 2, 4, 28, 2, -3, -1, -3, -1, -3, -1]\n" + "[0, 2, 4, 18, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 18, 0]\n" + "[0, 2, 4, 71, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 71, 0]\n" + "[0, 2, 4, 21, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 24, 0, 21, 0]\n" + "[0, 2, 4, 43, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 43, 0]\n" + "[0, 2, 4, 17, 2, -3, -1, -3, -1, -3, -1]\n" + "[0, 2, 4, 63, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 63, 0]\n" + "[0, 2, 4, 85, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 85, 0]\n" + "[0, 2, 4, 31, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 31, 0]\n" + "[0, 2, 4, 24, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 24, 0]\n" + "[0, 2, 4, 31, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 85, 0, 31, 0]\n" + "[0, 2, 4, 24, 0, -3, -1, -3, -1, -3, -1, 3, 3, 0, 24, 0, 20, 0, 21, 0]\n" + "[0, 2, 4, 28, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 85, 0, 28, 0]\n" + "[0, 2, 4, 24, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, -124, 0, 24, 0]\n" + "[0, 2, 4, -3, -1, -3, -1, -3, -1, -3, -1, 3, 1, 0, 17, 0]\n" + "[0, 2, 4, 22, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 45, 0, 22, 0]\n" + "[0, 2, 4, -3, -1, -3, -1, -3, -1, -3, -1, 3, 1, 0, 23, 0]\n" + "[0, 2, 4, 114, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 114, 0]\n" + "[0, 2, 4, 6, 2, -3, -1, -3, -1, -3, -1]\n" + "[0, 2, 4, 4, 2, -3, -1, -3, -1, -3, -1]\n" + "[0, 2, 4, 22, 2, -3, -1, -3, -1, -3, -1]\n" + "[0, 2, 4, 21, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 21, 0, 24, 0]\n" + "[0, 2, 4, 21, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 21, 0, 20, 0]\n" + "[0, 2, 4, -128, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, -128, 0]\n" + "[0, 2, 4, 118, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 118, 0]\n" + "[0, 2, 4, 13, 2, -3, -1, -3, -1, -3, -1]\n" + "[0, 2, 4, 23, 2, -3, -1, -3, -1, -3, -1]\n" + "[0, 2, 4, 10, 2, -3, -1, -3, -1, -3, -1]\n" + "[0, 2, 4, 41, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 41, 0]\n" + "[0, 2, 4, 1, 2, -3, -1, -3, -1, -3, -1]\n" + "[0, 2, 4, 116, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 116, 0, 24, 0]\n" + "[0, 2, 4, 48, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 48, 0]\n" + "[0, 2, 4, 18, 2, -3, -1, -3, -1, -3, -1]\n" + "[0, 2, 4, 98, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 98, 0]\n" + "[0, 2, 4, -1, -1, -3, -1, -3, -1, -3, -1, 3, 1, 0, 118, 0]\n" + "[0, 2, 4, 25, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 25, 0]\n" + "[0, 2, 4, 84, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 84, 0]\n" + "[0, 2, 4, -1, -1, -3, -1, -3, -1, -3, -1, 3, 2, 0, 124, 0, 21, 0]\n" + "[0, 2, 4, 32, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 32, 0]\n" + "[0, 2, 4, 23, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 48, 0, 23, 0]\n" + "[0, 2, 4, -2, -1, -3, -1, -3, -1, -3, -1, 3, 1, 0, 28, 0]\n" + "[0, 2, 4, -112, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, -112, 0]\n" + "[0, 2, 4, 40, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 40, 0]\n" + "[0, 2, 4, 94, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 17, 0, 94, 0]\n" + "[0, 2, 4, 28, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 31, 0, 28, 0]\n" + "[0, 2, 4, 29, 0, -3, -1, -3, -1, -3, -1, 3, 3, 0, 88, 0, 29, 0, 32, 0]\n" + "[0, 2, 4, 20, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, -118, 0, 20, 0]\n" + "[0, 2, 4, 93, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 93, 0]\n" + "[0, 2, 4, 88, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 88, 0]\n" + "[0, 2, 4, 37, 0, -3, -1, -3, -1, -3, -1, 3, 3, 0, 37, 0, 85, 0, 25, 0]\n" + "[0, 2, 4, 18, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 17, 0, 18, 0]\n" + "[0, 2, 4, -120, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, -120, 0]\n" + "[0, 2, 4, 49, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 49, 0]\n" + "[0, 2, 4, 16, 2, -3, -1, -3, -1, -3, -1]\n" + "[0, 2, 4, 50, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 50, 0]\n" + "[0, 1, 67, 3, -13, 0, -98, 0, 124, 0]\n" + "[0, 2, 4, 28, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 17, 0, 28, 0]\n" + "[0, 2, 4, -108, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, -108, 0]\n" + "[0, 2, 4, -1, -1, -3, -1, -3, -1, -3, -1, 3, 1, 0, 20, 0]\n" + "[0, 2, 4, 37, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 78, 0, 37, 0]\n" + "[0, 2, 4, 14, 2, -3, -1, -3, -1, -3, -1]\n" + "[0, 2, 4, 22, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 40, 0, 22, 0]\n" + "[0, 2, 4, -116, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, -116, 0]\n" + "[0, 1, 67, 3, 23, 0, 32, 1, 124, 0]\n" + "[0, 2, 4, 31, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 28, 0, 31, 0]\n" + "[0, 2, 4, 18, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 18, 0, 17, 0]\n" + "[0, 2, 4, 21, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, -122, 0, 21, 0]\n" + "[0, 2, 4, 104, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 104, 0]\n" + "[0, 2, 4, 53, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 53, 0]\n" + "[0, 2, 4, 17, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 18, 0, 17, 0]\n" + "[0, 2, 4, 18, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 23, 0, 18, 0]\n" + "[0, 2, 4, 49, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 19, 0, 49, 0]\n" + "[0, 2, 4, 17, 0, -3, -1, -3, -1, -3, -1, 3, 3, 0, 78, 0, 37, 0, 17, 0]\n" + "[0, 2, 4, 22, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 22, 0, 19, 0]\n" + "[0, 2, 4, -2, -1, -3, -1, -3, -1, -3, -1, 3, 6, 0, 17, 0, 18, 0, 28, 0, 25, 0, 37, 0, 31, 0]\n" + "[0, 2, 4, 52, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 52, 0, 23, 0]\n" + "[0, 2, 4, 17, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 28, 0, 17, 0]\n" + "[0, 2, 4, 22, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 22, 0, 43, 0]\n" + "[0, 2, 4, -122, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, -122, 0]\n" + "[0, 2, 4, 30, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 30, 0]\n" + "[0, 1, 68, 2, 108, 0, -78, 0]\n" + "[0, 1, 67, 3, -127, 0, 32, 1, 98, 0]\n" + "[0, 1, 66, 3, -33, 0, -97, 0, -86, 0]\n" + "[0, 1, 65, 3, 53, 0, -101, 0, 111, 0]\n" + "[0, 1, 83, 4, 18, 1, -80, 0, -100, 0, 112, 0]\n" + "[0, 2, 4, -1, -1, -3, -1, -3, -1, -3, -1, 3, 2, 0, 17, 0, 18, 0]\n" + "[0, 2, 4, 23, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 47, 0, 23, 0]\n" + "[0, 2, 4, 58, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 58, 0]\n" + "[0, 2, 4, 124, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 124, 0]\n" + "[0, 2, 4, -1, -1, -3, -1, -3, -1, -3, -1, 3, 1, 0, 23, 0]\n" + "[0, 2, 4, 120, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 120, 0]\n" + "[0, 2, 4, 28, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 28, 0, 69, 0]\n" + "[0, 2, 4, 47, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 47, 0, 23, 0]\n" + "[0, 2, 4, 111, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 111, 0]\n" + "[0, 2, 4, 47, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 40, 0, 47, 0]\n" + "[0, 2, 4, 94, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 94, 0]\n" + "[0, 2, 4, 23, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 23, 0, 18, 0]\n" + "[0, 2, 4, -2, -1, -3, -1, -3, -1, -3, -1, 3, 1, 0, 42, 0]\n" + "[0, 2, 4, 42, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 42, 0]\n" + "[0, 2, 4, -1, -1, -3, -1, -3, -1, -3, -1, 3, 2, 0, 116, 0, 24, 0]\n" + "[0, 2, 4, -3, -1, -3, -1, -3, -1, -3, -1, 3, 1, 0, 22, 0]\n" + "[0, 2, 4, 17, 0, -3, -1, -3, -1, -3, -1, 3, 3, 0, 17, 0, 69, 0, 28, 0]\n" + "[0, 2, 4, 41, 0, -3, -1, -3, -1, -3, -1, 3, 3, 0, 23, 0, 48, 0, 41, 0]\n" + "[0, 2, 4, 17, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 23, 0, 17, 0]\n" + "[0, 2, 4, -2, -1, -3, -1, -3, -1, -3, -1, 3, 1, 0, 63, 0]\n" + "[0, 2, 4, 78, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 78, 0]\n" + "[0, 2, 4, 23, 0, -3, -1, -3, -1, -3, -1, 3, 8, 0, 23, 0, 24, 0, 22, 0, 21, 0, 20, 0, 19, 0, 18, 0, 17, 0]\n" + "[0, 2, 4, 18, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 18, 0, 31, 0]\n" + "[0, 2, 4, 28, 0, -3, -1, -3, -1, -3, -1, 3, 3, 0, 109, 0, 31, 0, 28, 0]\n" + "[0, 2, 4, 122, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 122, 0, 21, 0]\n" + "[0, 2, 4, 90, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 90, 0]\n" + "[0, 2, 4, -3, -1, -3, -1, -3, -1, -3, -1, 3, 1, 0, 20, 0]\n" + "[0, 2, 4, 23, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 50, 0, 23, 0]\n" + "[0, 2, 4, 126, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 126, 0]\n" + "[0, 2, 4, -1, -1, -3, -1, -3, -1, -3, -1, 3, 2, 0, 116, 0, 21, 0]\n" + "[0, 2, 4, 30, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 73, 0, 30, 0]\n" + "[0, 2, 4, 28, 0, -3, -1, -3, -1, -3, -1, 3, 3, 0, 67, 0, 31, 0, 28, 0]\n" + "[0, 2, 4, 28, 0, -3, -1, -3, -1, -3, -1, 3, 3, 0, 17, 0, 18, 0, 28, 0]\n" + "[0, 2, 4, 28, 0, -3, -1, -3, -1, -3, -1, 3, 3, 0, 37, 0, 94, 0, 28, 0]\n" + "[0, 2, 4, -2, -1, -3, -1, -3, -1, -3, -1, 3, 1, 0, 17, 0]\n" + "[0, 2, 4, 21, 0, -3, -1, -3, -1, -3, -1, 3, 3, 0, 20, 0, 21, 0, 126, 0]\n" + "[0, 2, 4, -2, -1, -3, -1, -3, -1, -3, -1, 3, 7, 0, 17, 0, 28, 0, 18, 0, 25, 0, 31, 0, 37, 0, 95, 0]\n" + "[0, 2, 4, 20, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, -109, 0, 20, 0]\n" + "[0, 2, 4, 18, 0, -3, -1, -3, -1, -3, -1, 3, 3, 0, 17, 0, 28, 0, 18, 0]\n" + "[0, 1, 83, 4, 59, 0, 3, 1, 112, 0, -80, 0]\n" + "[0, 2, 4, 102, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 102, 0]\n" + "[0, 2, 4, 28, 0, -3, -1, -3, -1, -3, -1, 3, 4, 0, 106, 0, 28, 0, 25, 0, 37, 0]\n" + "[0, 2, 4, 23, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 23, 0, 54, 0]\n" + "[0, 2, 4, 24, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 21, 0, 24, 0]\n" + "[0, 2, 4, 112, 0, -3, -1, -3, -1, -3, -1, 3, 1, 0, 112, 0]\n" + "[0, 2, 4, -2, -1, -3, -1, -3, -1, -3, -1, 3, 1, 0, 18, 0]\n" + "[0, 2, 4, 38, 0, -3, -1, -3, -1, -3, -1, 3, 3, 0, 38, 0, 104, 0, 32, 0]\n" + "[0, 3, 1, 0, 24, 0]\n" + "[0, 2, 4, 21, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 126, 0, 21, 0]\n" + "[0, 2, 4, 17, 0, -3, -1, -3, -1, -3, -1, 3, 3, 0, 84, 0, 37, 0, 17, 0]\n" + "[0, 2, 4, 93, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 28, 0, 93, 0]\n" + "[0, 2, 4, 28, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 28, 0, 31, 0]\n" + "[0, 2, 4, 17, 0, -3, -1, -3, -1, -3, -1, 3, 2, 0, 17, 0, 23, 0]";
        String[] a = temp.split("\n");
        for (String s : a) {
            String s1 = s.replaceAll("[\\[\\] ]", "");
            String[] s2 = s1.split(",");
            if (s2.length > 1) {
                Byte[] bytes = Arrays.stream(s2).map(Byte::parseByte).toArray(Byte[]::new);
                byte[] data = new byte[bytes.length - 1];
                for (int i = 0; i < data.length; i++) {
                    data[i] = bytes[i + 1];
                }
                ByteBuffer buff = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
                short type = buff.getShort();

                if (type == 1026) {
                    String ss = getEnchantingString(buff);
                    System.out.println(ss);
                }
            }
        }
    }

    /**
     * Gets the enchantment string from the byte buffer.
     *
     * @param buff Byte buffer parsed from enchant string.
     * @return Name or description of the enchantment.
     */
    private static String getEnchantingString(ByteBuffer buff) {
        short enchantID = buff.getShort();
        short b1 = buff.getShort();
        short b2 = buff.getShort();
        short b3 = buff.getShort();
        if (b1 == -3 && b2 == -3 && b3 == -3) {
            if (enchantID == -2) return "[locked]";
//            if (enchantID == -3) return "no enchant slots";
            if (enchantID == -3) return "";
            if (buff.remaining() > 0) {
                byte b = buff.get();
                if (b == 3) {
                    short size = buff.getShort();
                    String[] enchs = new String[size];
                    int count = 0;
                    while (count < size) {
                        short info = buff.getShort();
                        enchs[count] = getEnchantmentString(info);
                        count++;
                    }
                    return "Tiered - Enchanting: " + getEnchantmentString(enchantID);
                }
            } else if (enchantID > 0) {
                return "UT - Engraving: " + getEnchantmentString(enchantID);
            } else {
                return "UT - Engraving: " + getEnchantmentString(enchantID);
            }
        } else {
            System.err.printf("spacing unmatched: %d %d %d\n", b1, b2, b3);
        }
        return null;
    }

    /**
     * Id to Enchant name.
     *
     * @param enchantID Enchant ID
     * @return Enchant name.
     */
    private static String getEnchantmentString(short enchantID) {
        return String.format("%s(%d)", ENCHANTS.get(enchantID), enchantID);
    }

    /**
     * Little Endian short reader from byte array.
     *
     * @param bytes  Bytes to be read.
     * @param offset Offset in the byte array.
     * @return Reads short from offset in the byte arra.
     */
    private static short readShort(byte[] bytes, int offset) {
        return (short) (bytes[offset] & 0xFF | (bytes[offset + 1] & 0xFF) << 8);
    }

    /**
     * Verification this byte array belongs to enchantments.
     *
     * @param bytes Byte array converted from enchantment string.
     * @return True if it is an enactment byte array.
     */
    private static boolean isEnchantmentByteArray(byte[] bytes) {
        return bytes[0] == 0 && bytes[1] == 2 && bytes[2] == 4;
    }

    /**
     * Parses the encoded enchantment string to displayable enchantment name as string.
     *
     * @param code Encoded enchantment string.
     * @return Decoded enchantment string.
     */
    public static String parse(String code) {
        if (code.length() == 0) return "";
        byte[] bytes = PcStatsDecoder.sixBitStringToBytes(code);

        ByteBuffer buff = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        buff.get();
        short type = buff.getShort();

        if (type == 1026) {
            return getEnchantingString(buff);
        }
        return "";
    }

    /**
     * Depricated
     */
    private static String parseOld(String code) {
        byte[] bytes = PcStatsDecoder.sixBitStringToBytes(code);

        if (bytes.length > 0 && isEnchantmentByteArray(bytes)) {
            short enchantId = readShort(bytes, 3);
            if (enchantId == -1) {
//                return "Unlocked - unenchanted";
            } else if (enchantId == -2) {
                return "<locked>";
            } else if (enchantId == -3) {
                return "DUD - no enchant slots";
            }
            byte[] b2 = Arrays.copyOfRange(bytes, 5, 11);
            byte[] spaceing = {-3, -1, -3, -1, -3, -1};
            boolean eq = Arrays.equals(b2, spaceing);
            byte[] b3 = Arrays.copyOfRange(bytes, 11, bytes.length);
            if (!eq) {
                System.err.println("spacing unmatched: " + Arrays.toString(b2));
            } else if (b3.length == 0) {
                return "Engraved: " + enchantId;
            } else if (b3.length > 0 && b3[0] == 3) {
                short loops = readShort(b3, 1);

                try {
                    for (int i = 0; i < loops; i++) {
                        loops = readShort(b3, i * 2 + 1);
                        System.out.println("   Unlocked enchants: " + enchantId);
                    }
                } catch (ArrayIndexOutOfBoundsException ignore) {
                    System.out.println(loops + " " + Arrays.toString(b3));
                }
            }
        }

        return null;
    }

    /**
     * Fully extracts all enchants from a player entity and returns all 4 equipped slots as array string.
     *
     * @param player Player entity to get the equiped enchant names.
     * @return Four strings representing the equipment enchants if they have any. Starting with weapon, ability, armor, ring.
     */
    public static String[] extractEnchants(Entity player) {
        StatData textureStat = player.stat.ENCHANTMENTS;
        String[] slotEnchant = {"", "", "", ""};
        if (textureStat != null) {
            String s = textureStat.stringStatValue;
            String[] ss = s.split(",");
            for (int i = 0; i < slotEnchant.length; i++) {
                if (i < ss.length) {
                    String e = ss[i];
                    slotEnchant[i] = ParseEnchants.parse(e);
                }
            }
        }
        return slotEnchant;
    }

    /**
     * Gets the four encoded enchantment strings of the players equipped items.
     *
     * @param player Player to return equipped enchantment strings
     * @return Four encoded enchantment strings of equipped player
     */
    public static String[] getEnchantStrings(Entity player) {
        StatData textureStat = player.stat.ENCHANTMENTS;
        String[] slotEnchant = {"", "", "", ""};

        if (textureStat != null) {
            String s = textureStat.stringStatValue;
            String[] ss = s.split(",");
            for (int i = 0; i < slotEnchant.length && i < ss.length; i++) {
                slotEnchant[i] = ss[i];
            }
        }
        return slotEnchant;
    }

    /**
     * Basic class to extract XML enchants.
     */
    private static class Enchantment {
        public short enchantId;
        public String name;
    }
}
