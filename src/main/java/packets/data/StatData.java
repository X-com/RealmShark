package packets.data;

import packets.data.enums.ConditionBits;
import packets.data.enums.ConditionNewBits;
import packets.data.enums.StatType;
import packets.reader.BufferReader;
import assets.IdToAsset;

import java.io.Serializable;

public class StatData implements Serializable {
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
        if (StatType.EXP_STAT.get() == statTypeNum
                || StatType.NAME_STAT.get() == statTypeNum
                || StatType.ACCOUNT_ID_STAT.get() == statTypeNum
                || StatType.OWNER_ACCOUNT_ID_STAT.get() == statTypeNum
                || StatType.GUILD_NAME_STAT.get() == statTypeNum
                || StatType.MATERIAL_STAT.get() == statTypeNum
                || StatType.MATERIAL_CAP_STAT.get() == statTypeNum
                || StatType.UNIQUE_DATA_STRING.get() == statTypeNum
                || StatType.GRAVE_ACCOUNT_ID.get() == statTypeNum
                || StatType.MODIFIERS_STAT.get() == statTypeNum
                || StatType.DUST_STAT.get() == statTypeNum
                || StatType.CRUCIBLE_STAT.get() == statTypeNum
                || StatType.DUST_AMOUNT_STAT.get() == statTypeNum
                || StatType.PET_NAME_STAT.get() == statTypeNum
                || StatType.BloodRitualStat.get() == statTypeNum
        ) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        String secondValue = statValueTwo == -1 ? "" : " [" + statValueTwo + "]";
        String stringValue = stringStatValue == null ? "" : " " + stringStatValue;
        String stringExtra = "";
        if (statTypeNum == 29) {
            stringExtra += " " + ConditionBits.effectsToString(statValue);
        } else if (statTypeNum == 96) {
            stringExtra += " " + ConditionNewBits.effectsToString(statValue);
        } else if (statTypeNum >= 8 && statTypeNum <= 19) {
            String name = IdToAsset.objectName(statValue);
            stringExtra += " " + name;
        }
        return "      " + statType + "(" + statTypeNum + ") = " + statValue + secondValue + stringValue + stringExtra;
    }
}
