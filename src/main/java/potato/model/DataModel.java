package potato.model;

import packets.data.*;
import packets.incoming.MapInfoPacket;
import packets.incoming.QuestObjectIdPacket;
import packets.incoming.TextPacket;
import potato.model.data.Entity;
import potato.view.opengl.OpenGLPotato;
import potato.control.InputController;
import potato.control.ScreenLocatorController;
import potato.control.ServerSynch;
import util.Util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class DataModel {

    private final OpenGLPotato renderer;
    private final HeroDetect heroDetect;
    private final ServerSynch server;
    private final InputController mouse;
    private final ScreenLocatorController locator;

    public float playerX;
    public float playerY;

    private long serverTime;
    private int heroesLeft = 0;
    private boolean inRealm = false;
    private boolean isShatters = false;

    private boolean newRealmCheck = false;
    private long seed;
    private int myId;
    private boolean saveData = false;
    private MapInfoPacket mapPacketData;

    private final HashSet<Integer>[] mapTileData;
    private final ArrayList<HeroLocations>[] mapHeroes;
    private final HashMap<Integer, Entity> entityList;
    private int mapIndex = 0;
    private String realmName = "";
    private String serverName = "";
    private String tpCooldownString = "";
    private long prismTimer = 0;
    private long castleTimer;
    private String castleTimerString = "";
    private int serverIp;
    private long tpCooldown;
    private boolean setTpCooldown;
    private int keyTime;
    private long openTime;

    private final HashMap<Integer, ObjectData> allEntitys = new HashMap<>();
    private final int[][] mapTiles = new int[2048][2048];
    private float mapWidth;
    private float mapHeight;
    private float zoomStep = 7.8f;
    private float zoomMax = 48f;
    private float zoom = 1f;


    public DataModel() {
        entityList = new HashMap<>();
        mapHeroes = Bootloader.loadMapCoords();
        mapTileData = Bootloader.loadTiles();

        renderer = new OpenGLPotato(this);
        heroDetect = new HeroDetect(this);
        server = new ServerSynch(this);
        mouse = new InputController(this, renderer, server);

        locator = new ScreenLocatorController(renderer);
        if (!Config.instance.manualAlignment) {
            locator.calcMapSizeLoc2();
        }

        renderer.start();
    }

    public ScreenLocatorController getAligner() {
        return locator;
    }

    public ArrayList<HeroLocations> mapHeroes() {
        return mapHeroes[mapIndex];
    }

    public HashMap<Integer, Entity> mapEntitys() {
        return entityList;
    }

    public void updateText(TextPacket p) {
        if (p.text.contains("oryx_closed_realm")) {
            castleTimer = serverTime + 130000;
        } else if (p.text.contains("Location has been marked") && p.objectId == -1) {
            prismTimer = System.currentTimeMillis() + 30000;
        }
//        System.out.println(p);
    }

    public void setPlayerCoords(float x, float y) {
        playerX = x;
        playerY = y;
        renderer.setCamera(x, y, zoom);
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
        for (int i = 0; i < mapHeroes[this.mapIndex].size() && i < markers.length; i++) {
            mapHeroes[this.mapIndex].get(i).setMarker(markers[i], false);
        }
        renderer.setMap(mapIndex);
        renderer.setCamera(playerX, playerY, zoom);
        renderer.renderMap(true);
    }

    public void heroSynch(int heroId, int heroState) {
        mapHeroes[this.mapIndex].get(heroId).setMarker(heroState, false);
    }

    public void editZoom(boolean zoomIn) {
        if (!zoomIn) {
            zoom += zoomStep;
            if (zoom > zoomMax) zoom = zoomMax;
        } else if (zoomIn) {
            zoom -= zoomStep;
            if (zoom < 1) zoom = 1f;
        }
        renderer.setCamera(playerX, playerY, zoom);
    }

    public void refresh() {
        renderer.setCamera(playerX, playerY, zoom);
    }

    public void setInRealm(String name, long s, long gameOpenedTime, int width, int height) {
        newRealmCheck = true;
        inRealm = true;
        if (seed != s) castleTimer = 0;
        seed = s;
        openTime = gameOpenedTime;
        mapWidth = width;
        mapHeight = height;
        setRealmName(name);
        setZoom(width);
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
        if (!inRealm) return;
        server.stopSynch(myId);
        inRealm = false;
        renderer.renderMap(false);
        heroesLeft = 0;

        for (int i = 0; i < mapHeroes[mapIndex].size(); i++) {
            mapHeroes[mapIndex].get(i).setMarker(0, true);
        }
        entityList.clear();
        heroDetect.reset();
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
//            System.out.printf("Index:%d Count:%d\n", i + 1, maps[i]);
            if (maps[i] > largest) {
                largest = maps[i];
                largestIndex = i;
            }
        }
        return largestIndex;
    }

    public void newRealm(GroundTileData[] tiles, WorldPosData pos) {
        if (!newRealmCheck) return;
        playerX = (int) pos.x;
        playerY = (int) pos.y;
        mapIndex = findMapIndex(tiles);
        server.startSynch(myId, serverIp, seed, mapIndex, (int) pos.x, (int) pos.y);

        newRealmCheck = false;
    }

    public void ipChanged(String name, int ip) {
        if (!name.equals("") && !name.equals(serverName)) {
            if (!serverName.equals("")) setTpCooldown = true;
            serverName = name;
            castleTimer = 0;
        }
        serverIp = ip;
    }

    public void checkNewNexus() {
        if (setTpCooldown) {
            tpCooldown = System.currentTimeMillis() + 124000;
            setTpCooldown = false;
        }
    }

    public int getIntPlayerX() {
        return (int) playerX;
    }

    public int getIntPlayerY() {
        return (int) playerY;
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

    public String extraInfo() {
        return String.format("%s%s", prismTimer(), tpCooldownString);
    }

    public String getDungeonTime() {
        long t = System.currentTimeMillis() / 1000 - openTime;
        return String.format("[%d:%02d:%02d]", t / 3600, (t / 60) % 60, t % 60);
    }

    public String getPlayerCoordString() {
        if (Config.instance.showPlayerCoords) return String.format(" x:%d y:%d", getIntPlayerX(), getIntPlayerY());
        return "";
    }

    private String prismTimer() {
        if (prismTimer == 0) return "";
        int t = (int) ((prismTimer - System.currentTimeMillis()) / 100);
        if (t < 1) {
            prismTimer = 0;
            return "";
        }
        return String.format("(%d:%d) ", t / 10, t % 10);
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

    public boolean inRealm() {
        return inRealm;
    }

    public void dispose() {
        locator.dispose();
        mouse.dispose();
        renderer.dispose();
        server.dispose();
    }

    public void updateLocations(GroundTileData[] tiles, ObjectData[] newObjects, int[] drops) {
        heroDetect.updateLocations(tiles, newObjects, drops);
        if (Config.instance.saveMapInfo && saveData) {
            for (GroundTileData gtd : tiles) {
                mapTiles[gtd.x][gtd.y] = gtd.type;
            }
            for (ObjectData od : newObjects) {
                allEntitys.put(od.status.objectId, od);
            }
        }
        if (isShatters) {
            for (ObjectData od : newObjects) {
                if (od.objectType == 33445) { // Shatters Void Phantasm
                    System.out.println("Shatters Void Phantasm added");
                    addEntity(od, "e");
                } else if (od.objectType == 29054) { // Shatters Village Switch
                    System.out.println("Shatters Village Switch added");
                    addEntity(od, "a");
                }
            }
            for (int drop : drops) {
                removeEntity(drop);
            }
        }
    }

    public void newTickUpdates(ObjectStatusData[] status) {
        heroDetect.newTickUpdates(status);
        if (isShatters) {
            for (ObjectStatusData osd : status) {
                int objectId = osd.objectId;
                if (entityList.containsKey(objectId)) {
                    entityList.get(osd.objectId).move(osd.pos);
                }
            }
            renderer.renderMap(true);
        }
    }

    public void uploadSingleHero(HeroLocations h) {
        server.uploadSingleHero(myId, h.getIndex(), h.getState());
    }

    public void questArrow(QuestObjectIdPacket h) {
        heroDetect.questArrow(h);
    }

    public int getMyId() {
        return myId;
    }

    public void setKeyTime(int keyTime) {
        this.keyTime = keyTime;
    }

    private static boolean filteredInstances(String dungName) {
        switch (dungName) {
            case "{s.vault}":  // vault
            case "Daily Quest Room": // quest room
            case "Pet Yard": // pet yard
            case "{s.guildhall}": // guild hall
            case "{s.nexus}": // nexus
            case "Grand Bazaar": // bazaar
                return false;
            default:
                return true;
        }
    }

    public void saveMap(MapInfoPacket packet) {
        if (saveData) {
            DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss");
            LocalDateTime dateTime = LocalDateTime.now();
            String time = dateTimeFormat.format(dateTime);
            String dungeon = inRealm ? ("Realm " + serverName + " " + realmName) : mapPacketData.name;
            String name = "mapData/" + dungeon + "." + time + ".mapdata2-";
            Util.print(name, dungeon);
            Util.print(name, mapPacketData.toString());
            if (inRealm) Util.print(name, "MapIndex:" + (mapIndex + 1));
            Util.print(name, "tiles");
            for (int i = 0; i < 2048; i++) {
                for (int j = 0; j < 2048; j++) {
                    int t = mapTiles[i][j];
                    if (t != 0) {
                        Util.print(name, String.format("%d:%d:%d", i, j, t));
                    }
                }
            }
            Util.print(name, "objects");
            for (ObjectData od : allEntitys.values()) {
                StringBuilder s = new StringBuilder();
                for (StatData sd : od.status.stats) {
                    s.append(";").append(sd.statValue).append(";").append(sd.statValueTwo).append(";").append(sd.stringStatValue).append(";").append(sd.statTypeNum);
                }
                Util.print(name, String.format("%d:%d:%f:%f:%s", od.status.objectId, od.objectType, od.status.pos.x, od.status.pos.y, s.substring(1)));
            }
            for (int[] row : mapTiles) {
                Arrays.fill(row, 0);
            }
        }
        allEntitys.clear();
        for (int i = 0; i < 2048; i++) {
            for (int j = 0; j < 2048; j++) {
                mapTiles[i][j] = 0;
            }
        }
        mapPacketData = packet;
        saveData = filteredInstances(packet.displayName);
    }

    public boolean isShatters() {
        return isShatters;
    }

    public void setInShatters(long s, long gameOpenedTime, int width, int height) {
        isShatters = true;
        seed = s;
        openTime = gameOpenedTime;
        entityList.clear();
        mapWidth = width;
        mapHeight = height;
        setZoom(width);
    }

    public void addEntity(ObjectData od, String shape) {
        entityList.put(od.status.objectId, new Entity(od.status.pos.x, od.status.pos.y, shape));
    }

    public void removeEntity(int id) {
        entityList.remove(id);
    }

    private void setZoom(int mapSize) {
        renderer.mapSize(mapSize);
        zoom = 1f;
        if (mapSize == 2048) {
            zoomMax = 48f;
            zoomStep = 7.8f;
        } else if (mapSize == 512) {
            zoomMax = 12f;
            zoomStep = 1.8f;
        } else if (mapSize == 256) {
            zoomMax = 6f;
            zoomStep = 1f;
        } else if (mapSize == 128) {
            zoomMax = 3f;
            zoomStep = 1f;
        }
    }
}
