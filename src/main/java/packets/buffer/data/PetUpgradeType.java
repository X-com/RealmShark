package packets.buffer.data;

public enum PetUpgradeType {
    PetYard(1),
    FeedPet(2),
    FusePet(3);

    private int index;

    PetUpgradeType(int i) {
        index = i;
    }

    public static PetUpgradeType byOrdinal(int ord) {
        for (PetUpgradeType o : PetUpgradeType.values()) {
            if (o.index == ord) {
                return o;
            }
        }
        return null;
    }
}
