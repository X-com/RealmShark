package packets.data;

import packets.reader.BufferReader;

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
     * Unknown
     */
    public int unknownInt;

    /**
     * Deserializer method to extract data from the buffer.
     *
     * @param buffer Data that needs deserializing.
     * @return Returns this object after deserializing.
     */
    public QuestData deserialize(BufferReader buffer) {
        id = buffer.readString();
        name = buffer.readString();
        description = buffer.readString();
        expiration = buffer.readString();
        category = buffer.readInt();
        unknownInt = buffer.readInt();

        requirements = new int[buffer.readShort()];
        for (int i = 0; i < requirements.length; i++) {
            requirements[i] = buffer.readInt();
        }
        rewards = new int[buffer.readShort()];
        for (int i = 0; i < rewards.length; i++) {
            rewards[i] = buffer.readInt();
        }

        completed = buffer.readBoolean();
        itemOfChoice = buffer.readBoolean();
        repeatable = buffer.readBoolean();

        return this;
    }
}
