package potato.model;

import packets.data.GroundTileData;
import packets.data.ObjectData;
import packets.data.ObjectStatusData;
import packets.data.StatData;
import packets.data.enums.StatType;
import packets.incoming.QuestObjectIdPacket;
import potato.model.data.HeroState;
import potato.model.data.HeroType;
import potato.model.data.IdData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class HeroDetect {

    private final DataModel model;

    private final boolean[][] snakeTiles;
    private final HashMap<Integer, ObjectData> allEntitys = new HashMap<>();
    private final HashMap<Integer, ObjectData> entitys = new HashMap<>();
    private final int[][] mapTiles = new int[2048][2048];

    private int questArrowId;
    private int questArrowIdChecked;
    private boolean questUpdateLevel;

    public HeroDetect(DataModel model) {
        this.model = model;
        snakeTiles = Bootloader.loadSnakePattern();
    }

    public HeroLocations findClosestHero(int x, int y) {
        return findClosestHero(x, y, model.mapHeroes());
    }

    public HeroLocations findClosestHero(int x, int y, ArrayList<HeroLocations> list) {
        HeroLocations hero = null;
        float dist = Float.MAX_VALUE;
        for (HeroLocations h : list) {
            float d = h.squareDistTo(x, y);
            if (d < dist) {
                h.dist = d;
                dist = d;
                hero = h;
            }
        }
        return hero;
    }

    private ArrayList<HeroLocations> getCloseHeroListForId(int x, int y) {
        ArrayList<HeroLocations> list = new ArrayList<>();
        for (HeroLocations h : model.mapHeroes()) {
            if (h.hasType()) continue;
            float d = h.squareDistTo(x, y);
            int CLOSEST_HERO_LIMIT = 3000;
            if (d < CLOSEST_HERO_LIMIT) {
                list.add(h);
            }
        }
        return list;
    }

    public void updateLocations(GroundTileData[] tiles, ObjectData[] newObjects, int[] drops) {
        if (!model.inRealm()) return;

        ArrayList<HeroLocations> nearHeroes = getCloseHeroListForId(model.getIntPlayerX(), model.getIntPlayerY());
        tileChecks(tiles, nearHeroes);
        entityChecks(newObjects, nearHeroes);
        questCheck();
        checkMissing();
        for (int drop : drops) {
            entitys.remove(drop);
        }
    }

    private void checkMissing() {
        HeroLocations h = findClosestHero(model.getIntPlayerX(), model.getIntPlayerY());
        if (h.isMissing(entitys)) {
            markDead(h);
        }
    }

    private void tileChecks(GroundTileData[] tiles, ArrayList<HeroLocations> nearHeroes) {
        for (GroundTileData gtd : tiles) {
            mapTiles[gtd.x][gtd.y] = gtd.type;

            if (nearHeroes.size() == 0) continue;

            if (gtd.type == IdData.SNAKE_STONE_TILE) {
                snakeTileCheck(nearHeroes, gtd, HeroType.SNAKE);
            } else if (gtd.type == IdData.LICH_BLUE_TILE) {
                markHeroes(nearHeroes, gtd, HeroType.LICH, 500, false);
            } else if (gtd.type == IdData.DEMON_LAVA_TILE) {
                markHeroes(nearHeroes, gtd, HeroType.DEMON, 500, false);
            } else if (gtd.type == IdData.PHENIX_BLACK_TILE) {
                if (phoenixTileCheck(gtd.x, gtd.y)) {
                    markHeroes(nearHeroes, gtd, HeroType.PHENIX, 250, false);
                }
            } else if (gtd.type == IdData.PARASITE_REDISH_TILE) {
                markHeroes(nearHeroes, gtd, HeroType.PARASITE, 500, true);
            }
        }
    }

    private void snakeTileCheck(ArrayList<HeroLocations> nearHeroes, GroundTileData gtd, HeroType hero) {
        for (HeroLocations h : nearHeroes) {
            int x = Math.max(0, Math.min(69, gtd.x - h.getX() + Bootloader.SNAKE_TILE_CENTERING));
            int y = Math.max(0, Math.min(69, gtd.y - h.getY() + Bootloader.SNAKE_TILE_CENTERING));

            if (snakeTiles[x][y]) {
                h.setType(hero);
                nearHeroes.remove(h);
                markDead(h);
                return;
            }
        }
    }

    private boolean phoenixTileCheck(short x, short y) {
        if (mapTiles[x + 7][y] == IdData.PHENIX_BLACK_TILE) return true;
        if (mapTiles[x - 7][y] == IdData.PHENIX_BLACK_TILE) return true;
        if (mapTiles[x][y + 7] == IdData.PHENIX_BLACK_TILE) return true;
        return mapTiles[x][y - 7] == IdData.PHENIX_BLACK_TILE;
    }

    private void markHeroes(ArrayList<HeroLocations> nearHeroes, GroundTileData gtd, HeroType hero, float dist, boolean markDead) {
        HeroLocations h = findClosestHero(gtd.x, gtd.y, nearHeroes);
        if (h.dist < dist) {
            nearHeroes.remove(h);
            h.setType(hero);
            if (markDead) {
                markDead(h);
            } else {
                markVisited(h);
            }
        }
    }

    private void entityChecks(ObjectData[] newObjects, ArrayList<HeroLocations> nearHeroes) {
        boolean wallAdded = false;
        for (ObjectData od : newObjects) {
            allEntitys.put(od.status.objectId, od);
            questArrowLevelCheck(od.status);
            if (nearHeroes.size() > 0) {
                HeroLocations h = null;
                if (od.objectType == IdData.ENT_CHERRY_TREE) {
                    h = getHeroLocationByDistance(nearHeroes, od, 500, HeroType.ENT);
                } else if (od.objectType == IdData.GRAVEYARD_CROSS) {
                    h = getHeroLocationByDistance(nearHeroes, od, 500, HeroType.GRAVE);
                } else if (od.objectType == IdData.WOODEN_WALL_HOUSE) {
                    h = getHeroLocationByDistance(nearHeroes, od, 500, HeroType.HOUSE);
                } else if (od.objectType == IdData.DEATH_TREE_MANOR) {
                    h = getHeroLocationByDistance(nearHeroes, od, 700, HeroType.MANOR);
                } else if (od.objectType == IdData.LILLYPAD) {
                    h = getHeroLocationByDistance(nearHeroes, od, 300, HeroType.OASIS);
                } else if (od.objectType == IdData.DESTRUCTIBLE_GRAY_WALL || od.objectType == IdData.GRAY_WALL) {
                    entitys.put(od.status.objectId, od);
                    wallAdded = true;
                    h = findClosestHero((int) od.status.pos.x, (int) od.status.pos.y, nearHeroes);
                    if (h.dist < 500) {
                        if (isCyclopsLoc((int) od.status.pos.x, (int) od.status.pos.y)) {
                            h.setType(HeroType.CYCLOPS);
                        }
                    }
                }

                if (h != null && h.hasType()) {
                    switch (h.getHeroType()) {
                        case GRAVE:
                        case HOUSE:
                        case MANOR:
                            markDead(h);
                            break;
                        default:
                            markVisited(h);
                    }
                    nearHeroes.remove(h);
                }
            }

            if (od.objectType == IdData.ENT_SMALL) {
                addHeroAndSetVisited(od, HeroType.ENT);
            } else if (od.objectType == IdData.ENT_BIG) {
                addHeroAndSetActive(od, HeroType.ENT);
            } else if (od.objectType == IdData.LICH) {
                addHeroAndSetVisited(od, HeroType.LICH);
            } else if (od.objectType == IdData.LICH_KILLABLE) {
                addHeroAndSetActive(od, HeroType.LICH);
            } else if (od.objectType == IdData.GHOST_INVON) {
                addHeroAndSetVisited(od, HeroType.GHOST);
            } else if (od.objectType == IdData.GHOST_KILLABLE) {
                addHeroAndSetActive(od, HeroType.GHOST);
            } else if (od.objectType == IdData.CYCLOPS) {
                addHeroAndSetActive(od, HeroType.CYCLOPS);
            } else if (od.objectType == IdData.OASIS_GIANT) {
                addHeroAndSetActive(od, HeroType.OASIS);
            } else if (od.objectType == IdData.PHENIX) { // Phoenix Lord / 1729 Phoenix Reborn
                addHeroAndSetActive(od, HeroType.PHENIX);
            } else if (od.objectType == IdData.DEMON) {
                addHeroAndSetActive(od, HeroType.DEMON);
            }
        }

        if (wallAdded && isGhostLoc()) {
            HeroLocations h = findClosestHero(model.getIntPlayerX(), model.getIntPlayerY(), nearHeroes);
            if (h.dist < 1000) {
                h.setType(HeroType.GHOST);
                markVisited(h);
                nearHeroes.remove(h);
            }
        }
    }

    private void questArrowLevelCheck(ObjectStatusData osd) {
        if (!questUpdateLevel && osd.objectId == model.getMyId()) {
            for (StatData sd : osd.stats) {
                if (sd.statType == StatType.LEVEL_STAT && sd.statValue == 20) {
                    questUpdateLevel = true;
                    System.out.println("questUpdateLevel " + questUpdateLevel);
                }
            }
        }
    }

    private void questCheck() {
        if (questUpdateLevel && questArrowId != questArrowIdChecked) {
            questArrowIdChecked = questArrowId;
            ObjectData od = allEntitys.get(questArrowId);
            if (od != null) {
                if (od.objectType == IdData.PHENIX) { // remove demons
                    setDeadHeroes(HeroType.DEMON);
                } else if (od.objectType == IdData.CYCLOPS) { // remove demons only, given phoenix shares loc with osasis
                    setDeadHeroes(HeroType.DEMON);
                } else if (od.objectType == IdData.GHOST_INVON || od.objectType == IdData.GHOST_KILLABLE) { // remove cyclops too
                    setDeadHeroes(HeroType.CYCLOPS);
                } else if (od.objectType == IdData.OASIS_GIANT) { // remove ghost too
                    setDeadHeroes(HeroType.GHOST);
                } else if (od.objectType == IdData.ENT_SMALL || od.objectType == IdData.ENT_BIG) { // remove oasis and phoenix too
                    setDeadHeroes(HeroType.OASIS);
                } else if (od.objectType == IdData.LICH || od.objectType == IdData.LICH_KILLABLE) { // remove ents too
                    setDeadHeroes(HeroType.ENT);
                }
            } else {
                questArrowIdChecked = 0;
            }
        }
    }

    private void setDeadHeroes(HeroType ht) {
        for (HeroLocations h : model.mapHeroes()) {
            if (h.getPossibleHeroType() <= ht.getPossibleType()) {
                markDead(h);
            }
        }
    }

    private HeroLocations getHeroLocationByDistance(ArrayList<HeroLocations> nearHeroes, ObjectData od, int dist, HeroType ht) {
        HeroLocations h;
        h = findClosestHero((int) od.status.pos.x, (int) od.status.pos.y, nearHeroes);
        if (h.dist < dist) {
            h.setType(ht);
        }
        return h;
    }

    private void addHeroAndSetVisited(ObjectData od, HeroType ht) {
        entitys.put(od.status.objectId, od);
        HeroLocations h = findClosestHero((int) od.status.pos.x, (int) od.status.pos.y);
        h.setType(ht);
        markVisited(h);
    }

    private void addHeroAndSetActive(ObjectData od, HeroType ht) {
        entitys.put(od.status.objectId, od);
        HeroLocations h = findClosestHero((int) od.status.pos.x, (int) od.status.pos.y);
        h.setType(ht);
        markActive(h);
    }

    private boolean isCyclopsLoc(int x, int y) {
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j < 2; j++) {
                if (i == 0 && j == 0) continue;
                int t = mapTiles[x + i][y + j];
                if (t == IdData.WATER_DEEP_TILE) return true;
            }
        }
        return false;
    }

    private boolean isGhostLoc() {
        int directionCheck = 0;
        HashSet<Float> xFound = new HashSet<>();
        HashSet<Float> yFound = new HashSet<>();
        for (ObjectData o : entitys.values()) {
            if (o.objectType == IdData.DESTRUCTIBLE_GRAY_WALL || o.objectType == IdData.GRAY_WALL) {
                float wallX = o.status.pos.x;
                float wallY = o.status.pos.y;
                if (waterCheck((int) wallX, (int) wallY)) {
                    if (!xFound.contains(wallX) && !yFound.contains(wallY)) {
                        xFound.add(wallX);
                        yFound.add(wallY);
                        directionCheck++;
                    }
                    for (ObjectData o2 : entitys.values()) {
                        if (o.status.objectId == o2.status.objectId) continue;
                        if (o2.objectType == IdData.DESTRUCTIBLE_GRAY_WALL || o2.objectType == IdData.GRAY_WALL) {
                            float wallX2 = o2.status.pos.x;
                            float wallY2 = o2.status.pos.y;
                            if (waterCheck((int) wallX2, (int) wallY2)) {
                                if (!xFound.contains(wallX) && !yFound.contains(wallY)) {
                                    xFound.add(wallX);
                                    yFound.add(wallY);
                                    directionCheck++;
                                }
                            }
                        }

                        if (directionCheck >= 4) {
                            return true;
                        }
                    }
                }
            }
        }
        return directionCheck >= 4;
    }

    private boolean waterCheck(int x, int y) {
        for (int i = -5; i <= 5; i++) {
            for (int j = -5; j < 5; j++) {
                if (i == 0 && j == 0) continue;
                int t = mapTiles[x + i][y + j];
                if (t == IdData.WATER_DEEP_TILE || t == IdData.WATER_SHALLOW_TILE) return false;
            }
        }
        return true;
    }

    public void newTickUpdates(ObjectStatusData[] status) {
        if (!model.inRealm()) return;

        for (ObjectStatusData osd : status) {
            ObjectData found = entitys.get(osd.objectId);
            questArrowLevelCheck(osd);
            if (found != null) {
                for (int j = 0; j < osd.stats.length; j++) {
                    if (osd.stats[j].statTypeNum == 1 && osd.stats[j].statValue == 0) {
                        HeroLocations h = findClosestHero((int) osd.pos.x, (int) osd.pos.y);
                        if (h.matchType(found)) {
                            markDead(h);
                        }
                    } else if (osd.stats[j].statTypeNum == 29 && (osd.stats[j].statValue & 16777216) > 0) {
                        HeroLocations h = findClosestHero((int) osd.pos.x, (int) osd.pos.y);
                        if (h.matchType(found)) {
                            markActive(h);
                        }
                    }
                }
            }
        }
    }

    public void questArrow(QuestObjectIdPacket quest) {
        questArrowId = quest.objectId;
    }

    public void markVisited(HeroLocations h) {
        if (h.getLocationState() == HeroState.MARK_DEAD) return;

        if (h.setState(HeroState.MARK_VISITED)) {
            model.uploadSingleHero(h);
        }
    }

    public void markActive(HeroLocations h) {
        if (h.setState(HeroState.MARK_ACTIVE)) {
            model.uploadSingleHero(h);
        }
    }

    public void markDead(HeroLocations h) {
        if (h.setState(HeroState.MARK_DEAD)) {
            model.uploadSingleHero(h);
        }
    }

    public void reset() {
        questArrowIdChecked = -1;
        allEntitys.clear();
        entitys.clear();
        questUpdateLevel = false;
        for (int i = 0; i < 2048; i++) {
            for (int j = 0; j < 2048; j++) {
                mapTiles[i][j] = 0;
            }
        }
    }
}
