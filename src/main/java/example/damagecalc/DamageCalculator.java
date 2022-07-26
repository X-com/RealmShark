package example.damagecalc;

import packets.Packet;
import packets.PacketType;
import packets.data.ObjectData;
import packets.data.StatData;
import packets.data.WorldPosData;
import packets.data.enums.StatType;
import packets.incoming.*;
import packets.outgoing.EnemyHitPacket;
import packets.outgoing.PlayerShootPacket;
import util.IdToName;
import util.Util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

public class DamageCalculator {
    private static int playerID;
    private static Entity player;
    private static RNG rng;

    public static void capturePackets(Packet packet) {
        if (packet instanceof MapInfoPacket) {
            DamageLogger.reopenLogger();
            MapInfoPacket mapInfoPacket = (MapInfoPacket) packet;
////            System.out.println(mapInfoPacket);
            DamageLogger.setName(mapInfoPacket.displayName);
            DamageLogger.logDmg(packet);
//
//             TODO present data
//            Entity.clear();
//            rng = new RNG(mapInfoPacket.seed);
        } else if (packet instanceof CreateSuccessPacket) {
            DamageLogger.logDmg(packet);
//            CreateSuccessPacket create = (CreateSuccessPacket) packet;
//            playerID = create.objectId;
//            player = new Entity(create.objectId);
        } else if (packet instanceof EnemyHitPacket) {
            DamageLogger.logDmg(packet);
//            EnemyHitPacket hit = (EnemyHitPacket) packet;
//            player.findBullet(hit);
        } else if (packet instanceof PlayerShootPacket) {
            DamageLogger.logDmg(packet);
//            PlayerShootPacket shoot = (PlayerShootPacket) packet;
//            Bullet bullet = new Bullet(shoot, PacketType.PLAYERSHOOT);
//            bullet.calcBulletDmg(rng, player);
//            player.addBullet(bullet);
        } else if (packet instanceof ServerPlayerShootPacket) {
            DamageLogger.logDmg(packet);
//            ServerPlayerShootPacket shootp = (ServerPlayerShootPacket) packet;
//            System.out.println(shootp);
        } else if (packet instanceof DamagePacket) {
            DamageLogger.logDmg(packet);
//            DamagePacket damage = (DamagePacket) packet;
//            int id = damage.targetId;
//            Bullet bullet = new Bullet(damage, PacketType.DAMAGE);
        } else if (packet instanceof NewTickPacket) {
            DamageLogger.logDmg(packet);
//            NewTickPacket newTickPacket = (NewTickPacket) packet;
//            System.out.println(newTickPacket);
//            for (int j = 0; j < newTickPacket.status.length; j++) {
//                int id = newTickPacket.status[j].objectId;
//                StatData[] stats = newTickPacket.status[j].stats;
//                if (id == playerID) {
//                    player.setStats(stats);
//                    continue;
//                }
//                Entity.setStats(id, stats);
//            }
        } else if (packet instanceof UpdatePacket) {
            DamageLogger.logDmg(packet);
//            UpdatePacket update = (UpdatePacket) packet;
//            for (int j = 0; j < update.newObjects.length; j++) {
//                int id = update.newObjects[j].status.objectId;
//                StatData[] stats = update.newObjects[j].status.stats;
//                if (id == playerID) {
//                    player.setStats(stats);
//                    continue;
//                }
//                int objectType = update.newObjects[j].objectType;
//                Entity.setType(id, objectType);
//                Entity.setStats(id, stats);
//            }
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
        LinkedHashMap<Integer, Target> targets = new LinkedHashMap<>();

        public Player(int o, String n, String t) {
            objectID = o;
            name = n;
            type = t;
        }
    }

    public static class Target {
        String name = null;
        int typeID;
        int dmg;

        public Target(int t) {
            typeID = t;
        }

        public void add(int d) {
            dmg += d;
        }

        public String name() {
            if (name == null) {
                name = IdToName.name(typeID);
                if (name == null) {
                    switch (typeID) {
                        case 1651:
                            name = "Oryx-2";
                            break;
                        case 2194:
                            name = "Leucoryx";
                            break;
                        case 1:
                            name = "Basia";
                            break;
                        case 9446:
                            name = "Oryx-3";
                            break;
                    }
                }
            }
            return name;
        }

        public int dmg() {
            return dmg;
        }
    }
}
