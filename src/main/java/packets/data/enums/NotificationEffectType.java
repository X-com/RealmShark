package packets.data.enums;

public enum NotificationEffectType {
    StatIncrease(0),
    ServerMessage(1),
    ErrorMessage(2),
    KeepMessage(3),
    UI(4),
    Queue(5),
    ObjectText(6),
    Death(7),
    DungeonOpened(8),
    TeleportationError(9),
    DungeonCall(10),
    ProgressBar(11),
    Behavior(12),
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
