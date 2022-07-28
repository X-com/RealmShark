package example.damagecalc;

import packets.Packet;
import packets.PacketType;
import packets.outgoing.PlayerShootPacket;
import util.IdToName;

public class Bullet {
    Bullet projectile;
    Packet packet;
    PacketType type;
    int totalDmg;
    boolean armorPiercing;

    int bulletTime;
    int bulletID;
    int[] effects;

    int eff1;
    int eff2;

    public Bullet(Packet p, PacketType t) {
        packet = p;
        type = t;
    }

    public Bullet(int dmg, int time, int id, int eff1, int eff2) {
        totalDmg = dmg;
        bulletTime = time;
        bulletID = id;
        this.eff1 = eff1;
        this.eff2 = eff2;
    }

    public void calcBulletDmg(RNG rng, Entity player) {
        if (player == null || rng == null) return;
        long r = rng.next();
        int weapon = ((PlayerShootPacket) packet).weaponId;
        int projectileId = ((PlayerShootPacket) packet).projectileId;
        if (projectileId == -1){
            projectileId = 0;
        }
        int min = IdToName.getIdProjectileMinDmg(weapon, projectileId);
        int max = IdToName.getIdProjectileMaxDmg(weapon, projectileId);
        boolean ap = IdToName.getIdProjectileArmorPierces(weapon, projectileId);
        int dmg = (int) (min + r % (max - min));
        float f = player.playerDmgMult();
//            System.out.println(IdToName.name(weapon) + " Min: " +  min + " Max: " + max + " ff: " + f + " dmg: " + dmg + " " + r % (max - min) + " id: " + ((PlayerShootPacket) packet).bulletID);
        totalDmg = (int) (dmg * f);
        armorPiercing = ap;
        effects = new int[1];
        effects[0] = player.stats[29].statValue;
//        System.out.println("shoot: " + effects);
    }

    public int damageWithDefense(int damage, boolean armorPiercing, int defence, int[] conditions) {
        if (damage == 0) return 0;

        if (armorPiercing || (conditions[0] & 0x4000000) != 0) {
            defence = 0;
        } else if ((conditions[0] & 0x2000000) != 0) {
            defence = (int) (defence * 1.5);
        }
        if ((conditions[1] & 0x20000) != 0) {
            defence = defence - 20;
        }
        int minDmg = damage * 2 / 20;
        int dmg = Math.max(minDmg, damage - defence);

        if ((conditions[0] & 0x1000000) != 0) {
            dmg = 0;
        }
        if ((conditions[1] & 0x8) != 0) {
            dmg = (int) (dmg * 0.9);
        }
        if ((conditions[1] & 0x40) != 0) {
            dmg = (int) (dmg * 1.25);
        }
        totalDmg = dmg;
        return dmg;
//        if (armorPiercing || (stat[0] & 0x4000000) != 0) {
//            defence = 0;
//        } else if ((stat[0] & 0x2000000) != 0) {
//            defence = defence * 1.5;
//        }
//        if ((stat[1] & 0x20000) != 0) {
//            defence = defence - 20;
//        }
//        int minDmg = damage * 2 / 20;
//        int dmg = Math.max(minDmg, damage - defence);
//        if ((stat[0] & 0x1000000) != 0) {
//            dmg = 0;
//        }
//        if ((stat[1] & 8) != 0) {
//            dmg = (int) (dmg * 0.9);
//        }
//        if ((stat[1] & 0x40) != 0) {
//            dmg = (int) (dmg * 1.25);
//        }
    }
}