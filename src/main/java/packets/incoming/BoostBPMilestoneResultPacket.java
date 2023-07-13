package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Packet received when redeeming battle pass items
 */
public class BoostBPMilestoneResultPacket extends Packet {
    /**
     * Indicating if item is redeemed
     */
    public boolean itemRedeemed;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        itemRedeemed = buffer.readBoolean();
    }

    @Override
    public String toString() {
        return "UnknownPacket150{" +
                "\n   itemRedeemed=" + itemRedeemed;
    }
}
