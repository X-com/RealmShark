package tomato.gui.dps.shared;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import tomato.backend.data.Damage;
import tomato.backend.data.Entity;
import tomato.backend.data.Equipment;

/**
 * Aggregates equipment usage per entity (mob) and per owner (player) across a fight.
 *
 * This centralizes the logic previously duplicated in multiple DPS UIs. It processes
 * all {@link Damage} events for a given {@link Entity} exactly once, and builds a
 * per-owner, per-slot breakdown of equipment contributions. Each slot's total damage
 * is tracked so consumers can compute percentage splits without rescanning the list.
 *
 * Semantics:
 * - "Most used item" for a slot is the item with the highest total damage contribution.
 * - Slot totals are the sum of all damage attributed to that slot for the owner.
 *
 * Notes:
 * - This class performs no UI formatting; it only aggregates data.
 * - Equipment id 0 (empty) is included if present in the damage stream, matching legacy behavior.
 * - The "enchant" string is carried through from the damage events (if provided).
 */
public final class EquipmentUsageAggregator {

    public static final int SLOT_COUNT = 4;

    /**
     * Aggregated usage data for a single equipment slot.
     * items: itemId -> Equipment { id, enchant, dmg, count, totalDmg (slot total) }
     * total: total damage across all items for this slot (AtomicInteger to match Equipment.totalDmg contract)
     */
    public static final class SlotUsage {

        public final Map<Integer, Equipment> items = new HashMap<>();
        public final AtomicInteger total = new AtomicInteger(0);
    }

    /**
     * Aggregated usage data for a single owner (player), across all 4 equipment slots.
     */
    public static final class OwnerUsage {

        public final SlotUsage[] slots = new SlotUsage[] {
            new SlotUsage(),
            new SlotUsage(),
            new SlotUsage(),
            new SlotUsage(),
        };
    }

    private final Map<Integer, OwnerUsage> byOwner = new HashMap<>();

    /**
     * Build an aggregator for a given entity (mob) by processing its damage list once.
     */
    public EquipmentUsageAggregator(Entity entity) {
        aggregate(entity);
    }

    /**
     * Factory method equivalent to new EquipmentUsageAggregator(entity).
     */
    public static EquipmentUsageAggregator of(Entity entity) {
        return new EquipmentUsageAggregator(entity);
    }

    /**
     * Returns the aggregated usage for a specific owner id, or null if absent.
     */
    public OwnerUsage getOwnerUsage(int ownerId) {
        return byOwner.get(ownerId);
    }

    /**
     * Returns the most-used item (by total damage) for the given owner and slot, or null if none.
     */
    public Equipment getMostUsedItem(int ownerId, int slotIndex) {
        OwnerUsage ou = byOwner.get(ownerId);
        if (ou == null || !isValidSlot(slotIndex)) return null;
        Collection<Equipment> values = ou.slots[slotIndex].items.values();
        if (values.isEmpty()) return null;
        return values
            .stream()
            .max(Comparator.comparingInt(e -> e.dmg))
            .orElse(null);
    }

    /**
     * Returns an unmodifiable collection of item usages for the given owner and slot.
     */
    public Collection<Equipment> getSlotBreakdown(int ownerId, int slotIndex) {
        OwnerUsage ou = byOwner.get(ownerId);
        if (
            ou == null || !isValidSlot(slotIndex)
        ) return Collections.emptyList();
        return Collections.unmodifiableCollection(
            ou.slots[slotIndex].items.values()
        );
    }

    /**
     * Returns the total damage attributed to the given owner's slot.
     */
    public int getSlotTotalDamage(int ownerId, int slotIndex) {
        OwnerUsage ou = byOwner.get(ownerId);
        if (ou == null || !isValidSlot(slotIndex)) return 0;
        return ou.slots[slotIndex].total.get();
    }

    /**
     * Returns the set of owner ids that have recorded equipment usage.
     */
    public Set<Integer> owners() {
        return Collections.unmodifiableSet(byOwner.keySet());
    }

    /**
     * Returns whether the aggregator contains no data.
     */
    public boolean isEmpty() {
        return byOwner.isEmpty();
    }

    private void aggregate(Entity entity) {
        if (entity == null) return;

        // Process all damage entries once to build per-owner, per-slot usage.
        for (Damage d : entity.getDamageList()) {
            if (
                d == null || d.owner == null || d.ownerInvntory == null
            ) continue;

            final int ownerId = d.owner.id;
            OwnerUsage ou = byOwner.computeIfAbsent(ownerId, k ->
                new OwnerUsage()
            );

            for (int i = 0; i < SLOT_COUNT; i++) {
                final int fi = i;
                SlotUsage su = ou.slots[fi];

                // Use inventory snapshot stored on the Damage
                int itemId = d.ownerInvntory[fi];

                // Ensure Equipment.totalDmg points to the slot total accumulator.
                Equipment eq = su.items.computeIfAbsent(itemId, id ->
                    new Equipment(
                        id,
                        String.valueOf(d.ownerEnchants[fi]),
                        su.total
                    )
                );
                eq.add(d.damage);
            }
        }
    }

    private static boolean isValidSlot(int slotIndex) {
        return slotIndex >= 0 && slotIndex < SLOT_COUNT;
    }
}
