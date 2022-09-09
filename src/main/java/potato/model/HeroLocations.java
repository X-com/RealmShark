package potato.model;

import packets.data.ObjectData;
import potato.data.HeroState;
import potato.data.HeroType;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class HeroLocations {
    public float dist; // Hacky workaround to store distance.

    private static final float MISSING_DIST = 16;

    private int index;
    private String indexString;
    private boolean newCoord = false;
    private int x;
    private int y;
    private int scaledX;
    private int scaledY;
    private int fileX;
    private int fileY;
    private int drawX;
    private int drawY;
    private Color color;
    private long resetTimer = 0;
    private HeroType locationType = HeroType.UNVISITED;
    private HeroState locationState = HeroState.MARK_UNVISITED;
    private int drawIndex = -1;

    public HeroLocations(int index, int x, int y) {
        this.index = index;
        indexString = Integer.toString(index + 1);
        this.x = x;
        this.y = y;
        this.color = Color.green;
        newCoord = true;
    }

    public float squareDistTo(int x, int y) {
        float dx = this.x - x;
        float dy = this.y - y;
        return dx * dx + dy * dy;
    }

    public String toString() {
        return String.format("Idx:%s x:%d y:%d", indexString, x, y);
    }

    public boolean hasType() {
        return locationType != HeroType.UNVISITED;
    }

    public HeroType getHeroType() {
        return locationType;
    }

    public void setType(HeroType type) {
        locationType = type;
        locationState = HeroState.MARK_VISITED;
        drawIndex = getDrawIndex();
        resetTimer = System.currentTimeMillis() + 2200;
    }

    public boolean setState(HeroState state) {
        if (locationState == state) return false;
        locationState = state;
        drawIndex = getDrawIndex();
        resetTimer = System.currentTimeMillis() + 2200;
        return true;
    }

    public boolean isMissing(HashMap<Integer, ObjectData> entitys) {
        if (locationState == HeroState.MARK_DEAD) return false;
        if (dist > locationType.missing()) return false;
        if (entitys.size() == 0) return true;
        for (Map.Entry<Integer, ObjectData> e : entitys.entrySet()) {
            ObjectData o = e.getValue();
            if (matchType(o) && squareDistTo((int) o.status.pos.x, (int) o.status.pos.y) < 500) return false;
        }
        return true;
    }

    public int getMarker() {
        return locationType.index() + locationState.index * 16;
    }

    public int getDrawIndex() {
        return (locationType.index() - 1) * 3 + (locationState.index - 1);
    }

    public void setMarker(int marker, boolean ignoreTimer) {
        long t = resetTimer - System.currentTimeMillis();
        if (!ignoreTimer && t > 0) return;

        int typeIndex = marker % 16;
        int stateIndex = marker / 16;
        locationType = HeroType.byOrdinal(typeIndex);
        locationState = HeroState.byOrdinal(stateIndex);
        drawIndex = getDrawIndex();
    }

    public boolean matchType(ObjectData found) {
        return locationType.typeMatch(found.objectType);
    }

    public int getIndex() {
        return index;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setDrawX(int i) {
        drawX = i;
    }

    public void setDrawY(int i) {
        drawY = i;
    }

    public int getDrawX() {
        return drawX;
    }

    public int getDrawY() {
        return drawY;
    }

    public Color getColor() {
        return color;
    }

    public String getIndexString() {
        return indexString;
    }

    public int getDrawIndexNum() {
        return drawIndex;
    }

    public void reset() {
        resetTimer = 0;
        locationType = HeroType.UNVISITED;
        locationState = HeroState.MARK_UNVISITED;
        drawIndex = -1;
    }

//    public void entityKilled(HeroType type) {
//        if (type != locationType) System.out.println("marking missmached");
//        locationState = HeroState.MARK_DEAD;
//
//        switch (type) {
//            case ENT:
//                break;
//            case LICH:
//                break;
//            case GHOST:
//                break;
//            case CYCLOPS:
//                return;
//            case PHENIX:
//                break;
//            case OASIS:
//                break;
//            case DEMON:
//                break;
//        }
//    }

//    public void entityActivated(HeroType type) {
//        if (type != locationType) System.out.println("marking missmached");
//        locationState = HeroState.MARK_ACTIVE;
//
//        switch (type) {
//            case ENT:
//                break;
//            case LICH:
//                break;
//            case GHOST:
//                break;
//        }
//    }
}
