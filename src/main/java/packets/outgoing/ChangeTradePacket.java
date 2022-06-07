package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

import java.util.Arrays;

/**
 * Sent to change the client's offer in the current active trade.
 */
public class ChangeTradePacket extends Packet {
    /**
     * A description of which items in the client's inventory are selected.
     * Items 0-3 are the hotbar items, and 4-12 are the 8 inventory slots.
     * <p>
     * If a value is `true`, then the item is selected.
     */
    public boolean[] offer;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        offer = new boolean[buffer.readShort()];
        for (int i = 0; i < offer.length; i++) {
            offer[i] = buffer.readBoolean();
        }
    }

    @Override
    public String toString() {
        return "ChangeTradePacket{" +
                "\n   offer=" + Arrays.toString(offer);
    }
}
