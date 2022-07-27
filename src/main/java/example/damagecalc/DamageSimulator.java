package example.damagecalc;

import example.gui.TomatoGUI;
import packets.Packet;
import packets.PacketType;
import packets.data.StatData;
import packets.incoming.*;
import packets.outgoing.EnemyHitPacket;
import packets.outgoing.PlayerShootPacket;
import packets.reader.BufferReader;
import util.IdToName;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DamageSimulator {

    public static void main(String[] args) {
        try {
            new DamageSimulator().readfile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    MapInfoPacket mapInfo = null;
    CreateSuccessPacket createSuccess = null;
    int playerID;
    long seed;
    RNG rng;
    RNG randy;
    long rrr;
    ArrayList<BufferReader> injectData = new ArrayList<>();
    ArrayList<Bullet> logBullets = new ArrayList<>();

    private void readfile() throws Exception {
//        String fileName = "dmgLog/Forest_Maze.dmgLog-2022-07-19-21.56.20.data";
//        String fileName = "dmgLog/Ice_Tomb.dmgLog-2022-07-19-22.35.28.data";
        String fileName = "dmgLog/Ice_Tomb.dmgLog-2022-07-19-22.24.15.data";
//        String fileName = "dmgLog/The_Third_Dimension.dmgLog-2022-07-13-03.19.20.data";
//        String fileName = "dmgLog/Davy_Jones'_Locker.dmgLog-2022-07-06-22.43.49.data";
//        String fileName = "dmgLog/{s.oryx_s_castle}.dmgLog-2022-07-17-04.19.35.data";
        File f = new File(fileName);

        BufferedReader br = new BufferedReader(new FileReader(f));
        String line;
        int i = 0;
        int counter = 0;
        int adds = 0;
        int notadd = 0;
        int logTarget = 721;
        NewTickPacket previousNewTick = null;

        while ((line = br.readLine()) != null) {
            i++;
            byte[] data = getByteArray(line);
            if (data.length > 5) {
                BufferReader pData = new BufferReader(ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN));
                int size = pData.readInt();
                if (size != data.length) throw new RuntimeException("Incorrect size");

                int type = pData.readByte();
                if (type == -1) {
                    int pack = pData.readByte();
                    if (pack == 0) {
                        injectData.add(pData);
                        int dmg = decodeInt(data, 6);
                        int time = decodeInt(data, 10);
                        int eff1 = decodeInt(data, 14);
                        int eff2 = decodeInt(data, 18);
                        int id = decodeShort(data, 22);
//                        System.out.printf("----dmg:%d time:%d id:%d, eff1:%x eff2:%x\n", dmg, time, id, eff1, eff2);
                        Bullet b = new Bullet(dmg, time, id, eff1, eff2);
                        logBullets.add(b);
                    } else if (pack == 1) {
                        int rr = decodeInt(data, 6);
                        int ef = decodeInt(data, 10);
                        int id = decodeShort(data, 14);
//                        System.out.printf("rng:%d id:%d seed:%d dif:%d\n", rr, id, rrr, (rr - rrr));
//                        System.out.printf("ef:%x\n", ef);
                        rrr = randy.next();
                    }
                    continue;
                }
                Packet packet = PacketType.getPacket(type).factory();
                packet.deserialize(pData);

                packetCapture(packet);
            }
        }

//        if(true) return;

        System.out.println(stringDmg());

//        if(true) return;

//        for (int ii = 0; ii < logBullets.size(); ii++) {
//            Bullet b = logBullets.get(ii);
//            boolean found = false;
//            for (Bullet hb : Entity.bulletsHit) {
//                if (b.bulletTime == hb.bulletTime && b.bulletID == hb.bulletID) {
//                    if ((b.totalDmg - hb.totalDmg) != 0)
//                        System.out.println("--" + ii + "--dmg: " + ((b.totalDmg - hb.totalDmg) == 0 ? ("r&C: " + b.totalDmg) : ("real: " + b.totalDmg + " calc: " + hb.totalDmg + " <- " + hb.projectile.totalDmg)) + " time: " + b.bulletTime + " id: " + b.bulletID + " " + ConditionBits.effectsToString(hb.effects[0]) + ConditionNewBits.effectsToString(hb.effects[1]) + "vs" + ConditionBits.effectsToString(b.eff1) + ConditionNewBits.effectsToString(b.eff2) + " ----- " + Arrays.toString(ConditionBits.getEffects(hb.projectile.effects[0])));
////                    if (b.eff2 > 0) System.out.printf("%x %x\n", b.eff1, b.eff2);
//                    found = true;
//                    break;
//                }
//            }
//            if (!found) System.out.println("log bullet failed " + b.totalDmg);
//        }
//        System.out.println(Entity.bulletsHit.size() + " " + logBullets.size());
    }

    public static boolean filterDungys(String dungName) {
        switch (dungName) {
            case "{s.vault}":
            case "Daily Quest Room":
            case "Pet Yard":
//            case "{s.guildhall}":
            case "{s.nexus}":
            case "{s.rotmg}":
                System.out.println("disabled - " + dungName);
                return false;
            default:
                System.out.println("enabled - " + dungName);
                return true;
        }
    }

    public void packetCapture(Packet packet) {
        if (packet instanceof MapInfoPacket) {
            if (mapInfo != null) TomatoGUI.setTextAreaDPS(stringDmg());
            mapInfo = (MapInfoPacket) packet;
            Entity.clear();
            seed = mapInfo.seed;
            rng = new RNG(seed);
            randy = new RNG(seed);
            if (!filterDungys(mapInfo.displayName)) {
                mapInfo = null;
            }
        } else if (mapInfo == null) {
            return;
        } else if (packet instanceof CreateSuccessPacket) {
            CreateSuccessPacket p = (CreateSuccessPacket) packet;
            createSuccess = p;
            playerID = p.objectId;
            Entity.player = new Entity(p.objectId);
        } else if (packet instanceof PlayerShootPacket) {
            PlayerShootPacket p = (PlayerShootPacket) packet;
            Bullet bullet = new Bullet(p, PacketType.PLAYERSHOOT);
            bullet.calcBulletDmg(rng, Entity.player);
            Entity.player.addBullet(p.bulletId, bullet);
        } else if (packet instanceof ServerPlayerShootPacket) {
            ServerPlayerShootPacket p = (ServerPlayerShootPacket) packet;
            Bullet bullet = new Bullet(p, PacketType.SERVERPLAYERSHOOT);
            bullet.totalDmg = p.damage;
            if (p.spellBulletData) {
                for (int j = p.bulletId; j < p.bulletId + p.bulletCount; j++) {
                    Entity.player.addBullet(p.bulletId, bullet);
                }
            }
//                    player.addBullet(p, p.damage);
        } else if (packet instanceof EnemyHitPacket) {
            EnemyHitPacket p = (EnemyHitPacket) packet;
//                    System.out.println("ENEMYHIT " + p.time);
//                    Bullet b = player.findBullet(p);
//                    if (b == null) {
//                        System.out.println("didn't find bullet ID");
////                        System.out.println(p);
//                        continue;
//                    }
            Bullet b = Entity.player.getBullet(p);
            Entity.hit(p.targetId, b, p);
//                    System.out.println(p);
//            counter++;
        } else if (packet instanceof DamagePacket) {
            DamagePacket p = (DamagePacket) packet;
            int id = p.targetId;
            Bullet bullet = new Bullet(packet, PacketType.DAMAGE);
            Entity.dmg(id, bullet);
        } else if (packet instanceof NewTickPacket) {
            NewTickPacket p = (NewTickPacket) packet;
//                    System.out.println("NEWTICK==="+p.tickId + " " + p.tickTime);
            for (int j = 0; j < p.status.length; j++) {
                int id = p.status[j].objectId;
                StatData[] stats = p.status[j].stats;
                if (id == playerID) {
                    Entity.player.setStats(stats);
                    continue;
                }
                Entity.setStats(id, stats);
            }
        } else if (packet instanceof UpdatePacket) {
            UpdatePacket p = (UpdatePacket) packet;
            for (int j = 0; j < p.newObjects.length; j++) {
                int id = p.newObjects[j].status.objectId;
                StatData[] stats = p.newObjects[j].status.stats;
                if (id == playerID) {
                    Entity.player.setStats(stats);
                    continue;
                }
//                        if(id == logTarget) System.out.println("new UPDATE updates " + p.newObjects[j].status);
                int objectType = p.newObjects[j].objectType;
                Entity.setType(id, objectType);
                Entity.setStats(id, stats);
            }
        }
    }

    public static String stringDmg() {
        StringBuilder sb = new StringBuilder();

        List<Entity> sortedList = Arrays.stream(Entity.list()).sorted(Comparator.comparingInt(Entity::maxHp).reversed()).collect(Collectors.toList());
        int count = 0;
        for (Entity e : sortedList) {
            if (count > 10) break;
            count++;
            if (e.maxHp() < 4000 || e.bullets.isEmpty()) continue;

            HashMap<Integer, Integer> players = new HashMap<>();
            int playerDmg = 0;
            for (Bullet b : e.bullets) {
                if (PacketType.DAMAGE == b.type) {
                    DamagePacket p = (DamagePacket) b.packet;
                    int ownerId = p.objectId;
                    if (ownerId == Entity.player.id) {
                        playerDmg += p.damageAmount;
                        continue;
                    }
                    if (!players.containsKey(ownerId)) {
                        players.put(ownerId, p.damageAmount);
                    } else {
                        int tot = players.get(ownerId);
                        players.put(ownerId, p.damageAmount + tot);
                    }
                } else if (PacketType.ENEMYHIT == b.type) {
                    playerDmg += b.totalDmg;
                }
            }

            sb.append(e + " " + e.maxHp() + " " + e.id + "\n");
            float playerPers = ((float) playerDmg * 100 / (float) e.maxHp());
            sb.append(String.format("  My DMG: %d  %.3f%%    [%s]\n\n", playerDmg, playerPers, itemToString(Entity.player)));
//
            Stream<Map.Entry<Integer, Integer>> sorted2 = players.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()));
            float tot = 0;
            int dmg = 0;
            for (Map.Entry<Integer, Integer> m : sorted2.collect(Collectors.toList())) {
                Entity player = Entity.list.get(m.getKey());
//                    System.out.println(m.getKey());
                if (player == null || player.stats[31] == null) continue;
                String name = player.stats[31].stringStatValue;
                float pers = ((float) m.getValue() * 100 / (float) e.maxHp());
                tot += pers;
                dmg += m.getValue();
                sb.append(String.format("  %s DMG: %d  %.3f%%    [%s]\n", name, m.getValue(), pers, itemToString(player)));
            }
//            sb.append(String.format("rest: %f\n", (100 - tot)));
//            sb.append(String.format("tot: %f\n", (playerPers + tot)));
//            sb.append(String.format("dmg: %d\n", (playerDmg + dmg)));
////            int dif = e.maxHp() - dmg;
////            System.out.println(((float) dif * 100 / (float) e.maxHp()));
//            sb.append(String.format("rem: %d\n", (e.maxHp() - dmg - playerDmg)));
            sb.append("\n");
        }

        return sb.toString();
    }

    public static String itemToString(Entity e) {
        String s = "";
        for (int k = 0; k < 256; k++) {
            if (k >= 8 && k <= 11 && e.stats[k] != null) {
                int itemID = e.stats[k].statValue;
                s += IdToName.name(itemID);
                if (k < 11) s += ", ";
            }
        }
        return s;
    }

    public static int decodeInt(byte[] bytes, int offset) {
        return (Byte.toUnsignedInt(bytes[3 + offset]) << 24) | (Byte.toUnsignedInt(bytes[2 + offset]) << 16) | (Byte.toUnsignedInt(bytes[1 + offset]) << 8) | Byte.toUnsignedInt(bytes[offset]);
    }

    public static int decodeShort(byte[] bytes, int offset) {
        return ((Byte.toUnsignedInt(bytes[1 + offset]) << 8) | Byte.toUnsignedInt(bytes[offset]));
    }

    public static byte[] getByteArray(String byteString) {
        String[] list = new String[0];

        boolean hex = false;
        if (byteString.contains("Hex stream")) {
            hex = true;
            list = byteString.replace("  Hex stream: ", "").split(" ");
        } else {
            int starts = byteString.indexOf('[');
            int ends = byteString.indexOf(']');
            if (starts != -1 && ends != -1)
                list = byteString.substring(starts, ends).replaceAll("[\\[\\] ]", "").split(",");
        }
        byte[] b = new byte[list.length];
        for (int i = 0; i < list.length; i++) {
            String s = list[i];
            if (hex) {
                b[i] = (byte) ((Character.digit(s.charAt(0), 16) << 4) + Character.digit(s.charAt(1), 16));
            } else {
                b[i] = Byte.parseByte(s);
            }
        }
        return b;
    }
}
