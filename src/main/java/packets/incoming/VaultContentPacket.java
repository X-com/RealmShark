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
     * Unknown int 1
     */
    public int unknownInt1;
    /**
     * Unknown int 2
     */
    public int unknownInt2;
    /**
     * Unknown int 3
     */
    public int unknownInt3;
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
    public void deserialize(BufferReader buffer) throws Exception {
        lastVaultPacket = buffer.readBoolean();
        unknownInt1 = buffer.readCompressedInt();
        unknownInt2 = buffer.readCompressedInt();
        unknownInt3 = buffer.readCompressedInt();

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

    @Override
    public String toString() {
        return "VaultContentPacket{" +
                "\n   unknownBool=" + lastVaultPacket +
                "\n   unknownInt1=" + unknownInt1 +
                "\n   unknownInt2=" + unknownInt2 +
                "\n   unknownInt3=" + unknownInt3 +
                "\n   vaultContents=" + Arrays.toString(vaultContents) +
                "\n   giftContents=" + Arrays.toString(giftContents) +
                "\n   potionContents=" + Arrays.toString(potionContents) +
                "\n   vaultUpgradeCost=" + vaultUpgradeCost +
                "\n   potionUpgradeCost=" + potionUpgradeCost +
                "\n   currentPotionMax=" + currentPotionMax +
                "\n   nextPotionMax=" + nextPotionMax +
                "\n   vaultItemString=" + vaultItemString +
                "\n   giftItemString=" + giftItemString;
    }
}