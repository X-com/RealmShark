package example.damagecalc;

import packets.PacketType;
import packets.data.StatData;
import packets.incoming.ServerPlayerShootPacket;
import packets.outgoing.EnemyHitPacket;
import packets.outgoing.PlayerShootPacket;
import util.IdToName;

import java.util.ArrayList;
import java.util.HashMap;

public class Entity {

    static HashMap<Integer, Entity> list = new HashMap<>();

    int id;
    //    WorldPosData pos;
    StatData[] stats = new StatData[256];
    int type = -1;
    ArrayList<Bullet> bullets = new ArrayList<>();
    Bullet[] bulletDmg = new Bullet[512];
    public static ArrayList<Bullet> bulletsHit = new ArrayList<>();
    public static Entity player;

    public Entity(int id) {
        this.id = id;
    }

    private static Entity getEntity(int id) {
        if (list.containsKey(id)) {
            return list.get(id);
        }
        Entity e = new Entity(id);
        list.put(id, e);
        return e;
    }

    public static void setStats(int id, StatData[] stats) {
        Entity e = getEntity(id);
        e.setStats(stats);
    }

    public static void clear() {
        bulletsHit.clear();
        list.clear();
    }

    public static Entity[] list() {
        return list.values().toArray(new Entity[0]);
    }

    public void setStats(StatData[] data) {
        for (StatData s : data) {
            stats[s.statTypeNum] = s;
        }
    }

    public static void hit(int id, Bullet bullet, EnemyHitPacket hit) {
        Entity e = getEntity(id);
        e.hit(bullet, hit);
    }

    public void hit(Bullet bullet, EnemyHitPacket hit) {
        if (bullet == null || bullet.totalDmg == 0) return;

        int[] conditions = new int[2];

        conditions[0] = stats[29] == null ? 0 : stats[29].statValue;
        conditions[1] = stats[96] == null ? 0 : stats[96].statValue;
        int defence = stats[21] == null ? 0 : stats[21].statValue;

        Bullet b = new Bullet(hit, PacketType.ENEMYHIT);
        b.bulletTime = hit.time;
        b.bulletID = hit.bulletId;
        b.projectile = bullet;
        b.effects = conditions;

        b.totalDmg = b.damageWithDefense(bullet.totalDmg, bullet.armorPiercing, defence, conditions);
//        int defence = stats[21] == null ? 0 : stats[21].statValue;
//        System.out.println("Hit dmg: " + bullet.totalDmg + " " + hit.time + " " + ((PlayerShootPacket)bullet.packet).bulletID + " def: " + defence);
        bullets.add(b);
        bulletsHit.add(b);
    }

    public static void dmg(int id, Bullet bullet) {
        Entity e = getEntity(id);
        e.dmg(bullet);
    }

    private void dmg(Bullet bullet) {
        bullets.add(bullet);
    }

    public static void setType(int id, int type) {
        Entity e = getEntity(id);
        e.setType(type);
    }

    private void setType(int type) {
        this.type = type;
    }

    public Bullet findBullet(EnemyHitPacket hit) {
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            if (b.type == PacketType.PLAYERSHOOT) {
                PlayerShootPacket shoot = (PlayerShootPacket) b.packet;
                if (shoot.bulletId == hit.bulletId) {
                    return b;
                }
            } else if (b.type == PacketType.SERVERPLAYERSHOOT) {
                ServerPlayerShootPacket shoot = (ServerPlayerShootPacket) b.packet;
                if (shoot.spellBulletData) {
                    int bottomId = shoot.bulletId;
                    int topId = (shoot.bulletId + shoot.bulletCount) % 512;
                    if ((bottomId >= topId || hit.bulletId >= bottomId) && hit.bulletId <= topId) {
                        return b;
                    }
                }
            }
        }
        return null;
    }

    public Bullet getBullet(EnemyHitPacket p) {
        return bulletDmg[p.bulletId];
    }

    public float playerDmgMult() {
        boolean weak = (stats[29].statValue & 0x40) != 0;
        boolean damaging = (stats[29].statValue & 0x40000) != 0;
        int attack = stats[20].statValue;
        float exaltDmgBonus = (float) stats[113].statValue / 1000;

//        System.out.printf("atk: %d exalt: %d weak: %b daming: %b\n", attack, exaltDmgBonus, weak, damaging);

        if (weak) {
            return 0.5f;
        }
        float number = (attack + 25) * 0.02f;
        if (damaging) {
            number *= 1.25;
        }
        return number * exaltDmgBonus;
    }

    public void addBullet(short bulletId, Bullet dmg) {
        bulletDmg[bulletId] = dmg;
    }

    @Override
    public String toString() {
        return IdToName.name(type);
    }

    public int maxHp() {
        if (stats[0] == null) return 0;
        return stats[0].statValue;
    }
}
