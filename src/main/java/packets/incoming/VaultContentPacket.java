package packets.incoming;

import packets.Packet;
import packets.buffer.PBuffer;

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
    /**
     * Strings of all vault item texts. Always empty
     */
    public String vaultItemString;
    /**
     * Strings of all gift item texts. Always empty
     */
    public String giftItemString;

    @Override
    public void deserialize(PBuffer buffer) throws Exception {
        unknownBool = buffer.readBoolean();
        vaultItemCount = buffer.readCompressedInt();
        giftItemCount = buffer.readCompressedInt();
        potionItemCount = buffer.readCompressedInt();

        vaultContents = new int[buffer.readCompressedInt()];
        for (int i = 0; i < vaultContents.length; i++) {
            vaultContents[i] = buffer.readCompressedInt();
        }

        giftContents = new int[buffer.readCompressedInt()];
        for (int i = 0; i < giftContents.length; i++) {
            giftContents[i] = buffer.readCompressedInt();
        }

        potionContents = new int[buffer.readCompressedInt()];
        for (int i = 0; i < potionContents.length; i++) {
            potionContents[i] = buffer.readCompressedInt();
        }

        vaultUpgradeCost = buffer.readShort();
        potionUpgradeCost = buffer.readShort();
        currentPotionMax = buffer.readShort();
        nextPotionMax = buffer.readShort();

        vaultItemString = buffer.readString();
        giftItemString = buffer.readString();
    }
}