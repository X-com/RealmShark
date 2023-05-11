package tomato.logic.backend;

import packets.Packet;
import packets.incoming.VaultContentPacket;
import tomato.logic.backend.data.RealmCharacter;
import tomato.logic.enums.StatPotion;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Data storage class for vault data.
 */
public class VaultData {

    private ArrayList<Integer> giftContents;
    private ArrayList<Integer> chestContents;
    private ArrayList<Integer> potionContents;
    private final HashMap<Integer, Integer> potsCharacterInventoryVault;
    private final HashMap<Integer, Integer> potsChestVault;
    private final HashMap<Integer, Integer> potsPotVault;
    private final HashMap<Integer, Integer> potsGiftVault;
    private boolean vaultUpdating;

    public VaultData() {
        vaultUpdating = false;

        giftContents = new ArrayList<>();
        chestContents = new ArrayList<>();
        potionContents = new ArrayList<>();
        potsCharacterInventoryVault = new HashMap<>();
        potsChestVault = new HashMap<>();
        potsPotVault = new HashMap<>();
        potsGiftVault = new HashMap<>();
    }

    /**
     * Vault packet received when entering vault.
     *
     * @param packet Vault data packet received from server.
     */
    public void vaultPacketUpdate(Packet packet) {
        VaultContentPacket vp = (VaultContentPacket) packet;
        if (!vaultUpdating) resetData();

        addAll(giftContents, vp.giftContents);
        addAll(chestContents, vp.vaultContents);
        addAll(potionContents, vp.potionContents);

        if (vp.lastVaultPacket) {
            updatePotData(giftContents, chestContents, potionContents, potsGiftVault, potsChestVault, potsPotVault);
            vaultUpdating = false;
        } else {
            vaultUpdating = true;
        }
    }

    private void addAll(ArrayList<Integer> container, int[] array) {
        for (int i : array) {
            container.add(i);
        }
    }

    private void resetData() {
        giftContents.clear();
        chestContents.clear();
        potionContents.clear();
    }

    public void clearChar() {
        potsCharacterInventoryVault.clear();
    }

    /**
     * Pot counter class to filter and store the amount of stat potions.
     *
     * @param container The container with the item IDs.
     * @param data      Data with potion stats and the amount of each to be updated.
     */
    private static void addPot(List<Integer> container, HashMap<Integer, Integer> data) {
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
     * Character update method to update character data when receiving http packet.
     *
     * @param realmchar Character data.
     */
    public void updateCharInventory(RealmCharacter realmchar) {
        List<Integer> list = Arrays.stream(realmchar.equipment).boxed().collect(Collectors.toList());
        addPot(list, potsCharacterInventoryVault);
    }

    /**
     * Data update method to update gift chest, chest vault and potion storage with the amount of total stat pots.
     */
    private static void updatePotData(ArrayList<Integer> giftContainer, ArrayList<Integer> vaultContainer, ArrayList<Integer> potContainer, HashMap<Integer, Integer> dataGift, HashMap<Integer, Integer> dataChest, HashMap<Integer, Integer> dataPot) {
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
    private static void addPots(int[] total, HashMap<Integer, Integer> add) {
        for (int id : StatPotion.POT_ID_LIST) {
            int index = StatPotion.getPotion(id).getIndex();
            if (add.get(id) != null) total[index] += add.get(id);
        }
    }

    /**
     * Computes the amount of pots in regular and seasonal storages specified by method name.
     */
    public void getPlayerInvPots(int[] regularTotalPots) {
        addPots(regularTotalPots, potsCharacterInventoryVault);
    }

    /**
     * Computes the amount of pots in regular and seasonal storages specified by method name.
     */
    public void getVaultChestPots(int[] regularTotalPots) {
        addPots(regularTotalPots, potsChestVault);
    }

    /**
     * Computes the amount of pots in regular and seasonal storages specified by method name.
     */
    public void getPotStoragePots(int[] regularTotalPots) {
        addPots(regularTotalPots, potsPotVault);
    }

    /**
     * Computes the amount of pots in regular and seasonal storages specified by method name.
     */
    public void getGiftChestPots(int[] regularTotalPots) {
        addPots(regularTotalPots, potsGiftVault);
    }
}
