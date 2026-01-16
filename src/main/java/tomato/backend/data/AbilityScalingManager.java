package tomato.backend.data;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import packets.data.enums.StatType;
import tomato.realmshark.ParseEnchants;

/**
 * Manages ability scaling data parsed from equip.xml.
 * Caches stat scaling information for all abilities to avoid repeated XML parsing.
 */
public class AbilityScalingManager {

    private static AbilityScalingManager instance;
    private final Map<Integer, AbilityScalingData> scalingData =
        new HashMap<>();
    private final Map<Integer, Integer> projectileToWeaponMap = new HashMap<>();
    private final Map<Integer, AbilityScalingData> projectileScalingData =
        new HashMap<>();

    /**
     * Data structure to hold scaling information for an ability.
     */
    public static class AbilityScalingData {

        public final int weaponId;
        public final StatType scalingStat;
        public final int scalingMin;
        public final float damagePerStat;
        public final int numShots;

        public AbilityScalingData(
            int weaponId,
            StatType scalingStat,
            int scalingMin,
            float damagePerStat,
            int numShots
        ) {
            this.weaponId = weaponId;
            this.scalingStat = scalingStat;
            this.scalingMin = scalingMin;
            this.damagePerStat = damagePerStat;
            this.numShots = numShots;
        }

        public boolean hasScaling() {
            return scalingStat != null && damagePerStat > 0;
        }
    }

    private AbilityScalingManager() {}

    public static AbilityScalingManager getInstance() {
        if (instance == null) {
            instance = new AbilityScalingManager();
        }
        return instance;
    }

    /**
     * Initializes the scaling manager by parsing equip.xml.
     * Should be called once on application startup.
     */
    public void initialize() {
        try {
            File equipFile = new File("assets/xml/equip.xml");
            if (!equipFile.exists()) {
                return;
            }

            DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(equipFile);
            document.getDocumentElement().normalize();

            parseEquipXml(document);

            System.out.println(
                "AbilityScalingManager initialized with " +
                    scalingData.size() +
                    " abilities"
            );
        } catch (Exception e) {
            System.err.println(
                "Failed to initialize AbilityScalingManager: " + e.getMessage()
            );
        }
    }

    private void parseEquipXml(Document document) {
        NodeList objectNodes = document.getElementsByTagName("Object");

        for (int i = 0; i < objectNodes.getLength(); i++) {
            Node objectNode = objectNodes.item(i);
            if (objectNode.getNodeType() != Node.ELEMENT_NODE) continue;

            Element objectElement = (Element) objectNode;
            int weaponId = parseWeaponId(objectElement);
            if (weaponId == -1) continue;

            // Check if this is a projectile object
            String projectileId = objectElement.getAttribute("id");
            if (projectileId != null && isProjectileObject(projectileId)) {
                mapProjectileToWeapon(objectElement, weaponId);
            }

            // Parse Activate elements
            parseActivateElements(objectElement, weaponId, "Activate");
            if (!scalingData.containsKey(weaponId)) {
                parseActivateElements(
                    objectElement,
                    weaponId,
                    "OnConditionEndActivate"
                );
            }
        }
    }

    private int parseWeaponId(Element objectElement) {
        String typeAttr = objectElement.getAttribute("type");
        if (typeAttr.isEmpty()) return -1;

        try {
            return Integer.parseInt(typeAttr.replace("0x", ""), 16);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private boolean isProjectileObject(String projectileId) {
        return (
            projectileId.contains("Proj") ||
            projectileId.contains("Missile") ||
            projectileId.contains("Proc")
        );
    }

    private void parseActivateElements(
        Element objectElement,
        int weaponId,
        String tagName
    ) {
        NodeList activateNodes = objectElement.getElementsByTagName(tagName);

        for (int j = 0; j < activateNodes.getLength(); j++) {
            Element activateElement = (Element) activateNodes.item(j);
            AbilityScalingData data = parseScalingData(
                activateElement,
                weaponId
            );

            if (data != null) {
                scalingData.put(weaponId, data);
                break;
            }
        }
    }

    private AbilityScalingData parseScalingData(
        Element activateElement,
        int weaponId
    ) {
        String scalingStatAttr = activateElement.getAttribute("scalingStat");
        if (scalingStatAttr.isEmpty()) return null;

        StatType scalingStat = parseStatType(scalingStatAttr);
        if (scalingStat == null) return null;

        float damagePerStat = calculateDamagePerStat(activateElement);
        if (damagePerStat <= 0) return null;

        int scalingMin = parseScalingMin(activateElement);
        int numShots = parseNumShots(activateElement);

        return new AbilityScalingData(
            weaponId,
            scalingStat,
            scalingMin,
            damagePerStat,
            numShots
        );
    }

    private float calculateDamagePerStat(Element activateElement) {
        String statModDamageAttr = activateElement.getAttribute(
            "statModDamage"
        );
        String statModFlatAttr = activateElement.getAttribute("statModFlat");
        String statModPercAttr = activateElement.getAttribute("statModPerc");

        if (!statModDamageAttr.isEmpty()) {
            return Float.parseFloat(statModDamageAttr);
        } else if (!statModFlatAttr.isEmpty() && !statModPercAttr.isEmpty()) {
            float flatBonus = Float.parseFloat(statModFlatAttr);
            float percBonus = Float.parseFloat(statModPercAttr);
            return flatBonus + (percBonus * 100);
        }
        return 0;
    }

    private StatType parseStatType(String statName) {
        switch (statName.toUpperCase()) {
            case "WIS":
            case "WISDOM":
                return StatType.WISDOM_STAT;
            case "DEX":
            case "DEXTERITY":
                return StatType.DEXTERITY_STAT;
            case "ATT":
            case "ATTACK":
                return StatType.ATTACK_STAT;
            case "DEF":
            case "DEFENSE":
                return StatType.DEFENSE_STAT;
            case "SPD":
            case "SPEED":
                return StatType.SPEED_STAT;
            case "VIT":
            case "VITALITY":
                return StatType.VITALITY_STAT;
            case "HP":
            case "HEALTH":
            case "LIFE":
                return StatType.MAX_HP_STAT;
            case "MP":
            case "MANA":
                return StatType.MAX_MP_STAT;
            default:
                return null;
        }
    }

    private int parseScalingMin(Element activateElement) {
        String scalingMinAttr = activateElement.getAttribute(
            "statModScalingMin"
        );
        if (!scalingMinAttr.isEmpty()) {
            try {
                return Integer.parseInt(scalingMinAttr);
            } catch (NumberFormatException e) {
                // Use default
            }
        }
        return 50;
    }

    private int parseNumShots(Element activateElement) {
        String numShotsAttr = activateElement.getAttribute("numShots");
        if (!numShotsAttr.isEmpty()) {
            try {
                return Integer.parseInt(numShotsAttr);
            } catch (NumberFormatException e) {
                // Use default
            }
        }
        return 1;
    }

    private void mapProjectileToWeapon(
        Element projectileElement,
        int projectileId
    ) {
        String projectileType =
            "0x" + Integer.toHexString(projectileId).toUpperCase();

        try {
            Document doc = projectileElement.getOwnerDocument();
            if (doc == null) return;

            NodeList objectNodes = doc.getElementsByTagName("Object");
            for (int i = 0; i < objectNodes.getLength(); i++) {
                Node objectNode = objectNodes.item(i);
                if (objectNode.getNodeType() != Node.ELEMENT_NODE) continue;

                Element objectElement = (Element) objectNode;
                Integer parentId = findProjectileMapping(
                    objectElement,
                    projectileType
                );

                if (parentId != null) {
                    projectileToWeaponMap.put(projectileId, parentId);
                    return;
                }
            }
        } catch (Exception e) {
            // Best-effort mapping
        }
    }

    private Integer findProjectileMapping(
        Element objectElement,
        String projectileType
    ) {
        String[] tagNames = { "OnPlayerShootActivate", "Activate" };

        for (String tagName : tagNames) {
            NodeList nodes = objectElement.getElementsByTagName(tagName);
            for (int j = 0; j < nodes.getLength(); j++) {
                Element act = (Element) nodes.item(j);
                if (matchesProjectileType(act, projectileType)) {
                    int weaponId = parseWeaponId(objectElement);
                    return (weaponId != -1) ? weaponId : null;
                }
            }
        }
        return null;
    }

    private boolean matchesProjectileType(
        Element element,
        String projectileType
    ) {
        if (element == null || projectileType == null) return false;

        String typeAttr = element.getAttribute("type");
        String text = element.getTextContent();
        String candidate = (typeAttr != null && !typeAttr.isEmpty())
            ? typeAttr.trim()
            : (text != null ? text.trim() : "");

        if (candidate.isEmpty()) return false;

        if (
            candidate.equalsIgnoreCase(projectileType) ||
            candidate.toUpperCase().endsWith(projectileType)
        ) {
            return true;
        }

        // Check for embedded hex
        String hex = extractHex(candidate);
        return hex != null && hex.equalsIgnoreCase(projectileType);
    }

    private String extractHex(String candidate) {
        int idx = candidate.indexOf("0x");
        if (idx == -1) return null;

        int end = idx + 2;
        while (
            end < candidate.length() &&
            Character.digit(candidate.charAt(end), 16) != -1
        ) {
            end++;
        }
        return (end > idx + 2) ? candidate.substring(idx, end) : null;
    }

    /**
     * Gets scaling data for a specific weapon ID.
     */
    public AbilityScalingData getScalingData(int weaponId) {
        AbilityScalingData data = scalingData.get(weaponId);
        if (data != null) return data;

        Integer parentWeaponId = projectileToWeaponMap.get(weaponId);
        if (parentWeaponId != null) {
            return scalingData.get(parentWeaponId);
        }

        return projectileScalingData.get(weaponId);
    }

    /**
     * Checks if a weapon has stat scaling.
     */
    public boolean hasScaling(int weaponId) {
        if (scalingData.containsKey(weaponId)) return true;

        Integer parentWeaponId = projectileToWeaponMap.get(weaponId);
        if (parentWeaponId != null && scalingData.containsKey(parentWeaponId)) {
            return true;
        }

        return projectileScalingData.containsKey(weaponId);
    }

    /**
     * Calculates the stat modifier bonus for a weapon based on player stats.
     */
    public int calculateStatBonus(int weaponId, Entity player) {
        return calculateStatBonus(weaponId, null, player);
    }

    /**
     * Calculates the stat modifier bonus for a weapon.
     * If statSnapshot is non-null, that value will be used instead of current player stat.
     */
    public int calculateStatBonus(
        int weaponId,
        Integer statSnapshot,
        Entity player
    ) {
        AbilityScalingData data = getScalingData(weaponId);
        if (data == null || !data.hasScaling() || player == null) {
            return 0;
        }

        Integer statValue = (statSnapshot != null)
            ? statSnapshot
            : getPlayerStatValue(player, data.scalingStat);
        if (statValue == null || statValue <= data.scalingMin) {
            return 0;
        }

        int statBonus = statValue - data.scalingMin;
        float baseDamage = statBonus * data.damagePerStat;
        float statDamageMultiplier = getStatDamageMultiplier(player, weaponId);

        return (int) (baseDamage * statDamageMultiplier);
    }

    private float getStatDamageMultiplier(Entity player, int weaponId) {
        Integer abilitySlot1ItemId = getPlayerStatValue(
            player,
            StatType.INVENTORY_1_STAT
        );

        if (abilitySlot1ItemId != null && abilitySlot1ItemId == weaponId) {
            String[] enchantStrings = ParseEnchants.getEnchantStrings(player);
            if (enchantStrings != null && enchantStrings.length > 1) {
                String abilityEnchantString = enchantStrings[1];
                if (
                    abilityEnchantString != null &&
                    !abilityEnchantString.isEmpty()
                ) {
                    return ParseEnchants.getStatDamageMultiplier(
                        abilityEnchantString
                    );
                }
            }
        }
        return 1.0f;
    }

    private Integer getPlayerStatValue(Entity player, StatType statType) {
        if (player.stat.get(statType) == null) {
            return null;
        }
        return player.stat.get(statType).statValue;
    }

    /**
     * Clears all cached data.
     */
    public void clear() {
        scalingData.clear();
        projectileToWeaponMap.clear();
        projectileScalingData.clear();
    }

    /**
     * Gets the number of abilities with scaling data.
     */
    public int getScalingAbilityCount() {
        return scalingData.size();
    }
}
