package example.damagecalc;

import example.gui.TomatoGUI;
import packets.Packet;
import packets.PacketType;
import packets.data.StatData;
import packets.incoming.*;
import packets.outgoing.EnemyHitPacket;
import packets.outgoing.PlayerShootPacket;
import util.IdToName;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Damage calculator class made to compute dps of all players in
 * view distance around the player from incoming and outgoing packets.
 */
public class DpsLogger {

    HashMap<Integer, Entity> entityList = new HashMap<>();
    MapInfoPacket mapInfo;
    Entity player;
    RNG rng;

    /**
     * Packet processing method used to unwrap packets and log any damage and effects.
     *
     * @param packet incoming packets to be processed.
     */
    public void packetCapture(Packet packet) {
        if (packet instanceof MapInfoPacket) {
            if (mapInfo != null) TomatoGUI.setTextAreaDPS(stringDmg(player, entityList));
            mapInfo = (MapInfoPacket) packet;
            entityList.clear();
            rng = new RNG(mapInfo.seed);
            if (!filteredInstances(mapInfo.displayName)) {
                mapInfo = null;
            }
        } else if (mapInfo == null) {
            return;
        }

        if (packet instanceof CreateSuccessPacket) {
            CreateSuccessPacket p = (CreateSuccessPacket) packet;
            player = new Entity(p.objectId);
        } else if (packet instanceof PlayerShootPacket) {
            PlayerShootPacket p = (PlayerShootPacket) packet;
            Bullet bullet = new Bullet(p, PacketType.PLAYERSHOOT);
            calculateBulletDamage(bullet, rng, player, p.weaponId, p.projectileId);
            player.setBullet(p.bulletId, bullet);
        } else if (packet instanceof ServerPlayerShootPacket) {
            ServerPlayerShootPacket p = (ServerPlayerShootPacket) packet;
            Bullet bullet = new Bullet(p, PacketType.SERVERPLAYERSHOOT);
            bullet.totalDmg = p.damage;
            if (p.spellBulletData) {
                for (int j = p.bulletId; j < p.bulletId + p.bulletCount; j++) {
                    player.setBullet(p.bulletId, bullet);
                }
            }
        } else if (packet instanceof EnemyHitPacket) {
            EnemyHitPacket p = (EnemyHitPacket) packet;
            Bullet bullet = player.getBullet(p.bulletId);
            Entity entity = getEntity(p.targetId);
            hit(entity, bullet, p);
        } else if (packet instanceof DamagePacket) {
            DamagePacket p = (DamagePacket) packet;
            if (p.damageAmount > 0) {
                Bullet bullet = new Bullet(packet, PacketType.DAMAGE);
                Entity entity = getEntity(p.targetId);
                entity.bulletDamageList.add(bullet);
            }
        } else if (packet instanceof NewTickPacket) {
            NewTickPacket p = (NewTickPacket) packet;
            for (int j = 0; j < p.status.length; j++) {
                int id = p.status[j].objectId;
                StatData[] stats = p.status[j].stats;
                if (id == player.id) {
                    player.setStats(stats);
                    continue;
                }
                entityList.get(id).setStats(stats);
            }
        } else if (packet instanceof UpdatePacket) {
            UpdatePacket p = (UpdatePacket) packet;
            for (int j = 0; j < p.newObjects.length; j++) {
                int id = p.newObjects[j].status.objectId;
                StatData[] stats = p.newObjects[j].status.stats;
                if (id == player.id) {
                    player.setStats(stats);
                    continue;
                }
                int objectType = p.newObjects[j].objectType;
                entityList.get(id).setType(objectType);
                entityList.get(id).setStats(stats);
            }
        }
    }

    /**
     * Filtered dungeons that will never need logging such as Nexus dungeons.
     *
     * @param dungName Name of the dungeon.
     * @return true if dungeon should be logged.
     */
    public static boolean filteredInstances(String dungName) {
        switch (dungName) {
            case "{s.vault}":
            case "Daily Quest Room":
            case "Pet Yard":
            case "{s.guildhall}":
            case "{s.nexus}":
            case "{s.rotmg}":
                return false;
            default:
                return true;
        }
    }

    /**
     * Full output string from damage logs of all hostile mobs.
     *
     * @param player     entity object.
     * @param entityList full list of all entities logged.
     * @return logged dps output as a string.
     */
    public static String stringDmg(Entity player, HashMap<Integer, Entity> entityList) {
        StringBuilder sb = new StringBuilder();

        List<Entity> sortedList = Arrays.stream(entityList.values().toArray(new Entity[0])).sorted(Comparator.comparingInt(Entity::maxHp).reversed()).collect(Collectors.toList());
        int count = 0;
        for (Entity e : sortedList) {
            if (count > 10) break;
            count++;
            if (e.maxHp() < 4000 || e.bulletDamageList.isEmpty()) continue;

            HashMap<Integer, Integer> players = new HashMap<>();
            int playerDmg = 0;
            for (Bullet b : e.bulletDamageList) {
                if (PacketType.DAMAGE == b.type) {
                    DamagePacket p = (DamagePacket) b.packet;
                    int ownerId = p.objectId;
                    if (ownerId == player.id) {
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

            sb.append(e).append(" ").append(e.maxHp()).append("\n");
            float playerPers = ((float) playerDmg * 100 / (float) e.maxHp());
            sb.append(String.format("  My DMG: %d  %.3f%%    [%s]\n\n", playerDmg, playerPers, itemToString(player)));
            Stream<Map.Entry<Integer, Integer>> sorted2 = players.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()));
            for (Map.Entry<Integer, Integer> m : sorted2.collect(Collectors.toList())) {
                Entity entityPlayer = entityList.get(m.getKey());
                if (entityPlayer == null || entityPlayer.getStat(31) == null) continue;
                String name = entityPlayer.getStat(31).stringStatValue;
                float pers = ((float) m.getValue() * 100 / (float) e.maxHp());
                sb.append(String.format("  %s DMG: %d  %.3f%%    [%s]\n", name, m.getValue(), pers, itemToString(player)));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Item list to string parser.
     *
     * @param entity A player entity with items equipped needing to be parsed.
     * @return String of player equipped items.
     */
    private static String itemToString(Entity entity) {
        StringBuilder s = new StringBuilder();
        for (int k = 0; k < 256; k++) {
            if (k >= 8 && k <= 11 && entity.getStat(k) != null) {
                int itemID = entity.getStat(k).statValue;
                s.append(IdToName.name(itemID));
                if (k < 11) s.append(", ");
            }
        }
        return s.toString();
    }

    /**
     * Gets the entity from id or creates a new Entity object, adds it to the list and returns it.
     *
     * @param id requested entity by id.
     * @return the entity to be requested by id.
     */
    private Entity getEntity(int id) {
        if (entityList.containsKey(id)) {
            return entityList.get(id);
        }
        Entity e = new Entity(id);
        entityList.put(id, e);
        return e;
    }

    /**
     * Calculates the damage to shot bullet from the player from main weapon.
     *
     * @param bullet       The bullet being shot.
     * @param rng          Seed used with randomizer to find the exact value of used weapon from min to max range of weapon dmg.
     * @param player       The player entity.
     * @param weaponId     Weapon ID used (retrieved from packet being sent).
     * @param projectileId Projectile ID used (retrieved from packet being sent).
     */
    private static void calculateBulletDamage(Bullet bullet, RNG rng, Entity player, int weaponId, int projectileId) {
        if (player == null || rng == null) return;
        long r = rng.next();
        if (projectileId == -1) {
            projectileId = 0;
        }
        int min = IdToName.getIdProjectileMinDmg(weaponId, projectileId);
        int max = IdToName.getIdProjectileMaxDmg(weaponId, projectileId);
        boolean ap = IdToName.getIdProjectileArmorPierces(weaponId, projectileId);
        int dmg = (int) (min + r % (max - min));
        float f = playerStatsMultiplier(player);
        bullet.totalDmg = (int) (dmg * f);
        bullet.armorPiercing = ap;
    }

    /**
     * Player entity stats multiplier such as attack, exalts and other buffs.
     *
     * @param player The player entity.
     * @return damage multiplier from player stats.
     */
    private static float playerStatsMultiplier(Entity player) {
        boolean weak = (player.getStat(29).statValue & 0x40) != 0;
        boolean damaging = (player.getStat(29).statValue & 0x40000) != 0;
        int attack = player.getStat(20).statValue;
        float exaltDmgBonus = (float) player.getStat(113).statValue / 1000;

        if (weak) {
            return 0.5f;
        }
        float number = (attack + 25) * 0.02f;
        if (damaging) {
            number *= 1.25;
        }
        return number * exaltDmgBonus;
    }

    /**
     * Used when an entity takes damage taking defence and other effects into account for final damage to entity.
     *
     * @param damage        the base damage the bullet can do.
     * @param armorPiercing if the bullet ignores defence.
     * @param defence       defence of the entity being hit.
     * @param conditions    condition effects the entity being shot can have.
     * @return final damage applied to the entity.
     */
    private static int damageWithDefense(int damage, boolean armorPiercing, int defence, int[] conditions) {
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
        return dmg;
    }

    /**
     * Computes the effect of a player bullet hitting an entity.
     *
     * @param entity Entity being hit by a bullet.
     * @param bullet The bullet hitting the entity.
     * @param hit    The packet sent when an entity is hit.
     */
    private static void hit(Entity entity, Bullet bullet, EnemyHitPacket hit) {
        if (bullet == null || bullet.totalDmg == 0) return;

        int[] conditions = new int[2];

        conditions[0] = entity.getStat(29) == null ? 0 : entity.getStat(29).statValue;
        conditions[1] = entity.getStat(96) == null ? 0 : entity.getStat(96).statValue;
        int defence = entity.getStat(21) == null ? 0 : entity.getStat(21).statValue;

        Bullet b = new Bullet(hit, PacketType.ENEMYHIT);
        b.totalDmg = damageWithDefense(bullet.totalDmg, bullet.armorPiercing, defence, conditions);

        if (b.totalDmg > 0) entity.bulletDamageList.add(b);
    }

    /**
     * Class used for storing bullet data.
     */
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

    /**
     * Class used for entity info.
     */
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
            return IdToName.name(objectType);
        }
    }
}
