package tomato.realmshark;

import com.google.gson.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Stack;
import java.util.concurrent.Semaphore;
import packets.data.StatData;
import packets.data.WorldPosData;
import packets.data.enums.StatType;
import packets.incoming.MapInfoPacket;
import tomato.backend.data.Entity;
import tomato.backend.data.TomatoData;
import tomato.version.Version;

public class SendLoot {

    private static WebSocket webSocket;

    private static Semaphore sem = new Semaphore(0);

    private static Stack<byte[]> stack = new Stack<>();
    private static int currentTickSeed = -1;

    /**
     * Called at the start of each loot tick (from TomatoData).
     * Flushes any pending overflow bag from the previous tick before setting the new tick seed.
     */
    public static void beginLootTick(int tickSeed) {
        flushPendingOverflow();
        currentTickSeed = tickSeed;
    }

    /**
     * Flush a pending full bag that never received an overflow partner.
     */
    public static void flushPendingOverflow() {
        if (pendingFullBag == null) return;

        PendingBag pb = pendingFullBag;

        JsonObject flush = new JsonObject();
        flush.addProperty("bag", pb.bagId);
        flush.addProperty("pos", String.format("%f,%f", pb.x, pb.y));
        flush.addProperty("dung", pb.dungeon);
        if (pb.mods != null) {
            flush.add("mods", pb.mods);
        } else {
            flush.add("mods", new JsonArray());
        }
        if (pb.mobOverride != null) {
            flush.addProperty("mob", pb.mobOverride);
        } else {
            flush.addProperty("mob", pb.mob);
        }
        flush.addProperty("share", pb.sharedLoot);
        flush.add("items", pb.items);
        flush.addProperty("exalt", pb.exaltBonus);
        flush.addProperty("ld", pb.lootDrop);
        flush.addProperty("seas", pb.isSeasonal);
        flush.addProperty("cruc", pb.crucible);
        flush.addProperty("lben", pb.lootEnchant);
        flush.addProperty("ver", Version.VERSION);

        byte[] out = flush.toString().getBytes(StandardCharsets.UTF_8);
        stack.push(out);
        sem.release();
        pendingFullBag = null;
    }

    // ----------------- Overflow / Merge Configuration -----------------

    // Maximum squared distance (tiles^2) to consider two bags part of one logical drop (2 tiles -> 4 distance squared)

    // (Distance check removed; merging is now unconditional within the loot tick)

    // Dungeons with special attribution logic where we DO NOT merge bags automatically

    private static final java.util.Set<String> SPECIAL_ATTRIBUTION_DUNGEONS =
        new java.util.HashSet<>(
            java.util.Arrays.asList(
                "The Shatters",
                "Oryx's Sanctuary",
                "Moonlight Village"
            )
        );

    // Pending full bag waiting for potential overflow partner
    private static PendingBag pendingFullBag;

    private static class PendingBag {

        int bagId;

        double x;

        double y;

        String dungeon;

        String mobOverride;

        int mob;

        int sharedLoot;

        JsonArray items;

        JsonArray mods;
        int exaltBonus;

        boolean lootDrop;

        boolean isSeasonal;

        int crucible;

        float lootEnchant;

        long created;

        int tickSeed;
    }

    static {
        try {
            webSocket = new WebSocket("ws://38.45.66.65:3008");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        sendLoop();
    }

    public static void sendLoot(
        TomatoData data,
        MapInfoPacket map,
        Entity bag,
        Entity dropper,
        Entity player,
        long time
    ) {
        webSocket.con();

        int bagId = -1;
        WorldPosData pos = bag.pos;
        String dungeon = "";
        JsonArray mods = new JsonArray();
        int mob = -1;
        int sharedLoot = -1;
        JsonArray items = new JsonArray();
        int exaltBonus = -1;
        boolean lootDrop = false;
        boolean isSeasonal = false;
        int cruc = 0;

        if (bag != null) {
            bagId = bag.objectType;

            String[] enchants = null;
            StatData udata = bag.stat.get(StatType.UNIQUE_DATA_STRING);
            if (udata != null && udata.stringStatValue != null) {
                enchants = udata.stringStatValue.split(",");
            }

            for (int i = 0; i < 8; i++) {
                StatData sd = bag.stat.get(StatType.INVENTORY_0_STAT.get() + i);

                if (sd == null || sd.statValue < 1) continue;

                JsonObject item = new JsonObject();

                item.addProperty("id", sd.statValue);

                int sl = 0;

                if (
                    enchants != null &&
                    i < enchants.length &&
                    !enchants[i].isEmpty() &&
                    !enchants[i].equals("AAIE_f_9__3__f8=")
                ) {
                    String enchantText = ParseEnchants.parse(enchants[i]);

                    if (!enchantText.isEmpty()) {
                        sl = Math.min(4, enchantText.split("\n").length);
                    }
                    // NOTE: the enchant ping is deliberately NOT fired here.
                    // This method is the loot-sharing uploader and only runs
                    // when sharing is enabled, which made a local sound alert
                    // depend on opting into telemetry. LootGUI owns the ping.
                }

                item.addProperty("sl", sl);

                items.add(item);
            }
        }

        float lootEnch = 0f;

        if (player != null) {
            exaltBonus = RealmCharacter.exaltLootBonus(player.objectType);

            lootDrop = player.lootDropTime(time) > 0;

            cruc = player.isCrucible() ? 1 : 0;

            StatData sesn = player.stat.get(StatType.SEASONAL.get());

            if (sesn != null) {
                if (sesn.statValue == 1) {
                    isSeasonal = true;
                }
            }

            // Aggregate total loot bonus percent from player enchantments
            String[] playerEnchantCodes = ParseEnchants.getEnchantStrings(
                player
            );
            lootEnch = ParseEnchants.getTotalLootBonusPercent(
                playerEnchantCodes
            );
        }

        if (map != null) {
            dungeon = map.name;
            int[] dungeonMods = ParseDungeon.getModIds(
                ParseDungeon.getModifiersString(map)
            );
            for (int i = 0; i < dungeonMods.length; i++) {
                mods.add(dungeonMods[i]);
            }
            if (dungeon.equals("Moonlight Village")) {
                int flames = data.getMoonlightFlameCount();
                if (flames > 0) {
                    mods.add("Flames:" + flames);
                }
            }
        }

        String mobOverride = null;
        if (dropper != null) {
            mob = dropper.objectType;

            sharedLoot = dropper.playersRemainAtKill();

            if (
                dropper.lootMobIdOverride != null &&
                !dropper.lootMobIdOverride.isEmpty()
            ) {
                mobOverride = dropper.lootMobIdOverride;
            }
        }

        // ------------------ Bag Merge (Overflow) Handling ------------------
        // Goal: Some dungeons can drop a "second" bag very near the first because the first
        // filled its 8 item slots. For statistics we want to treat the two physical bags
        // as ONE logical drop if:

        //  - Dungeon is not in SPECIAL_ATTRIBUTION_DUNGEONS

        //  - First bag was full (8 items)

        //  - Second bag (next bag in same loot tick) will always merge; distance ignored

        //  - No special mob override attribution (mobOverride == null)

        //

        // Implementation: When we encounter a full bag, we hold it briefly (pendingFullBag)
        // instead of sending immediately. If another bag appears meeting the criteria,
        // we merge their item arrays and send a single combined entry (using the first bag's
        // position / metadata). If no suitable second bag arrives before timeout, we flush
        // the pending bag as-is.
        long now = System.currentTimeMillis();

        // Removed stale flush logic: no waiting window required

        boolean specialDungeon = SPECIAL_ATTRIBUTION_DUNGEONS.contains(dungeon);

        boolean canAttemptMerge = !specialDungeon && mobOverride == null;

        boolean isFullBag = items.size() >= 8;

        if (canAttemptMerge) {
            if (pendingFullBag == null) {
                // Always hold the first bag in the tick (even if not full) so that
                // if a second bag (or this one) is/was full we can merge overflow.
                pendingFullBag = new PendingBag();

                pendingFullBag.bagId = bagId;

                pendingFullBag.x = pos.x;

                pendingFullBag.y = pos.y;

                pendingFullBag.dungeon = dungeon;

                pendingFullBag.mobOverride = mobOverride;

                pendingFullBag.mob = mob;

                pendingFullBag.sharedLoot = sharedLoot;

                pendingFullBag.items = items.deepCopy();

                pendingFullBag.mods = mods.deepCopy();

                pendingFullBag.exaltBonus = exaltBonus;

                pendingFullBag.lootDrop = lootDrop;

                pendingFullBag.isSeasonal = isSeasonal;

                pendingFullBag.crucible = cruc;

                pendingFullBag.lootEnchant = lootEnch;

                pendingFullBag.created = now;

                pendingFullBag.tickSeed = currentTickSeed;

                // Do NOT send yet — wait to see next bag in this tick

                return;
            } else if (pendingFullBag != null) {
                // If tick changed, flush old pending before handling this bag

                if (pendingFullBag.tickSeed != currentTickSeed) {
                    flushPendingOverflow();

                    // Start new pending with current (regardless of fullness)
                    pendingFullBag = new PendingBag();

                    pendingFullBag.bagId = bagId;

                    pendingFullBag.x = pos.x;

                    pendingFullBag.y = pos.y;

                    pendingFullBag.dungeon = dungeon;

                    pendingFullBag.mobOverride = mobOverride;

                    pendingFullBag.mob = mob;

                    pendingFullBag.sharedLoot = sharedLoot;

                    pendingFullBag.items = items.deepCopy();

                    pendingFullBag.mods = mods.deepCopy();

                    pendingFullBag.exaltBonus = exaltBonus;

                    pendingFullBag.lootDrop = lootDrop;

                    pendingFullBag.isSeasonal = isSeasonal;

                    pendingFullBag.crucible = cruc;

                    pendingFullBag.lootEnchant = lootEnch;

                    pendingFullBag.created = now;

                    pendingFullBag.tickSeed = currentTickSeed;

                    return;
                }

                // Merge / flush decision (same tick):
                if (
                    pendingFullBag != null &&
                    pendingFullBag.tickSeed == currentTickSeed
                ) {
                    boolean pendingWasFull = pendingFullBag.items.size() >= 8;
                    // Merge if either bag is full (overflow scenario)
                    if (pendingWasFull || isFullBag) {
                        JsonArray mergedItems = new JsonArray();
                        for (int i = 0; i < pendingFullBag.items.size(); i++) {
                            mergedItems.add(pendingFullBag.items.get(i));
                        }
                        for (int i = 0; i < items.size(); i++) {
                            mergedItems.add(items.get(i));
                        }
                        JsonObject merged = new JsonObject();
                        merged.addProperty("bag", pendingFullBag.bagId); // keep first bag id
                        merged.addProperty(
                            "pos",
                            String.format(
                                "%f,%f",
                                pendingFullBag.x,
                                pendingFullBag.y
                            )
                        );
                        merged.addProperty("dung", pendingFullBag.dungeon);
                        merged.add("mods", mods);
                        if (pendingFullBag.mobOverride != null) {
                            merged.addProperty(
                                "mob",
                                pendingFullBag.mobOverride
                            );
                        } else {
                            merged.addProperty("mob", pendingFullBag.mob);
                        }
                        merged.addProperty("share", pendingFullBag.sharedLoot);
                        merged.add("items", mergedItems);
                        merged.addProperty("exalt", pendingFullBag.exaltBonus);
                        merged.addProperty("ld", pendingFullBag.lootDrop);
                        merged.addProperty("seas", pendingFullBag.isSeasonal);
                        merged.addProperty("cruc", pendingFullBag.crucible);
                        merged.addProperty("lben", pendingFullBag.lootEnchant);
                        merged.addProperty("ver", Version.VERSION);
                        byte[] mout = merged
                            .toString()
                            .getBytes(StandardCharsets.UTF_8);
                        /*
                        System.out.println(
                            "[" +
                                new Date() +
                                "] Queued merged loot data: " +
                                merged.toString()
                                ); */
                        stack.push(mout);
                        sem.release();
                        pendingFullBag = null;
                        return;
                    } else {
                        // Neither bag full -> send pending as normal, hold current as new pending
                        JsonObject flush = new JsonObject();
                        flush.addProperty("bag", pendingFullBag.bagId);
                        flush.addProperty(
                            "pos",
                            String.format(
                                "%f,%f",
                                pendingFullBag.x,
                                pendingFullBag.y
                            )
                        );
                        flush.addProperty("dung", pendingFullBag.dungeon);
                        if (pendingFullBag.mods != null) {
                            flush.add("mods", pendingFullBag.mods);
                        } else {
                            flush.add("mods", new JsonArray());
                        }
                        if (pendingFullBag.mobOverride != null) {
                            flush.addProperty(
                                "mob",
                                pendingFullBag.mobOverride
                            );
                        } else {
                            flush.addProperty("mob", pendingFullBag.mob);
                        }
                        flush.addProperty("share", pendingFullBag.sharedLoot);
                        flush.add("items", pendingFullBag.items);
                        flush.addProperty("exalt", pendingFullBag.exaltBonus);
                        flush.addProperty("ld", pendingFullBag.lootDrop);
                        flush.addProperty("seas", pendingFullBag.isSeasonal);
                        flush.addProperty("cruc", pendingFullBag.crucible);
                        flush.addProperty("lben", pendingFullBag.lootEnchant);
                        flush.addProperty("ver", Version.VERSION);
                        byte[] pout = flush
                            .toString()
                            .getBytes(StandardCharsets.UTF_8);
                        /* System.out.println(
                                "[" +
                                    new Date() +
                                    "] Queued merged loot data (flush): " +
                                    flush.toString()
                            ); */
                        stack.push(pout);
                        sem.release();
                        // Start new pending with current bag
                        pendingFullBag = new PendingBag();
                        pendingFullBag.bagId = bagId;
                        pendingFullBag.x = pos.x;
                        pendingFullBag.y = pos.y;
                        pendingFullBag.dungeon = dungeon;
                        pendingFullBag.mobOverride = mobOverride;
                        pendingFullBag.mob = mob;
                        pendingFullBag.sharedLoot = sharedLoot;
                        pendingFullBag.items = items.deepCopy();
                        pendingFullBag.mods = mods.deepCopy();
                        pendingFullBag.exaltBonus = exaltBonus;
                        pendingFullBag.lootDrop = lootDrop;
                        pendingFullBag.isSeasonal = isSeasonal;
                        pendingFullBag.crucible = cruc;
                        pendingFullBag.lootEnchant = lootEnch;
                        pendingFullBag.created = now;
                        pendingFullBag.tickSeed = currentTickSeed;
                        return;
                    }
                }
            } // end else-if (pendingFullBag != null)
        } // end canAttemptMerge

        // ------------------ Normal (non-merged) send path ------------------
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("bag", bagId);
        jsonObject.addProperty("pos", String.format("%f,%f", pos.x, pos.y));
        jsonObject.addProperty("dung", dungeon);
        jsonObject.add("mods", mods);

        if (mobOverride != null) {
            jsonObject.addProperty("mob", mobOverride);
        } else {
            jsonObject.addProperty("mob", mob);
        }

        jsonObject.addProperty("share", sharedLoot);
        jsonObject.add("items", items);
        jsonObject.addProperty("exalt", exaltBonus);
        jsonObject.addProperty("ld", lootDrop);
        jsonObject.addProperty("seas", isSeasonal);
        jsonObject.addProperty("cruc", cruc);
        jsonObject.addProperty("lben", lootEnch);
        jsonObject.addProperty("ver", Version.VERSION);

        byte[] out = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
        /*
        System.out.println(
            "[" +
                new Date() +
                "] Queued loot data for WebSocket: " +
                jsonObject.toString()
        );
        */
        stack.push(out);
        sem.release();
    }

    private static void sendLoop() {
        new Thread(() -> {
            while (true) {
                try {
                    sem.acquire();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                while (stack.size() > 0) {
                    byte[] out = stack.pop();

                    // Log the exact JSON string being sent
                    String payload = new String(out, StandardCharsets.UTF_8);
                    /*
                    System.out.println(
                        "[" +
                            new Date() +
                            "] WebSocket Sending: " + payload
                    );
                    */
                    webSocket.sendBytes(out);
                }
            }
        })
            .start();
    }

    public static void main(String[] args) {
        webSocket.con();
        //        String s = "{\"bagId\":1287,\"dungeon\":\"Spider Den\",\"dungeonMods\":\"BONUSCONSUMABLES;ENERGIZEDMINIONS_1;|D\",\"mob\":2358,\"sharedLoot\":1,\"items\":\"1799:2773[UT - Engraving: RELATIVE_SPEED_BONUS_1(320)]:2655:2745[UT - Engraving: RELATIVE_SPEED_BONUS_1(320)]:6141:1799:1799:1799\",\"exaltBonus\":35,\"lootDrop\":false,\"isSeasonal\":true}";
        //        String jj = JSONParser.quote(s);
        //
        //        JsonObject j = new JsonObject();
        //        System.out.println(jj);

        int bagId = -1;
        WorldPosData pos = new WorldPosData();
        String dungeon = "Realm of the Mad God";
        int[] dungeonMods = ParseDungeon.getModIds(
            "BONUSCONSUMABLES;ENERGIZEDMINIONS_1;|D"
        );
        int mob = 17735;
        int sharedLoot = 1;
        int exaltBonus = 35;
        boolean lootDrop = false;
        boolean isSeasonal = true;

        JsonObject jsonObject = new JsonObject();
        JsonArray items = new JsonArray();
        for (int i = 0; i < 8; i++) {
            JsonObject item = new JsonObject();
            item.addProperty("id", 1234);
            items.add(item);
        }

        jsonObject.addProperty("bag", bagId);
        jsonObject.addProperty("pos", String.format("%s,%s", pos.x, pos.y));
        jsonObject.addProperty("dung", dungeon);
        JsonArray mods = new JsonArray();
        for (int i = 0; i < dungeonMods.length; i++) {
            mods.add(dungeonMods[i]);
        }
        jsonObject.add("mods", mods);

        jsonObject.addProperty("mob", mob);

        jsonObject.addProperty("share", sharedLoot);

        jsonObject.add("items", items);

        jsonObject.addProperty("exalt", exaltBonus);
        jsonObject.addProperty("ld", lootDrop);
        jsonObject.addProperty("seas", isSeasonal);

        System.out.println(jsonObject.toString());

        //        String s = "{\"bagId\":1287,\"dungeon\":\"Realm of the Mad God\",\"dungeonMods\":\"\",\"mob\":17735,\"sharedLoot\":1,\"items\":\"2783[UT - Engraving: ONHIT_DAMAGING_1(49)]\",\"exaltBonus\":35,\"lootDrop\":false,\"isSeasonal\":true}";
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(jsonObject);
        System.out.println(jsonOutput);

        byte[] out = jsonObject.toString().getBytes(StandardCharsets.UTF_8);

        webSocket.sendBytes(out);
    }
}
