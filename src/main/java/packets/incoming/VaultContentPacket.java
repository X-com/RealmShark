package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;
import packets.buffer.data.CompressedInt;
import packets.buffer.data.GroundTileData;

/**
 * Received when the player enters or updates their vault
 */
public class VaultContentPacket extends Packet {
    /**
     * Unknown
     */
    public boolean unknownBool;
    /**
     * The amount of items in the player vault
     */
    public int vaultItemCount;
    /**
     * The amount of items in the gift vault
     */
    public int giftItemCount;
    /**
     * The amount of items in the potion vault
     */
    public int potionItemCount;
    /**
     * The contents of the players vault, sent as an array of item object IDs or -1 if the slot is empty
     */
    public int[] vaultContents;
    /**
     * The contents of the player's gift vault
     */
    public int[] giftContents;
    /**
     * The contents of the player's potion vault
     */
    public int[] potionContents;
    /**
     * The cost in gold for the next upgrade to the vault
     */
    public short vaultUpgradeCost;
    /**
     * The cost in gold for the next upgrade to the potion vault
     */
    public short potionUpgradeCost;
    /**
     * The current slot size of the player's potion vault
     */
    public short currentPotionMax;
    /**
     * The size of the player's potion vault after they purchase the current upgrade
     */
    public short nextPotionMax;

    @Override
    public void deserialize(PBuffer buffer) {
        unknownBool = buffer.readBoolean();
        vaultItemCount = new CompressedInt().deserialize(buffer);
        giftItemCount = new CompressedInt().deserialize(buffer);
        potionItemCount = new CompressedInt().deserialize(buffer);

        vaultContents = new int[new CompressedInt().deserialize(buffer)];
        for (int i = 0; i < vaultContents.length; i++) {
            vaultContents[i] = new CompressedInt().deserialize(buffer);
        }

        giftContents = new int[new CompressedInt().deserialize(buffer)];
        for (int i = 0; i < giftContents.length; i++) {
            giftContents[i] = new CompressedInt().deserialize(buffer);
        }

        potionContents = new int[new CompressedInt().deserialize(buffer)];
        for (int i = 0; i < potionContents.length; i++) {
            potionContents[i] = new CompressedInt().deserialize(buffer);
        }

        vaultUpgradeCost = buffer.readShort();
        potionUpgradeCost = buffer.readShort();
        currentPotionMax = buffer.readShort();
        nextPotionMax = buffer.readShort();
    }
}