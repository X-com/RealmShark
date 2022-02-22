package packets.data;

import packets.data.enums.StatType;
import packets.reader.BufferReader;
import util.Util;

public class StatData {
    /**
     * The type of stat
     */
    public StatType statType;
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
     * <p>
     * TODO: full stat types list is not available
     */
    public StatData deserialize(BufferReader buffer) {
        int type = buffer.readUnsignedByte();
        statType = StatType.byOrdinal(type);
        if (statType == null) Util.print("StatType enum error in deserializer: " + type);

        if (isStringStat()) {
            stringStatValue = buffer.readString();
        } else {
            statValue = buffer.readCompressedInt();
        }
        statValueTwo = buffer.readByte();

        return this;
    }

    private boolean isStringStat() {
        if (StatType.NAME_STAT == statType // 31
                || StatType.GUILD_NAME_STAT == statType // 62
                || StatType.PET_NAME_STAT == statType // 82
                || StatType.ACCOUNT_ID_STAT == statType // 38
                || StatType.OWNER_ACCOUNT_ID_STAT == statType // 54
                || StatType.GRAVE_ACCOUNT_ID == statType // 115
                || StatType.TEXTURE_STAT == statType // 80
                || StatType.UNKNOWN121 == statType // 121
                || StatType.UNKNOWN123 == statType) { // 123
            return true;
        }
        return false;
    }
}
