package packets.data.enums;

import java.io.Serializable;

public enum ActivePetUpdateType implements Serializable {
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
