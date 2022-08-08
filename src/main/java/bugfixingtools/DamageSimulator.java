package bugfixingtools;

import example.damagecalc.DpsLogger;
import packets.Packet;
import packets.PacketType;
import packets.data.StatData;
import packets.incoming.NewTickPacket;
import packets.incoming.TextPacket;
import packets.incoming.UpdatePacket;
import packets.reader.BufferReader;
import util.IdToName;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

public class DamageSimulator {

    public static void main(String[] args) {
        try {
            new DamageSimulator().readfile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static ArrayList<BufferReader> injectData = new ArrayList<>();
    DpsLogger dpsLogger = new DpsLogger();
    HashMap<Integer, Entity> entityList = new HashMap<>();

    private void readfile() throws Exception {
//        String fileName = "dmgLog/Oryx's_Sanctuary.dmgLog-2022-07-25-11.12.35.data";
//        String fileName = "dmgLog/Oryx's_Sanctuary.dmgLog-2022-07-25-16.30.50.data";
        String fileName = "loggedDps/Oryx's Sanctuary-2022-08-08-01.47.43.data";
        File f = new File(fileName);

        BufferedReader br = new BufferedReader(new FileReader(f));
        String line;

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

                packetTest(packet);
            }
        }

//        System.out.println(dpsLogger.stringDmg());
    }

    int val = 0;

    private Entity getEntity(int id) {
        if (entityList.containsKey(id)) {
            return entityList.get(id);
        }
        Entity e = new Entity(id);
        entityList.put(id, e);
        return e;
    }

    public void packetTest(Packet packet) {
        if (packet instanceof NewTickPacket) {
            NewTickPacket p = (NewTickPacket) packet;
            for (int j = 0; j < p.status.length; j++) {
                int id = p.status[j].objectId;
                StatData[] stats = p.status[j].stats;
//                if (id == player.id) {
//                    player.setStats(stats);
//                    continue;
//                }
                Entity entity = getEntity(id);
                entity.setStats(stats);
                if (8776 == id || id == 8189 || id == 11962) {
                    for (StatData sd : stats) {
                        if (sd.statTypeNum == 0 || sd.statTypeNum == 1 || sd.statTypeNum == 29 || sd.statTypeNum == 96)
                            continue;
                        if (sd.statTypeNum == 125) {
                            System.out.print(sd);
//                            System.out.println(sd.statValue - val);
//                            val = sd.statValue;
                        }
//                        if (sd.statTypeNum == 126) System.out.print(sd);
                    }
                }
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
                entity.setType(objectType);
                entity.setStats(stats);
                if (8776 == id || id == 8189) {
                    System.out.printf("%s %d %d\n", entity, objectType, id);
                }
                if (objectType == 45363) {
                    System.out.printf("%s %d %d\n", entity, objectType, id);
                }
            }
        } else if (packet instanceof TextPacket) {
            TextPacket p = (TextPacket) packet;
            if (p.objectId == 11962 && !p.text.equals("")) {
                System.out.println();
                System.out.print(p.text);
            }
        }
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
        private final ArrayList<Bullet> bulletDamageList = new ArrayList<>();

        public Entity(int id) {
            this.id = id;
        }

        public void setBullet(short bulletId, Bullet bullet) {
            bulletDmg[bulletId] = bullet;
        }

        public Bullet getBullet(short p) {
            return bulletDmg[p];
        }

        public void setStats(StatData[] stats) {
            for (StatData sd : stats) {
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

        @Override
        public String toString() {
            return IdToName.getIdName(objectType);
        }
    }
}