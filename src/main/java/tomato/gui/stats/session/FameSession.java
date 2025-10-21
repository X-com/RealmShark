package tomato.gui.stats.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import tomato.gui.stats.Fame;
import tomato.gui.stats.data.MapFameData;

/**
 * Represents a fame tracking session that can be saved and loaded
 */
public class FameSession {

    private String sessionName;
    private long createdTimestamp;
    private long lastModifiedTimestamp;
    private HashMap<Integer, List<Fame>> characterFameData;
    private HashMap<Integer, List<MapFameData>> characterMapFameData;
    private HashMap<Integer, String> characterClassNames;
    private String description;
    private boolean readOnly;

    public FameSession() {
        this.sessionName = "Unnamed Session";
        this.createdTimestamp = System.currentTimeMillis();
        this.lastModifiedTimestamp = this.createdTimestamp;
        this.characterFameData = new HashMap<>();
        this.characterMapFameData = new HashMap<>();
        this.characterClassNames = new HashMap<>();
        this.description = "";
        this.readOnly = false;
    }

    public FameSession(String sessionName) {
        this();
        this.sessionName = sessionName;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
        updateModifiedTimestamp();
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public long getLastModifiedTimestamp() {
        return lastModifiedTimestamp;
    }

    public HashMap<Integer, List<Fame>> getCharacterFameData() {
        return characterFameData;
    }

    public void setCharacterFameData(
        HashMap<Integer, List<Fame>> characterFameData
    ) {
        this.characterFameData = characterFameData;
        updateModifiedTimestamp();
    }

    public HashMap<Integer, List<MapFameData>> getCharacterMapFameData() {
        return characterMapFameData;
    }

    public HashMap<Integer, String> getCharacterClassNames() {
        return characterClassNames;
    }

    public void setCharacterClassNames(
        HashMap<Integer, String> characterClassNames
    ) {
        this.characterClassNames = characterClassNames;
        updateModifiedTimestamp();
    }

    public void setCharacterMapFameData(
        HashMap<Integer, List<MapFameData>> characterMapFameData
    ) {
        this.characterMapFameData = characterMapFameData;
        updateModifiedTimestamp();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        checkReadOnly();
        this.description = description;
        updateModifiedTimestamp();
    }

    public void addCharacterData(int characterId, List<Fame> fameData) {
        checkReadOnly();
        characterFameData.put(characterId, new ArrayList<>(fameData));
        updateModifiedTimestamp();
    }

    public void addCharacterData(
        int characterId,
        String className,
        List<Fame> fameData
    ) {
        checkReadOnly();
        characterFameData.put(characterId, new ArrayList<>(fameData));
        characterClassNames.put(characterId, className);
        updateModifiedTimestamp();
    }

    public void addCharacterMapData(
        int characterId,
        List<MapFameData> mapFameData
    ) {
        checkReadOnly();
        characterMapFameData.put(characterId, new ArrayList<>(mapFameData));
        updateModifiedTimestamp();
    }

    public void addCharacterMapData(
        int characterId,
        String className,
        List<MapFameData> mapFameData
    ) {
        checkReadOnly();
        characterMapFameData.put(characterId, new ArrayList<>(mapFameData));
        characterClassNames.put(characterId, className);
        updateModifiedTimestamp();
    }

    public List<Fame> getCharacterData(int characterId) {
        return characterFameData.get(characterId);
    }

    public boolean hasCharacterData(int characterId) {
        return characterFameData.containsKey(characterId);
    }

    public void removeCharacterData(int characterId) {
        checkReadOnly();
        characterFameData.remove(characterId);
        characterMapFameData.remove(characterId);
        characterClassNames.remove(characterId);
        updateModifiedTimestamp();
    }

    public void clearAllData() {
        checkReadOnly();
        characterFameData.clear();
        characterMapFameData.clear();
        characterClassNames.clear();
        updateModifiedTimestamp();
    }

    private void updateModifiedTimestamp() {
        checkReadOnly();
        this.lastModifiedTimestamp = System.currentTimeMillis();
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    private void checkReadOnly() {
        if (readOnly) {
            throw new IllegalStateException("Cannot modify read-only session");
        }
    }

    @Override
    public String toString() {
        return (
            "FameSession{" +
            "sessionName='" +
            sessionName +
            '\'' +
            ", createdTimestamp=" +
            createdTimestamp +
            ", lastModifiedTimestamp=" +
            lastModifiedTimestamp +
            ", characterCount=" +
            characterFameData.size() +
            ", mapDataCount=" +
            characterMapFameData.size() +
            ", classNamesCount=" +
            characterClassNames.size() +
            ", description='" +
            description +
            '\'' +
            ", readOnly=" +
            readOnly +
            '}'
        );
    }
}
