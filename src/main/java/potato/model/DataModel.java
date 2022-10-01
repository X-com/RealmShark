package potato.model;

import packets.data.GroundTileData;
import packets.data.ObjectData;
import packets.data.ObjectStatusData;
import packets.data.WorldPosData;
import potato.control.MouseController;
import potato.control.ScreenLocatorController;
import potato.control.ServerSynch;
import potato.data.HeroState;
import potato.data.HeroType;
import potato.data.IdData;
import potato.view.RenderViewer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class DataModel {

    private final RenderViewer renderer;
    private final ServerSynch server;
    private final MouseController mouse;
    private final ScreenLocatorController locator;

    private int zoom = 0;
    private int playerX;
    private int playerY;
    private int offsetX;
    private int offsetY;
    private int imageOffsetX;
    private int imageOffsetY;

    private long serverTime;
    private int heroesLeft = 0;
    private boolean inRealm = true;

    private final HashMap<Integer, ObjectData> entitys = new HashMap<>();
    private final HashSet<Integer>[] mapTileData;
    private final int[][] mapTiles = new int[2048][2048];
    private boolean newRealmCheck = false;
    private long seed;
    private int myId;

    private final ArrayList<HeroLocations>[] mapCoords;
    private int frameWidth = 0;
    private int frameHeight = 0;
    private int mapIndex = 0;
    private String realmName = "";
    private String serverName = "";
    private String tpCooldownString = "";
    private long castleTimer;
    private String castleTimerString = "";
    private int serverIp;
    private long tpCooldown;

    private final int[] circleSize = {5, 7, 8, 9, 10, 16, 130};
    private final int[] fontSize = {0, 8, 8, 8, 10, 16, 130};
    public static final int[] imageSize = {308, 366, 452, 600, 925, 1725, 14788};
    private final int[] zooms = {0, 70, 172, 340, 670, 1560, 15300};
    private final float[] m = {0, 0.0294f, 0.072f, 0.1447f, 0.286f, 2f / 3f, 6.66f};
    private final int[] k = {0, 6, 12, 22, 44, 100, 820};
    public static final float[] imageM = {0, -0.03f, -0.071f, -0.144f, -0.3f, -0.691f, -7.222f};
    public static final int[] imageK = {22, 24, 24, 24, 24, 20, 175};

    private final BufferedImage[] images;
    private final Image[] heroIcon;

    public DataModel() {
        mapCoords = Bootloader.loadMapCoords();
        mapTileData = Bootloader.loadTiles();
        server = new ServerSynch(this);
        renderer = new RenderViewer(this);
        locator = new ScreenLocatorController(renderer, this);
        mouse = new MouseController(this, renderer, server);

        images = Bootloader.loadMaps();
        heroIcon = Bootloader.loadHeroIcons();

        locator.locateLoop();
        renderer.renderLoop();
    }

    private ArrayList<HeroLocations> mapCoords() {
        return mapCoords[mapIndex];
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

    private ArrayList<HeroLocations> getCloseHeroListForId(int x, int y) {
        ArrayList<HeroLocations> list = new ArrayList<>();
        for (HeroLocations h : mapCoords()) {
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
        if (!inRealm) return;

        ArrayList<HeroLocations> nearHeroes = getCloseHeroListForId(playerX, playerY);
        tileChecks(tiles, nearHeroes);
        entityChecks(newObjects, nearHeroes);
        checkMissing(nearHeroes);
        for (int drop : drops) {
            entitys.remove(drop);
//            WorldPosData h = entitys.remove(drops[i]);
//            if (h != null) {
//                System.out.println("removed: " + drops[i]);
//            }
        }
    }

    private void checkMissing(ArrayList<HeroLocations> nearHeroes) {
        HeroLocations h = findClosestHero(playerX, playerY);
        if (h.isMissing(entitys)) {
            markDead(h);
        }
    }

    private void tileChecks(GroundTileData[] tiles, ArrayList<HeroLocations> nearHeroes) {
        for (GroundTileData gtd : tiles) {
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
        return mapTiles[x][y - 7] == IdData.PHENIX_BLACK_TILE;
    }

    private void entityChecks(ObjectData[] newObjects, ArrayList<HeroLocations> nearHeroes) {
        boolean wallAdded = false;
        for (ObjectData od : newObjects) {
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
            HeroLocations h = findClosestHero(playerX, playerY, nearHeroes);
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
        if (!inRealm) return;

        for (ObjectStatusData osd : status) {
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

    public void updateText(String text) {
        if (text.contains("oryx_closed_realm")) {
            castleTimer = serverTime + 130000;
        }
    }

    public void setSize(int width, int height) {
        frameWidth = width;
        frameHeight = height;
    }

    public void setPlayerCoords(int x, int y) {
        playerX = x;
        playerY = y;
        offsetX = (int) (m[zoom] * x + k[zoom]);
        offsetY = (int) (m[zoom] * y + k[zoom]);
        imageOffsetX = (int) (imageM[zoom] * x + imageK[zoom]);
        imageOffsetY = (int) (imageM[zoom] * y + imageK[zoom]) + 4;
        calcCoords();
    }

    private void calcCoords() {
//        System.out.printf("offset x:%d y:%d\n", offsetX, offsetY);
        int sW = frameWidth + zooms[zoom];
        int sH = frameHeight + zooms[zoom];
        for (HeroLocations h : mapCoords[mapIndex]) {
            double dx = ((float) (h.getX() + 150) / 2350);
            double dy = ((float) (h.getY() + 150) / 2350);
            h.setDrawX((int) (dx * sW) - offsetX);
            h.setDrawY((int) (dy * sH) - offsetY);
        }
    }

    public void setServerTime(long l) {
        serverTime = l;
        if (castleTimer != 0) {
            long remTime = (castleTimer - serverTime) / 1000;
            if (remTime <= 0) {
                castleTimer = 0;
                castleTimerString = "";
                return;
            }
            castleTimerString = String.format("Castle %d:%02d", remTime / 60, remTime % 60);
        }
    }

    public void serverTickTime(int l) {
        if (tpCooldown == 0) return;
        int remTime = (int) ((tpCooldown - System.currentTimeMillis()) / 1000);
        if (remTime < 0) {
            tpCooldown = 0;
            tpCooldownString = "";
            return;
        }
        tpCooldownString = String.format("(tp:%ds)", remTime);
    }

    public void initSynch(int mapIndex, int[] markers) {
        this.mapIndex = mapIndex;
        for (int i = 0; i < mapCoords[this.mapIndex].size(); i++) {
            mapCoords[this.mapIndex].get(i).setMarker(markers[i], false);
        }
        calcCoords();
        renderer.renderMap(true);
    }

    public void heroSynch(int heroId, int heroState) {
        mapCoords[this.mapIndex].get(heroId).setMarker(heroState, false);
    }

    public void editZoom(int i) {
        if (i == 1 && zoom > 0) zoom--;
        else if (i == -1 && zoom < 6) zoom++;
        imageOffsetX = (int) (imageM[zoom] * playerX + imageK[zoom]);
        imageOffsetY = (int) (imageM[zoom] * playerY + imageK[zoom]) + 4;
        offsetX = (int) (m[zoom] * playerX + k[zoom]);
        offsetY = (int) (m[zoom] * playerY + k[zoom]);
        calcCoords();
    }

    public void markVisited(HeroLocations h) {
        if (h.setState(HeroState.MARK_VISITED)) {
            System.out.println("upload visited");
            server.uploadSingleHero(myId, h.getIndex(), h.getState());
        }
    }

    public void markActive(HeroLocations h) {
        if (h.setState(HeroState.MARK_ACTIVE)) {
            server.uploadSingleHero(myId, h.getIndex(), h.getState());
        }
    }

    public void markDead(HeroLocations h) {
        if (h.setState(HeroState.MARK_DEAD)) {
            server.uploadSingleHero(myId, h.getIndex(), h.getState());
        }
    }

    public void setInRealm(String name, long s) {
        inRealm = true;
        zoom = 6;
        newRealmCheck = true;
        seed = s;
        setRealmName(name);
    }

    public void setRealmName(String name) {
        int i = name.indexOf('.');
        if (i > 0) {
            realmName = name.substring(i + 1);
        } else {
            realmName = name;
        }
    }

    public void setHeroesLeft(int i) {
        heroesLeft = i;
    }

    public void reset() {
        server.stopSynch(myId);
        inRealm = false;
        renderer.renderMap(false);
        heroesLeft = 0;
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
            System.out.printf("Index:%d Count:%d\n", i + 1, maps[i]);
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
        server.startSynch(myId, serverIp, seed, mapIndex, (int) pos.x, (int) pos.y);

//        for (int j = 0; j < mapCoords[mapIndex].size(); j++) {
//            mapCoords[mapIndex].get(j).reset();
//        }
//        renderer.editMapIndex(mapIndex);
//
//        renderer.stuffRender(true);

        newRealmCheck = false;
    }

    public void ipChanged(String name, int ip) {
        if (!name.equals("") && !name.equals(serverName)) {
            if (!serverName.equals("")) tpCooldown = System.currentTimeMillis() + 124000;
            serverName = name;
            castleTimer = 0;
        }
        serverIp = ip;
    }

    public void setMyId(int id) {
        myId = id;
    }

    public String getServerName() {
        return serverName;
    }

    public String getRealmName() {
        return realmName;
    }

    public String getTpCooldown() {
        return tpCooldownString;
    }

    public ArrayList<HeroLocations> getMapCoords() {
        return mapCoords[mapIndex];
    }

    public int getMapIndex() {
        return mapIndex;
    }

    public Image getMapImage() {
        return images[mapIndex];
    }

    public int getPlayerX() {
        return playerX;
    }

    public int getPlayerY() {
        return playerY;
    }

    public int getFontSize() {
        return fontSize[zoom];
    }

    public int getImageOffsetX() {
        return imageOffsetX;
    }

    public int getImageOffsetY() {
        return imageOffsetY;
    }

    public int getImageSize() {
        return imageSize[zoom];
    }

    public int getCircleSize() {
        return circleSize[zoom];
    }

    public Image getHeroImage(int drawIndex) {
        return heroIcon[drawIndex];
    }

    public int getHeroesLeft() {
        return heroesLeft;
    }

    public String getCastleTimer() {
        return castleTimerString;
    }

    public boolean renderCastleTimer() {
        return castleTimer != 0;
    }

    public int getFrameHeight() {
        return frameHeight;
    }

    public boolean inRealm() {
        return inRealm;
    }

    public void dispose() {
        locator.dispose();
        mouse.dispose();
        renderer.dispose();
        server.dispose();
    }
}
