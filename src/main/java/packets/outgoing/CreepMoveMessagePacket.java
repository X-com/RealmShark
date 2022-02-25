package packets.outgoing;


import packets.Packet;
import packets.data.WorldPosData;
import packets.reader.BufferReader;

/**
 * Sent when playing the Summoner class and a spawned creep minion has to move position.
 */
public class CreepMoveMessagePacket extends Packet {
    /**
     * The object ID of the Summoner's creep to move.
     */
    public int objectId;
    /**
     * The position to move the creep to.
     */
    public WorldPosData position;
    /**
     * Whether the Summoner ability key is held down.
     */
    public boolean hold;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        objectId = buffer.readInt();
        position = new WorldPosData().deserialize(buffer);
        hold = buffer.readBoolean();
    }
}