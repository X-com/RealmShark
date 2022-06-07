package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

import java.util.Arrays;

/**
 * Received when the active trade is accepted
 */
public class TradeAcceptedPacket extends Packet {
    /**
     * A description of which items in the client's inventory are selected.
     * Items 0-3 are the hotbar items, and 4-12 are the 8 inventory slots.
     * <p>
     * If a value is `true`, then the item is selected
     */
    public boolean[] clientOffer;
    /**
     * A description of which items in the trade partner's inventory are selected.
     * Items 0-3 are the hotbar items, and 4-12 are the 8 inventory slots.
     * <p>
     * If a value is `true`, then the item is selected
     */
    public boolean[] partnerOffer;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        clientOffer = new boolean[buffer.readShort()];
        for (int i = 0; i < clientOffer.length; i++) {
            clientOffer[i] = buffer.readBoolean();
        }
        partnerOffer = new boolean[buffer.readShort()];
        for (int i = 0; i < partnerOffer.length; i++) {
            partnerOffer[i] = buffer.readBoolean();
        }
    }

    @Override
    public String toString() {
        return "TradeAcceptedPacket{" +
                "\n   clientOffer=" + Arrays.toString(clientOffer) +
                "\n   partnerOffer=" + Arrays.toString(partnerOffer);
    }
}