package tomato.realmshark.enums;

import assets.ImageBuffer;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Stat potion enum to get potion name.
 */
public enum StatPotion {
    Life(0, 2793, 9070, 5471),
    Mana(1, 2794, 9071, 5472),
    Attack(2, 2591, 9064, 5465),
    Defense(3, 2592, 9065, 5466),
    Speed(4, 2593, 9066, 5467),
    Dexterity(5, 2636, 9069, 5470),
    Vitality(6, 2612, 9067, 5468),
    Wisdom(7, 2613, 9068, 5469);

    private final int index;
    private final int smallId;
    private final int greaterId;
    private final int soulboundId;

    private static final HashMap<Integer, String> POTION_NAME = new HashMap<>();
    private static final HashMap<Integer, StatPotion> POTION = new HashMap<>();
    private static final HashMap<Integer, Integer> POT_STAT_SIZE = new HashMap<>();
    public static final HashSet<Integer> POT_ID_LIST = new HashSet<>();

    static {
        try {
            for (StatPotion o : StatPotion.values()) {
                POTION_NAME.put(o.smallId, o.toString());
                POTION_NAME.put(o.greaterId, "Greater " + o);
                POTION_NAME.put(o.soulboundId, "Soulbound " + o);
                POTION.put(o.smallId, o);
                POTION.put(o.greaterId, o);
                POTION.put(o.soulboundId, o);
                POT_ID_LIST.add(o.smallId);
                POT_ID_LIST.add(o.greaterId);
                POT_ID_LIST.add(o.soulboundId);
                POT_STAT_SIZE.put(o.smallId, 1);
                POT_STAT_SIZE.put(o.greaterId, 2);
                POT_STAT_SIZE.put(o.soulboundId, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    StatPotion(int index, int small, int greater, int soulbound) {
        this.index = index;
        this.smallId = small;
        this.greaterId = greater;
        this.soulboundId = soulbound;
    }

    /**
     * Potion id to stat potion name.
     *
     * @param id Potion id
     * @return Potion name
     */
    public static String getName(int id) {
        return POTION_NAME.get(id);
    }

    /**
     * Potion id to enum.
     *
     * @param id Potion id
     * @return Potion enum
     */
    public static StatPotion getPotion(int id) {
        return POTION.get(id);
    }

    /**
     * Potion id to stat gain.
     *
     * @param id Potion id
     * @return Potion size
     */
    public static int getStatGain(int id) {
        return POT_STAT_SIZE.get(id);
    }

    /**
     * Potion index relative to the order of potions listed above.
     *
     * @return Potion index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Icon ID of the small potions
     *
     * @return Small potion icon ID
     */
    public int smallId() {
        return smallId;
    }
}
