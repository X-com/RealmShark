package tomato.logic;

import packets.Packet;
import packets.data.StatData;
import packets.data.WorldPosData;
import packets.incoming.CreateSuccessPacket;
import packets.incoming.NewTickPacket;
import packets.incoming.UpdatePacket;
import tomato.gui.TomatoGUI;
import assets.AssetMissingException;
import assets.IdToName;
import util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class Parse {

    private final HashMap<Integer, Entity> entityList = new HashMap<>();
    private final HashMap<Integer, Entity> playerHash = new HashMap<>();
    Entity player;
    private static boolean invDirty;

    public void packetCapture(Packet packet) {
        if (packet instanceof CreateSuccessPacket) {
            CreateSuccessPacket p = (CreateSuccessPacket) packet;
            playerHash.clear();
            player = new Entity(p.objectId, "isMe");
            playerHash.put(player.id, player);
            invDirty = true;
        } else if (packet instanceof NewTickPacket) {
            NewTickPacket p = (NewTickPacket) packet;
            for (int j = 0; j < p.status.length; j++) {
                int id = p.status[j].objectId;
                StatData[] stats = p.status[j].stats;
                Entity entity = getEntity(id);
                if (entity == null) return;
                WorldPosData pos = p.status[j].pos;
                entity.setTime(p.serverRealTimeMS);
                entity.setStats(stats);
                entity.setPos(pos);
            }
            updateStuff();
        } else if (packet instanceof UpdatePacket) {
            UpdatePacket p = (UpdatePacket) packet;
            for (int j = 0; j < p.newObjects.length; j++) {
                int id = p.newObjects[j].status.objectId;
                StatData[] stats = p.newObjects[j].status.stats;
                WorldPosData pos = p.newObjects[j].status.pos;
                int objectType = p.newObjects[j].objectType;
                Entity entity = new Entity(id);
                entity.setStats(stats);
                if (entity.stats[0] != null && entity.stats[31] != null) {
                    entity.setType(objectType);
                    entity.setPos(pos);
                    playerHash.put(id, entity);
                    invDirty = true;
                }
            }
            for (int j = 0; j < p.drops.length; j++) {
                if (playerHash.remove(p.drops[j]) != null) {
                    invDirty = true;
                }
            }
            updateStuff();
        }
    }

    private void updateStuff() {
        if (!invDirty) return;
        ArrayList<Pair<String, int[]>> newList = new ArrayList<>();
        for (Entity e : playerHash.values()) {
            StatData playerName = e.stats[31];
            if (playerName != null && e.stats[0] != null) {
                String name = playerName.stringStatValue;
                int[] inv = new int[4];
                for (int i = 0; i < 4; i++) {
                    StatData sd = e.stats[i + 8];
                    if (sd != null) {
                        inv[i] = sd.statValue;
                    }
                }
                Pair p = new Pair(name, inv);
                newList.add(p);

            }
        }
        invDirty = false;
        TomatoGUI.setParsePlayers(newList);
    }

    /**
     * Gets the entity from id or creates a new Entity object, adds it to the list and returns it.
     *
     * @param id requested entity by id.
     * @return the entity to be requested by id.
     */
    private Entity getEntity(int id) {
        if (playerHash.containsKey(id)) {
            return playerHash.get(id);
        }
        return null;
    }


    /**
     * Class used for entity info.
     */
    private static class Entity {

        private boolean isMe = false;
        private final int id;
        private int objectType = -1;
        private final StatData[] stats = new StatData[256];
        private long entityStartTime;
        private long entityTime;

        public Entity(int id) {
            this.id = id;
        }

        public Entity(int id, String isMe) {
            this.id = id;
            this.isMe = true;
        }

        public void setStats(StatData[] stats) {
            for (StatData sd : stats) {
                if (this.stats[sd.statTypeNum] != null && this.stats[sd.statTypeNum].statValue > 7 && this.stats[sd.statTypeNum].statValue < 12) {
                    invDirty = true;
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

        public void setTime(long time) {
            entityTime = time;
            if (entityStartTime == 0) entityStartTime = time;
        }

        public void setPos(WorldPosData pos) {

        }

        @Override
        public String toString() {
            try {
                return IdToName.objectName(objectType);
            } catch (AssetMissingException e) {
                e.printStackTrace();
            }
            return "";
        }
    }
}
