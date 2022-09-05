package bugfixingtools;

import tomato.damagecalc.DpsLogger;
import packets.Packet;
import packets.PacketType;
import packets.data.StatData;
import packets.incoming.DamagePacket;
import packets.incoming.NewTickPacket;
import packets.incoming.TextPacket;
import packets.incoming.UpdatePacket;
import packets.reader.BufferReader;
import util.IdToName;
import util.Pair;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Map.Entry.comparingByValue;

/**
 * Test class for checking dps calculations. Please ignore.
 */
public class DamageSimulator {

    public static final String TEXT_RESET = "\u001B[0m";
    public static final String TEXT_BLACK = "\u001B[30m";
    public static final String TEXT_RED = "\u001B[31m";
    public static final String TEXT_GREEN = "\u001B[32m";
    public static final String TEXT_YELLOW = "\u001B[33m";
    public static final String TEXT_BLUE = "\u001B[34m";
    public static final String TEXT_PURPLE = "\u001B[35m";
    public static final String TEXT_CYAN = "\u001B[36m";
    public static final String TEXT_WHITE = "\u001B[37m";

    public static void main(String[] args) {
        try {
            new DamageSimulator().readfile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static ArrayList<BufferReader> injectData = new ArrayList<>();
    public DpsLogger dpsLogger = new DpsLogger();
    HashMap<Integer, Entity> entityList = new HashMap<>();
    long serverTime = 0;
    long serverFirstTime = 0;
    private HashMap<Integer, Integer> crystalList = new HashMap<>();

    public void readfile() throws Exception {
//        String fileName = "dpsLogs/outdated/Oryx's_Sanctuary.dmgLog-2022-07-25-11.12.35.data";
//        String fileName = "dpsLogs/outdated/Oryx's_Sanctuary.dmgLog-2022-07-25-16.30.50.data";
//        String fileName = "dpsLogs/outdated/Oryx's Sanctuary-2022-08-08-01.47.43.data";
//        String fileName = "dpsLogs/outdated/Oryx's Sanctuary-2022-08-08-10.43.45.data";
//        String fileName = "dpsLogs/outdated/The Shatters-2022-08-12-13.20.43.data";
//        String fileName = "dpsLogs/Oryx's Sanctuary-2022-08-14-03.22.45.data";
//        String fileName = "dpsLogs/ppxkow.data";
//        String fileName = "dpsLogs/s.rotmg-2022-09-01-01.03.12.data";
        String fileName = "dpsLogs/s.rotmg-2022-09-01-01.17.21.data";
        File f = new File(fileName);

        BufferedReader br = new BufferedReader(new FileReader(f));
        String line;

        System.out.println("clearconsole");
        while ((line = br.readLine()) != null) {
            byte[] data = getByteArray(line);
            if (data.length > 5) {
                BufferReader pData = new BufferReader(ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN));
                int size = pData.readInt();
                if (size != data.length) throw new RuntimeException("Incorrect size");

                int type = pData.readByte();
                if (type == -1) {
//                    processHooks(pData, data);
                    continue;
                }
                Packet packet = PacketType.getPacket(type).factory();
                packet.deserialize(pData);

                dpsLogger.packetCapture(packet, false);
//                packetCapture(packet);
            }
        }

//        for (Entity e : entityList.values()) {
//            String s = e.showInv(serverFirstTime, serverTime);
//            if (!s.equals("")) System.out.println(s);
//        }

//        dpsLogger.addToLogs();
//        System.out.println(dpsLogger.stringDmg(dpsLogger.displayList(), null));
    }

    private Entity getEntity(int id) {
        if (entityList.containsKey(id)) {
            return entityList.get(id);
        }
        Entity e = new Entity(id);
        entityList.put(id, e);
        return e;
    }

    int count = 0;
    long markerTime = 0;

    public void packetCapture(Packet packet) {
        if (packet instanceof DamagePacket) {
            DamagePacket p = (DamagePacket) packet;
            if (p.damageAmount > 0) {
                if (p.targetId == 1181 && count < 3) {
                    count++;
//                    System.out.print("\ndmg: " + p.damageAmount);
                }
            }
        }
        if (packet instanceof NewTickPacket) {
            NewTickPacket p = (NewTickPacket) packet;
            serverTime = p.serverRealTimeMS;
            if (serverFirstTime == 0) serverFirstTime = serverTime;
            for (int j = 0; j < p.status.length; j++) {
                int id = p.status[j].objectId;
                StatData[] stats = p.status[j].stats;
//                if (id == player.id) {
//                    player.setStats(stats);
//                    continue;
//                }
                Entity entity = getEntity(id);
                entity.setStats(stats, serverTime);
//                if (8776 == id || id == 8189 || id == 11962) {
                o3phases(stats, entity);

//                if (entity.objectType == 9635) { // Dammah
                if (entity.objectType == 29039) { // Shat king
                    if (markerTime == 0) markerTime = serverTime;
                    for (StatData sd : stats) {
                        if (sd.statTypeNum == 0 || sd.statTypeNum == 1 || sd.statTypeNum == 126 || sd.statTypeNum == 96)
                            continue;
                        if (sd.statTypeNum == 29) {
                            System.out.printf("%s  %.2f min", sd, (float) (serverTime - markerTime) / 60000);
                            continue;
                        }

                        if(sd.statValue == -123818367 && floorPlanCrystals() == 12) {
                            System.out.println("------------------- wallgarden reflector ------------------ " + (float) (serverTime - markerTime) / 1000);
                        }
                        count = 0;
                        System.out.printf("%s  %.2f sec", sd, (float) (serverTime - markerTime) / 1000);

                    }
                }
                float time = ((float) (serverTime - markerTime) / 1000);
//                if (entity.toString().contains("Crystal") && time < 500) {
//                    System.out.println(entity + " " + entity.objectType + " " + p.status[j] + " " + time);
//                }

//                for (int k = 0; k < 256; k++) {
//                    if (k >= 8 && k <= 11 && entity.getStat(k) != null) {
//                        int itemID = entity.getStat(k).statValue;
//                        System.out.println(IdToName.getIdName(itemID));
//                    }
//                }
            }
        } else if (packet instanceof UpdatePacket) {
            UpdatePacket p = (UpdatePacket) packet;
            for (int j = 0; j < p.newObjects.length; j++) {
                int id = p.newObjects[j].status.objectId;
                StatData[] stats = p.newObjects[j].status.stats;
//                if (id == player.id) {
//                    player.setStats(stats);
//                    continue;
//                }
                int objectType = p.newObjects[j].objectType;
                Entity entity = getEntity(id);
                float time = ((float) (serverTime - markerTime) / 1000);
//                if(time < 300) System.out.println(entity + " " + time);
                entity.setType(objectType);
                entity.setStats(stats, serverTime);
                crystalTracker(id, objectType);
//                if (8776 == id || id == 8189) {
//                    System.out.printf("%s %d %d\n", entity, objectType, id);
//                }
//                if (10251 == id) {
//                    System.out.printf("%s %d %d\n", entity, objectType, id);
//                    //O3 Counter Condition 45538 10251
//                }
//                if (objectType == 45363) {
//                    System.out.printf("%s %d %d\n", entity, objectType, id);
//                }
                if (objectType == 9635) {
//                    System.out.printf("%s %d %d\n", entity, objectType, id);
                }
//                if(entity.toString().equals("Shatters King")) System.out.printf("%s %d %d\n", entity, objectType, id);
//                if (objectType == 33119) {
//                    System.out.printf("%s %d %d %.1f min\n", entity, objectType, id, (float)(serverTime - serverFirstTime)/60000);
//                }
//                if(entity.toString().equals("Chancellor Dammah")) {
//                    System.out.printf("%s %d %d\n", entity, objectType, id);
//                    System.out.println(IdToName.name(objectType));
//                    System.out.println(IdToName.getDisplayName(objectType));
//                    System.out.print(IdToName.getIdName(objectType));
//                    for (StatData sd : stats) {
//                        System.out.print(sd);
//                    }
//                    System.out.println();
//                }
                if (objectType == 33445) {
//                    System.out.println(p);
//                    System.out.printf("%s %d %d\n", entity, objectType, id);
//                    System.out.printf("%s %d %d\n", entity, objectType, id);
                }
            }
            for (int j = 0; j < p.drops.length; j++) {
//                if (p.drops[j] == 1287) System.out.println(p.drops[j]);
                crystalTracker(p.drops[j], 0);
            }
//            if (floorPlanCrystals() != 0) System.out.println(floorPlanCrystals());
        } else if (packet instanceof TextPacket) {
            TextPacket p = (TextPacket) packet;
//            if (p.objectId == 10245 && !p.text.equals("")) {
            System.out.println();
            System.out.println(p.text + " " + p.objectId);
//            }
            if (p.objectId == 1181 && !p.text.equals("")) {
//                System.out.println();
//                System.out.println(p.text + " " + p.objectId);
            }
        }
    }

    private void o3phases(StatData[] stats, Entity entity) {
        if (entity.objectType == 453630) {
            for (StatData sd : stats) {
                if (sd.statTypeNum == 0 || sd.statTypeNum == 1 || sd.statTypeNum == 29 || sd.statTypeNum == 96)
                    continue;
                if (sd.statTypeNum == 125) {
                    System.out.print(sd);
                    if (sd.statValue == -409726348) System.out.print("\nBlack Cosmos");
                    if (sd.statValue == -935464302) System.out.print("\nBlack GUARD");
                    if (sd.statValue == -443134491) System.out.print("\nBlack Splendor");
                    if (sd.statValue == -901909064) System.out.print("\nBlack Stagger");
                    if (sd.statValue == 1804374907) System.out.print("\nBlack neutral");
                    if (sd.statValue == -868353826) System.out.print("\nBlack Slashes");
                    if (sd.statValue == 1821299621) System.out.print("\nBlack Melts");
                    if (sd.statValue == 1519449574) System.out.print("\nBlack Run");
                    if (sd.statValue == -727250676) System.out.print("\nBlack Stationary");
                    if (sd.statValue == 1955520573) System.out.print("\nBlack Outer");


                    if (sd.statValue == -392948729) System.out.print("\nWhite Cosmos");
                    if (sd.statValue == -918686683) System.out.print("\nWhite GUARD");
                    if (sd.statValue == -476689729) System.out.print("\nWhite Splendor");
                    if (sd.statValue == -885131445) System.out.print("\nWhite Staggered");
                    if (sd.statValue == 1888263002) System.out.print("\nWhite Celest Guarded");
                    if (sd.statValue == 488135007) System.out.print("\nWhite Celest Damageable");
                    if (sd.statValue == 1905040621) System.out.print("\nWhite Celest Staggered");
                    if (sd.statValue == -784465731) System.out.print("\nWhite Neutral");
                    if (sd.statValue == 1485894336) System.out.print("\nWhite Melt");
                    if (sd.statValue == -645598752) System.out.print("\nWhite Fate");
                    if (sd.statValue == -376862658) System.out.print("\nWhite Fleeing");
                    if (sd.statValue == -527861229) System.out.print("\nWhite Panic");
                    if (sd.statValue == 1553004812) System.out.print("\nWhite Run");
                    if (sd.statValue == -578341181) System.out.print("\nWhite Control");
                    if (sd.statValue == 1888410097) System.out.print("\nWhite Outer");
                    if (sd.statValue == -560577824) System.out.print("\nWhite Inner");
                    if (sd.statValue == -511230705) System.out.print("\nWhite Crumple");
                    if (sd.statValue == -851576207) System.out.print("\nWhite Slashes");
                    if (sd.statValue == -375729825) System.out.print("\nWhite Stationary");

//                            System.out.println(sd.statValue - val);
//                            val = sd.statValue;
                }
//                        if (sd.statTypeNum == 126) System.out.print(sd);
            }
        }
    }

    /**
     * Adds shatters crystal entities to a short list to track floor pattern in shatters king fight.
     *
     * @param id   id of the entity.
     * @param type type of the entity.
     */
    private void crystalTracker(int id, int type) { // blue, yellow, red, green crystal IDs in that order.
        if (type == 46721 || type == 46771 || type == 29501 || type == 33656) {
            crystalList.put(id, type);
        } else {
            crystalList.remove(id);
        }
    }

    /**
     * Determines the floor pattern in shatters king fight.
     *
     * Blue   == 1
     * Yellow == 2
     * Red    == 4
     * Green  == 8
     *
     * @return Gives a mask id indicating the crystal colors in the king fight.
     */
    private int floorPlanCrystals() {
        int mask = 0;
        for (Integer m : crystalList.values()) {
            if (m == 46721) {
                mask |= 1;
            } else if (m == 46771) {
                mask |= 2;
            } else if (m == 29501) {
                mask |= 4;
            } else if (m == 33656) {
                mask |= 8;
            }
        }
        return mask;
    }

    public static void processHooks(BufferReader pData, byte[] data) {
        int pack = pData.readByte();
        if (pack == 0) {
            injectData.add(pData);
            int dmg = decodeInt(data, 6);
            int time = decodeInt(data, 10);
            int eff1 = decodeInt(data, 14);
            int eff2 = decodeInt(data, 18);
            int id = decodeShort(data, 22);
            System.out.printf("----dmg:%d time:%d id:%d, eff1:%x eff2:%x\n", dmg, time, id, eff1, eff2);
//            Bullet b = new Bullet(dmg, time, id, eff1, eff2);
//            logBullets.add(b);
        } else if (pack == 1) {
            int rr = decodeInt(data, 6);
            int ef = decodeInt(data, 10);
            int id = decodeShort(data, 14);
//            System.out.printf("rng:%d id:%d seed:%d dif:%d\n", rr, id, rrr, (rr - rrr));
//            System.out.printf("ef:%x\n", ef);
//            rrr = randy.next();
        }
    }

    public static int decodeInt(byte[] bytes, int offset) {
        return (Byte.toUnsignedInt(bytes[3 + offset]) << 24) | (Byte.toUnsignedInt(bytes[2 + offset]) << 16) | (Byte.toUnsignedInt(bytes[1 + offset]) << 8) | Byte.toUnsignedInt(bytes[offset]);
    }

    public static int decodeShort(byte[] bytes, int offset) {
        return ((Byte.toUnsignedInt(bytes[1 + offset]) << 8) | Byte.toUnsignedInt(bytes[offset]));
    }

    public static byte[] getByteArray(String byteString) {
        String[] list = new String[0];

        int type = 0;
        if (byteString.contains("Hex stream")) {
            type = 1;
            list = byteString.replace("  Hex stream: ", "").split(" ");
        } else if (byteString.contains("[")) {
            type = 2;
            int starts = byteString.indexOf('[');
            int ends = byteString.indexOf(']');
            if (starts != -1 && ends != -1)
                list = byteString.substring(starts, ends).replaceAll("[\\[\\] ]", "").split(",");
        } else {
            type = 3;
            list = new String[byteString.length() / 2];
            for (int i = 0; i < byteString.length(); i += 2) {
                String sub = byteString.substring(i, i + 2);
                list[i / 2] = sub;
            }
        }
        byte[] b = new byte[list.length];
        for (int i = 0; i < list.length; i++) {
            String s = list[i];
            if (type == 1) {
                b[i] = (byte) ((Character.digit(s.charAt(0), 16) << 4) + Character.digit(s.charAt(1), 16));
            } else if (type == 2) {
                b[i] = Byte.parseByte(s);
            } else if (type == 3) {
                b[i] = (byte) ((Character.digit(s.charAt(0), 16) << 4) + Character.digit(s.charAt(1), 16));
            }
        }
        return b;
    }

    private static class Bullet {
        Packet packet;
        PacketType type;
        int totalDmg;
        boolean armorPiercing;

        public Bullet(Packet p, PacketType t) {
            packet = p;
            type = t;
        }
    }

    private static class Entity {

        private final int id;
        private int objectType = -1;
        private final StatData[] stats = new StatData[256];
        private final Bullet[] bulletDmg = new Bullet[512];

        private final ArrayList<Pair<StatData, Long>>[] inv = new ArrayList[4];

        public Entity(int id) {
            this.id = id;
            for (int i = 0; i < inv.length; i++) {
                inv[i] = new ArrayList<>();
            }
        }

        public void setBullet(short bulletId, Bullet bullet) {
            bulletDmg[bulletId] = bullet;
        }

        public Bullet getBullet(short p) {
            return bulletDmg[p];
        }

        public void setStats(StatData[] stats, long serverTime) {
            for (StatData sd : stats) {
                if (sd.statTypeNum >= 8 && sd.statTypeNum <= 11) {
                    inv[sd.statTypeNum - 8].add(new Pair(sd, serverTime));
                }
                this.stats[sd.statTypeNum] = sd;
            }
        }

        public StatData getStat(int id) {
            return stats[id];
        }

        public void setType(int objectType) {
            this.objectType = objectType;
        }

        public int maxHp() {
            if (stats[0] == null) return 0;
            return stats[0].statValue;
        }

        public String showInv(long firstServertime, long endServerTime) {
            if (stats[31] == null) return "";
            String s = "";
            for (int inventory = 0; inventory < 4; inventory++) {
                s += "<";

                if (inv[inventory].size() == 0) {
                    s += "  ";
                } else if (inv[inventory].size() == 1) {
                    s += String.format("%s %.1fsec %s\n", IdToName.name(inv[inventory].get(0).left().statValue), (float) (endServerTime - firstServertime) / 1000, "100% Equipped:1 ");
                } else {
                    HashMap<Integer, Equipment> gear = new HashMap<>();
                    Pair<StatData, Long> pair2 = null;
                    long firstTime = 0;
                    boolean sameItem = false;
                    for (int i = 1; i < inv[inventory].size(); i++) {
                        Pair<StatData, Long> pair1 = inv[inventory].get(i - 1);
                        pair2 = inv[inventory].get(i);
                        if (pair1.left().statValue == pair2.left().statValue) sameItem = true;
                        else sameItem = false;
                        long time1 = pair1.right();
                        if (time1 == 0) time1 = firstServertime;
                        if (firstTime == 0) firstTime = time1;
                        addGear(gear, time1, pair2.right(), pair1.left().statValue, sameItem);
                    }
                    long totalTime = endServerTime - firstTime;
                    addGear(gear, pair2.right(), endServerTime, pair2.left().statValue, false);

                    Stream<Map.Entry<Integer, Equipment>> sorted2 = gear.entrySet().stream().sorted(comparingByValue());
                    for (Map.Entry<Integer, Equipment> m : sorted2.collect(Collectors.toList())) {
                        s += String.format("%s %.1fsec %.2f%% Equipped:%d ,", IdToName.name(m.getKey()), ((float) m.getValue().time / 1000), ((float) m.getValue().time * 100 / totalTime), m.getValue().swaps);
                    }
                }
                s = s.substring(0, s.length() - 2);
                s += "> ";
            }
            return s.substring(0, s.length() - 1);
        }

        private void addGear(HashMap<Integer, Equipment> gear, long time1, long time2, int itemID, boolean sameItem) {
            if (!gear.containsKey(itemID)) {
                Equipment e = new Equipment(itemID, time2 - time1, sameItem ? 0 : 1);
                gear.put(itemID, e);
            } else {
                Equipment e = gear.get(itemID);
                e.time += time2 - time1;
                e.swaps++;
            }
        }

        @Override
        public String toString() {
            return IdToName.name(objectType);
        }
    }

    private static class Equipment implements Comparable {
        int id;
        long time;
        int swaps;

        public Equipment(int id, long time, int swaps) {
            this.id = id;
            this.time = time;
            this.swaps = swaps;
        }

        @Override
        public int compareTo(Object o) {
            return (int) (time - ((Equipment) o).time);
        }
    }
}