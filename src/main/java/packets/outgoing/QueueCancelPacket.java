package packets.outgoing;

import packets.Packet;
import packets.buffer.PBuffer;

/**
 * Sent when the clients position in the queue should be cancelled (DOINKMPEGPF)
 */
public class QueueCancelPacket extends Packet {
    @Override
    public void deserialize(PBuffer buffer) throws Exception {
        // TODO: add this proper
    }
}