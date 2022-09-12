package packets.data;

import packets.data.enums.ConditionBits;
import packets.data.enums.ConditionNewBits;
import packets.data.enums.StatType;
import packets.reader.BufferReader;
import util.IdToName;

public class StatData {
    /**
     * The type of stat
     */
    public int statTypeNum;
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
     */
    public StatData deserialize(BufferReader buffer) {
        statTypeNum = buffer.readUnsignedByte();
        statType = StatType.byOrdinal(statTypeNum);

        if (isStringStat()) {
            stringStatValue = buffer.readString();
        } else {
            statValue = buffer.readCompressedInt();
        }
        statValueTwo = buffer.readCompressedInt();

        return this;
    }

    private boolean isStringStat() {
        if (StatType.EXP_STAT.get() == statTypeNum // 6
                || StatType.NAME_STAT.get() == statTypeNum // 31
                || StatType.ACCOUNT_ID_STAT.get() == statTypeNum // 38
                || StatType.OWNER_ACCOUNT_ID_STAT.get() == statTypeNum // 54
                || StatType.GUILD_NAME_STAT.get() == statTypeNum // 62
                || StatType.TEXTURE_STAT.get() == statTypeNum // 80
                || StatType.PET_NAME_STAT.get() == statTypeNum // 82
                || StatType.GRAVE_ACCOUNT_ID.get() == statTypeNum // 115
                || StatType.UNKNOWN121.get() == statTypeNum // 121
        ) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        String valueExtended = (statValueTwo == -1 ? "" : " [" + statValueTwo + "]");
        if (statTypeNum == 29) {
            valueExtended += " " + ConditionBits.effectsToString(statValue);
        } else if (statTypeNum == 96) {
            valueExtended += " " + ConditionNewBits.effectsToString(statValue);
        } else if (statTypeNum >= 8 && statTypeNum <= 19) {
            valueExtended += " " + IdToName.objectName(statValue);
        }
        return "\n      " + statType + " = " + statValue + valueExtended;
    }
}
