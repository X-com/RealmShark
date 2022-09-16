package potato.model;

import packets.data.GroundTileData;
import packets.data.ObjectData;
import packets.data.ObjectStatusData;
import packets.data.WorldPosData;
import potato.control.MouseKeyController;
import potato.control.ScreenLocatorController;
import potato.control.ServerHTTP;
import potato.data.HeroState;
import potato.data.HeroType;
import potato.data.IdData;
import potato.view.RenderViewer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class DataModel {

    RenderViewer renderer;
    ServerHTTP serverHTTP;
    MouseKeyController mouseKey;
    ScreenLocatorController locator;

    private final int CLOSEST_HERO_LIMIT = 3000;

    int zoom = 0;
    int mapWidth = 300;
    int mapHeight = 300;
    int x;
    int y;
    private long serverTime;
    private long realmClosingTime;
    private int heroesLeft = 0;
    private boolean inRealm = true;

    private int mapIndex = 0;
    private ArrayList<HeroLocations>[] mapCoords;
    private HashMap<Integer, ObjectData> entitys = new HashMap<>();
    private int[][] mapTiles = new int[2048][2048];
    HashSet<Integer>[] mapTileData;
    private boolean newRealmCheck = false;
    private long seed;
    private String realmName;

    public DataModel() {
        mapCoords = Bootloader.loadMapCoords();
        mapTileData = Bootloader.loadTiles();
        serverHTTP = new ServerHTTP(this);
        renderer = new RenderViewer(mapCoords);
        locator = new ScreenLocatorController(renderer, this);
        mouseKey = new MouseKeyController(this, renderer, serverHTTP);

        locator.locateLoop();
        renderer.renderLoop();
    }

    private ArrayList<HeroLocations> mapCoords() {
        return mapCoords[mapIndex];
    }

    public HeroLocations findClosestHero() {
        return findClosestHero(x, y);
    }

    public HeroLocations findClosestHero(int x, int y) {
        return findClosestHero(x, y, mapCoords[mapIndex]);
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

    private ArrayList<HeroLocations> getCloseHeroListForIding(int x, int y) {
        ArrayList<HeroLocations> list = new ArrayList<>();
        for (HeroLocations h : mapCoords()) {
            if (h.hasType()) continue;
            float d = h.squareDistTo(x, y);
            if (d < CLOSEST_HERO_LIMIT) {
                list.add(h);
            }
        }
        return list;
    }

    public void updateLocations(GroundTileData[] tiles, ObjectData[] newObjects, int[] drops) {
        if (!inRealm) return;

        ArrayList<HeroLocations> nearHeroes = getCloseHeroListForIding(x, y);
        tileChecks(tiles, nearHeroes);
        entityChecks(newObjects, nearHeroes);
        checkMissing(nearHeroes);
        for (int i = 0; i < drops.length; i++) {
            entitys.remove(drops[i]);
//            WorldPosData h = entitys.remove(drops[i]);
//            if (h != null) {
//                System.out.println("removed: " + drops[i]);
//            }
        }
    }

    private void checkMissing(ArrayList<HeroLocations> nearHeroes) {
        HeroLocations h = findClosestHero(x, y);
//        System.out.println(h.dist);
        if (h.isMissing(entitys)) {
            markDead(h);
        }
    }

    private void tileChecks(GroundTileData[] tiles, ArrayList<HeroLocations> nearHeroes) {
        for (int i = 0; i < tiles.length; i++) {
            GroundTileData gtd = tiles[i];
            mapTiles[gtd.x][gtd.y] = gtd.type;

            if (nearHeroes.size() == 0) continue;

            HeroType hero = null;
            float dist = 0;

            if (gtd.type == IdData.SNAKE_STONE_TILE) {
                hero = HeroType.SNAKE;
                dist = 1000;
            } else if (gtd.type == IdData.LICH_BLUE_TILE) {
                hero = HeroType.LICH;
                dist = 500;
            } else if (gtd.type == IdData.DEMON_LAVA_TILE) {
                hero = HeroType.DEMON;
                dist = 500;
            } else if (gtd.type == IdData.PHENIX_BLACK_TILE) {
                if (phoenixTileCheck(gtd.x, gtd.y)) {
                    hero = HeroType.PHENIX;
                    dist = 250;
                }
            } else if (gtd.type == IdData.PARASITE_REDISH_TILE) {
                hero = HeroType.PARASITE;
                dist = 500;
            }

            if (hero != null) {
                HeroLocations h = findClosestHero(gtd.x, gtd.y, nearHeroes);
                if (h.dist < dist) {
                    nearHeroes.remove(h);
                    h.setType(hero);
                    switch (hero) {
                        case PARASITE:
                        case SNAKE:
                            markDead(h);
                            break;
                        default:
                            markVisited(h);
                    }
                }
            }
        }
    }

    private boolean phoenixTileCheck(short x, short y) {
        if (mapTiles[x + 7][y] == IdData.PHENIX_BLACK_TILE) return true;
        if (mapTiles[x - 7][y] == IdData.PHENIX_BLACK_TILE) return true;
        if (mapTiles[x][y + 7] == IdData.PHENIX_BLACK_TILE) return true;
        if (mapTiles[x][y - 7] == IdData.PHENIX_BLACK_TILE) return true;

        return false;
    }

    private void entityChecks(ObjectData[] newObjects, ArrayList<HeroLocations> nearHeroes) {
        boolean wallAdded = false;
        for (int i = 0; i < newObjects.length; i++) {
            ObjectData od = newObjects[i];
            if (nearHeroes.size() > 0) {
                HeroLocations h = null;
                if (od.objectType == IdData.ENT_CHERRY_TREE) {
                    h = findClosestHero((int) od.status.pos.x, (int) od.status.pos.y, nearHeroes);
                    if (h.dist < 500) {
                        h.setType(HeroType.ENT);
                    }
                } else if (od.objectType == IdData.GRAVEYARD_CROSS) {
                    h = findClosestHero((int) od.status.pos.x, (int) od.status.pos.y, nearHeroes);
                    if (h.dist < 500) {
                        h.setType(HeroType.GRAVE);
                    }
                } else if (od.objectType == IdData.WOODEN_WALL_HOUSE) {
                    h = findClosestHero((int) od.status.pos.x, (int) od.status.pos.y, nearHeroes);
                    if (h.dist < 500) {
                        h.setType(HeroType.HOUSE);
                    }
                } else if (od.objectType == IdData.DEATH_TREE_MANOR) {
                    h = findClosestHero((int) od.status.pos.x, (int) od.status.pos.y, nearHeroes);
                    if (h.dist < 700) {
                        h.setType(HeroType.MANOR);
                    }
                } else if (od.objectType == IdData.LILLYPAD) {
                    h = findClosestHero((int) od.status.pos.x, (int) od.status.pos.y, nearHeroes);
                    if (h.dist < 300) {
                        h.setType(HeroType.OASIS);
                    }
                } else if (od.objectType == IdData.GHOST_INVON || od.objectType == IdData.GHOST_KILLABLE) {
                    h = findClosestHero((int) od.status.pos.x, (int) od.status.pos.y, nearHeroes);
                    if (h.dist < 300) {
                        h.setType(HeroType.GHOST);
                    }
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
                entitys.put(od.status.objectId, od);
            } else if (od.objectType == IdData.ENT_BIG) {
                entitys.put(od.status.objectId, od);
                HeroLocations h = findClosestHero((int) od.status.pos.x, (int) od.status.pos.y);
                markActive(h);
            } else if (od.objectType == IdData.LICH) {
                entitys.put(od.status.objectId, od);
            } else if (od.objectType == IdData.LICH_KILLABLE) {
                entitys.put(od.status.objectId, od);
                HeroLocations h = findClosestHero((int) od.status.pos.x, (int) od.status.pos.y);
                markActive(h);
            } else if (od.objectType == IdData.GHOST_INVON) {
                entitys.put(od.status.objectId, od);
            } else if (od.objectType == IdData.GHOST_KILLABLE) {
                entitys.put(od.status.objectId, od);
                HeroLocations h = findClosestHero((int) od.status.pos.x, (int) od.status.pos.y);
                markActive(h);
            } else if (od.objectType == IdData.CYCLOPS) { // cyclops
                entitys.put(od.status.objectId, od);
            } else if (od.objectType == IdData.OASIS_GIANT) { // Oasis Giant
                entitys.put(od.status.objectId, od);
            } else if (od.objectType == IdData.PHENIX) { // Phoenix Lord / 1729 Phoenix Reborn
                entitys.put(od.status.objectId, od);
            } else if (od.objectType == IdData.DEMON) { // Phoenix Lord / 1729 Phoenix Reborn
                entitys.put(od.status.objectId, od);
            }
//            if (!hashTester.containsKey(od.objectType)) {
//                hashTester.put(od.objectType, od);
//
//                if (od.status.stats.length == 4) {
//                    System.out.println(od);
//                    System.out.println(od.status.stats.length);
//                }
//            }
        }
        if (wallAdded && isGhostLoc()) {
            HeroLocations h = findClosestHero(x, y, nearHeroes);
            if (h.dist < 1000) {
                h.setType(HeroType.GHOST);
                markVisited(h);
                nearHeroes.remove(h);
            }
        }

    }

//    HashMap<Integer, ObjectData> hashTester = new HashMap<>();

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
                if (!waterCheck((int) wallX, (int) wallY, 5)) {
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
                            if (!waterCheck((int) wallX2, (int) wallY2, 5)) {
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

    private boolean waterCheck(int x, int y, int size) {
        for (int i = -size; i <= size; i++) {
            for (int j = -size; j < size; j++) {
                if (i == 0 && j == 0) continue;
                int t = mapTiles[x + i][y + j];
                if (t == IdData.WATER_DEEP_TILE || t == IdData.WATER_SHALLOW_TILE) return true;
            }
        }
        return false;
    }

    public void newTickUpdates(ObjectStatusData[] status) {
        if (!inRealm) return;

        for (int i = 0; i < status.length; i++) {
            ObjectStatusData osd = status[i];
            ObjectData found = entitys.get(osd.objectId);
            if (found != null) {
                for (int j = 0; j < osd.stats.length; j++) {
//                    System.out.println(osd.stats[j]);
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

    public void updateText(String text, int objectId) {
        if (text.contains("oryx_closed_realm")) {
//                System.out.println("-----------" + serverTime + "------------");
            realmClosingTime = serverTime;
            renderer.realmClosed();
        }
    }

    public void setSize(int width, int height) {
        this.mapWidth = width;
        this.mapHeight = height;
    }

    public void setPlayerCoords(int x, int y) {
        this.x = x;
        this.y = y;
        renderer.setPlayerCoords(x, y);
    }

    public void setServerTime(long l) {
        serverTime = l;
        renderer.setServerTime(l);
    }

    public void synchUpdate(int mapIndex, int[] markers) {
        this.mapIndex = mapIndex;
        for (int i = 0; i < mapCoords[mapIndex].size(); i++) {
            mapCoords[mapIndex].get(i).setMarker(markers[i], false);
        }
    }

    public void uploadMap() {
        System.out.println("upload");
//        serverHTTP.uploadMap(mapIndex, mapCoords());
    }

    public void synch() {
        System.out.println("synch");
        serverHTTP.synch();
    }

    public void editZoom(int i) {
        if (i == 1 && zoom > 0) zoom--;
        else if (i == -1 && zoom < 6) zoom++;
        renderer.setZoom(zoom, x, y);
    }

    public void markVisited(HeroLocations h) {
        if (h.setState(HeroState.MARK_VISITED)) {
            serverHTTP.uploadSingleDot(mapIndex, h.getIndex(), h.getMarker());
        }
    }

    public void markActive(HeroLocations h) {
        if (h.setState(HeroState.MARK_ACTIVE)) {
            serverHTTP.uploadSingleDot(mapIndex, h.getIndex(), h.getMarker());
        }
    }

    public void markDead(HeroLocations h) {
        if (h.setState(HeroState.MARK_DEAD)) {
            serverHTTP.uploadSingleDot(mapIndex, h.getIndex(), h.getMarker());
        }
    }

    public void editMapIndex(int i) {
        if (i == 1) {
            if (mapIndex > 0) {
                mapIndex--;
            } else {
                mapIndex = 12;
            }
        } else if (i == -1) {
            if (mapIndex < 12) {
                mapIndex++;
            } else {
                mapIndex = 0;
            }
        }
        for (int j = 0; j < mapCoords[mapIndex].size(); j++) {
            mapCoords[mapIndex].get(j).reset();
        }
        renderer.editMapIndex(mapIndex);
        System.out.println("selecting map: " + (mapIndex + 1));
    }

    public void setInRealm(String name, long s) {
        inRealm = true;
        zoom = 6;
        newRealmCheck = true;
        seed = s;
        realmName = name;
    }

    public void setHeroesLeft(int i) {
        renderer.setHeroesLeft(i);
    }

    public void reset() {
        inRealm = false;
        renderer.setInRealm(false);
        renderer.stuffRender(false);
        heroesLeft = 0;
        renderer.setHeroesLeft(0);
        entitys.clear();

        for (int i = 0; i < mapCoords[mapIndex].size(); i++) {
            mapCoords[mapIndex].get(i).setMarker(0, true);
        }
        for (int i = 0; i < 2048; i++) {
            for (int j = 0; j < 2048; j++) {
                mapTiles[i][j] = 0;
            }
        }
    }

    private int findMapIndex(GroundTileData[] tiles) {
        int[] maps = new int[13];
        for (GroundTileData t : tiles) {
            int num = t.x + t.y * 2048 + t.type * 4194304;
            for (int map = 0; map < mapTileData.length; map++) {
                if (mapTileData[map].contains(num)) {
                    maps[map]++;
                }
            }
        }
        int largest = 0;
        int largestIndex = 0;
        for (int i = 0; i < 13; i++) {
            System.out.printf("Index:%d Count:%d\n", i+1, maps[i]);
            if (maps[i] > largest) {
                largest = maps[i];
                largestIndex = i;
            }
        }
        return largestIndex;
    }

    public void newRealm(GroundTileData[] tiles, WorldPosData pos) {
        if (!newRealmCheck) return;
        mapIndex = findMapIndex(tiles);

        for (int j = 0; j < mapCoords[mapIndex].size(); j++) {
            mapCoords[mapIndex].get(j).reset();
        }
        renderer.editMapIndex(mapIndex);

        renderer.setInRealm(true);
        renderer.setZoom(zoom, (int) pos.x, (int) pos.y);
        renderer.stuffRender(true);

        newRealmCheck = false;
    }
}
