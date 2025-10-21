package tomato.gui.dps;

import assets.IdToAsset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import packets.incoming.MapInfoPacket;
import packets.incoming.NotificationPacket;
import tomato.backend.data.Damage;
import tomato.backend.data.Entity;
import tomato.backend.data.Equipment;
import tomato.backend.data.PlayerRemoved;
import tomato.gui.dps.shared.DeathParser;
import tomato.gui.dps.shared.DpsTextFormat;
import tomato.gui.dps.shared.EquipmentUsageAggregator;
import tomato.gui.dps.shared.GuardsHandler;
import tomato.realmshark.enums.CharacterClass;

/**
 * Builds the string-based DPS view used in the "logs" tab.
 *
 * Behavior preserved:
 * - Header and per-entity sections
 * - Player filter/highlight and "me" indicator
 * - Extra notes (guarded/dammah/garden) + death/nexus info
 * - Equipment:
 *   - option 0: hidden
 *   - option 1: bracketed "[slot0 / slot1 / slot2 / slot3]" showing most-used item per slot
 *   - option 2: per-slot line with slash-separated items in that slot, sorted by descending %
 *     - Single-item slot shows "100%"
 *     - Tiny non-zero percentages show "< 0.1%"
 */
public class DpsToString {

    /**
     * Real time string display.
     *
     * @return logged dps output as a string.
     */
    public static String stringDmgRealtime(
        MapInfoPacket map,
        List<Entity> sortedEntityHitList,
        ArrayList<NotificationPacket> notifications,
        Entity player,
        long totalDungeonPcTime
    ) {
        StringBuilder sb = new StringBuilder();

        if (DpsDisplayOptions.equipmentOption == 3) {
            sb.append(
                "Icons are not visible in live tab. Use \"<\" to see icons.\n\n"
            );
        }

        if (map != null) {
            sb
                .append(map.name)
                .append(" ")
                .append(DpsGUI.systemTimeToString(totalDungeonPcTime))
                .append("\n\n");
        }

        Map<String, Integer> deathMap = DeathParser.parseDeathsToMap(
            notifications
        );

        for (Entity e : sortedEntityHitList) {
            if (!isValidEntity(e)) continue;

            EquipmentUsageAggregator eqAgg =
                (DpsDisplayOptions.equipmentOption == 0)
                    ? null
                    : EquipmentUsageAggregator.of(e);

            sb.append(display(e, deathMap, player, eqAgg)).append("\n");
        }

        return sb.toString();
    }

    /**
     * Renders a single entity section.
     */
    public static String display(
        Entity entity,
        Map<String, Integer> deathMap,
        Entity player,
        EquipmentUsageAggregator eqAgg
    ) {
        if (entity == null) return "";

        StringBuilder sb = new StringBuilder();

        // Entity header + table header
        sb.append(buildEntityHeader(entity));

        List<Damage> playerDamageList = entity.getPlayerDamageList();
        int counter = 0;

        for (Damage dmg : playerDamageList) {
            if (dmg == null || dmg.owner == null) continue;

            counter++;

            // Filter logic
            int filterDecision = Filter.filter(dmg.owner, player);
            if (Filter.shouldFilter() && filterDecision != 1) continue;

            boolean highlight = (filterDecision == 2);

            // Name cleanup and prefix
            String rawName = dmg.owner.getStatName();
            if (rawName == null) continue;
            String name = cleanName(rawName);

            String prefix = formatUserPrefix(dmg, highlight);

            // Damage contribution vs mob HP
            float percentOfMob = safePercent(dmg.damage, entity.maxHp());

            // Extra tag (guarded/dammah/garden), or 4 spaces
            String extra = GuardsHandler.buildExtraTag(entity, dmg);
            if (extra.isEmpty()) extra = "    ";

            // Death/Nexus info, if available
            if (entity.playerDropped != null) {
                PlayerRemoved pr = entity.playerDropped.get(dmg.owner.id);
                if (pr != null) {
                    boolean dead =
                        deathMap != null && deathMap.containsKey(name);
                    float hpPct = safePercent(pr.hp, pr.max);
                    extra +=
                        (dead ? "Died " : "Nexus ") +
                        DpsTextFormat.percent2(hpPct) +
                        "% [" +
                        DpsTextFormat.grouped(pr.hp) +
                        " / " +
                        DpsTextFormat.grouped(pr.max) +
                        "]";
                }
            }

            // Equipment
            String inv = "";
            if (DpsDisplayOptions.equipmentOption != 0 && eqAgg != null) {
                int ownerId = dmg.owner.id;
                if (DpsDisplayOptions.equipmentOption == 1) {
                    inv = formatEquipmentOption1(eqAgg, ownerId);
                } else if (DpsDisplayOptions.equipmentOption == 2) {
                    inv = formatEquipmentOption2(eqAgg, ownerId);
                }
            }

            // Row
            sb.append(prefix).append(' ');
            DpsTextFormat.appendPaddedInt(sb, counter, 3);
            sb.append("  ");
            DpsTextFormat.appendPaddedRight(sb, name, 10);
            sb.append(" DMG: ");
            DpsTextFormat.appendPaddedInt(sb, dmg.damage, 7);
            sb
                .append(' ')
                .append(DpsTextFormat.percent3(percentOfMob))
                .append("% ")
                .append(extra);
            if (!inv.isEmpty()) {
                sb.append(' ').append(inv);
            }
            sb.append('\n');
        }

        sb.append("\n");
        return sb.toString();
    }

    // === Helpers ========================================================================

    private static boolean isValidEntity(Entity e) {
        return (
            e != null &&
            e.maxHp() > 0 &&
            !CharacterClass.isPlayerCharacter(e.objectType)
        );
    }

    private static String buildEntityHeader(Entity entity) {
        StringBuilder sb = new StringBuilder();
        sb
            .append(entity.name())
            .append(" HP: ")
            .append(entity.maxHp())
            .append(entity.getFightTimerString())
            .append("\n")
            .append("    #   Player      DMG         % \n")
            .append("    -----------------------------------------------\n");
        return sb.toString();
    }

    private static String cleanName(String statName) {
        int index = statName.indexOf(',');
        return (index != -1) ? statName.substring(0, index) : statName;
    }

    private static String formatUserPrefix(Damage dmg, boolean highlight) {
        if (dmg.owner.isUser() && DpsDisplayOptions.showMe) return " ->";
        return highlight ? ">>>" : "   ";
    }

    private static float safePercent(int part, int total) {
        if (total <= 0) return 0f;
        return ((float) part * 100f) / (float) total;
    }

    private static String formatEquipmentOption1(
        EquipmentUsageAggregator eqAgg,
        int ownerId
    ) {
        StringBuilder s = new StringBuilder("[");
        for (int i = 0; i < EquipmentUsageAggregator.SLOT_COUNT; i++) {
            if (i != 0) s.append(" / ");
            Equipment max = eqAgg.getMostUsedItem(ownerId, i);
            int id = (max != null) ? max.id : 0;
            s.append(IdToAsset.objectName(id));
        }
        s.append("]");
        return s.toString();
    }

    private static String formatEquipmentOption2(
        EquipmentUsageAggregator eqAgg,
        int ownerId
    ) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < EquipmentUsageAggregator.SLOT_COUNT; i++) {
            s.append("\n       ");
            Collection<Equipment> list = eqAgg.getSlotBreakdown(ownerId, i);
            int total = eqAgg.getSlotTotalDamage(ownerId, i);

            // Sort by descending contribution
            List<Equipment> sorted = new ArrayList<>(list);
            sorted.sort((a, b) -> Integer.compare(b.dmg, a.dmg));

            boolean first = true;
            int size = sorted.size();

            for (Equipment e2 : sorted) {
                if (size > 1) {
                    if (!first) s.append(" /");
                    double pct = (total > 0) ? (100.0 * e2.dmg) / total : 0.0;
                    s
                        .append(' ')
                        .append(
                            DpsTextFormat.formatPercentWithTinyThreshold(pct)
                        )
                        .append(' ');
                } else {
                    // Single item => 100%
                    s.append(" 100% ");
                }
                s.append(IdToAsset.objectName(e2.id));
                first = false;
            }
        }
        return s.toString();
    }
}
