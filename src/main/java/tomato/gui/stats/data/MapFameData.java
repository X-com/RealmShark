package tomato.gui.stats.data;

/**
 * Represents fame tracking data for a specific map/dungeon session
 * This class is used for serialization and persistence of map fame data
 */
public class MapFameData {
    public String mapName;
    public long startTime;
    public long endTime;
    public double startFame;
    public double endFame;

    // Default constructor for Gson serialization
    public MapFameData() {
    }

    public MapFameData(String mapName, long startTime, double startFame) {
        this.mapName = mapName;
        this.startTime = startTime;
        this.endTime = startTime;
        this.startFame = startFame;
        this.endFame = startFame;
    }

    public long getTimeSpent() {
        return endTime - startTime;
    }

    public double getFameGained() {
        return endFame - startFame;
    }

    public String getTimeSpentFormatted() {
        long seconds = getTimeSpent() / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    public String toString() {
        return "MapFameData{" +
                "mapName='" + mapName + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", startFame=" + startFame +
                ", endFame=" + endFame +
                ", fameGained=" + getFameGained() +
                ", timeSpent=" + getTimeSpentFormatted() +
                '}';
    }
}
