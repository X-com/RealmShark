package tomato.backend.data;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import packets.data.enums.StatType;
import tomato.backend.data.Entity;

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
        public final float ignoreFlat;
        public final float ignorePerc;
        public final float statModPerc;

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
            this.ignoreFlat = 0;
            this.ignorePerc = 0;
            this.statModPerc = 0;
        }

        public AbilityScalingData(
            int weaponId,
            StatType scalingStat,
            int scalingMin,
            float damagePerStat,
            int numShots,
            float ignoreFlat,
            float ignorePerc,
            float statModPerc
        ) {
            this.weaponId = weaponId;
            this.scalingStat = scalingStat;
            this.scalingMin = scalingMin;
            this.damagePerStat = damagePerStat;
            this.numShots = numShots;
            this.ignoreFlat = ignoreFlat;
            this.ignorePerc = ignorePerc;
            this.statModPerc = statModPerc;
        }

        public boolean hasScaling() {
            return scalingStat != null && damagePerStat > 0;
        }

        public boolean hasDefenseIgnore() {
            return ignoreFlat > 0 || ignorePerc > 0;
        }
    }

    private AbilityScalingManager() {
        // Private constructor for singleton
    }

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
            // System.out.println(
            //     "Looking for equip.xml at: " + equipFile.getAbsolutePath()
            // );
            if (!equipFile.exists()) {
                // System.err.println(
                //     "equip.xml not found at: " + equipFile.getAbsolutePath()
                // );
                return;
            }
            // System.out.println("Found equip.xml, starting parsing...");

            DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(equipFile);
            document.getDocumentElement().normalize();

            parseEquipXml(document);

            // Add automatic Lethal Strike projectile scaling
            addLethalStrikeProjectileScaling(document);

            // System.out.println(
            //     "AbilityScalingManager initialized with " +
            //         scalingData.size() +
            //         " abilities"
            // );

            // Debug: Print first few abilities found
            // int count = 0;
            // for (Map.Entry<
            //     Integer,
            //     AbilityScalingData
            // > entry : scalingData.entrySet()) {
            //     if (count++ < 5) {
            //         AbilityScalingData data = entry.getValue();
            //         System.out.println(
            //             "Found ability: ID=" +
            //                 data.weaponId +
            //                 ", stat=" +
            //                 data.scalingStat +
            //                 ", min=" +
            //                 data.scalingMin +
            //                 ", dmgPerStat=" +
            //                 data.damagePerStat
            //         );
            //     }
            // }
        } catch (Exception e) {
            System.err.println(
                "Failed to initialize AbilityScalingManager: " + e.getMessage()
            );
            e.printStackTrace();
        }
    }

    /**
     * Parses the equip.xml document and extracts ability scaling data.
     */
    private void parseEquipXml(Document document) {
        NodeList objectNodes = document.getElementsByTagName("Object");

        for (int i = 0; i < objectNodes.getLength(); i++) {
            Node objectNode = objectNodes.item(i);
            if (objectNode.getNodeType() == Node.ELEMENT_NODE) {
                Element objectElement = (Element) objectNode;

                try {
                    // Get weapon ID from type attribute (hex format like "0x2062")
                    String typeAttr = objectElement.getAttribute("type");
                    if (typeAttr.isEmpty()) continue;

                    int weaponId = Integer.parseInt(
                        typeAttr.replace("0x", ""),
                        16
                    );

                    // Check if this is a projectile object and map it to its weapon
                    String projectileId = objectElement.getAttribute("id");
                    if (
                        projectileId.contains("Proj") ||
                        projectileId.contains("Missile") ||
                        projectileId.contains("Proc")
                    ) {
                        // This is likely a projectile object, try to find its parent weapon
                        mapProjectileToWeapon(objectElement, weaponId);
                    }

                    // Look for Activate elements with stat scaling
                    NodeList activateNodes = objectElement.getElementsByTagName(
                        "Activate"
                    );
                    // Also look for OnConditionEndActivate elements with stat scaling
                    NodeList onConditionEndActivateNodes =
                        objectElement.getElementsByTagName(
                            "OnConditionEndActivate"
                        );

                    // Debug: Check if this is the specific containerType we're looking for
                    // if (weaponId == 17966) {
                    //     System.out.println(
                    //         "DEBUG: Found object with ID 17966 (0x462E): " +
                    //             objectElement.getAttribute("id")
                    //     );
                    // }

                    // Check Activate elements
                    for (int j = 0; j < activateNodes.getLength(); j++) {
                        Element activateElement = (Element) activateNodes.item(
                            j
                        );

                        // Check if this Activate element has stat scaling attributes
                        String scalingStatAttr = activateElement.getAttribute(
                            "scalingStat"
                        );
                        String statModDamageAttr = activateElement.getAttribute(
                            "statModDamage"
                        );
                        String statModFlatAttr = activateElement.getAttribute(
                            "statModFlat"
                        );
                        String statModPercAttr = activateElement.getAttribute(
                            "statModPerc"
                        );

                        // Handle different scaling formulas:
                        // - statModDamage: +X damage per stat over min
                        // - statModFlat + statModPerc: flat + percentage scaling
                        if (!scalingStatAttr.isEmpty()) {
                            StatType scalingStat = parseStatType(
                                scalingStatAttr
                            );
                            int scalingMin = parseScalingMin(activateElement);
                            int numShots = parseNumShots(activateElement);

                            if (scalingStat != null) {
                                float damagePerStat = 0;
                                String scalingType = "unknown";

                                if (!statModDamageAttr.isEmpty()) {
                                    // Standard scaling: +X damage per stat
                                    damagePerStat = Float.parseFloat(
                                        statModDamageAttr
                                    );
                                    scalingType = "per_stat";
                                } else if (
                                    !statModFlatAttr.isEmpty() &&
                                    !statModPercAttr.isEmpty()
                                ) {
                                    // Proc scaling: flat + percentage
                                    float flatBonus = Float.parseFloat(
                                        statModFlatAttr
                                    );
                                    float percBonus = Float.parseFloat(
                                        statModPercAttr
                                    );
                                    // For proc scaling, we'll use a simplified approach
                                    // In a complete implementation, this would need the base damage
                                    damagePerStat =
                                        flatBonus + (percBonus * 100); // Estimate
                                    scalingType = "proc";
                                }

                                if (damagePerStat > 0) {
                                    AbilityScalingData data =
                                        new AbilityScalingData(
                                            weaponId,
                                            scalingStat,
                                            scalingMin,
                                            damagePerStat,
                                            numShots
                                        );
                                    scalingData.put(weaponId, data);
                                    // System.out.println(
                                    //     "Added scaling for weapon " +
                                    //         weaponId +
                                    //         " (" +
                                    //         typeAttr +
                                    //         "): " +
                                    //         scalingStatAttr +
                                    //         " scaling, +" +
                                    //         damagePerStat +
                                    //         " per stat over " +
                                    //         scalingMin +
                                    //         ", numShots=" +
                                    //         numShots +
                                    //         ", type=" +
                                    //         scalingType
                                    // );

                                    // Debug: Log specifically for containerType 17966
                                    // if (weaponId == 17966) {
                                    //     System.out.println(
                                    //         "DEBUG: Successfully added scaling for containerType 17966!"
                                    //     );
                                    // }
                                    break; // Use first valid scaling found
                                }
                            }
                        }
                    }

                    // Check OnConditionEndActivate elements
                    for (
                        int j = 0;
                        j < onConditionEndActivateNodes.getLength();
                        j++
                    ) {
                        Element activateElement =
                            (Element) onConditionEndActivateNodes.item(j);

                        // Check if this OnConditionEndActivate element has stat scaling attributes
                        String scalingStatAttr = activateElement.getAttribute(
                            "scalingStat"
                        );
                        String statModDamageAttr = activateElement.getAttribute(
                            "statModDamage"
                        );
                        String statModFlatAttr = activateElement.getAttribute(
                            "statModFlat"
                        );
                        String statModPercAttr = activateElement.getAttribute(
                            "statModPerc"
                        );

                        // Handle different scaling formulas:
                        // - statModDamage: +X damage per stat over min
                        // - statModFlat + statModPerc: flat + percentage scaling
                        if (!scalingStatAttr.isEmpty()) {
                            StatType scalingStat = parseStatType(
                                scalingStatAttr
                            );
                            int scalingMin = parseScalingMin(activateElement);
                            int numShots = parseNumShots(activateElement);

                            if (scalingStat != null) {
                                float damagePerStat = 0;
                                String scalingType = "unknown";

                                if (!statModDamageAttr.isEmpty()) {
                                    // Standard scaling: +X damage per stat
                                    damagePerStat = Float.parseFloat(
                                        statModDamageAttr
                                    );
                                    scalingType = "per_stat";
                                } else if (
                                    !statModFlatAttr.isEmpty() &&
                                    !statModPercAttr.isEmpty()
                                ) {
                                    // Proc scaling: flat + percentage
                                    float flatBonus = Float.parseFloat(
                                        statModFlatAttr
                                    );
                                    float percBonus = Float.parseFloat(
                                        statModPercAttr
                                    );
                                    // For proc scaling, we'll use a simplified approach
                                    // In a complete implementation, this would need the base damage
                                    damagePerStat =
                                        flatBonus + (percBonus * 100); // Estimate
                                    scalingType = "proc";
                                }

                                if (damagePerStat > 0) {
                                    AbilityScalingData data =
                                        new AbilityScalingData(
                                            weaponId,
                                            scalingStat,
                                            scalingMin,
                                            damagePerStat,
                                            numShots
                                        );
                                    scalingData.put(weaponId, data);
                                    // System.out.println(
                                    //     "Added scaling for weapon " +
                                    //         weaponId +
                                    //         " (" +
                                    //         typeAttr +
                                    //         "): " +
                                    //         scalingStatAttr +
                                    //         " scaling, +" +
                                    //         damagePerStat +
                                    //         " per stat over " +
                                    //         scalingMin +
                                    //         ", numShots=" +
                                    //         numShots +
                                    //         ", type=" +
                                    //         scalingType +
                                    //         " (OnConditionEndActivate)"
                                    // );

                                    // Debug: Log specifically for containerType 17966
                                    // if (weaponId == 17966) {
                                    //     System.out.println(
                                    //         "DEBUG: Successfully added scaling for containerType 17966!"
                                    //     );
                                    // }
                                    break; // Use first valid scaling found
                                }
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    // Skip objects with invalid IDs
                    continue;
                }
            }
        }
    }

    /**
     * Parses the stat type from XML attribute string.
     */
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

    /**
     * Parses the scaling minimum stat value from Activate element.
     */
    private int parseScalingMin(Element activateElement) {
        String scalingMinAttr = activateElement.getAttribute(
            "statModScalingMin"
        );
        if (!scalingMinAttr.isEmpty()) {
            try {
                return Integer.parseInt(scalingMinAttr);
            } catch (NumberFormatException e) {
                // Use default if parsing fails
            }
        }
        return 50; // Default scaling minimum
    }

    /**
     * Parses the number of shots from Activate element.
     */
    private int parseNumShots(Element activateElement) {
        String numShotsAttr = activateElement.getAttribute("numShots");
        if (!numShotsAttr.isEmpty()) {
            try {
                return Integer.parseInt(numShotsAttr);
            } catch (NumberFormatException e) {
                // Use default if parsing fails
            }
        }
        return 1; // Default to single shot
    }

    /**
     * Maps projectile objects to their parent weapons.
     */
    private void mapProjectileToWeapon(
        Element projectileElement,
        int projectileId
    ) {
        // Look for the parent weapon by checking if this projectile is referenced
        // in any weapon's Activate element with type attribute
        String projectileType =
            "0x" + Integer.toHexString(projectileId).toUpperCase();

        // For now, we'll use a simple heuristic: if this is a projectile object,
        // we'll need to find which weapon uses it by checking Activate elements
        // This would require more complex XML traversal to be fully accurate
        // System.out.println(
        //     "Found projectile object: " +
        //         projectileId +
        //         " (" +
        //         projectileType +
        //         "): " +
        //         projectileElement.getAttribute("id")
        // );

        // TODO: Implement proper projectile-to-weapon mapping
        // This would require parsing all weapons and finding which ones reference this projectile
    }

    /**
     * Automatically detect and add scaling for all Lethal Strike proc projectiles.
     * Finds items with OnConditionEndActivate containing LethalStrike and maps their projectiles.
     */
    private void addLethalStrikeProjectileScaling(Document document) {
        // System.out.println("Scanning for Lethal Strike items...");

        NodeList objectNodes = document.getElementsByTagName("Object");
        int lethalStrikeCount = 0;

        for (int i = 0; i < objectNodes.getLength(); i++) {
            Node objectNode = objectNodes.item(i);
            if (objectNode.getNodeType() == Node.ELEMENT_NODE) {
                Element objectElement = (Element) objectNode;

                // Look for OnConditionEndActivate elements with LethalStrike
                NodeList lethalStrikeNodes = objectElement.getElementsByTagName(
                    "OnConditionEndActivate"
                );
                for (int j = 0; j < lethalStrikeNodes.getLength(); j++) {
                    Element lethalStrikeElement =
                        (Element) lethalStrikeNodes.item(j);

                    // Check if this is a LethalStrike activation
                    if (
                        lethalStrikeElement
                            .getTextContent()
                            .contains("LethalStrike")
                    ) {
                        String scalingStatAttr =
                            lethalStrikeElement.getAttribute("scalingStat");
                        String statModFlatAttr =
                            lethalStrikeElement.getAttribute("statModFlat");
                        String statModPercAttr =
                            lethalStrikeElement.getAttribute("statModPerc");
                        String ignoreFlatAttr =
                            lethalStrikeElement.getAttribute("ignoreFlat");
                        String ignorePercAttr =
                            lethalStrikeElement.getAttribute("ignorePerc");

                        if (
                            !scalingStatAttr.isEmpty() &&
                            !statModFlatAttr.isEmpty()
                        ) {
                            // Found a Lethal Strike item - now find its projectile types
                            StatType scalingStat = parseStatType(
                                scalingStatAttr
                            );
                            int scalingMin = parseScalingMin(
                                lethalStrikeElement
                            );
                            float flatBonus = Float.parseFloat(statModFlatAttr);
                            float percBonus = !statModPercAttr.isEmpty()
                                ? Float.parseFloat(statModPercAttr)
                                : 0;
                            float ignoreFlat = !ignoreFlatAttr.isEmpty()
                                ? Float.parseFloat(ignoreFlatAttr)
                                : 0;
                            float ignorePerc = !ignorePercAttr.isEmpty()
                                ? Float.parseFloat(ignorePercAttr)
                                : 0;

                            // For Lethal Strike, store defense ignore values for proper scaling
                            // For Lethal Strike, use actual stat scaling values
                            // flatBonus = statModFlat, percBonus = statModPerc
                            // These provide additional flat damage per stat above scalingMin
                            float damagePerStat = flatBonus + (percBonus * 50); // Estimate for typical stat values

                            // Find projectile types used by this item
                            findAndAddLethalStrikeProjectiles(
                                objectElement,
                                scalingStat,
                                scalingMin,
                                damagePerStat,
                                ignoreFlat,
                                ignorePerc,
                                percBonus
                            );
                            lethalStrikeCount++;
                        }
                    }
                }
            }
        }

        // System.out.println(
        //     "Found " + lethalStrikeCount + " Lethal Strike items with scaling"
        // );
    }

    /**
     * Find projectile types used by a Lethal Strike item and add their scaling data.
     */
    private void findAndAddLethalStrikeProjectiles(
        Element weaponElement,
        StatType scalingStat,
        int scalingMin,
        float damagePerStat,
        float ignoreFlat,
        float ignorePerc,
        float statModPerc
    ) {
        String weaponId = weaponElement.getAttribute("type");
        String weaponName = weaponElement.getAttribute("id");

        if (weaponId.isEmpty()) return;

        try {
            int weaponIdInt = Integer.parseInt(weaponId.replace("0x", ""), 16);

            // Look for OnPlayerShootActivate elements to find projectile types
            NodeList shootActivateNodes = weaponElement.getElementsByTagName(
                "OnPlayerShootActivate"
            );
            for (int i = 0; i < shootActivateNodes.getLength(); i++) {
                Element shootElement = (Element) shootActivateNodes.item(i);
                String projectileType = shootElement.getAttribute("type");

                if (!projectileType.isEmpty()) {
                    try {
                        int projectileId = Integer.parseInt(
                            projectileType.replace("0x", ""),
                            16
                        );

                        // Add scaling for this projectile type
                        AbilityScalingData projectileScaling =
                            new AbilityScalingData(
                                projectileId,
                                scalingStat,
                                scalingMin,
                                damagePerStat,
                                1, // Lethal Strike typically creates 1 projectile per activation
                                ignoreFlat,
                                ignorePerc,
                                statModPerc // statModPerc for defense ignore scaling
                            );
                        projectileScalingData.put(
                            projectileId,
                            projectileScaling
                        );

                        // System.out.println(
                        //     "Added Lethal Strike scaling for projectile " +
                        //         projectileId +
                        //         " (0x" +
                        //         Integer.toHexString(projectileId) +
                        //         ") from " +
                        //         weaponName +
                        //         ": " +
                        //         scalingStat +
                        //         " scaling, +" +
                        //         damagePerStat +
                        //         " per stat over " +
                        //         scalingMin
                        // );
                    } catch (NumberFormatException e) {
                        // Skip invalid projectile types
                    }
                }
            }
        } catch (NumberFormatException e) {
            // Skip invalid weapon IDs
        }
    }

    /**
     * Gets scaling data for a specific weapon ID.
     */
    public AbilityScalingData getScalingData(int weaponId) {
        // First try the weapon ID directly
        AbilityScalingData data = scalingData.get(weaponId);
        if (data != null) {
            return data;
        }

        // If not found, check if this is a projectile ID mapped to a weapon
        Integer parentWeaponId = projectileToWeaponMap.get(weaponId);
        if (parentWeaponId != null) {
            return scalingData.get(parentWeaponId);
        }

        // Check if this is a projectile with direct scaling data
        if (projectileScalingData.containsKey(weaponId)) {
            return projectileScalingData.get(weaponId);
        }

        return null;
    }

    /**
     * Checks if a weapon has stat scaling.
     */
    public boolean hasScaling(int weaponId) {
        boolean hasScaling = scalingData.containsKey(weaponId);

        // Debug: Log scaling check for specific container types
        // if (weaponId == 17966) {
        //     System.out.println(
        //         "DEBUG: hasScaling check for containerType 17966: " + hasScaling
        //     );
        //     if (hasScaling) {
        //         AbilityScalingData data = scalingData.get(weaponId);
        //         System.out.println(
        //             "DEBUG: Scaling data for 17966 - stat: " +
        //                 data.scalingStat +
        //                 ", min: " +
        //                 data.scalingMin +
        //                 ", dmgPerStat: " +
        //                 data.damagePerStat
        //         );
        //     }
        // }

        if (hasScaling) {
            return true;
        }

        // Check if this is a projectile ID mapped to a weapon with scaling
        Integer parentWeaponId = projectileToWeaponMap.get(weaponId);
        if (parentWeaponId != null && scalingData.containsKey(parentWeaponId)) {
            return true;
        }

        // Check if this is a projectile with direct scaling data
        return projectileScalingData.containsKey(weaponId);
    }

    /**
     * Calculates the stat modifier bonus for a weapon based on player stats.
     */
    public int calculateStatBonus(int weaponId, Entity player) {
        AbilityScalingData data = getScalingData(weaponId);
        if (data == null || !data.hasScaling() || player == null) {
            return 0;
        }

        // Get the relevant stat from player
        Integer statValue = getPlayerStatValue(player, data.scalingStat);
        if (statValue == null || statValue <= data.scalingMin) {
            return 0;
        }

        // Calculate bonus: (stat - min) × damage per stat
        int statBonus = statValue - data.scalingMin;
        return (int) (statBonus * data.damagePerStat);
    }

    /**
     * Calculates the defense ignore bonus for Lethal Strike based on target defense and player stats.
     */
    public int calculateDefenseIgnoreBonus(
        int weaponId,
        int targetDefense,
        Entity player
    ) {
        AbilityScalingData data = getScalingData(weaponId);
        if (
            data == null ||
            !data.hasDefenseIgnore() ||
            targetDefense <= 0 ||
            player == null
        ) {
            return 0;
        }

        // Get the relevant stat from player for percentage scaling
        Integer statValue = getPlayerStatValue(player, data.scalingStat);
        if (statValue == null || statValue <= data.scalingMin) {
            // Use base ignorePerc if player doesn't meet stat requirements
            int defenseIgnore = (int) (data.ignoreFlat +
                (targetDefense * data.ignorePerc));
            return Math.max(0, defenseIgnore);
        }

        // Calculate scaled ignore percentage: base ignorePerc + (statBonus * statModPerc)
        int statBonus = statValue - data.scalingMin;
        float scaledIgnorePerc =
            data.ignorePerc + (statBonus * data.statModPerc);

        // Calculate defense ignore: ignoreFlat + (targetDefense * scaledIgnorePerc)
        int defenseIgnore = (int) (data.ignoreFlat +
            (targetDefense * scaledIgnorePerc));
        return Math.max(0, defenseIgnore);
    }

    /**
     * Gets the value of a specific stat from the player entity.
     */
    private Integer getPlayerStatValue(Entity player, StatType statType) {
        if (player.stat.get(statType) == null) {
            return null;
        }
        return player.stat.get(statType).statValue;
    }

    /**
     * Clears all cached data (for testing or reloading).
     */
    public void clear() {
        scalingData.clear();
    }

    /**
     * Gets the number of abilities with scaling data.
     */
    public int getScalingAbilityCount() {
        return scalingData.size();
    }
}
