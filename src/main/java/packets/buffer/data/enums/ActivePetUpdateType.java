package packets.buffer.data.enums;

public enum ActivePetUpdateType {
    Follow(0),
    Release(1),
    Unfollow(2),
    Unknown(3);

    private final int index;

    ActivePetUpdateType(int i) {
        index = i;
    }

    public static ActivePetUpdateType byOrdinal(int ord) {
        for (ActivePetUpdateType o : ActivePetUpdateType.values()) {
            if (o.index == ord) {
                return o;
            }
        }
        return null;
    }
}
