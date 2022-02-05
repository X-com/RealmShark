package packets.buffer.data;

import packets.buffer.PBuffer;

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
     */
    public StatData deserialize(PBuffer buffer) {
        statType = StatType.byOrdinal(buffer.readUnsignedByte());
        if (statType == null) System.err.println("StatType enum error in deserializer");

        if (isStringStat()) {
            stringStatValue = buffer.readString();
        } else {
            statValue = new CompressedInt().deserialize(buffer);
        }
        statValueTwo = buffer.readByte();

        return this;
    }

    private boolean isStringStat() {
        if (StatType.NAME_STAT.equals(statType)
        || StatType.GUILD_NAME_STAT.equals(statType)
        || StatType.PET_NAME_STAT.equals(statType)
        || StatType.ACCOUNT_ID_STAT.equals(statType)
        || StatType.OWNER_ACCOUNT_ID_STAT.equals(statType)
        || StatType.GRAVE_ACCOUNT_ID.equals(statType)) {
            return true;
        }
        return false;
    }
}
