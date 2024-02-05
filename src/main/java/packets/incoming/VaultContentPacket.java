package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

import java.util.Arrays;

/**
 * Received when the player enters or updates their vault
 */
public class VaultContentPacket extends Packet {
    /**
     * If this is the last vault packet
     */
    public boolean lastVaultPacket;
    /**
     * Vault chest object ID
     */
    public int vaultChestObjectId;
    /**
     * Gift chest object ID
     */
    public int giftChestObjectId;
    /**
     * Potion storage object ID
     */
    public int potionStorageObjectId;
    /**
     * Seasonal spoils object ID
     */
    public int seasonalSpoilChestObjectId;
    /**
     * Unknown
     */
    public int unknownInt;
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
     * The contents of the player's seasonal spoils items
     */
    int[] seasonalSpoilContent;
    /**
     * Unknown compressed int array
     */
    int[] unknownCompressedIntArray;
    /**
     * Unknown
     */
    byte unknownByte;
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
     * Unknown string
     */
    public String vaultChestEnchants;
    /**
     * Unknown string
     */
    public String giftChestEnchants;
    /**
     * Unknown string
     */
    public String spoilsChestEnchants;
    /**
     * Unknown
     */
    public short unknownShort;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        lastVaultPacket = buffer.readBoolean();
        vaultChestObjectId = buffer.readCompressedInt();
        giftChestObjectId = buffer.readCompressedInt();
        potionStorageObjectId = buffer.readCompressedInt();
        seasonalSpoilChestObjectId = buffer.readCompressedInt();
        unknownInt = buffer.readCompressedInt();

        vaultContents = new int[buffer.readCompressedInt()];
        for (int i = 0; i < vaultContents.length; i++) {
            vaultContents[i] = buffer.readCompressedInt();
        }
        unknownCompressedIntArray = new int[buffer.readCompressedInt()];
        for (int i = 0; i < unknownCompressedIntArray.length; i++) {
            unknownCompressedIntArray[i] = buffer.readCompressedInt();
        }
        giftContents = new int[buffer.readCompressedInt()];
        for (int i = 0; i < giftContents.length; i++) {
            giftContents[i] = buffer.readCompressedInt();
        }
        potionContents = new int[buffer.readCompressedInt()];
        for (int i = 0; i < potionContents.length; i++) {
            potionContents[i] = buffer.readCompressedInt();
        }
        seasonalSpoilContent = new int[buffer.readCompressedInt()];
        for (int i = 0; i < seasonalSpoilContent.length; i++) {
            seasonalSpoilContent[i] = buffer.readCompressedInt();
        }

        vaultUpgradeCost = buffer.readShort();
        unknownShort = buffer.readShort();
        potionUpgradeCost = buffer.readShort();
        currentPotionMax = buffer.readShort();
        nextPotionMax = buffer.readShort();

        vaultChestEnchants = buffer.readString();
        giftChestEnchants = buffer.readString();
        spoilsChestEnchants = buffer.readString();
    }

    @Override
    public String toString() {
        return "VaultContentPacket{" +
                "\n   lastVaultPacket=" + lastVaultPacket +
                "\n   vaultChestObjectId=" + vaultChestObjectId +
                "\n   giftChestObjectId=" + giftChestObjectId +
                "\n   potionStorageObjectId=" + potionStorageObjectId +
                "\n   seasonalSpoilChestObjectId=" + seasonalSpoilChestObjectId +
                "\n   unknownInt=" + unknownInt +
                "\n   vaultContents=" + Arrays.toString(vaultContents) +
                "\n   giftContents=" + Arrays.toString(giftContents) +
                "\n   potionContents=" + Arrays.toString(potionContents) +
                "\n   seasonalSpoilContent=" + Arrays.toString(seasonalSpoilContent) +
                "\n   unknownCompressedIntArray=" + Arrays.toString(unknownCompressedIntArray) +
                "\n   unknownByte=" + unknownByte +
                "\n   vaultUpgradeCost=" + vaultUpgradeCost +
                "\n   potionUpgradeCost=" + potionUpgradeCost +
                "\n   unknownShort=" + unknownShort +
                "\n   currentPotionMax=" + currentPotionMax +
                "\n   nextPotionMax=" + nextPotionMax +
                "\n   vaultChestEnchants=" + vaultChestEnchants +
                "\n   giftChestEnchants=" + giftChestEnchants +
                "\n   spoilsChestEnchants=" + spoilsChestEnchants
                ;
    }
}