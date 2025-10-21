package tomato.realmshark;

import org.xml.sax.SAXException;
import packets.data.StatData;
import packets.data.enums.StatType;
import packets.reader.BufferReader;
import tomato.backend.data.Entity;
import util.StringXML;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Enchantment parser using the sixBitStringToBytes system from the player packets.
 * Converting the encoded string to an encoded byte array then extract the enchant ID.
 * The enchant ID is then used to get the enchant description from the XML assets.
 */
public class ParseEnchants {

    private static final String ENCHANT_XML_PATH = "assets/xml/enchantments.xml";
    public static final HashMap<Short, String> ENCHANTS = new HashMap<>();

    /**
     * Load Enchant XML data to get names from file.
     */
    static {
        loadEnchants(ENCHANT_XML_PATH);
        ENCHANTS.put((short) -1, "[empty]");
    }

    private static void loadEnchants(String path) {
        try {
            FileInputStream file = new FileInputStream(path);
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
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the enchant IDs on the item.
     *
     * @return Enchant IDs on the item
     */
    private static int[] enchantId(ByteBuffer buff) {
        short enchantID = buff.getShort();
        short b1 = buff.getShort();
        short b2 = buff.getShort();
        short b3 = buff.getShort();
        if (b1 == -3 && b2 == -3 && b3 == -3) {
            return new int[]{enchantID};
        } else {
            System.err.printf("spacing unmatched: %d %d %d\n", b1, b2, b3);
        }
        return null;
    }

    /**
     * Gets the enchantment string from the byte buffer.
     *
     * @param buff Byte buffer parsed from enchant string.
     * @param code
     * @return Name or description of the enchantment.
     */
    private static String getEnchantingString(ByteBuffer buff, String code) {
        short enchantID = buff.getShort();
        short b1 = buff.getShort();
        short b2 = buff.getShort();
        short b3 = buff.getShort();
//        System.out.printf("%d %x %d %d %d\n", enchantID, enchantID, b1, b2, b3);
        if (b1 == -3 && b2 == -3 && b3 == -3) {
            if (enchantID == -2) return "[locked]";
//            if (enchantID == -3) return "no enchant slots";
            if (enchantID == -3) return "";
            if (buff.remaining() > 0) {
                return getEnchantmentString(enchantID);
//                byte b = buff.get();
//                if (b == 3) {
//                    short size = buff.getShort();
//                    String[] enchs = new String[size];
//                    int count = 0;
//                    while (count < size) {
//                        short info = buff.getShort();
//                        enchs[count] = getEnchantmentString(info);
//                        count++;
//                    }
//                    return "Tiered - Enchanting: " + getEnchantmentString(enchantID);
//                }
            } else if (enchantID > 0) {
                return "UT - Engraving: " + getEnchantmentString(enchantID);
            } else {
                return "UT - Engraving: " + getEnchantmentString(enchantID);
            }
        } else {
            System.err.printf("spacing unmatched: %d %d %d\n", b1, b2, b3);
        }
        return code;
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
        //System.out.println("Parsing enchantment code: " + code);
        if (code.isEmpty()) return "";

        byte[] rawBytes = PcStatsDecoder.sixBitStringToBytes(code);
        // Expected buffer size: 1 byte header + 2 bytes type + 4 enchantments (8 bytes)
        int expectedSize = 1 + 2 + 8;
        if (rawBytes.length > expectedSize) {
            rawBytes = Arrays.copyOfRange(rawBytes, 0, expectedSize);
        }

        BufferReader byteBuffer = new BufferReader(
                ByteBuffer.wrap(rawBytes).order(ByteOrder.LITTLE_ENDIAN)
        );
        byteBuffer.readByte();
        StringBuilder res = new StringBuilder();
        if (byteBuffer.readShort() != 1026) {
            //System.out.println("Invalid enchantment header, skipping.");
            return res.toString();
        }
        while (!byteBuffer.isBufferFullyParsed()) {
            short enchantId = byteBuffer.readShort();
            //System.out.println("Found enchantment ID: " + enchantId);
            if (enchantId == -3) return res.toString();
            else if (enchantId == -2) return res + "[locked]";
            else if (enchantId == -1) return res + "empty";
            String enchantName = getEnchantmentString(enchantId);
            //System.out.println("Enchantment: " + enchantName);
            res.append(enchantName);
            res.append("\n");
        }
        return res.toString();
    }

    /**
     * Extract all enchant IDs from an encoded enchant string.
     * Returns an empty list if none or if the format is invalid.
     */
    public static List<Short> extractEnchantIds(String code) {
        List<Short> ids = new ArrayList<>();
        if (code == null || code.isEmpty()) return ids;

        byte[] rawBytes = PcStatsDecoder.sixBitStringToBytes(code);
        int expectedSize = 1 + 2 + 8;
        if (rawBytes.length > expectedSize) {
            rawBytes = Arrays.copyOfRange(rawBytes, 0, expectedSize);
        }

        BufferReader byteBuffer = new BufferReader(
                ByteBuffer.wrap(rawBytes).order(ByteOrder.LITTLE_ENDIAN)
        );
        byteBuffer.readByte();
        if (byteBuffer.readShort() != 1026) {
            return ids;
        }
        while (!byteBuffer.isBufferFullyParsed()) {
            short enchantId = byteBuffer.readShort();
            if (enchantId == -3) break;
            if (enchantId == -2 || enchantId == -1) continue;
            ids.add(enchantId);
        }
        return ids;
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
        StatData textureStat = player.stat.get(StatType.UNIQUE_DATA_STRING);
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
        StatData textureStat = player.stat.get(StatType.UNIQUE_DATA_STRING);
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

    public static int getEnchantId(String code) {
        if (code.length() == 0) return -1;
        byte[] bytes = PcStatsDecoder.sixBitStringToBytes(code);

        ByteBuffer buff = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        buff.get();
        short type = buff.getShort();

        if (type == 1026) {
            return buff.getShort();
        }
        return -1;
    }

    /**
     * Basic class to extract XML enchants.
     */
    private static class Enchantment {
        public short enchantId;
        public String name;
    }
}
