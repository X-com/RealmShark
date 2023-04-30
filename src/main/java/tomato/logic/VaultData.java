package tomato.logic;

import packets.Packet;
import packets.incoming.VaultContentPacket;
import tomato.gui.TomatoGUI;
import tomato.logic.enums.StatPotion;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Data storage class for vault data.
 */
public class VaultData {

    private int[] giftContents;
    private int[] chestContents;
    private int[] potionContents;
    private int[] chestContentsSeasonal;
    private int[] potionContentsSeasonal;
    private final HashMap<Integer, Integer> potsCharacterInventoryVault = new HashMap<>();
    private final HashMap<Integer, Integer> potsChestVault = new HashMap<>();
    private final HashMap<Integer, Integer> potsPotVault = new HashMap<>();
    private final HashMap<Integer, Integer> potsCharacterInventoryVaultSeasonal = new HashMap<>();
    private final HashMap<Integer, Integer> potsChestVaultSeasonal = new HashMap<>();
    private final HashMap<Integer, Integer> potsPotVaultSeasonal = new HashMap<>();
    private final HashMap<Integer, Integer> potsGiftVault = new HashMap<>();

    /**
     * Pot counter class to filter and store the amount of stat potions.
     *
     * @param container The container with the item IDs.
     * @param data      Data with potion stats and the amount of each to be updated.
     */
    private static void addPot(int[] container, HashMap<Integer, Integer> data) {
        if (container == null) return;
        for (int item : container) {
            if (!StatPotion.POT_ID_LIST.contains(item)) continue;
            Integer i = data.get(item);
            int count = 0;
            if (i != null) count = i;
            count += StatPotion.getStatGain(item);
            data.put(item, count);
        }
    }

    /**
     * Vault packet received when entering vault.
     *
     * @param packet Vault data packet received from server.
     */
    public void vaultPacketUpdate(Packet packet) {
        VaultContentPacket vp = (VaultContentPacket) packet;

//        if (vp.seasonalVault && vp.potionContents.length != 0) {
//            giftContents = vp.giftContents;
//            chestContentsSeasonal = vp.vaultContents;
//            potionContentsSeasonal = vp.potionContents;
//            updatePotData(giftContents, chestContentsSeasonal, potionContentsSeasonal, potsGiftVault, potsChestVaultSeasonal, potsPotVaultSeasonal);
//            TomatoGUI.getCharacterPanel().vaultDataUpdate(this);
//        } else if (!vp.seasonalVault) {
//            giftContents = vp.giftContents;
//            chestContents = vp.vaultContents;
//            potionContents = vp.potionContents;
//            updatePotData(giftContents, chestContents, potionContents, potsGiftVault, potsChestVault, potsPotVault);
//            TomatoGUI.getCharacterPanel().vaultDataUpdate(this);
//        }
    }

    /**
     * Character update method to update all character data when receiving http char list packet.
     *
     * @param charList Character list data.
     */
    public void updateCharInventory(ArrayList<RealmCharacter> charList) {
        potsCharacterInventoryVaultSeasonal.clear();
        potsCharacterInventoryVault.clear();
        for (RealmCharacter c : charList) {
            if (c.seasonal) addPot(c.equipment, potsCharacterInventoryVaultSeasonal);
            else addPot(c.equipment, potsCharacterInventoryVault);
        }
    }

    /**
     * Data update method to update gift chest, chest vault and potion storage with the amount of total stat pots.
     */
    private static void updatePotData(int[] giftContainer, int[] vaultContainer, int[] potContainer, HashMap<Integer, Integer> dataGift, HashMap<Integer, Integer> dataChest, HashMap<Integer, Integer> dataPot) {
        dataGift.clear();
        dataChest.clear();
        dataPot.clear();
        addPot(giftContainer, dataGift);
        addPot(vaultContainer, dataChest);
        addPot(potContainer, dataPot);
    }

    /**
     * Gets the total number of pots in the specified regular and seasonal pots into arrays A and B.
     */
    private static void addPots(int[] totalA, int[] totalB, HashMap<Integer, Integer> addA, HashMap<Integer, Integer> addB) {
        for (int id : StatPotion.POT_ID_LIST) {
            int index = StatPotion.getPotion(id).getIndex();
            if (addA.get(id) != null) totalA[index] += addA.get(id);
            if (addB.get(id) != null) totalB[index] += addB.get(id);
        }
    }

    /**
     * Computes the amount of pots in regular and seasonal storages specified by method name.
     */
    public void getPlayerInvPots(int[] regularTotalPots, int[] seasonalTotalPots) {
        addPots(regularTotalPots, seasonalTotalPots, potsCharacterInventoryVault, potsCharacterInventoryVaultSeasonal);
    }

    /**
     * Computes the amount of pots in regular and seasonal storages specified by method name.
     */
    public void getVaultChestPots(int[] regularTotalPots, int[] seasonalTotalPots) {
        addPots(regularTotalPots, seasonalTotalPots, potsChestVault, potsChestVaultSeasonal);
    }

    /**
     * Computes the amount of pots in regular and seasonal storages specified by method name.
     */
    public void getPotStoragePots(int[] regularTotalPots, int[] seasonalTotalPots) {
        addPots(regularTotalPots, seasonalTotalPots, potsPotVault, potsPotVaultSeasonal);
    }

    /**
     * Computes the amount of pots in regular and seasonal storages specified by method name.
     */
    public void getGiftChestPots(int[] regularTotalPots, int[] seasonalTotalPots) {
        addPots(regularTotalPots, seasonalTotalPots, potsGiftVault, potsGiftVault);
    }
}
