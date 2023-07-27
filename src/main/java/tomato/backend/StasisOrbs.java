package tomato.backend;

import java.util.HashMap;

/**
 * Stasis orb enum.
 */
public enum StasisOrbs {
    STASIS_ORB(3.0f, 2788, "T0 Stasis Orb"),
    ORB_OF_SWEET_DEMISE(3.0f, 306, "UT Orb of Sweet Demise"),
    SUSPENSION_ORB(3.5f, 2626, "T1 Suspension Orb"),
    IMPRISONMENT_ORB(4.0f, 2627, "T2 Imprisonment Orb"),
    ENCHANTMENT_ORB(4.0f, 2111, "UT Enchantment Orb"),
    NEUTRALIZATION_ORB(4.5f, 2628, "T3 Neutralization Orb"),
    TIMELOCK_ORB(5.0f, 2629, "T4 Timelock Orb"),
    BANISHMENT_ORB(5.5f, 2630, "T5 Banishment Orb"),
    PLANEFETTER_ORB(6.0f, 2861, "T6 Planefetter Orb"),
    SNOWBOUND_ORB(6.0f, 8334, "UT Snowbound Orb"),
    SOUL_OF_THE_BEARER(6.0f, 9058, "Soul of the Bearer"),
    KARMA_ORB(6.0f, 23352, "UT Karma Orb"),
    ORB_OF_THE_SABBATH(6.0f, 25752, "Orb of the Sabbath"),
    DIMENSIONGATE_ORB(6.5f, 8287, "T7 Dimensiongate Orb"),
    ORB_OF_CONFLICT(7.0f, 3083, "UT Orb of Conflict"),
    ORB_OF_TERROR(7.0f, 29647, "UT Orb of Terror"),
    ;

    private float delay;
    private int id;
    private String name;
    private static HashMap<Integer, StasisOrbs> list;

    static {
        list = new HashMap<>();
        for (StasisOrbs o : StasisOrbs.values()) {
            list.put(o.id, o);
        }
    }

    StasisOrbs(float d, int i, String n) {
        delay = d;
        id = i;
        name = n;
    }

    /**
     * Gets if orb is used by player using item in 2nd slot and duration matches.
     *
     * @param id    Id of the item from the 2nd slot.
     * @param delay Delay of the stasis.
     * @return True if player is wearing orb with corresponding stasis delay.
     */
    public static boolean usingOrb(int id, float delay) {
        StasisOrbs o = list.get(id);
        if (o != null) {
            return o.delay == delay;
        }
        return false;
    }

    /**
     * Name of the stasis orb used.
     *
     * @param id Id of the stasis orb.
     * @return Orb name from item id.
     */
    public static String getName(int id) {
        StasisOrbs o = list.get(id);
        if (o != null) {
            return o.name;
        }
        return "";
    }
}
