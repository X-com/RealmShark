package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Received when a player dies
 */
public class DeathPacket extends Packet {
    /**
     * The account id of the player who died
     */
    public String accountId;
    /**
     * The character id of the player who died
     */
    public int charId;
    /**
     * The cause of death
     */
    public String killedBy;
    /**
     * The object id of the zombie, if the player died wearing a cursed amulet
     */
    public int zombieId;
    /**
     * The type of zombie, if the player died wearing a cursed amulet
     */
    public int zombieType;
    /**
     * Whether or not a zombie was spawned
     * <p>
     * This is a derived property, and is the result of `zombieId !== -1`
     */
    public boolean isZombie;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        accountId = buffer.readString();
        charId = buffer.readInt();
        killedBy = buffer.readString();
        zombieType = buffer.readInt();
        zombieId = buffer.readInt();
        isZombie = zombieId != -1;
    }
}