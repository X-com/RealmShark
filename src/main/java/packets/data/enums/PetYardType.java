package packets.data.enums;

import java.io.Serializable;

public enum PetYardType implements Serializable {
    Common(1),
    Uncommon(2),
    Rare(3),
    Legendary(4),
    Divine(5);

    private final int index;

    PetYardType(int i) {
        index = i;
    }

    public static PetYardType byOrdinal(int ord) {
        for (PetYardType o : PetYardType.values()) {
            if (o.index == ord) {
                return o;
            }
        }
        return null;
    }
}
