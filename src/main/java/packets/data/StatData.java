package packets.data;

import packets.data.enums.StatType;
import packets.reader.BufferReader;

public class StatData {
    /**
     * The type of stat
     */
//    public StatType statType;
    public int statType;
    /**
     * The number value of this stat, if this is not a string stat
     */
    public int statValue;
    /**
     * The string value of this stat, if this is a string stat
     */
    public String stringStatValue;
    /**
     * The secondary stat value
     */
    public int statValueTwo;

    /**
     * Deserializer method to extract data from the buffer.
     *
     * @param buffer Data that needs deserializing.
     * @return Returns this object after deserializing.
     *
     * TODO: full stat types list is not available
     */
    public StatData deserialize(BufferReader buffer) {
        statType = buffer.readUnsignedByte();
//        statType = StatType.byOrdinal(buffer.readUnsignedByte());
//        if (statType == null) Util.print("StatType enum error in deserializer");

        if (isStringStat()) {
            stringStatValue = buffer.readString();
        } else {
            statValue = buffer.readCompressedInt();
        }
        statValueTwo = buffer.readByte();

        return this;
    }

    private boolean isStringStat() {
        if (StatType.NAME_STAT.get() == statType // 31
        || StatType.GUILD_NAME_STAT.get() == statType // 62
        || StatType.PET_NAME_STAT.get() == statType // 82
        || StatType.ACCOUNT_ID_STAT.get() == statType // 38
        || StatType.OWNER_ACCOUNT_ID_STAT.get() == statType // 54
        || StatType.GRAVE_ACCOUNT_ID.get() == statType // 115
        || StatType.TEXTURE_STAT.get() == statType // 80
        || StatType.UNKNOWN121.get() == statType // 121
        || StatType.UNKNOWN123.get() == statType) { // 123
            return true;
        }
        return false;
    }
}
