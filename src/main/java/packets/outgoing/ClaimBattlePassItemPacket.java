package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Packet sent when redeeming battle pass items
 */
public class ClaimBattlePassItemPacket extends Packet {
    /**
     * Index of the battle pass item requested to be redeemed
     */
    public int battlePassItemIndex;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        battlePassItemIndex = buffer.readByte();
    }

    @Override
    public String toString() {
        return "ClaimBattlePassItemPacket{" +
                "\n   battlePassItemIndex=" + battlePassItemIndex;
    }
}
