package packets.data.enums;

public enum ModeratorActionType {
    MUTE(0, "MuteAccount"),
    UNMUTE(1, "UnmuteAccount"),
    KICK(2, "Kick"),
    BLOCK(3, "AccountBlock");

    private final int index;
    private final String actMessage;

    ModeratorActionType(int i, String m) {
        this.index = i;
        this.actMessage = m;
    }

    /**
     * Return an ItemUseType object from a given ID.
     *
     * @param ord The integer ID of the usage type.
     * @return A UseItemType object or null if not found.
     */
    public static ModeratorActionType byOrdinal(int ord) {
        for (ModeratorActionType o : ModeratorActionType.values()) {
            if (o.index == ord) {
                return o;
            }
        }
        return null;
    }
}