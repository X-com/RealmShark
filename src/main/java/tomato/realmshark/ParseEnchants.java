package tomato.realmshark;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import packets.data.StatData;
import packets.data.enums.StatType;
import packets.reader.BufferReader;
import tomato.backend.data.Entity;
import util.StringXML;

/**
 * Enchantment parsing utilities.
 *
 * Responsibilities:
 * - Load enchantment definitions from assets/xml/enchantments.xml
 * - Parse encoded enchant strings from UNIQUE_DATA_STRING
 * - Aggregate DPS-relevant multipliers: damage (min/max) and rate-of-fire
 * - Provide helper methods to compute multipliers and readable strings
 */
public class ParseEnchants {

    private static final String ENCHANT_XML_PATH =
        "assets/xml/enchantments.xml";

    // Maps enchant type ID -> human readable name (<DisplayId> from the XML,
    // e.g. "Attack Bonus I"). Falls back to the internal id if absent.
    public static final HashMap<Short, String> ENCHANTS = new HashMap<>();

    // Maps enchant type ID -> internal id (the "id" attribute, e.g.
    // "Attack_Bonus_1" / "LUCKY_STREAK"). Kept because the all-uppercase ones
    // mark unique/ST enchants, which the ping list groups separately.
    public static final HashMap<Short, String> ENCHANT_INTERNAL_IDS =
        new HashMap<>();

    // Maps enchant type ID -> parsed effect multipliers
    private static final HashMap<Short, EnchantEffect> ENCHANT_EFFECTS =
        new HashMap<>();

    // Maps enchant type ID -> regen effects (HP/MP, flat/percent)
    private static final HashMap<Short, RegenEffect> ENCHANT_REGEN =
        new HashMap<>();

    // Maps enchant type ID -> total LootBonus percent (additive)
    private static final HashMap<Short, Float> ENCHANT_LOOT_BONUS =
        new HashMap<>();

    static {
        loadEnchants(ENCHANT_XML_PATH);
        ENCHANTS.put((short) -1, "[empty]");
    }

    private static void loadEnchants(String path) {
        try {
            FileInputStream file = new FileInputStream(path);
            String result = new BufferedReader(new InputStreamReader(file))
                .lines()
                .collect(Collectors.joining("\n"));
            StringXML base = StringXML.getParsedXML(result);

            for (StringXML xml : base) {
                if (!Objects.equals(xml.name, "Enchantment")) continue;

                Short enchantType = null;
                String enchantName = null;
                String displayName = null;
                EnchantEffect effect = new EnchantEffect();
                RegenEffect regen = new RegenEffect();
                float lootBonus = 0f;

                for (StringXML node : xml) {
                    if (Objects.equals(node.name, "id")) {
                        enchantName = node.value;
                    } else if (Objects.equals(node.name, "DisplayId")) {
                        displayName = readTextContent(node);
                    } else if (Objects.equals(node.name, "type")) {
                        try {
                            enchantType = Short.decode(node.value);
                        } catch (NumberFormatException ignore) {
                            /* ignore invalid type */
                        }
                    } else if (Objects.equals(node.name, "Mutators")) {
                        // Parse DPS-influencing mutators and regen mutators
                        for (StringXML mut : node) {
                            if (Objects.equals(mut.name, "MultiplyMinDamage")) {
                                effect.minDamageMult *= readNumericChild(mut);
                            } else if (
                                Objects.equals(mut.name, "MultiplyMaxDamage")
                            ) {
                                effect.maxDamageMult *= readNumericChild(mut);
                            } else if (
                                Objects.equals(mut.name, "MultiplyRateOfFire")
                            ) {
                                effect.rateOfFireMult *= readNumericChild(mut);
                            } else if (
                                Objects.equals(mut.name, "ActivateOnEquip")
                            ) {
                                String statVal = null;
                                Float amountVal = null;
                                String kindVal = null; // "Flat" or "Percent"
                                boolean isLootBonus = false;
                                boolean isStatModMult = false;
                                for (StringXML mchild : mut) {
                                    if (Objects.equals(mchild.name, "stat")) {
                                        statVal = mchild.value;
                                    } else if (
                                        Objects.equals(mchild.name, "amount")
                                    ) {
                                        try {
                                            amountVal = Float.parseFloat(
                                                mchild.value
                                            );
                                        } catch (Exception ignore) {}
                                    } else if (mchild.value != null) {
                                        String v = mchild.value.trim();
                                        if (Objects.equals(v, "FlatRegen")) {
                                            kindVal = "Flat";
                                        } else if (
                                            Objects.equals(v, "PercentageRegen")
                                        ) {
                                            kindVal = "Percent";
                                        } else if (
                                            Objects.equals(v, "LootBonus")
                                        ) {
                                            isLootBonus = true;
                                        } else if (
                                            Objects.equals(v, "StatModMult")
                                        ) {
                                            isStatModMult = true;
                                        }
                                    }
                                }
                                // Regen effects
                                if (
                                    statVal != null &&
                                    amountVal != null &&
                                    kindVal != null
                                ) {
                                    if (Objects.equals(statVal, "MP")) {
                                        if (Objects.equals(kindVal, "Flat")) {
                                            regen.manaFlatPerSec += amountVal;
                                        } else {
                                            regen.manaPercentOfMaxPerSec +=
                                                amountVal;
                                        }
                                    } else if (Objects.equals(statVal, "HP")) {
                                        if (Objects.equals(kindVal, "Flat")) {
                                            regen.lifeFlatPerSec += amountVal;
                                        } else {
                                            regen.lifePercentOfMaxPerSec +=
                                                amountVal;
                                        }
                                    }
                                }
                                // Loot bonus (any ActivateOnEquip ... >LootBonus</ActivateOnEquip>)
                                if (isLootBonus && amountVal != null) {
                                    lootBonus += amountVal;
                                }
                                // Stat Mod Multiplier (any ActivateOnEquip ... >StatModMult</ActivateOnEquip>)
                                if (isStatModMult && amountVal != null) {
                                    effect.statDamageMult *= amountVal;
                                }
                            }
                        }
                    }
                }

                if (enchantType != null) {
                    // Prefer the readable <DisplayId>; fall back to the internal
                    // id so an entry is never nameless.
                    String shown = (displayName != null &&
                            !displayName.trim().isEmpty())
                        ? displayName.trim()
                        : enchantName;
                    if (shown != null) {
                        ENCHANTS.put(enchantType, shown);
                    }
                    if (enchantName != null) {
                        ENCHANT_INTERNAL_IDS.put(enchantType, enchantName);
                    }
                    ENCHANT_EFFECTS.put(enchantType, effect);
                    ENCHANT_REGEN.put(enchantType, regen);
                    ENCHANT_LOOT_BONUS.put(enchantType, lootBonus);
                }
            }
        } catch (
            ParserConfigurationException
            | SAXException
            | RuntimeException
            | java.io.IOException e
        ) {
            // If the asset is missing at runtime we still want the app to work; maps will be partially populated.
            // You can log this if desired.
        }
    }

    /**
     * Parse an encoded enchantment string into a human-readable multi-line list of
     * "DisplayName(id)" entries. Keeps legacy locked/empty handling.
     */
    public static String parse(String code) {
        if (code == null || code.isEmpty()) return "";
        byte[] rawBytes = PcStatsDecoder.sixBitStringToBytes(code);

        // Trim to expected maximum: 1 byte header + 2 bytes type + up to 4 enchants (8 bytes)
        final int expectedSize = 1 + 2 + 8;
        if (rawBytes.length > expectedSize) {
            rawBytes = Arrays.copyOfRange(rawBytes, 0, expectedSize);
        }

        BufferReader br = new BufferReader(
            ByteBuffer.wrap(rawBytes).order(ByteOrder.LITTLE_ENDIAN)
        );
        // header byte
        if (br.isBufferFullyParsed()) return "";
        br.readByte();

        StringBuilder out = new StringBuilder();
        if (br.isBufferFullyParsed()) return out.toString();

        short type = br.readShort();
        if (type != 1026) return out.toString();

        while (!br.isBufferFullyParsed()) {
            short enchantId = br.readShort();
            if (enchantId == -3) break; // terminator
            if (enchantId == -2) return out + "[locked]";
            if (enchantId == -1) return out + "empty";
            out.append(getEnchantmentString(enchantId)).append("\n");
        }
        return out.toString();
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
     * Returns raw enchant strings for 4 equipped slots: weapon, ability, armor, ring.
     * If UNIQUE_DATA_STRING is missing or malformed, returns empty strings.
     */
    public static String[] getEnchantStrings(Entity player) {
        String[] slotEnchant = { "", "", "", "" };
        if (player == null) return slotEnchant;

        StatData s = player.stat.get(StatType.UNIQUE_DATA_STRING);
        if (s == null || s.stringStatValue == null) return slotEnchant;

        String[] ss = s.stringStatValue.split(",");
        for (int i = 0; i < slotEnchant.length && i < ss.length; i++) {
            slotEnchant[i] = ss[i];
        }
        return slotEnchant;
    }

    /**
     * Returns parsed enchant descriptions (multi-line) for 4 equipped slots.
     */
    public static String[] extractEnchants(Entity player) {
        String[] raw = getEnchantStrings(player);
        String[] parsed = new String[raw.length];
        for (int i = 0; i < raw.length; i++) {
            parsed[i] = parse(raw[i]);
        }
        return parsed;
    }

    /**
     * Helper: get first enchant id in code, or -1 if not present.
     */
    public static int getEnchantId(String code) {
        if (code == null || code.isEmpty()) return -1;
        byte[] bytes = PcStatsDecoder.sixBitStringToBytes(code);

        ByteBuffer buff = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        if (!buff.hasRemaining()) return -1;
        buff.get(); // header

        if (buff.remaining() < 2) return -1;
        short type = buff.getShort();
        if (type != 1026 || buff.remaining() < 2) return -1;

        return buff.getShort();
    }

    /**
     * Aggregated multipliers across all enchants applied to a weapon.
     */
    public static class Totals {

        public float minDamage = 1f;
        public float maxDamage = 1f;
        public float rateOfFire = 1f;
        public float statDamage = 1f;

        @Override
        public String toString() {
            return (
                "Totals{minDamage=" +
                minDamage +
                ", maxDamage=" +
                maxDamage +
                ", rateOfFire=" +
                rateOfFire +
                ", statDamage=" +
                statDamage +
                "}"
            );
        }
    }

    /**
     * Aggregated mana regen bonuses from enchants.
     * Values are per second; percent is fraction of Max MP per second.
     */
    public static class RegenTotals {

        public float manaFlatPerSec = 0f;
        public float manaPercentOfMaxPerSec = 0f;
        public float lifeFlatPerSec = 0f;
        public float lifePercentOfMaxPerSec = 0f;
    }

    /**
     * Compute mana regeneration bonuses from a single encoded enchant string.
     * Applies only the special enchants: Flat Mana Regeneration and Percentage Mana Regeneration.
     * Note: Some enchants double their bonus out of combat; apply doubling in the caller if needed.
     */
    public static RegenTotals computeManaRegenBonuses(String code) {
        RegenTotals totals = new RegenTotals();
        if (code == null || code.isEmpty()) return totals;

        byte[] rawBytes = PcStatsDecoder.sixBitStringToBytes(code);
        final int expectedSize = 1 + 2 + 8; // header + type + up to 4 enchants
        if (rawBytes.length > expectedSize) {
            rawBytes = Arrays.copyOfRange(rawBytes, 0, expectedSize);
        }

        BufferReader br = new BufferReader(
            ByteBuffer.wrap(rawBytes).order(ByteOrder.LITTLE_ENDIAN)
        );
        if (br.isBufferFullyParsed()) return totals;
        br.readByte(); // header

        if (br.isBufferFullyParsed()) return totals;
        if (br.readShort() != 1026) return totals;

        while (!br.isBufferFullyParsed()) {
            short enchantId = br.readShort();
            if (enchantId == -3) break; // terminator
            if (enchantId <= 0) continue; // skip locked/empty

            switch (enchantId) {
                // Flat Mana Regeneration I-IV
                case (short) 0x5FF:
                    totals.manaFlatPerSec += 2f;
                    break; // +2 MP/s
                case (short) 0x600:
                    totals.manaFlatPerSec += 3f;
                    break; // +3 MP/s
                case (short) 0x601:
                    totals.manaFlatPerSec += 4f;
                    break; // +4 MP/s
                case (short) 0x602:
                    totals.manaFlatPerSec += 5f;
                    break; // +5 MP/s
                // Percentage Mana Regeneration I-IV (fraction of Max MP per second)
                case (short) 0x604:
                    totals.manaPercentOfMaxPerSec += 0.0075f;
                    break; // +0.75%/s
                case (short) 0x605:
                    totals.manaPercentOfMaxPerSec += 0.01f;
                    break; // +1.00%/s
                case (short) 0x606:
                    totals.manaPercentOfMaxPerSec += 0.0125f;
                    break; // +1.25%/s
                case (short) 0x607:
                    totals.manaPercentOfMaxPerSec += 0.015f;
                    break; // +1.50%/s
                default:
                    // Other enchants not affecting mana regen are ignored here
                    break;
            }
        }

        return totals;
    }

    /**
     * Compute mana regeneration bonuses from multiple encoded enchant strings (e.g., weapon/ability/armor/ring).
     */
    public static RegenTotals computeManaRegenBonuses(String[] codes) {
        RegenTotals sum = new RegenTotals();
        if (codes == null) return sum;
        for (String code : codes) {
            RegenTotals t = computeManaRegenBonuses(code);
            sum.manaFlatPerSec += t.manaFlatPerSec;
            sum.manaPercentOfMaxPerSec += t.manaPercentOfMaxPerSec;
        }
        return sum;
    }

    /**
     * Convenience: get total mana regen per second provided by enchants for a single item.
     * If outOfCombat is true, the enchant bonus is doubled (base regen should be added separately by caller).
     */
    public static float getManaRegenPerSecondFromEnchants(
        String code,
        int maxMp,
        boolean outOfCombat
    ) {
        RegenTotals t = computeManaRegenBonuses(code);
        float bonus = t.manaFlatPerSec + (t.manaPercentOfMaxPerSec * maxMp);
        return outOfCombat ? (bonus * 2f) : bonus;
    }

    /**
     * Convenience: aggregate mana regen per second from multiple items' enchant strings.
     * If outOfCombat is true, the enchant bonus is doubled (base regen should be added separately by caller).
     */
    public static float getManaRegenPerSecondFromEnchants(
        String[] codes,
        int maxMp,
        boolean outOfCombat
    ) {
        RegenTotals t = computeManaRegenBonuses(codes);
        float bonus = t.manaFlatPerSec + (t.manaPercentOfMaxPerSec * maxMp);
        return outOfCombat ? (bonus * 2f) : bonus;
    }

    /**
     * Compute life (HP) regeneration bonuses from a single encoded enchant string.
     * Uses parsed ActivateOnEquip(mutators) for FlatRegen/PercentageRegen on HP.
     */
    public static RegenTotals computeLifeRegenBonuses(String code) {
        RegenTotals totals = new RegenTotals();
        if (code == null || code.isEmpty()) return totals;

        byte[] rawBytes = PcStatsDecoder.sixBitStringToBytes(code);
        final int expectedSize = 1 + 2 + 8; // header + type + up to 4 enchants
        if (rawBytes.length > expectedSize) {
            rawBytes = Arrays.copyOfRange(rawBytes, 0, expectedSize);
        }

        BufferReader br = new BufferReader(
            ByteBuffer.wrap(rawBytes).order(ByteOrder.LITTLE_ENDIAN)
        );
        if (br.isBufferFullyParsed()) return totals;
        br.readByte(); // header

        if (br.isBufferFullyParsed()) return totals;
        if (br.readShort() != 1026) return totals;

        while (!br.isBufferFullyParsed()) {
            short enchantId = br.readShort();
            if (enchantId == -3) break; // terminator
            if (enchantId <= 0) continue; // skip locked/empty

            RegenEffect eff = ENCHANT_REGEN.get(enchantId);
            if (eff != null) {
                totals.lifeFlatPerSec += eff.lifeFlatPerSec;
                totals.lifePercentOfMaxPerSec += eff.lifePercentOfMaxPerSec;
            }
        }

        return totals;
    }

    /**
     * Compute life (HP) regeneration bonuses from multiple encoded enchant strings.
     */
    public static RegenTotals computeLifeRegenBonuses(String[] codes) {
        RegenTotals sum = new RegenTotals();
        if (codes == null) return sum;
        for (String code : codes) {
            RegenTotals t = computeLifeRegenBonuses(code);
            sum.lifeFlatPerSec += t.lifeFlatPerSec;
            sum.lifePercentOfMaxPerSec += t.lifePercentOfMaxPerSec;
        }
        return sum;
    }

    /**
     * Convenience: get total life (HP) regen per second provided by enchants for a single item.
     * If outOfCombat is true, the enchant bonus is doubled (base regen should be added separately by caller).
     */
    public static float getLifeRegenPerSecondFromEnchants(
        String code,
        int maxHp,
        boolean outOfCombat
    ) {
        RegenTotals t = computeLifeRegenBonuses(code);
        float bonus = t.lifeFlatPerSec + (t.lifePercentOfMaxPerSec * maxHp);
        return outOfCombat ? (bonus * 2f) : bonus;
    }

    /**
     * Convenience: aggregate life (HP) regen per second from multiple items' enchant strings.
     * If outOfCombat is true, the enchant bonus is doubled (base regen should be added separately by caller).
     */
    public static float getLifeRegenPerSecondFromEnchants(
        String[] codes,
        int maxHp,
        boolean outOfCombat
    ) {
        RegenTotals t = computeLifeRegenBonuses(codes);
        float bonus = t.lifeFlatPerSec + (t.lifePercentOfMaxPerSec * maxHp);
        return outOfCombat ? (bonus * 2f) : bonus;
    }

    /**
     * Compute damage and rate-of-fire multipliers from an encoded enchant string.
     * Supports:
     * - Damage Bonus I-IV (MultiplyMinDamage/MultiplyMaxDamage)
     * - FireRate Bonus I-IV (MultiplyRateOfFire)
     * - Tradeoffs (combinations of the above)
     */
    public static Totals computeWeaponMultipliers(String code) {
        Totals totals = new Totals();
        if (code == null || code.isEmpty()) return totals;

        byte[] rawBytes = PcStatsDecoder.sixBitStringToBytes(code);
        final int expectedSize = 1 + 2 + 8;
        if (rawBytes.length > expectedSize) {
            rawBytes = Arrays.copyOfRange(rawBytes, 0, expectedSize);
        }

        BufferReader br = new BufferReader(
            ByteBuffer.wrap(rawBytes).order(ByteOrder.LITTLE_ENDIAN)
        );
        if (br.isBufferFullyParsed()) return totals;
        br.readByte(); // header

        if (br.isBufferFullyParsed()) return totals;
        if (br.readShort() != 1026) return totals;

        while (!br.isBufferFullyParsed()) {
            short enchantId = br.readShort();
            if (enchantId == -3) break; // terminator
            if (enchantId <= 0) continue; // skip locked/empty

            EnchantEffect eff = ENCHANT_EFFECTS.get(enchantId);
            if (eff != null) {
                totals.minDamage *= eff.minDamageMult;
                totals.maxDamage *= eff.maxDamageMult;
                totals.rateOfFire *= eff.rateOfFireMult;
                totals.statDamage *= eff.statDamageMult;
            }
        }

        return totals;
    }

    public static float getMinDamageMultiplier(String code) {
        return computeWeaponMultipliers(code).minDamage;
    }

    public static float getMaxDamageMultiplier(String code) {
        return computeWeaponMultipliers(code).maxDamage;
    }

    public static float getRateOfFireMultiplier(String code) {
        return computeWeaponMultipliers(code).rateOfFire;
    }

    public static float getStatDamageMultiplier(String code) {
        return computeWeaponMultipliers(code).statDamage;
    }

    /**
     * Compute (sum) the Loot Bonus percentages contributed by all enchants in a single
     * encoded enchant string by looking up each enchant ID's LootBonus amount parsed from XML.
     * The returned value is an additive percent value (e.g. 2.5 means +2.5%).
     *
     * Any <ActivateOnEquip amount="X">LootBonus</ActivateOnEquip> encountered for an enchant is
     * aggregated during XML load and stored in ENCHANT_LOOT_BONUS; this method just decodes the
     * enchant IDs and sums those mapped values.
     */
    public static float computeLootBonus(String code) {
        if (code == null || code.isEmpty()) return 0f;

        byte[] rawBytes = PcStatsDecoder.sixBitStringToBytes(code);
        final int expectedSize = 1 + 2 + 8;
        if (rawBytes.length > expectedSize) {
            rawBytes = java.util.Arrays.copyOfRange(rawBytes, 0, expectedSize);
        }

        java.nio.ByteBuffer br = java.nio.ByteBuffer.wrap(rawBytes).order(
            java.nio.ByteOrder.LITTLE_ENDIAN
        );

        if (!br.hasRemaining()) return 0f;
        br.get(); // header

        if (br.remaining() < 2) return 0f;
        short type = br.getShort();
        if (type != 1026) return 0f;

        float bonus = 0f;
        while (br.remaining() >= 2) {
            short enchantId = br.getShort();
            if (enchantId == -3) break; // terminator
            if (enchantId <= 0) continue; // skip locked / empty

            Float b = ENCHANT_LOOT_BONUS.get(enchantId);
            if (b != null) bonus += b;
        }
        return bonus;
    }

    /**
     * Aggregate loot bonus across multiple encoded enchant strings (e.g., weapon, ability, armor, ring).
     */
    public static float computeLootBonus(String[] codes) {
        if (codes == null) return 0f;
        float total = 0f;
        for (String c : codes) {
            total += computeLootBonus(c);
        }
        return total;
    }

    /**
     * Public convenience method naming consistent with prospective SendLoot integration.
     * Returns total Loot Bonus percent (additive) from all provided encoded enchant strings.
     */
    public static float getTotalLootBonusPercent(String[] codes) {
        return computeLootBonus(codes);
    }

    // ===== Helpers =====

    /**
     * Reads an element's text content. util.StringXML keeps text as the first
     * child rather than on the node itself, so check there before node.value.
     */
    private static String readTextContent(StringXML node) {
        if (node == null) return null;
        if (node.children != null && !node.children.isEmpty()) {
            String v = node.children.get(0).value;
            if (v != null) return v;
        }
        return node.value;
    }

    private static String getEnchantmentString(short enchantID) {
        String name = ENCHANTS.get(enchantID);
        if (name == null) name = "Unknown";
        return String.format("%s(%d)", name, enchantID);
    }

    private static boolean isEnchantmentByteArray(byte[] bytes) {
        return (
            bytes.length >= 3 && bytes[0] == 0 && bytes[1] == 2 && bytes[2] == 4
        );
    }

    /**
     * Read a float value from a node's text content.
     * The util.StringXML used in this project often keeps text content as children; we also fallback to node.value.
     */
    private static float readNumericChild(StringXML node) {
        if (node == null) return 1f;

        // Prefer children values (common representation for our XML)
        for (StringXML child : node) {
            if (
                Objects.equals(child.name, "projectileId") ||
                Objects.equals(child.name, "subAttackIndex")
            ) {
                continue; // not value content
            }
            String v = child.value;
            if (v == null) continue;
            v = v.replace("\t", "").trim();
            try {
                return Float.parseFloat(v);
            } catch (NumberFormatException ignore) {
                /* try fallback */
            }
        }

        // Fallback to node.value
        if (node.value != null) {
            String v = node.value.replace("\t", "").trim();
            try {
                return Float.parseFloat(v);
            } catch (NumberFormatException ignore) {
                /* no-op */
            }
        }

        return 1f;
    }

    // ===== Data holders =====

    private static class EnchantEffect {

        float minDamageMult = 1f;
        float maxDamageMult = 1f;
        float rateOfFireMult = 1f;
        float statDamageMult = 1f;
    }

    private static class RegenEffect {

        float manaFlatPerSec = 0f;
        float manaPercentOfMaxPerSec = 0f;
        float lifeFlatPerSec = 0f;
        float lifePercentOfMaxPerSec = 0f;
    }
}
