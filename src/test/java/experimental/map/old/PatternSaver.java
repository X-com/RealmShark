package experimental.map.old;

import packets.Packet;
import packets.data.GroundTileData;
import packets.data.ObjectData;
import packets.data.StatData;
import packets.data.enums.StatType;
import packets.incoming.MapInfoPacket;
import packets.incoming.UpdatePacket;
import packets.packetcapture.PacketProcessor;
import packets.packetcapture.register.Register;
import util.Util;

import java.util.Arrays;
import java.util.HashMap;

public class PatternSaver {
    static PacketProcessor packetProcessor;
    private static boolean runSaver = false;
    private static int[][] mapTiles = new int[2048][2048];
    private static HashMap<Integer, ObjectData> allObjects = new HashMap<>();
    private static boolean saveStuff = true;
    private static long seed;

    public static void main(String[] args) {
        new PatternSaver().saveAll();
    }

    public void saveAll() {
        packetProcessor = new PacketProcessor();
        Register.INSTANCE.registerAll(PatternSaver::readAll);
        packetProcessor.start();
    }

    public static void readAll(Packet packet) {
        if (packet instanceof UpdatePacket) {
            if (!runSaver) return;
            UpdatePacket p = (UpdatePacket) packet;
            GroundTileData[] tiles = p.tiles;
            for (int i = 0; i < tiles.length; i++) {
                GroundTileData gtd = tiles[i];
                mapTiles[gtd.x][gtd.y] = gtd.type;
            }
            for (ObjectData od : p.newObjects) {
                if (shouldSkip(od)) continue;
                int id = od.status.objectId;
                if (!allObjects.containsKey(id)) {
                    if(od.status.stats.length > 4) System.out.println(od);
                    allObjects.put(id, od);
                }
            }
        } else if (packet instanceof MapInfoPacket) {
            MapInfoPacket p = (MapInfoPacket) packet;
            if (!p.displayName.equals("{s.rotmg}")) {
                runSaver = false;
                if (saveStuff) {
                    if(seed == 0) {
                        return;
                    }
                    Util.print("tiles/pattern/allObjects", "seed:" + seed);
                    Util.print("tiles/pattern/allObjects", "tiles");
                    for (int i = 0; i < 2048; i++) {
                        for (int j = 0; j < 2048; j++) {
                            int t = mapTiles[i][j];
                            if (t != 0) {
                                Util.print("tiles/pattern/allObjects", String.format("%d:%d:%d", i, j, t));
                            }
                        }
                    }

                    Util.print("tiles/pattern/allObjects", "objects");
                    for (ObjectData od : allObjects.values()) {
                        StringBuilder s = new StringBuilder();
                        for (StatData sd : od.status.stats) {
                            s.append(";").append(sd.statValue).append(";").append(sd.statValueTwo).append(";").append(sd.stringStatValue).append(";").append(sd.statTypeNum);
                        }
                        Util.print("tiles/pattern/allObjects", String.format("%d:%f:%f:%s", od.objectType, od.status.pos.x, od.status.pos.y, s.substring(1)));
                    }
                }
                System.out.println("done");
                for (int[] row : mapTiles) {
                    Arrays.fill(row, 0);
                }
                allObjects.clear();
                seed = 0;
                packetProcessor.closeSniffer();
            } else {
                for (int[] row : mapTiles) {
                    Arrays.fill(row, 0);
                }
                allObjects.clear();
                seed = p.seed;
                runSaver = true;
            }
        }
    }

    private static boolean shouldSkip(ObjectData od) {
        if(od.objectType == 290) return false; // stone wall
        if(od.objectType == 6175) return false; // tomestone
        if(od.objectType == 1755) return false; // manors stone
        if(od.objectType == 1287) return true;
        if(od.objectType == 1286) return true;
        if(od.objectType == 1280) return true;
        if(od.objectType == 1813) return true;

        if(od.objectType == 782) return true;
        if(od.objectType == 804) return true;
        for (StatData sd : od.status.stats) {
            if (sd.statType == StatType.PET_NAME_STAT) {
                return true;
            }
            if (sd.statType == StatType.NAME_CHOSEN_STAT) {
                return true;
            }
            if (sd.statType == StatType.NAME_STAT) {
                System.out.println("----skip----");
                System.out.println(od);
                return true;
            }
        }
        return od.status.stats.length > 0 && od.status.stats[0].statValue > 0;
    }
}
