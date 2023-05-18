package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * This packet has unknown usage and the game will function without using it.
 * It's most likely used by the server to track the total count of ShootAckPackets sent over time.
 */
public class ShootAckCounterPacket extends Packet {
    /**
     * Time
     */
    public int time;
    /**
     * Enemy shots ACKed
     */
    public short ack;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        time = buffer.readInt();
        ack = buffer.readShort();
    }

    @Override
    public String toString() {
        return "ShootAckCounterPacket{" +
                "\n   time=" + time +
                "\n   ack=" + ack;
    }
}
