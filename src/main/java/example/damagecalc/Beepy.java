package example.damagecalc;

import packets.Packet;
import packets.data.ObjectData;
import packets.data.StatData;
import packets.data.WorldPosData;
import packets.data.enums.StatType;
import packets.incoming.CreateSuccessPacket;
import packets.incoming.UpdatePacket;
import util.IdToName;
import util.Util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

public class Beepy {
    private static HashMap<Integer, DamageCalculator.Player> players = new HashMap<>();
    private static HashMap<Integer, Integer> objects = new HashMap<>();
    private static HashSet<WorldPosData> walls = new HashSet<>();
    private static boolean wallFound = false;

    public static void capturePackets(Packet packet) {
        if (packet instanceof CreateSuccessPacket) {
            DamageLogger.logDmg(packet);
            CreateSuccessPacket create = (CreateSuccessPacket) packet;
            System.out.println(create);
//            playerID = create.objectId;
//            players.clear();
            objects.clear();
            walls.clear();
            wallFound = false;
        } else if (packet instanceof UpdatePacket) {
            DamageLogger.logDmg(packet);
            UpdatePacket update = (UpdatePacket) packet;
            System.out.println(update);
            for (ObjectData o : update.newObjects) {
                String type = playerClass(o.objectType);
                int objID = o.status.objectId;
                if (type != null) {
                    String name = null;
                    for (StatData s : o.status.stats) {
                        if (s.statType == StatType.NAME_STAT) {
                            name = s.stringStatValue;
                        }
                    }
                    Player player = new Player(objID, name, type);
//                    players.put(objID, player);
                } else {
                    if (IdToName.name(o.objectType).equals("All Black Wall")) {
//                        System.out.println(objID);
//                        System.out.println(o.objectType);
//                        System.out.println(o.status.pos);
                        WorldPosData pos = o.status.pos;
                        if (!wallFound) {
                            for (WorldPosData p : walls) {
                                if ((p.x + 2 == pos.x) || (p.x - 2 == pos.x)) {
                                    if (p.y == pos.y) {
//                                System.out.println("found " + (p.x + 2 == pos.x) + " " + (p.x - 2 == pos.x));
//                                System.out.println(p.x + " " + pos.x);
                                        wallFound = true;
                                        Util.playSound();
                                    }
                                } else if ((p.y + 2 == pos.y) || (p.y - 2 == pos.y)) {
                                    if (p.x == pos.x) {
//                                System.out.println("found " + (p.y + 2 == pos.y) + " " + (p.y - 2 == pos.y));
//                                System.out.println(p + " " + pos);
                                        wallFound = true;
                                        Util.playSound();
                                    }
                                }
                            }
//                        System.out.println(pos);
                            walls.add(pos);
                        }
                    }
//                    objects.put(objID, o.objectType);
                }
            }
        }
    }

    private static String playerClass(int objectID) {
        String playerClass = null;

        if (objectID == 768) playerClass = "Rogue";
        else if (objectID == 775) playerClass = "Archer";
        else if (objectID == 782) playerClass = "Wizard";
        else if (objectID == 784) playerClass = "Priest";
        else if (objectID == 785) playerClass = "Samurai";
        else if (objectID == 796) playerClass = "Bard";
        else if (objectID == 797) playerClass = "Warrior";
        else if (objectID == 798) playerClass = "Knight";
        else if (objectID == 799) playerClass = "Paladin";
        else if (objectID == 800) playerClass = "Assassin";
        else if (objectID == 801) playerClass = "Necromancer";
        else if (objectID == 802) playerClass = "Huntress";
        else if (objectID == 803) playerClass = "Mystic";
        else if (objectID == 804) playerClass = "Trickster";
        else if (objectID == 805) playerClass = "Sorcerer";
        else if (objectID == 806) playerClass = "Ninja";
        else if (objectID == 817) playerClass = "Summoner";
        else if (objectID == 818) playerClass = "Kensei";

        return playerClass;
    }

    public static class Player {
        String type;
        String name;
        int objectID;
        LinkedHashMap<Integer, DamageCalculator.Target> targets = new LinkedHashMap<>();

        public Player(int o, String n, String t) {
            objectID = o;
            name = n;
            type = t;
        }
    }
}
