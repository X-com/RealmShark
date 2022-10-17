package potato.model;

import packets.data.ObjectData;
import potato.data.HeroState;
import potato.data.HeroType;
import potato.view.OpenGLPotato;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class HeroLocations {
    public float dist; // Hacky workaround to store distance.

    private final int index;
    private final String indexString;
    private final int x;
    private final int y;
    private final Color color;
    private long resetTimer = 0;
    private HeroType locationType = HeroType.UNVISITED;
    private HeroState locationState = HeroState.MARK_UNVISITED;
    public static int largest = 0;

    public HeroLocations(int index, int x, int y) {
        this.index = index;
        indexString = Integer.toString(index + 1);
        this.x = x;
        this.y = y;
        this.color = Color.green;
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

    public void setType(HeroType type, OpenGLPotato renderer) {
        locationType = type;
        renderer.updateHero(this);
        resetTimer = System.currentTimeMillis() + 2200;
    }

    public boolean setState(HeroState state, OpenGLPotato renderer) {
        if (locationState == state) return false;
        locationState = state;
        renderer.updateHero(this);
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

    public int getState() {
        return locationType.getIndex() + locationState.getIndex() * 16;
    }

    public void setMarker(int marker, boolean ignoreTimer, OpenGLPotato renderer) {
        long t = resetTimer - System.currentTimeMillis();
        if (!ignoreTimer && t > 0) return;

        int typeIndex = marker % 16;
        int stateIndex = marker / 16;
        locationType = HeroType.byOrdinal(typeIndex);
        locationState = HeroState.byOrdinal(stateIndex);
        renderer.updateHero(this);
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

    public Color getColor() {
        return locationState.getColor();
    }

    public int getHeroTypeId() {
        return locationType.getIndex();
    }

    public String getIndexString() {
        return indexString;
    }

    public void reset() {
        resetTimer = 0;
        locationType = HeroType.UNVISITED;
        locationState = HeroState.MARK_UNVISITED;
    }
}
