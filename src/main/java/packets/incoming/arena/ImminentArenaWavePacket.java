package packets.incoming.arena;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Received when a new arena wave is about to begin.
 */
public class ImminentArenaWavePacket extends Packet {
    /**
     * The length of time the player has been in the arena for.
     */
    public int currentRuntime;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        currentRuntime = buffer.readInt();
    }

    @Override
    public String toString() {
        return "ImminentArenaWavePacket{" +
                "\n   currentRuntime=" + currentRuntime;
    }
}