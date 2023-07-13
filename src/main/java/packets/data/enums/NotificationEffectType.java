package packets.data.enums;

public enum NotificationEffectType {
    StatIncrease(0),
    ServerMessage(1),
    ErrorMessage(2),
    StickyMessage(3),
    UI(4),
    Queue(5),
    ObjectText(6),
    Death(7),
    PortalOpened(8),
    TeleportationError(9),
    PlayerCallout(10),
    ProgressBar(11),
    Behavior(12),
    Emote(13),
    Victory(14),
    MissionRefresh(15),
    MissionsProgressOnlyRefresh(16),
    BlueprintUnlock(20),
    WithIcon(21),
    FameBonus(22),
    ForgeFire(23);

    private final int index;

    NotificationEffectType(int i) {
        index = i;
    }

    public int get() {
        return index;
    }

    public static NotificationEffectType byOrdinal(byte ord) {
        for (NotificationEffectType o : NotificationEffectType.values()) {
            if (o.index == ord) {
                return o;
            }
        }
        return null;
    }
}
