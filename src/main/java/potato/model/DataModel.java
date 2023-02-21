package potato.model;

import packets.incoming.QuestObjectIdPacket;
import packets.incoming.TextPacket;
import potato.view.opengl.OpenGLPotato;
import packets.data.GroundTileData;
import packets.data.ObjectData;
import packets.data.ObjectStatusData;
import packets.data.WorldPosData;
import potato.control.InputController;
import potato.control.ScreenLocatorController;
import potato.control.ServerSynch;

import java.util.ArrayList;
import java.util.HashSet;

public class DataModel {

    private final OpenGLPotato renderer;
    private final HeroDetect heroDetect;
    private final ServerSynch server;
    private final InputController mouse;
    private final ScreenLocatorController locator;

    private int zoom = 0;
    public float playerX;
    public float playerY;

    private long serverTime;
    private int heroesLeft = 0;
    private boolean inRealm = false;

    private boolean newRealmCheck = false;
    private long seed;
    private int myId;

    private final HashSet<Integer>[] mapTileData;
    private final ArrayList<HeroLocations>[] mapHeroes;
    private int mapIndex = 0;
    private String realmName = "";
    private String serverName = "";
    private String tpCooldownString = "";
    private long castleTimer;
    private String castleTimerString = "";
    private int serverIp;
    private long tpCooldown;
    private boolean setTpCooldown;

    public DataModel() {
        mapHeroes = Bootloader.loadMapCoords();
        mapTileData = Bootloader.loadTiles();

        heroDetect = new HeroDetect(this);
        server = new ServerSynch(this);
        renderer = new OpenGLPotato(this);
        locator = new ScreenLocatorController(renderer);
        mouse = new InputController(this, renderer, server);

        renderer.start();
        if (!Config.instance.manualAlignment) {
            try {
                while (renderer.waitfor) {
                    Thread.sleep(1);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            locator.calcMapSizeLoc2();
        }
    }

    public ScreenLocatorController getAligner() {
        return locator;
    }

    public ArrayList<HeroLocations> mapHeroes() {
        return mapHeroes[mapIndex];
    }

    public void updateText(TextPacket p) {
        if (p.text.contains("oryx_closed_realm")) {
            castleTimer = serverTime + 130000;
            System.out.println(p);
        }
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
        for (int i = 0; i < mapHeroes[this.mapIndex].size(); i++) {
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
        if (!zoomIn && zoom > 0) zoom--;
        else if (zoomIn && zoom < 6) zoom++;
        renderer.setCamera(playerX, playerY, zoom);
    }

    public void refresh() {
        renderer.setCamera(playerX, playerY, zoom);
    }

    public void setInRealm(String name, long s) {
        newRealmCheck = true;
        inRealm = true;
        zoom = 6;
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

        for (int i = 0; i < mapHeroes[mapIndex].size(); i++) {
            mapHeroes[mapIndex].get(i).setMarker(0, true);
        }
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

    public String getTpCooldown() {
        return tpCooldownString;
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
    }

    public void newTickUpdates(ObjectStatusData[] status) {
        heroDetect.newTickUpdates(status);
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
}
