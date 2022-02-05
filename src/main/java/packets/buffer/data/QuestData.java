package packets.buffer.data;

import packets.buffer.PBuffer;

public class QuestData {
    /**
     * The id of this quest
     */
    public String id;
    /**
     * The name of this quest
     */
    public String name;
    /**
     * The description of this quest
     */
    public String description;
    /**
     * The expiration time of this quest
     */
    public String expiration;
    /**
     * The list of item IDs which are required to complete this quest
     */
    public int[] requirements;
    /**
     * The list of item IDs which are awarded upon completion of this quest
     */
    public int[] rewards;
    /**
     * Whether or not this quest has been completed
     */
    public boolean completed;
    /**
     * > Unknown
     */
    public boolean itemOfChoice;
    /**
     * Whether or not the quest is repeatable
     */
    public boolean repeatable;
    /**
     * The category of this quest
     */
    public int category;

    /**
     * Deserializer method to extract data from the buffer.
     *
     * @param buffer Data that needs deserializing.
     * @return Returns this object after deserializing.
     */
    public QuestData deserialize(PBuffer buffer) {
        id = buffer.readString();
        name = buffer.readString();
        description = buffer.readString();
        expiration = buffer.readString();
        category = buffer.readInt();
        short requirementsLen = buffer.readShort();
        requirements = new int[requirementsLen];
        for (int i = 0; i < requirementsLen; i++) {
            requirements[i] = buffer.readInt();
        }

        short rewardsLen = buffer.readShort();
        rewards = new int[rewardsLen];
        for (int i = 0; i < rewardsLen; i++) {
            rewards[i] = buffer.readInt();
        }
        completed = buffer.readBoolean();
        itemOfChoice = buffer.readBoolean();
        repeatable = buffer.readBoolean();

        return this;
    }
}
