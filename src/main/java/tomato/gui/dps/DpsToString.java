package tomato.gui.dps;

import assets.IdToAsset;
import packets.incoming.MapInfoPacket;
import packets.incoming.NotificationPacket;
import tomato.backend.data.*;
import tomato.realmshark.enums.CharacterClass;
import util.Pair;

import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DpsToString {
    private TomatoData data;
    private static final DecimalFormat df = new DecimalFormat("#,###,###");

    // Batch processing and throttling components
    private final BlockingQueue<DataBundle> packetQueue = new LinkedBlockingQueue<>();
    private final ExecutorService processingExecutor = Executors.newSingleThreadExecutor();
    private volatile String lastDisplayString = "";
    private long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL_MS = 100; // 100ms update interval

    // Data sampling components
    private static final long SAMPLE_INTERVAL_MS = 50; // Sample every 50ms
    private long lastSampleTime = 0;
    private DataBundle lastSampledBundle = null;

    public DpsToString(TomatoData data) {
        this.data = data;
    }

    /**
     * Real time string display.
     *
     * @return logged dps output as a string.
     */
    public static String stringDmgRealtime(MapInfoPacket map, List<Entity> sortedEntityHitList, ArrayList<NotificationPacket> notifications, Entity player, long totalDungeonPcTime) {
        StringBuilder sb = new StringBuilder();

        if(DpsDisplayOptions.equipmentOption == 3) sb.append("Icons are not visible in live tab. Use \"<\" to see icons.\n\n");

        if (map != null) {
            sb.append(map.name).append(" ").append(DpsGUI.systemTimeToString(totalDungeonPcTime)).append("\n\n");
        }

        ArrayList<Pair<String, Integer>> deaths = new ArrayList<>();
        for (NotificationPacket n : notifications) {
            if (n.message != null) {
                String[] parts = n.message.split("\"");
                if (parts.length > 9) {
                    String name = parts[9];
                    deaths.add(new Pair<>(name, n.pictureType));
                }
            }
        }

        for (Entity e : sortedEntityHitList) {
            if (e == null || e.maxHp() <= 0 || CharacterClass.isPlayerCharacter(e.objectType)) continue;
            sb.append(display(e, deaths, player)).append("\n");
        }

        return sb.toString();
    }

    /**
     * New batch processing entry point with data sampling
     */
    public void handleNewData(MapInfoPacket map, List<Entity> sortedEntityHitList,
                              ArrayList<NotificationPacket> notifications,
                              Entity player, long totalDungeonPcTime) {
        long now = System.currentTimeMillis();

        // Only add to queue if it's time for a new sample
        if (now - lastSampleTime >= SAMPLE_INTERVAL_MS) {
            lastSampleTime = now;
            DataBundle newBundle = new DataBundle(map, new ArrayList<>(sortedEntityHitList),
                    new ArrayList<>(notifications), player, totalDungeonPcTime);

            // Merge with previous sample if needed
            if (lastSampledBundle != null) {
                newBundle = mergeBundles(lastSampledBundle, newBundle);
            }

            packetQueue.add(newBundle);
            lastSampledBundle = newBundle;
            processingExecutor.submit(this::processPackets);
        }
    }

    public String getCurrentDisplay() {
        return lastDisplayString;
    }

    private void processPackets() {
        try {
            List<DataBundle> batch = new ArrayList<>();
            packetQueue.drainTo(batch, 100);

            if (!batch.isEmpty()) {
                DataBundle aggregated = aggregateBundles(batch);

                long now = System.currentTimeMillis();
                if (now - lastUpdateTime >= UPDATE_INTERVAL_MS) {
                    lastUpdateTime = now;
                    lastDisplayString = stringDmgRealtime(
                            aggregated.map,
                            aggregated.sortedEntityHitList,
                            aggregated.notifications,
                            aggregated.player,
                            aggregated.totalDungeonPcTime
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private DataBundle aggregateBundles(List<DataBundle> bundles) {
        MapInfoPacket currentMap = null;
        List<Entity> currentEntities = new ArrayList<>();
        ArrayList<NotificationPacket> currentNotifications = new ArrayList<>();
        Entity currentPlayer = null;
        long currentTime = 0;

        for (DataBundle bundle : bundles) {
            if (bundle.map != null) currentMap = bundle.map;
            currentEntities.addAll(bundle.sortedEntityHitList);
            currentNotifications.addAll(bundle.notifications);
            if (bundle.player != null) currentPlayer = bundle.player;
            currentTime = bundle.totalDungeonPcTime;
        }

        return new DataBundle(currentMap, currentEntities, currentNotifications, currentPlayer, currentTime);
    }

    /**
     * Helper method to merge two data bundles for sampling
     */
    private DataBundle mergeBundles(DataBundle oldBundle, DataBundle newBundle) {
        // For map and player, prefer newer data if available
        MapInfoPacket map = newBundle.map != null ? newBundle.map : oldBundle.map;
        Entity player = newBundle.player != null ? newBundle.player : oldBundle.player;

        // For time, use the latest
        long time = Math.max(newBundle.totalDungeonPcTime, oldBundle.totalDungeonPcTime);

        // For entities and notifications, combine both
        List<Entity> combinedEntities = new ArrayList<>(oldBundle.sortedEntityHitList);
        combinedEntities.addAll(newBundle.sortedEntityHitList);

        ArrayList<NotificationPacket> combinedNotifs = new ArrayList<>(oldBundle.notifications);
        combinedNotifs.addAll(newBundle.notifications);

        return new DataBundle(map, combinedEntities, combinedNotifs, player, time);
    }

    public static String showInv(int equipmentFilter, Entity owner, Entity entity) {
        if (equipmentFilter == 0 || owner == null || owner.getStatName() == null) return "";

        HashMap<Integer, Equipment>[] inv = new HashMap[4];

        for (int i = 0; i < 4; i++) {
            AtomicInteger tot = new AtomicInteger(0);
            inv[i] = new HashMap<>();
            for (Damage d : entity.getDamageList()) {
                if (d.owner == null || d.owner.id != owner.id || d.ownerInvntory == null) continue;

                int finalI = i;
                Equipment equipment = inv[i].computeIfAbsent(d.ownerInvntory[i],
                        id -> new Equipment(id, String.valueOf(d.ownerEnchants[finalI]), tot));
                equipment.add(d.damage);
            }
        }

        if (equipmentFilter == 1) {
            StringBuilder s = new StringBuilder("[");
            for (int i = 0; i < 4; i++) {
                Equipment max = inv[i].values().stream()
                        .max(Comparator.comparingInt(e -> e.dmg))
                        .orElse(new Equipment(0, "0", new AtomicInteger(0)));
                if (i != 0) s.append(" / ");
                s.append(IdToAsset.objectName(max.id));
            }
            return s.append("]").toString();
        } else if (equipmentFilter == 2) {
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                s.append("\n       ");
                Collection<Equipment> list = inv[i].values();
                boolean first = true;
                for (Equipment e : list) {
                    if (list.size() > 1) {
                        if (!first) s.append(" /");
                        s.append(String.format(" %.1f%% ", 100f * e.dmg / e.totalDmg.get()));
                    } else {
                        s.append(" ");
                    }
                    s.append(IdToAsset.objectName(e.id));
                    first = false;
                }
            }
            return s.toString();
        }

        return "";
    }

    public static String display(Entity entity, ArrayList<Pair<String, Integer>> deaths, Entity player) {
        if (entity == null) return "";

        StringBuilder sb = new StringBuilder();
        sb.append(entity.name()).append(" HP: ").append(entity.maxHp())
                .append(entity.getFightTimerString()).append("\n");

        List<Damage> playerDamageList = entity.getPlayerDamageList();
        int counter = 0;

        for (Damage dmg : playerDamageList) {
            if (dmg == null || dmg.owner == null) continue;

            boolean highlight = false;
            counter++;
            int filter = Filter.filter(dmg.owner, player);

            if (Filter.shouldFilter() && filter != 1) continue;
            if (filter == 2) highlight = true;

            String name = dmg.owner.getStatName();
            if (name == null) continue;

            String extra = "    ";
            String isMe = (dmg.owner.isUser() && DpsDisplayOptions.showMe) ? " ->" :
                    (highlight ? ">>>" : "   ");

            int index = name.indexOf(',');
            if (index != -1) name = name.substring(0, index);

            float pers = ((float) dmg.damage * 100 / (float) entity.maxHp());

            if (dmg.oryx3GuardDmg) {
                extra = String.format("[Guarded Hits:%d Dmg:%d]", dmg.counterHits, dmg.counterDmg);
            } else if (entity.dammahCountered && dmg.chancellorDammahDmg) {
                extra = String.format("[Dammah Hits:%d Dmg:%d]", dmg.counterHits, dmg.counterDmg);
            } else if (dmg.walledGardenReflectors) {
                extra = String.format("[Garden Hits:%d Dmg:%d]", dmg.counterHits, dmg.counterDmg);
            }

            if (entity.playerDropped != null) {
                for (int id : entity.playerDropped.keySet()) {
                    if (dmg.owner.id == id) {
                        PlayerRemoved pr = entity.playerDropped.get(id);
                        boolean dead = isDeadPlayer(name, deaths);
                        extra += String.format("%s %.2f%% [%s / %s]",
                                dead ? "Died" : "Nexus",
                                ((float) pr.hp / pr.max) * 100,
                                df.format(pr.hp).replaceAll(",", " "),
                                df.format(pr.max).replaceAll(",", " "));
                    }
                }
            }

            String inv = showInv(DpsDisplayOptions.equipmentOption, dmg.owner, entity);
            sb.append(String.format("%s %3d %10s DMG: %7d %6.3f%% %s %s\n",
                    isMe, counter, name, dmg.damage, pers, extra, inv));
        }
        sb.append("\n");

        return sb.toString();
    }

    private static boolean isDeadPlayer(String name, ArrayList<Pair<String, Integer>> deaths) {
        if (name == null || deaths == null) return false;

        for (Pair<String, Integer> p : deaths) {
            if (name.equals(p.left())) return true;
        }
        return false;
    }

    private static class DataBundle {
        final MapInfoPacket map;
        final List<Entity> sortedEntityHitList;
        final ArrayList<NotificationPacket> notifications;
        final Entity player;
        final long totalDungeonPcTime;

        DataBundle(MapInfoPacket map, List<Entity> sortedEntityHitList,
                   ArrayList<NotificationPacket> notifications,
                   Entity player, long totalDungeonPcTime) {
            this.map = map;
            this.sortedEntityHitList = sortedEntityHitList;
            this.notifications = notifications;
            this.player = player;
            this.totalDungeonPcTime = totalDungeonPcTime;
        }
    }

    public void shutdown() {
        processingExecutor.shutdown();
        try {
            if (!processingExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                processingExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            processingExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}